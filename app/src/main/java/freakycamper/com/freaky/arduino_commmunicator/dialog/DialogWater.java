package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyWaterPanel;

/**
 * Created by lsa on 12/01/15.
 */
public class DialogWater extends DialogPopUpDelayed {

    public static final int WATER_LEVEL_GRAPH_PLOTS_NB = 50;

    public DialogWater(Context context, WaterManager manager) {
        super(context, context.getString(R.string.title_dialog_water), R.layout.layout_water, android.R.style.Theme_Holo_Dialog_NoActionBar);

        FreakyWaterPanel fpn = (FreakyWaterPanel)findViewById(R.id.dlg_water_bg);
        fpn.setWaterManager(manager);



    }

    public void updateDialog()
    {

    }
}
