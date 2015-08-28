package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

import android.content.Context;

import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogLights;

/**
 * Created by lsa on 01/10/14.
 */
public class LightManager extends MainManager {

    ArrayList<LightItem> _lLights;
    DialogLights _dialog = null;

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
        updateGui();
    }

    public int getLightCount(){
        return _lLights.size();
    }

    public void showDialog(Context context){
        _dialog = new DialogLights(context, this);
        _dialog.show();
    }

    private void updateGui(){
        if (_dialog != null){
            _dialog.updateGui(this);
        }
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
