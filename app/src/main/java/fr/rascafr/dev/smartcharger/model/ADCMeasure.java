package fr.rascafr.dev.smartcharger.model;

/**
 * Created by Rascafr on 08/12/2015.
 */
public class ADCMeasure {

    private final static double ADC_REF = 5.0;
    private final static int ADC_RESOLUTION = 1024;
    private int current, voltage;

    public ADCMeasure() {
        current = 0;
        voltage = 0;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getCurrent() {

        return current;
    }

    public int getVoltage() {
        return voltage;
    }

    public double getTrueVoltage () {
        return 0.0025 * voltage + 0.4852;

        // y = 0,0025x + 0,4852

        /*
        102 ->
         */
    }

    public double getTrueCurrent () {
        //return current * (ADC_REF / ADC_RESOLUTION);

        // y = 0,0125x - 4,4936


        return 0.0125 * current - 4.4936;

        /*
        86 -> 414 mV -> 16.0491 mA

         */
    }
}
