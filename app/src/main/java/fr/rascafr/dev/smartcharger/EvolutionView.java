package fr.rascafr.dev.smartcharger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import fr.rascafr.dev.smartcharger.model.ADCMeasure;

/**
 * Graphic class for scope view
 */
public class EvolutionView extends View {

    // Paint brushes
    private Paint backgroundPaint;
    private Paint axisPaint;
    private Paint voltagePaint;
    private Paint currentPaint;

    // Context
    private Context context;

    // Sizes
    private int screenW, screenH;
    private double pxH, pxHcurrent, pxHvoltage;
    private int pxW;
    private static int last = 0;

    // Data -> TODO in a static class (with instance and singleton)
    private final static int MAX_POINTS = 400;
    private ADCMeasure screenPoints[];
    private int iterator = 0;

    // Constructor
    public EvolutionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Data / context
        this.context = context;
        screenPoints = new ADCMeasure[MAX_POINTS];
        for (int i=0;i<MAX_POINTS;i++) {
            screenPoints[i] = new ADCMeasure();
        }

        // Create objects
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.DKGRAY);
        axisPaint = new Paint();
        axisPaint.setColor(Color.LTGRAY);
        voltagePaint = new Paint();
        voltagePaint.setColor(Color.GREEN);
        voltagePaint.setTextSize(20);
        voltagePaint.setAntiAlias(true);
        voltagePaint.setTypeface(Typeface.MONOSPACE);
        currentPaint = new Paint();
        currentPaint.setColor(Color.RED);
        currentPaint.setTextSize(20);
        currentPaint.setAntiAlias(true);
        currentPaint.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.screenW = w;
        this.screenH = h;
        pxH = screenH/1024.0;
        pxW = screenW/MAX_POINTS;

        // Max : 5A, min : -0.50A
        pxHcurrent = screenH / 5.5;

        // Max : 3.5, min : -0.05V
        pxHvoltage = screenH / 4;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    // Draw method
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawPaint(backgroundPaint);
        if (iterator > 0) {
            canvas.drawText(String.format("%.3f mV", screenPoints[iterator-1].getTrueVoltage()*1000), 50, 50, voltagePaint);
            canvas.drawText(String.format("%.3f mA", screenPoints[iterator-1].getTrueCurrent()*1000), 50, 85, currentPaint);
        }

        // Draw each lines / points
        for (int i=1;i<iterator;i++) {
            ADCMeasure thisone = screenPoints[i];
            ADCMeasure previous = screenPoints[i-1];
            canvas.drawLine((i-1)*pxW, (int)(screenH-(previous.getTrueVoltage()*pxHvoltage)), i*pxW, (int)(screenH-(thisone.getTrueVoltage()*pxHvoltage)), voltagePaint);
            canvas.drawLine((i-1)*pxW, (int)(screenH-(previous.getTrueCurrent()*pxHcurrent)), i*pxW, (int)(screenH-(thisone.getTrueCurrent()*pxHcurrent)), currentPaint);
        }
    }

    // Add a double point ... voltage + current to screen
    // Values are in ADC format (0 -> 0V, 1023 -> MAX)
    public void addPoint (int adcVolt, int adcCurrent) {
        screenPoints[iterator].setVoltage(adcVolt);
        screenPoints[iterator].setCurrent(adcCurrent);
        iterator = (iterator + 1) % MAX_POINTS;
        this.invalidate(); // Refresh screen
    }

    // Convert an ADC value into a screen Y position
    // 0 -> 0V -> screenH-1
    // 1023 -> MAX -> 0
    private int adcToScreen (int adcVal) {
        return (int)(screenH-adcVal*pxH);
    }

    // Reset cursor
    public void reset() {
        iterator = 0;
        this.invalidate();
    }
}