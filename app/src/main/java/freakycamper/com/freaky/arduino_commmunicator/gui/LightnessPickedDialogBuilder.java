package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.ColorPickerView;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.OnColorSelectedListener;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.builder.ColorPickerClickListener;
import freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.slider.LightnessSlider;

/**
 * Created by lsa on 20/10/16.
 */

public class LightnessPickedDialogBuilder {


        private AlertDialog.Builder builder;
        private LinearLayout pickerContainer;
        private ColorPickerView colorPickerView;
        private LightnessSlider lightnessSlider;

        private int defaultMargin = 0;
        private Integer[] initialColor = new Integer[]{null, null, null, null, null};

        private LightnessPickedDialogBuilder(Context context) {
            this(context, 0);
        }

        private LightnessPickedDialogBuilder(Context context, int theme) {
            defaultMargin = getDimensionAsPx(context, R.dimen.default_slider_margin);
            final int dialogMarginBetweenTitle = getDimensionAsPx(context, R.dimen.default_slider_margin_btw_title);

            builder = new AlertDialog.Builder(context, theme);
            pickerContainer = new LinearLayout(context);
            pickerContainer.setOrientation(LinearLayout.VERTICAL);
            pickerContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            pickerContainer.setPadding(defaultMargin, dialogMarginBetweenTitle, defaultMargin, defaultMargin);

            LinearLayout.LayoutParams layoutParamsForColorPickerView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            layoutParamsForColorPickerView.weight = 1;
            colorPickerView = new ColorPickerView(context);
            colorPickerView.setLightnessCursorOnly();

            builder.setView(pickerContainer);
        }

        public static LightnessPickedDialogBuilder with(Context context) {
            return new LightnessPickedDialogBuilder(context);
        }

        public static LightnessPickedDialogBuilder with(Context context, int theme) {
            return new LightnessPickedDialogBuilder(context, theme);
        }

        public LightnessPickedDialogBuilder setTitle(String title) {
            builder.setTitle(title);
            return this;
        }

        public LightnessPickedDialogBuilder setTitle(int titleId) {
            builder.setTitle(titleId);
            return this;
        }

        public LightnessPickedDialogBuilder initialColor(int initialColor) {
            this.initialColor[0] = initialColor;
            return this;
        }

        public LightnessPickedDialogBuilder setOnColorSelectedListener(OnColorSelectedListener onColorSelectedListener) {
            colorPickerView.addOnColorSelectedListener(onColorSelectedListener);
            return this;
        }

        public LightnessPickedDialogBuilder setPositiveButton(CharSequence text, final ColorPickerClickListener onClickListener) {
            builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    positiveButtonOnClick(dialog, onClickListener);
                }
            });
            return this;
        }

        public LightnessPickedDialogBuilder setNegativeButton(CharSequence text, DialogInterface.OnClickListener onClickListener) {
            builder.setNegativeButton(text, onClickListener);
            return this;
        }

        public AlertDialog build() {
            Context context = builder.getContext();
            colorPickerView.setInitialColors(initialColor, getStartOffset(initialColor));

            LinearLayout.LayoutParams layoutParamsForLightnessBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDimensionAsPx(context, R.dimen.default_slider_height));
            lightnessSlider = new LightnessSlider(context);
            lightnessSlider.setLayoutParams(layoutParamsForLightnessBar);
            pickerContainer.addView(lightnessSlider);
            colorPickerView.setLightnessSlider(lightnessSlider);
            lightnessSlider.setColor(getStartColor(initialColor));

            return builder.create();
        }

        private Integer getStartOffset(Integer[] colors) {
            Integer start = 0;
            for (int i = 0; i < colors.length; i++) {
                if (colors[i] == null) {
                    return start;
                }
                start = (i + 1) / 2;
            }
            return start;
        }

        private int getStartColor(Integer[] colors) {
            Integer startColor = getStartOffset(colors);
            return startColor == null ? Color.WHITE : colors[startColor];
        }

        private static int getDimensionAsPx(Context context, int rid) {
            return (int) (context.getResources().getDimension(rid) + .5f);
        }

        private void positiveButtonOnClick(DialogInterface dialog, ColorPickerClickListener onClickListener) {
            int selectedColor = colorPickerView.getSelectedColor();
            Integer[] allColors = colorPickerView.getAllColors();
            onClickListener.onClick(dialog, selectedColor, allColors);
        }


}
