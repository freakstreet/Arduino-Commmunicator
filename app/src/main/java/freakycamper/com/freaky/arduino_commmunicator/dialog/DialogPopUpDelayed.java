package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;

/**
 * Created by lsa on 01/10/14.
 */
public class DialogPopUpDelayed extends Dialog {

    private CountDownTimer countDownTimer;
    private View.OnTouchListener touchListener;

    public DialogPopUpDelayed(Context context, String title, int layout, int styleOption) {
        super(context, styleOption);

        countDownTimer = new CountDownTimer(5000 /* milis */, 5000 /* countdown interval */) {
            @Override
            public void onTick(long millisUntilFinished) {
                return;
            }

            @Override
            public void onFinish() {
                countdownTimerFinished();
            }
        };

        this.setContentView(layout);
        this.setTitle(title);

        int titleId = context.getResources().getIdentifier("action_bar_title", "id", "android");
        TextView tv = (TextView) findViewById(titleId);

        if (tv != null)
        {
            tv.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_LIGHT));
            tv.setTextSize(24);
        }

        this.getWindow().setWindowAnimations(R.style.dialog_animation_fade);
    }

    public void chandeHideTimer(int milis){
        countDownTimer.onTick(milis);
    }

    @Override
    public void show(){
        countDownTimer.start();
        super.show();
    }

    public void setMonitoredComponents(ArrayList<View> viewList){
        for (View v:viewList){
            setMonitoredComponent(v);
        }
    }

    public void blockAutoCloseTimer(){
        countDownTimer.cancel();
    }

    public void releaseAutoCloseTimer(){
        countDownTimer.start();
    }

    public void setMonitoredComponent(View view){
        if (touchListener == null) {
            touchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    resetCountDown();
                    return false;
                }
            };
        }
        view.setOnTouchListener(touchListener);
    }

    public void setDimensions(int w, int h){
        this.getWindow().setLayout(w,h);
    }

    private void resetCountDown(){
        countDownTimer.cancel();
        countDownTimer.start();
    }

    private void countdownTimerFinished(){
        this.dismiss();
    }
}
