package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogPickColor;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;


/**
 * Created by lsa on 18/10/16.
 */

public class FreakyLightLedRgbToggleButton extends ToggleButton implements DialogPickColor.OnSelectedColorListener {

    LightItem item = null;

    OnLightItemChangeAsked listener = null;

    public interface OnLightItemChangeAsked{
        public void updateLightRGB(int rgbColor, int lightId);
        public void updateLightDIMM(int dimmVAl, int lightId);
        public void updateLightSwitch(boolean isOn, int lightId);
    }

    public FreakyLightLedRgbToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public FreakyLightLedRgbToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreakyLightLedRgbToggleButton(Context context) {
        super(context);
    }

    public void connectToLightItem(LightItem light)
    {
        item = light;
        switch (item.getLightType()) {
            case NORMAL_ON_OFF:
                this.setChecked(item.getIsOn());
                this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        listener.updateLightSwitch(isChecked, item.getId());
                    }
                });
                break;

            case RGB_DIMMER:
                this.setBackgroundColor(Color.rgb(item.getRedValue(), item.getGreenValue(), item.getBlueValue()));
                break;

            case DIMMER:
                break;
        }

    }

    public void setListener(OnLightItemChangeAsked listener)
    {
        this.listener = listener;

    }

    @Override
    public boolean performClick() {
        if (item==null)
            return true;

        switch (item.getLightType())
        {
            case NORMAL_ON_OFF:
                setChecked(!isChecked());
                break;

            case RGB_DIMMER:
                DialogPickColor dlg = new DialogPickColor(getContext());
                dlg.setLightColor(Color.rgb(item.getRedValue(), item.getGreenValue(), item.getBlueValue()));
                dlg.setListener(this);
                dlg.show();
                break;

            case DIMMER:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (item==null)
        {
            return;
        }


        switch (item.getLightType())
        {
            case NORMAL_ON_OFF:
                break;

            case RGB_DIMMER:
    /*            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(Color.rgb(item.getRedValue(), item.getGreenValue(), item.getBlueValue()));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(4);
                canvas.drawRect (23, 23, 313, 83, p);*/
                break;

            case DIMMER:
                Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setARGB(255, 200, 200, 200);
                textPaint.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_EXTRA_LIGHT));
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(26);
                canvas.drawText(item.getDimmValue() + " %", 250, 60, textPaint);
                break;
        }

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void colorSelectedInt(int colorIntValue) {
        if (listener != null)
        {
            listener.updateLightRGB(colorIntValue, item.getId());
            this.setBackgroundColor(colorIntValue);
            this.invalidate();
        }
    }
}
