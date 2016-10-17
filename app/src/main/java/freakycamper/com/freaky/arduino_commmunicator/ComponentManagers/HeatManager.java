package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

import android.content.Context;
import android.content.DialogInterface;

import freakycamper.com.freaky.arduino_commmunicator.campdatas.HeatItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TemperatureItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogHeat;

/**
 * Created by lsa on 17/12/14.
 */
public class HeatManager extends MainManager {

    private HeatItem _heat;
    private float[] _temps;

    public static HeatManager initialiseHeatManager(Context context, SendTcListener listener, final TemperatureManager tempTm) {
        HeatManager ret = new HeatManager(context, listener, tempTm);
        return ret;
    }

    public HeatManager(Context context, SendTcListener listener, TemperatureManager tempTm) {
        super(listener);

        tempTm.addTempTmUpdateListener(new TemperatureManager.OnTempTmReceived() {
            @Override
            public void newTempTm(float[] tempsTm) {
 //               updateTemps(tempsTm);
            }
        });

        _temps = new float[2];
        _heat = new HeatItem(HeatItem.eHeatModuleState.HEAT_MODULE_OFF, -99, (char)0, (char)0);
    }

    public void showDialog(Context context){
        correspondingDialog = new DialogHeat(context, this, _temps);
        correspondingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                correspondingDialog = null;
            }
        });
        correspondingDialog.show();
    }

    public HeatItem getHeatParams(){
        return _heat;
    }

    private void updateTemps(float[] tempsTm) {

        float t1, t2;
        t1 = _temps[0];
        t2 = _temps[1];

        _temps[0] = TemperatureItem.getTemperatureFromType(tempsTm, TemperatureItem.eTemperatureType.TEMP_INSIDE1);
        _temps[1] = TemperatureItem.getTemperatureFromType(tempsTm, TemperatureItem.eTemperatureType.TEMP_INSIDE2);

        if ((t1 != _temps[0]) || (t2 != _temps[1])){
            if (correspondingDialog != null){
                ((DialogHeat)correspondingDialog).updateHeatDialog(_temps);
            }
        }
    }

    @Override
    public void updateFromTM(char[] tm){
        _heat = new HeatItem(HeatItem.eHeatModuleState.values()[tm[3]], CampDuinoProtocol.decodeFloatFromTm(tm[4], tm[5]), tm[6], tm[7]);
        if (correspondingDialog != null){
            ((DialogHeat)correspondingDialog).updateFromTm(_heat);
            _heat.updateFanVal( tm[4], true);
            _heat.updateFanVal( tm[5], false);
        }
    };
}
