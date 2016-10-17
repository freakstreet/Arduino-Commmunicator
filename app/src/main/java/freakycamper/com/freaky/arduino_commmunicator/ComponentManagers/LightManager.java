package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogLights;

/**
 * Created by lsa on 01/10/14.
 */
public class LightManager extends MainManager {

    ArrayList<LightItem> _lLights;

    public interface switchLightModule {
        public boolean functionSwitch();
    }

    boolean moduleActive = false;

    static public LightManager initialiseLightManager(SendTcListener listener){
        LightManager ret = new LightManager(listener);
        return ret;
    }

    public LightManager(MainManager.SendTcListener listener){
        super(listener);
        _lLights = new ArrayList<LightItem>();
    }

    public LightItem getLight(int lightId){
        for(LightItem l: _lLights)
            if (l.getId() == lightId) return l;
        return null;
    }

    public void updateFromTM(char[] tm){
        int idx = tm[1];
        LightItem l = getLight(idx);
        if (l==null){
            l = new LightItem(idx, LightItem.eLightTypes.values()[tm[2]]);
            _lLights.add(l);
        }

        l.updateLightStatus(tm[3], tm[4], tm[5], tm[6]);
    }

    @Override
    public String getStringFromTm(char[] tm)
    {
        String str = "";

        switch (tm[0])
        {
            case CampDuinoProtocol.TM_LIGHT:
                for (int i=0;i<_lLights.size(); i++)
                {
                    str += "Lights: ";
                    LightItem l = _lLights.get(i);
                    switch (l.getLightType())
                    {
                        case NORMAL_ON_OFF:
                            str += LightItem.lightNames[i] + " type " + LightItem.lightTypes[l.getLightType().value] + " is " + (l.getIsOn()?"ON":"OFF");
                            break;

                        case DIMMER:
                            str += LightItem.lightNames[i] + " type " + LightItem.lightTypes[l.getLightType().value] + " Dimm=" + l.getDimmValue();
                            break;

                        case RGB_DIMMER:
                            str += LightItem.lightNames[i] + " type " + LightItem.lightTypes[l.getLightType().value] +  " R:" + l.getRedValue() + " G:" + l.getGreenValue() + " B:" + l.getBlueValue();
                            break;
                    }

                    if (i < _lLights.size()-1)
                        str += "\n";
                }
                break;
        }
        return str;
    }

    public int getLightCount(){
        return _lLights.size();
    }

    public void showDialog(Context context){
        correspondingDialog = new DialogLights(context, this);
        correspondingDialog .setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                correspondingDialog  = null;
            }
        });
        correspondingDialog.show();
    }

    @Override public void updateDialog()
    {
        updateGui();
    }

    private void updateGui(){
        ((DialogLights)correspondingDialog).updateGui(this);
    }

    public boolean switchModuleActivation(){
        if (!moduleActive)
            moduleActive = true;
        else {
            // switch off all lights

            moduleActive = false;
        }
        return moduleActive;
    }

    public boolean getModuleIsAstive(){
        return moduleActive;
    }


}
