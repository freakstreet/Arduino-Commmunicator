package freakycamper.com.freaky.arduino_commmunicator.campdatas;

/**
 * Created by lsa on 28/01/15.
 */
public class CurrentItem {
    public enum eCurrentType{
        I_COLD      ((char)1),
        I_WATER     ((char)2),
        I_HEATER    ((char)3),
        I_LIGHT     ((char)4),
        I_AUX       ((char)5),
        I_SPARE     ((char)6),
        I_SOLAR     ((char)7);

        public char value;
        private eCurrentType(char value) {
            this.value = value;
        }
    }

    private float fCurrent;

    public CurrentItem(float current){
        fCurrent = current;
    }

    public float getCurrentVal(){
        return fCurrent;
    }

}
