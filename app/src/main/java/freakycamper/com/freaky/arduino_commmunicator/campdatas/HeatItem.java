package freakycamper.com.freaky.arduino_commmunicator.campdatas;

/**
 * Created by lsa on 17/12/14.
 */
public class HeatItem {

    public static String[] STR_HEAT_MODE_TEXT = {
            "Module désactivé",
            "Ventilation manuelle",
            "Ventilation automatique",
            "Chauffage manuel",
            "Chauffage automatique"
    };

    public enum eHeatModuleState {
        HEAT_MODULE_OFF((byte) 0),
        HEAT_MODULE_VENT_MANUAL((byte) 1),
        HEAT_MODULE_VENT_AUTO((byte) 2),
        HEAT_MODULE_HEAT_MANUAL((byte) 3),
        HEAT_MODULE_HEAT_AUTO((byte) 4);

        public byte value;

        eHeatModuleState(byte value) {
            this.value = value;
        }
    };

    private eHeatModuleState _status = eHeatModuleState.HEAT_MODULE_OFF;
    private char[]  _fanSpeeds = {(char)0, (char)0};
    private float   _tempTarget = -99;

    public HeatItem(eHeatModuleState status, float tempConsigne, char fan1, char fan2){
        _status = status;
        _tempTarget = tempConsigne;
        _fanSpeeds[0] = fan1;
        _fanSpeeds[1] = fan2;
    }

    public HeatItem(HeatItem cp){
        _status = cp.getStatus();
        _tempTarget = cp.getTempConsigne();
        _fanSpeeds[0] = (char)cp.getFanSpeed(0);
        _fanSpeeds[1] = (char)cp.getFanSpeed(1);
    }

    public eHeatModuleState getStatus(){return _status;}

    public int getFanSpeed(int fanIdx){
        if (fanIdx<2)
            return _fanSpeeds[fanIdx];
        else return 0;
    }

    public float getTempConsigne()
    {
        return _tempTarget;
    }

    public boolean isDifferentFrom(HeatItem comp){
        return (this._status != comp._status) ||
            (this._tempTarget != comp._tempTarget) ||
            (this.getFanSpeed(1)!= comp.getFanSpeed(1)) ||
            (this.getFanSpeed(0) != comp.getFanSpeed(0));
    }

    public void updateFanVal(char val, boolean isZ1){
        if (isZ1) _fanSpeeds[0] = val;
        else _fanSpeeds[1] = val;;
    }

    public void updateTempTarget(float newTemp){
        _tempTarget = newTemp;
    }

    public void updateState(eHeatModuleState newState){
        _status = newState;
    }


}

