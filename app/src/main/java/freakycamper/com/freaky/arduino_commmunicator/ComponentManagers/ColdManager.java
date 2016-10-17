package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;


import android.content.Context;
import android.content.DialogInterface;

import com.google.common.collect.EvictingQueue;

import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TemperatureItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogFridge;

/**
 * Created by lsa on 09/12/14.
 */
public class ColdManager extends MainManager implements TemperatureManager.OnTempTmReceived, ElectricalManager.ListenerRelayModuleUpdate {

    public static final int DEFAULT_TEMP_FRIDGE = 6;
    private float                           _tempConsigne = -99;
    private float                           _lastKnownTemp = -99;
    private EvictingQueue<TemperatureItem>  _tempArray;
    private boolean                         _relayColdStatus = false;
    private ElectricalManager               _elecManager;

    static public ColdManager initialiseColdManager(Context context, SendTcListener listener, final ElectricalManager elecTm, final TemperatureManager tempTm, SQLDatasHelper database) {
        ColdManager ret = new ColdManager(context, listener, elecTm, tempTm, database);
        return ret;
    }

    public ColdManager(Context context, SendTcListener listener, ElectricalManager elecTm, TemperatureManager tempTm, SQLDatasHelper database ) {
        super(listener);

        _tempArray = database.retrieveLastLoggedFridgeTemps();
        _elecManager = elecTm;
        elecTm.addRelayModuleListener(this);
        tempTm.addTempTmUpdateListener(this);
    }


    public void updateColdTm(char[] tm){
        float[] tmp = CampDuinoProtocol.decodeFloatOnlyTm(tm);
        _tempConsigne = tmp[0];
    }

    private void sendTempConsigne(float value){
        char[] tc = new char[3];
        char[] tmp;
        tc[0] = CampDuinoProtocol.TM_COLD_HOT;
        tmp = CampDuinoProtocol.encodeFloatToTm(value);
        tc[1] = tmp[0];
        tc[2] = tmp[1];
        sendTc(tc);
    }

    public void showDialog(Context context){
        correspondingDialog  = new DialogFridge(context, this);
        correspondingDialog .setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                correspondingDialog  = null;
            }
        });
        correspondingDialog.show();
    }

    public float getTempConsigne(){
        return _tempConsigne;
    }

    public boolean getColdModuleStatus(){
        return _relayColdStatus;
    }

    public float getTempFridge(){
        return _lastKnownTemp;
    }

    public EvictingQueue<TemperatureItem>getFridgeTempHistoric(){
        return _tempArray;
    }

    @Override
    public void relayModuleUpdated() {
        _relayColdStatus = _elecManager.getRelayStatus(ElectricalItem.eRelayType.R_COLD);
        if (correspondingDialog  != null) {
            ((DialogFridge)correspondingDialog).updateSwitchColdStatus(_relayColdStatus);
        }
    }

    @Override
    public void newTempTm(float[] tempsTm) {
        float fridgeTemp = TemperatureItem.getTemperatureFromType(tempsTm, TemperatureItem.eTemperatureType.TEMP_FRIDGE);
        _lastKnownTemp = fridgeTemp;
        TemperatureItem t = new TemperatureItem(fridgeTemp);
        _tempArray.add(t);
        // add value to database
        //_dbHelper.registerNewTemp(t);


    }

    @Override public void updateDialog()
    {
        relayModuleUpdated();
        ((DialogFridge)correspondingDialog).updateGui();
    }
}
