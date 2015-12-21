package fr.rascafr.dev.smartcharger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity {

    // Custom view
    private EvolutionView evolutionView;

    // Buttons
    private Button bpConnect, bpDisconnect;

    // Virtual monitor

    // Bluetooth manager
    private BluetoothSPP bluetoothSPP;
    private final static String DEVICE_NAME = "HC-06";
    private int lag = 0;

    // Layout / alerts
    private MaterialDialog materialDialog;
    private RelativeLayout rl1, rl2, rl3;
    private TextView tv1, tv2, tv3;

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothSPP.stopService();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!bluetoothSPP.isBluetoothEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not enabled !\nPlease set it ON and reload app", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get layout objects
        bpConnect = (Button) findViewById(R.id.bpConnect);
        bpDisconnect = (Button) findViewById(R.id.bpDisconnect);
        evolutionView = (EvolutionView) findViewById(R.id.evolutionView);
        rl1 = (RelativeLayout) findViewById(R.id.rlPause);
        rl2 = (RelativeLayout) findViewById(R.id.rlCharge);
        rl3 = (RelativeLayout) findViewById(R.id.rlChargeLow);
        tv1 = (TextView) findViewById(R.id.tvPause);
        tv2 = (TextView) findViewById(R.id.tvCharge);
        tv3 = (TextView) findViewById(R.id.tvChargeLow);

        // Instantiate bluetooth spp
        bluetoothSPP = new BluetoothSPP(this);

        // On disconnect button listener
        bpDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothSPP.disconnect();
            }
        });

        // On connect button listener
        bpConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothSPP.isBluetoothAvailable()) {
                    materialDialog = new MaterialDialog.Builder(MainActivity.this)
                            .title("Erreur")
                            .content("Le bluetooth n'est pas activé.")
                            .show();
                } else {
                    materialDialog = new MaterialDialog.Builder(MainActivity.this)
                            .title("Veuillez patienter")
                            .content("Connexion au chargeur en cours ...")
                            .cancelable(false)
                            .progressIndeterminateStyle(false)
                            .progress(true, 0)
                            .show();

                    // Search for module
                    String[] devNames = bluetoothSPP.getPairedDeviceName();
                    String[] devAddress = bluetoothSPP.getPairedDeviceAddress();
                    String myDevAddress = "";

                    for (int i = 0; i < devNames.length; i++) {
                        if (devNames[i].equalsIgnoreCase(DEVICE_NAME)) {
                            myDevAddress = devAddress[i];
                        }
                    }

                    bluetoothSPP.setupService();
                    bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
                    bluetoothSPP.connect(myDevAddress);

                    bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                        public void onDeviceConnected(String name, String address) {
                            // Do something when successfully connected
                            Toast.makeText(getApplicationContext(), "Device connected !", Toast.LENGTH_SHORT).show();
                            evolutionView.reset();
                            materialDialog.hide();
                        }

                        public void onDeviceDisconnected() {
                            // Do something when connection was disconnected
                            Toast.makeText(getApplicationContext(), "Device disconnected !", Toast.LENGTH_SHORT).show();
                        }

                        public void onDeviceConnectionFailed() {
                            // Do something when connection failed
                            Toast.makeText(getApplicationContext(), "Device connection failure !", Toast.LENGTH_SHORT).show();
                            materialDialog.hide();
                        }
                    });

                    bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                        public void onDataReceived(byte[] data, String message) {

                            // Do something when data incoming
                            if (lag == 0) parseBluetoothData(message);
                            lag = (lag + 1) % 3;

                            /*
                            // Add text to monitor
                            /*String term = message + "\n" + tvMonitor.getText();
                            if (term.length() > 500)
                                term = term.substring(0, 500);
                            tvMonitor.setText(term);*/
                        }
                    });
                }
            }
        });
    }

    /**
     * Function to parse data from bluetooth module
     */
    public void parseBluetoothData (String data) {

        // Check if data is ok
        if (data != null && data.length() >= 10 && data.contains("U=") && data.contains(",I=") && data.contains(";")) {

            int currentADC = 0, voltADC = 0, status = 0;
            // Get voltage value
            int end = data.indexOf(",I="), st = data.indexOf("U=");
            if (st >= 0 && end > 0 && st < end) {
                voltADC = Integer.parseInt(data.substring(st+2, end));
                end = data.indexOf(",S=");
                st = data.indexOf(",I=");
                if (st > 0 && end > 0 && st < end) {
                    currentADC = Integer.parseInt(data.substring(st+3, end));

                    st = data.indexOf(",S=");
                    end = data.indexOf(";");

                    if (st > 0 && end > 0 && st < end) {

                        status = Integer.parseInt(data.substring(st+3, end));

                        // Print data to screen
                        updateLayoutFromStatus(status);

                        // Add the points to view
                        if (end > 0) evolutionView.addPoint(voltADC, currentADC);
                    }
                }
            }
        }
    }

    /**
     * Set recycler view and text view colors
     */
    void updateLayoutFromStatus (int status) {

        rl1.setBackgroundColor(getResources().getColor(status == 0 ? R.color.md_red_500 : R.color.md_white_1000));
        tv1.setTextColor(getResources().getColor(status == 0 ? R.color.md_red_500 : R.color.md_white_1000));
        rl2.setBackgroundColor(getResources().getColor(status == 1 ? R.color.md_red_500 : R.color.md_white_1000));
        tv2.setTextColor(getResources().getColor(status == 1 ? R.color.md_red_500 : R.color.md_white_1000));
        rl3.setBackgroundColor(getResources().getColor(status == 2 ? R.color.md_red_500 : R.color.md_white_1000));
        tv3.setTextColor(getResources().getColor(status == 2 ? R.color.md_red_500 : R.color.md_white_1000));
    }
}
