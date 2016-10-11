package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

import java.util.ArrayList;
import java.util.List;

import freakycamper.com.freaky.arduino_commmunicator.ArduinoCommunicatorActivity;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TemperatureItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;

/**
 * Created by lsa on 09/12/14.
 */
public class TemperatureManager extends MainManager {

    private float[]_lTempItems;

    private List<OnTempTmReceived> _onTempTmUpdateListeners;
    private SQLDatasHelper _database;


    static public TemperatureManager initialiseTemperatureManager(ArduinoCommunicatorActivity mainActivity, SQLDatasHelper database) {
        TemperatureManager ret = new TemperatureManager(mainActivity, database);
        return ret;
    }

    public interface OnTempFridgeUpdate {
        public void tempFridgeUpdate(float fridgeTemp);
    }

    public interface OnTempTmReceived {
        public void newTempTm(float[] tempsTm);
    }


    public TemperatureManager(SendTcListener listener, SQLDatasHelper database) {
        super(listener);
        _lTempItems = new float[TemperatureItem.TEMP_SENSORS_COUNT];
        _onTempTmUpdateListeners = new ArrayList<OnTempTmReceived>();
        _database = database;
    }

    public void updateTemperatures(char[] tm){
        _lTempItems = CampDuinoProtocol.decodeFloatOnlyTm(tm);
       fireNewTempTmEvent();
    };

    @Override
    public String getStringFromTm(char[] tm)
    {
        String str = "";

        switch (tm[0])
        {
            case CampDuinoProtocol.TM_TEMPERATURE:
                str += "Temperatures1: ";
                for (int i=0;i<_lTempItems.length; i++)
                {
                    str += TemperatureItem.tempNames[i] + " " + Float.toString(_lTempItems[i]) + "°C";
                    if (i < _lTempItems.length-1)
                    {
                        if (i == 3) str += "\nTemperatures2: ";
                        else str += " - ";
                    }
                }
                break;
        }
        return str;
    }

    public float getTempFromIdx(int idx){
        return _lTempItems[idx];
    }

    public int getTempsCount(){
        return _lTempItems.length;
    }

    public void addTempTmUpdateListener(OnTempTmReceived listener){
        _onTempTmUpdateListeners.add(listener);
    }

    public void removeTempTmUpdateListener(OnTempTmReceived listener){
        _onTempTmUpdateListeners.remove(listener);
    }

    private void fireNewTempTmEvent(){
        for (OnTempTmReceived listener : _onTempTmUpdateListeners){
            listener.newTempTm(_lTempItems);
        }
    }



}
