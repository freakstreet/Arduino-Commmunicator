package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;


import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

import freakycamper.com.freaky.arduino_commmunicator.ArduinoCommunicatorActivity;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.CurrentItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TensionItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.WaterItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogElectrical;

/**
 * Created by lsa on 08/12/14.
 */
public class ElectricalManager extends MainManager implements WaterItem.ToggleSwitchWaterPumpRelay {

    DialogElectrical _dialog = null;
    LightManager.switchLightModule listenerSwitchLightModule;

    static public ElectricalManager initialiseElectricalManager(ArduinoCommunicatorActivity mainActivity) {
        return new ElectricalManager(mainActivity);
    }

    @Override
    public void actionToggleSwitchPumpRelay(boolean newStatus) {
        toggleRelay(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_WATER_MODULE, newStatus);
    }

    private void toggleRelay(CampDuinoProtocol.eProtTcSwitch switchId, boolean newStatus){

        // else for other switches, prepare TC
        char[] tc = new char[2];
        tc[0] = (char)switchId.value;
        tc[1] = (newStatus?(char)1:(char)0);
        //send TC
        _sendTcListener.sendTC(tc);
    }

    public void setListenerSwitchLightModule(LightManager.switchLightModule listener){
        listenerSwitchLightModule = listener;
    }

    public interface ListenerCurrentUpdate {
        public void currentUpdated(float[] currentList);
    }

    public interface ListenerTensionUpdate {
        public void tensionUpdated(float[] tensionList);
    }

    public interface ListenerRelayModuleUpdate {
        public void relayModuleUpdated();
    }

    public interface ListenerManagerPowerSourceChange {
        public void onManagerPowerSourceChanged(ElectricalItem.eAlimSource source);
    }

    public interface ListenerFullPowerSourceChange {
        public void onFullPowerSourceChanged(ElectricalItem.eAlimSource source);
    }

    private float[]     _tensions;
    private float[]     _currents;
    private boolean[]   _relays;
    private ElectricalItem.eAlimSource     _eSourceManagerPower;
    private ElectricalItem.eAlimSource     _eSourceFullPower;

    private List<ListenerCurrentUpdate>         _lCurrentListener;
    private List<ListenerTensionUpdate>         _lTensionListener;
    private List<ListenerRelayModuleUpdate>     _lRelayModuleListener;
    private ListenerManagerPowerSourceChange    _lOnManagerPowerSourceChanged;
    private ListenerFullPowerSourceChange       _lOnFullPowerSourceChanged;


    public ElectricalManager(SendTcListener listener){
        super(listener);
        _tensions = new float[ElectricalItem.TENSION_COUNT];
        _currents = new float[ElectricalItem.CURRENT_COUNT];
        _relays = new boolean[ElectricalItem.PROT_RELAYS_COUNT];

        for (float f : _tensions) f = 0.0f;
        for (float f : _currents) f = 0.0f;

        _lCurrentListener =     new ArrayList<ListenerCurrentUpdate>();
        _lTensionListener =     new ArrayList<ListenerTensionUpdate>();
        _lRelayModuleListener = new ArrayList<ListenerRelayModuleUpdate>();
        _lOnManagerPowerSourceChanged = null;
        _lOnFullPowerSourceChanged = null;
    }

    public void updateCurrents(char[] tm){
        _currents = CampDuinoProtocol.decodeFloatOnlyTm(tm);
        for (ListenerCurrentUpdate listener : _lCurrentListener) listener.currentUpdated(_currents);
    }

    public void updateTensions(char[] tm){
        _tensions = CampDuinoProtocol.decodeFloatOnlyTm(tm);
        for (ListenerTensionUpdate listener : _lTensionListener) listener.tensionUpdated(_tensions);
    }

    public void updateRelays(char[] tm){
        int i=1;
        for (boolean bR: _relays){
            _relays[i-1] = tm[i] == 1;
            i++;
        }
    }

    @Override
    public String getStringFromTm(char[] tm)
    {
        String str = "";
        float tmp[];

        switch (tm[0])
        {
            case CampDuinoProtocol.TM_CURRENT:
                str += "Currents:";
                tmp = CampDuinoProtocol.decodeFloatOnlyTm(tm);
                for (int i=0;i<tmp.length; i++)
                {
                    str += ElectricalItem.currentNames[i] + " " + Float.toString(tmp[i]) + "A";
                    if (i < tmp.length-1)
                        str += " - ";
                }
                break;
            case CampDuinoProtocol.TM_TENSION:
                str += "Tensions:";
                tmp = CampDuinoProtocol.decodeFloatOnlyTm(tm);
                for (int i=0;i<tmp.length; i++)
                {
                    str += ElectricalItem.tensionNames[i] + " " + Float.toString(tmp[i]) + "V";
                    if (i < tmp.length-1)
                        str += " - ";
                }
                break;
            case CampDuinoProtocol.TM_RELAY:
                str += "Relays status:";
                for (int i=0;i<_relays.length; i++)
                {
                    str += ElectricalItem.relayNames[i] + (_relays[i]?" On":" Off");
                    if (i < _relays.length-1)
                        str += " - ";
                }
                break;
            case CampDuinoProtocol.TM_ELEC_CONF:

                break;
        }
        return str;
    }

    public void updateAlimConfig(char[] tm){
        ElectricalItem.eAlimSource managerPow, fullPow;
        managerPow = (tm[1] ==1? ElectricalItem.eAlimSource.ALIM_AUXILIARY: ElectricalItem.eAlimSource.ALIM_PRIMARY);
        fullPow = (tm[2] == 1? ElectricalItem.eAlimSource.ALIM_AUXILIARY: ElectricalItem.eAlimSource.ALIM_PRIMARY);

        if (managerPow != _eSourceManagerPower) _lOnManagerPowerSourceChanged.onManagerPowerSourceChanged(managerPow);
        if (fullPow != _eSourceFullPower) _lOnFullPowerSourceChanged.onFullPowerSourceChanged(fullPow);

        _eSourceManagerPower = managerPow;
        _eSourceFullPower = fullPow;

    }

    public float getCurrent (CurrentItem.eCurrentType currentId){
        return _currents[currentId.value-1];
    }

    public float getTension (TensionItem.eTensionType tensionId){
        return _tensions[tensionId.value-1];
    }

    public boolean getRelayStatus(ElectricalItem.eRelayType relayId){
        return _relays[relayId.value];
    }

    public void addTensionListener(ListenerTensionUpdate listener){
        _lTensionListener.add(listener);
    }

    public void addCurrentListener(ListenerCurrentUpdate listener){
        _lCurrentListener.add(listener);
    }

    public void addRelayModuleListener(ListenerRelayModuleUpdate listener){
        _lRelayModuleListener.add(listener);
    }

    public void removeTensionListener(ListenerTensionUpdate listener){
        _lTensionListener.remove(listener);
    }

    public void removeCurrentListener(ListenerCurrentUpdate listener) {
        _lCurrentListener.remove(listener);
    }

    public void removeRelayModuleListener(ListenerRelayModuleUpdate listener){
        _lRelayModuleListener.remove(listener);
    }

    public void showDialog(Context context, boolean lightModuleActivated){
        _dialog = new DialogElectrical(context, this);
        addRelayModuleListener(_dialog);
        _dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                removeDialog();
            }
        });
        setDialog(_dialog);
        _dialog.show();
    }

    public void switchLightningFunction(){
        _relays[ElectricalItem.eRelayType.R_LIGHT.value] = listenerSwitchLightModule.functionSwitch();
    }

    @Override public void updateDialog()
    {
        _dialog.relayModuleUpdated();
    }

}
