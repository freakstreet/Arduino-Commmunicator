package freakycamper.com.freaky.arduino_commmunicator.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;


/**
 * Created by lsa on 08/01/15.
 */
public class FreakyGauge extends ImageView {

    private static int NO_ICON = -1;

    boolean active_mode = false;
    boolean numerical_mode = false;
    float min, max, numerical_value = 0;
    String strValue = "N/A";
    Bitmap bBackground = null, bCircleMask= null, bMask1= null, bMask2= null, bIcon = null;
    int _percentFill = 50;
    ArrayList<Integer> lst_icons;
    int icon_idx = NO_ICON;
    String text="";
    Typeface font = null;

    float gaugeCenterRadiusFactor = 0.78f;


    public FreakyGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lst_icons = new ArrayList<Integer>();
    }

    public FreakyGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        lst_icons = new ArrayList<Integer>();
    }

    public FreakyGauge(Context context) {
        super(context);
        lst_icons = new ArrayList<Integer>();
    }

    private void bufferizeBitmapsComponents(){
        int middle = Math.min(getWidth(), getHeight())/2;

        // **** crée le gauge background ****
        Bitmap bTmp = BitmapFactory.decodeResource(getResources(), R.drawable.gauge);
        bBackground = Bitmap.createScaledBitmap(bTmp, this.getWidth(), this.getHeight(), true);

        // **** crée le masque circulaire de remplissage
        bCircleMask = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bCircleMask);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(middle, middle, middle*gaugeCenterRadiusFactor, paint );

        preloadMasks();

        if (lst_icons.size() > 0)
            preloadIcon();

    }

    public void setActiveMode(boolean active){
        active_mode = active;
        this.setEnabled(active);
    }

    public void preloadMasks(){
        // **** créer le masque 1 ****
        bMask1 = BitmapFactory.decodeResource(getResources(), R.drawable.mask);
        bMask1 = Bitmap.createScaledBitmap(bMask1, getWidth(), getHeight(), true);

        // **** créer le masque 2 ****
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        bMask2 = Bitmap.createBitmap(bMask1, 0, 0, bMask1.getWidth(), bMask1.getHeight(), m, false);
    }

    public void preloadIcon(){
        int ratio = 1;
        // **** load icon bitmap mask ****
        bIcon = BitmapFactory.decodeResource(getResources(), lst_icons.get(icon_idx));
        if (bIcon.getHeight()>bIcon.getWidth())
            ratio = bIcon.getHeight()/24;
        else
            ratio = bIcon.getWidth()/24;
        bIcon = Bitmap.createScaledBitmap(bIcon, bIcon.getWidth() / ratio, bIcon.getHeight() / ratio, true);
    }

    public void setLegend(String newText){
        text = newText;
 //       this.invalidate();
    }

    public String getLegend(){
        return text;
    }

    public void setNumericalMode(float min, float max)
    {
        numerical_mode = true;
        this.min = min;
        this.max = max;
    }

    public void set_ValueText(String strVal)
    {
        strValue = strVal;
    }

    public void set_Value(float val)
    {
        numerical_value = val;

        if (val > max) set_percentFill(100);
        else if (val<min) set_percentFill(0);
        else
        {
            float percent = 100.0f * (val-min)/(max-min);
            int pct = Math.round(percent);
            set_percentFill(pct);
        }
    }

    public void set_percentFill(int value){
        int newVal = Math.min(Math.max(0, value), 100);
        if (newVal != _percentFill){
            _percentFill = newVal;
            if (bMask1 != null)
                preloadMasks();
        }
    }

    public void setFont(Typeface newFont){
        font = newFont;
    }

    public int getIconIdx(){
        return icon_idx;
    }

    public int getPercentFill(){
        return _percentFill;
    }

    public void addIcon(int icon){
        lst_icons.add(icon);
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
        int w, h, offset;
        w = getWidth();
        h = getHeight();
        offset = 10+8*h*(100-_percentFill)/1000;

        if (bBackground == null) bufferizeBitmapsComponents();

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Bitmap bStep1 = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
        Canvas cStep1 = new Canvas(bStep1);

        cStep1.drawBitmap(bMask1, 0, offset, null);
        cStep1.drawBitmap(bCircleMask, 0, 0, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cStep1.drawRect(0, 0, w, h, p);

        Bitmap bStep12 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cStep12 = new Canvas(bStep12);
        Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setColor(getColor(true));
        cStep12.drawBitmap(bStep1, 0, 0, p2);
        p2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cStep12.drawRect(0, 0, w, h, p2);

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Bitmap bStep2 = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
        Canvas cStep2 = new Canvas(bStep2);
        cStep2.drawBitmap(bMask2, 0, offset, null);
        cStep2.drawBitmap(bCircleMask, 0, 0, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cStep2.drawRect(0, 0, w, h, p);

        Bitmap bStep22 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cStep22 = new Canvas(bStep22);
        p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setColor(getColor(false));
        cStep22.drawBitmap(bStep2, 0, 0, p2);
        p2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cStep22.drawRect(0, 0, w, h, p2);

        // **** empilement des images ****
        canvas.drawBitmap(bBackground, 0, -3, null);
        canvas.drawBitmap(bStep22, 0, 0, null);
        canvas.drawBitmap(bStep12, 0, 0, null);

        // **** Affichage de la valeur ****
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_EXTRA_LIGHT));
        textPaint.setARGB(255, 255, 255, 255);
        textPaint.setTextAlign(Paint.Align.CENTER);
        //textPaint.setTypeface(font);
        textPaint.setTextSize(40);
        if (!numerical_mode) {
            canvas.drawText(_percentFill + "%", w / 2, 10 + h / 2, textPaint);
        }
        else
        {
            canvas.drawText(strValue, w / 2, 10 + h / 2, textPaint);
        }

        // **** Affichage de la légende ****
        textPaint.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_LIGHT));
        textPaint.setTextSize(11);
        textPaint.setColor(active_mode?Color.WHITE:Color.DKGRAY);
        canvas.drawText(text, w/2, h/2+25, textPaint);

        // **** draw icon ****
        if (bIcon != null){
            canvas.drawBitmap(bIcon, (w-bIcon.getWidth())/2, h*2/3, textPaint);
        }

    }

    private int getColor(boolean brightOne)
    {
        float fct;
        float ratio = 2f;
        if (_percentFill > 100/ratio)
            fct = 100;
        else fct = ratio * _percentFill;
        float col[] = { fct, 0.8f, (brightOne?0.8f:0.6f)};
        return Color.HSVToColor(col);
    }




}
