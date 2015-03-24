package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.R;

/**
 * Created by lsa on 14/01/15.
 */
public class FreakyButton extends ImageView {

    private static int NO_ICON = -1;

    ArrayList<Integer> lst_icons;
    int icon_idx = NO_ICON, iconSize =24;
    Bitmap bBackground = null, bIcon = null;

    public FreakyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lst_icons = new ArrayList<Integer>();
    }

    public FreakyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        lst_icons = new ArrayList<Integer>();
    }
    public FreakyButton(Context context) {
        super(context);
        lst_icons = new ArrayList<Integer>();
    }

    public int getIconIdx(){
        return icon_idx;
    }

    public void setIconSize(int newSize){
        iconSize = newSize;
        if ((icon_idx != NO_ICON) && (lst_icons.size()>0)) preloadIcon();
    }

    public void addIcon(int icon){
        lst_icons.add(icon);
    }

    public void preloadIcon(){
        int ratio = 1;
        // **** load icon bitmap mask ****
        bIcon = BitmapFactory.decodeResource(getResources(), lst_icons.get(icon_idx));
        if (bIcon.getHeight()>bIcon.getWidth())
            ratio = bIcon.getHeight()/iconSize;
        else
            ratio = bIcon.getWidth()/iconSize;
        bIcon = Bitmap.createScaledBitmap(bIcon, bIcon.getWidth() / ratio, bIcon.getHeight() / ratio, true);
    }

    public boolean setIconIdx(int idx){
        if (idx >= lst_icons.size()) return false;
        if (idx != icon_idx){
            icon_idx = idx;
            preloadIcon();
            this.invalidate();
        }
        return true;
    }

    private void bufferizeBitmapsComponents(){
        // **** crée le gauge background ****
        bBackground = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.button), this.getWidth(), this.getHeight(), true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bBackground == null) bufferizeBitmapsComponents();
        if (bIcon == null && icon_idx != NO_ICON) preloadIcon();

        // **** empilement des images ****
        canvas.drawBitmap(bBackground, 0, 0, null);
        if (icon_idx>NO_ICON)
            canvas.drawBitmap(bIcon, (getWidth()-bIcon.getWidth())/2, (getHeight()-bIcon.getHeight())/2, null);
    }


}
