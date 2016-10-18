package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.rarepebble.colorpicker.ColorPickerView;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;

/**
 * Created by lsa on 18/10/16.
 */

public class DialogPickColor extends Dialog {

    ColorPickerView pick = null;
    OnSelectedColorListener listener = null;

    public interface OnSelectedColorListener{
        public void colorSelectedInt(int colorIntValue);
    }


    public DialogPickColor(Context context) {
        super(context);
        initialize();
    }

    public DialogPickColor(Context context, int theme) {
        super(context, theme);
        initialize();
    }

    public void setListener(OnSelectedColorListener l)
    {
        listener = l;
    }

    protected DialogPickColor(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initialize();
    }

    public void setLightColor(int color)
    {
        pick.setColor(color);
    }

    private void initialize()
    {
        setContentView(R.layout.dlg_pick_color);
        pick = (ColorPickerView)findViewById(R.id.colorpickerview);
        pick.showAlpha(false);
        Button bt = (Button)findViewById(R.id.bt_colorpickup_cancel);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });

        bt = (Button)findViewById(R.id.bt_colorpickup_validate);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateColor();
            }
        });
    }

    private void closeDialog(){
        dismiss();
    }

    private void validateColor(){
        fireSelectedColorEvent();
        dismiss();
    }

    private void fireSelectedColorEvent()
    {
        if (listener != null)
            listener.colorSelectedInt(pick.getColor());
    }
}
