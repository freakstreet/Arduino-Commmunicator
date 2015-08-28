package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;


import android.content.Context;

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
    private EvictingQueue<TemperatureItem> _tempArray;
    private boolean                         _relayColdStatus = false;
    private ElectricalManager               _elecManager;

    static public ColdManager initialiseColdManager(Context context, SendTcListener listener, final ElectricalManager elecTm, final TemperatureManager tempTm, SQLDatasHelper database) {
        ColdManager ret = new ColdManager(context, listener, elecTm, tempTm, database);
        return ret;
    }

    DialogFridge _dialog = null;

    public ColdManager(Context context, SendTcListener listener, ElectricalManager elecTm, TemperatureManager tempTm, SQLDatasHelper database ) {
        super(listener);

        _tempArray = database.retrieveLastLoggedFridgeTemps();
        _elecManager = elecTm;
        elecTm.addRelayModuleListener(this);
        tempTm.addTempTmUpdateListener(this);
    }


    public void updateColdTm(char[] tm){
        _tempConsigne = CampDuinoProtocol.decodeTempFromChar(tm[1]);
    }

    private void sendTempConsigne(float value){
        char[] tc = new char[2];
        tc[0] = CampDuinoProtocol.TM_COLD_HOT;
        tc[0] = CampDuinoProtocol.encodeTempToChar(value);
        sendTc(tc);
    }

    public void showDialog(Context context){
        _dialog = new DialogFridge(context, this);
        _dialog.show();
    }

    public float getTempConsigne(){
        return _tempConsigne;
    }

    public boolean getColdModuleStatus(){
        return _relayColdStatus;
    }

    public float getTempFridge(){
        if (_tempArray.size() == 0) return -99;
        return _tempArray.element().getTemperature();
    }


    public EvictingQueue<TemperatureItem>getFridgeTempHistoric(){
        return _tempArray;
    }


    @Override
    public void relayModuleUpdated() {
        _relayColdStatus = _elecManager.getRelayStatus(ElectricalItem.eRelayType.R_COLD);
        if (_dialog != null) {
            _dialog.updateSwitchColdStatus(_relayColdStatus);
        }
    }

    @Override
    public void newTempTm(float[] tempsTm) {
        float fridgeTemp = TemperatureItem.getTemperatureFromType(tempsTm, TemperatureItem.eTemperatureType.TEMP_FRIDGE);
            TemperatureItem t = new TemperatureItem(fridgeTemp);
            _tempArray.add(t);
            // add value to database
            //_dbHelper.registerNewTemp(t);


    }
}
