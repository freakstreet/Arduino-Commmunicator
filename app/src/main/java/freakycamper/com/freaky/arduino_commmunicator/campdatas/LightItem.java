package freakycamper.com.freaky.arduino_commmunicator.campdatas;

/**
 * Created by lsa on 08/12/14.
 */
public class LightItem {

    public enum eLightTypes {
        NORMAL_ON_OFF   ((char)0),
        DIMMER          ((char)1),
        RGB_DIMMER      ((char)2);

        public char value;
        private eLightTypes(char value) {
            this.value = value;
        }
    };

    private eLightTypes _type;
    private int _id;
    private char _r, _g, _b, _dimm;


    public LightItem(int lightId, eLightTypes type) {
        _id = lightId;
        _type = type;
        _dimm = 0;
        _r = 0;
        _g = 0;
        _b = 0;
    }

    public eLightTypes getLightType() {
        return _type;
    }

    public int getId() {
        return _id;
    }

    public void updateLightStatus(char dimm, char r, char g, char b){
        _dimm = dimm;
        _r = r;
        _g = g;
        _b = b;
    };

    public boolean getIsOn(){
        return _r>0;
    }

    public char getDimmValue(){
        return _dimm;
    }

    public char getRedValue(){
        return _r;
    }

    public char getGreenValue(){
        return _g;
    }

    public char getBlueValue(){
        return _b;
    }
}

