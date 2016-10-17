package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;

/**
 * Created by lsa on 17/10/16.
 */

public class FreakyWaterPanel extends ImageView {

    WaterManager _manager = null;

    public FreakyWaterPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FreakyWaterPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreakyWaterPanel(Context context) {
        super(context);
    }


    public void setWaterManager(WaterManager manager)
    {
        _manager = manager;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // **** Affichage de l'indisponibilité de la valeur du niveau de réservoir ****
        if (_manager == null)
        {
            textPaint.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_EXTRA_LIGHT));
            textPaint.setARGB(255, 50, 50, 50);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(40);
            canvas.drawText("N/A", 95, 170, textPaint);
        }
        else
        {
            textPaint.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_EXTRA_LIGHT));
            textPaint.setARGB(255, 50, 50, 50);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(20);
            canvas.drawText("Niveau du réservoir", 100, 90, textPaint);
            textPaint.setTextSize(40);
            canvas.drawText(_manager.getTankLevel() + " %", 100, 130, textPaint);
            textPaint.setTextSize(16);
            canvas.drawText("Conso pompe" , 100, 165, textPaint);
            textPaint.setTextSize(20);
            canvas.drawText(_manager.getConsoPump() + " A", 100, 185, textPaint);
            textPaint.setTextSize(16);
            canvas.drawText("Debit sortant" , 100, 215, textPaint);
            textPaint.setTextSize(20);
            canvas.drawText(_manager.getWaterFlow() + " l/min", 100, 235, textPaint);


            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.rgb(255, 0, 0));
            p.setStrokeWidth(4);
            // Reservoir eaux grises plein
            if (_manager.isGrayWaterFull())
            {

            }

            // Evier ouvert
            if (_manager.isTapOpened())
                canvas.drawCircle(348, 187, 34, p);

            // Douche ouverte
            if (_manager.isShowerOpened())
                canvas.drawCircle(508, 187, 34, p);

            // Pompe active
            if (_manager.isPumpActive())
                canvas.drawCircle(427, 51, 34, p);
        }


    }
}
