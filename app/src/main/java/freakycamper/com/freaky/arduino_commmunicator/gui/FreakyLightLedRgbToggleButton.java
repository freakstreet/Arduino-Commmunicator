package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogPickColor;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.ColorPickerView;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.OnColorSelectedListener;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.builder.ColorPickerClickListener;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.builder.ColorPickerDialogBuilder;
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


    public void pickRGBColor(){

        final Context context = getContext();

        ColorPickerDialogBuilder
                .with(context, R.style.AppTheme)
                .setTitle("Select light desired color")
                .initialColor(Color.rgb(item.getRedValue(), item.getGreenValue(), item.getBlueValue()))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showColorEdit(false)
                .showAlphaSlider(false)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        setBackgroundColor(selectedColor);
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        item.updateLightRGBStatus((char)Color.red(selectedColor), (char)Color.green(selectedColor), (char)Color.blue(selectedColor));
                        setBackgroundColor(selectedColor);
                        if (allColors != null) {
                            StringBuilder sb = null;

                            for (Integer color : allColors) {
                                if (color == null)
                                    continue;
                                if (sb == null)
                                    sb = new StringBuilder("Color List:");
                                sb.append("\r\n#" + Integer.toHexString(color).toUpperCase());
                            }

                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                //.setColorEditTextColor(ContextCompat.getColor(SampleActivity.this, android.R.color.holo_blue_bright))
                .build()
                .show();


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
                pickRGBColor();
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
