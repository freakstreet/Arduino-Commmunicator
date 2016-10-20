package freakycamper.com.freaky.arduino_commmunicator.gui.FlaskColorPicker.builder;

import android.content.DialogInterface;

/**
 * Created by Charles Anderson on 4/17/15.
 */
public interface ColorPickerClickListener {
    void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors);
}
