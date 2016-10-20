package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

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
                this.setTextColor(Color.rgb(255-Color.red(item.getRedValue()), 255-Color.green(item.getGreenValue()), 255-Color.blue(item.getBlueValue())));
                break;

            case DIMMER:
                break;
        }

    }

    public void setListener(OnLightItemChangeAsked listener)
    {
        this.listener = listener;

    }

    private void pickDimmValue(){
        final LightnessPickedDialogBuilder dlg = LightnessPickedDialogBuilder
                .with(getContext(), android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Dialog)
                .setTitle("Select light desired dimm value");

        dlg.setPositiveButton("OK", new ColorPickerClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                listener.updateLightDIMM(Color.red(selectedColor), item.getId());
            }
        });
        dlg.setNegativeButton("Cancel", null);
        dlg.initialDimmValue(item.getDimmValue());
        dlg.build().show();
    }

    private void pickRGBColor(){

        final ColorPickerDialogBuilder dlg = ColorPickerDialogBuilder
                .with(getContext(), android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Dialog)
                .setTitle("Select light desired color")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(8)
                .showColorEdit(false)
                .showAlphaSlider(false);

        dlg.setPositiveButton("OK", new ColorPickerClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                listener.updateLightRGB(selectedColor, item.getId());
                setBackgroundColor(selectedColor);
            }
        });

        dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setBackgroundColor(Color.rgb(item.getRedValue(), item.getGreenValue(), item.getBlueValue()));
                    setTextColor(Color.rgb(255-Color.red(item.getRedValue()), 255-Color.green(item.getGreenValue()), 255-Color.blue(item.getBlueValue())));
                }
        });

        dlg.setOnColorSelectedListener(new OnColorSelectedListener() {
                @Override
                public void onColorSelected(int selectedColor) {
                    setBackgroundColor(selectedColor);
                    setTextColor(Color.rgb(255-Color.red(selectedColor), 255-Color.green(selectedColor), 255-Color.blue(selectedColor)));
                    dlg.setColorEditTextColor(selectedColor);
                }
        });

        int c = Color.rgb(item.getRedValue(), item.getGreenValue(), item.getBlueValue());
        // if actual params is black, then display the white color for suggestion
        if (c == Color.BLACK)
        {
            c = Color.WHITE;
        }
        dlg.initialColor(c);
        dlg.build().show();
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
                pickDimmValue();
                break;
        }
        return true;
    }

 /*   @Override
    public boolean
*/
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
                canvas.drawText(String.valueOf((100*item.getDimmValue())/255) + " %", 250, 60, textPaint);
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
