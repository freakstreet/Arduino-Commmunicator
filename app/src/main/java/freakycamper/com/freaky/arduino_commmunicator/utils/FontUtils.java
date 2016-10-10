package freakycamper.com.freaky.arduino_commmunicator.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * Created by lsa on 21/01/15.
 */
public class FontUtils {

    // font file name
    public static final String FONT_DOSIS_EXTRA_LIGHT   = "Dosis-ExtraLight.ttf";
    public static final String FONT_DOSIS_LIGHT         = "Dosis-Light.ttf";
    public static final String FONT_DOSIS_MEDIUM        = "Dosis-Medium.ttf";
    public static final String FONT_TRIPLE_DOT_DIGITAL  = "Triple_dot_digital-7.ttf";

    // store the opened typefaces(fonts)
    private static final Hashtable<String, Typeface> mCache = new Hashtable<String, Typeface>();

    /**
     * Load the given font from assets
     *
     * @param fontName font name
     * @return Typeface object representing the font painting
     */
    public static Typeface loadFontFromAssets(Context context, String fontName) {

        // make sure we load each font only once
        synchronized (mCache) {
            if (! mCache.containsKey(fontName)) {
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/"+fontName);
                mCache.put(fontName, typeface);
            }
            return mCache.get(fontName);
        }
    }
}
