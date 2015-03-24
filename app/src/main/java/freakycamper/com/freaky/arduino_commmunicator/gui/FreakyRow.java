package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;

/**
 * Created by lsa on 14/01/15.
 */
public class FreakyRow extends ImageView {

    private static int NO_ICON      = -1;
    private static int TEXT_SIZE    = 20;

    String label = "";
    ArrayList<Integer> lst_icons;
    int icon_idx = NO_ICON, iconSize =24;
    boolean isMainRow = false;
    Bitmap bBackground = null, bIcon = null;

    public FreakyRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lst_icons = new ArrayList<Integer>();
    }

    public FreakyRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        lst_icons = new ArrayList<Integer>();
    }
    public FreakyRow(Context context) {
        super(context);
        lst_icons = new ArrayList<Integer>();
    }

    public void setLabel(String newLabel){
        label = newLabel;
        invalidate();
    }

    public void setMainRow(){isMainRow = true;}

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


    @Override
    protected void onDraw(Canvas canvas) {
        int w, h;
        w = getWidth();
        h = getHeight();

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (isMainRow){
            p.setColor(Color.argb(30, 255, 255, 255));
            canvas.drawRect(0, 0, w, h, p);
        }
        p.setColor(Color.argb(200, 255, 255, 255));
        canvas.drawLine(0, h, w, h, p);

        if (label.length() > 0) {
            p.setColor(Color.argb(255, 255, 255, 255));
            p.setTextSize(TEXT_SIZE);
            if (isMainRow)
                p.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_MEDIUM));
            else
                p.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_LIGHT));
            canvas.drawText(label, w/8, (h+TEXT_SIZE)/2, p);
        }

    }

}
