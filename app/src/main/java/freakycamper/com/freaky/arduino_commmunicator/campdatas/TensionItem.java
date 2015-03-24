package freakycamper.com.freaky.arduino_commmunicator.campdatas;

/**
 * Created by lsa on 28/01/15.
 */
public class TensionItem {

    public enum eTensionType{
        U_BATT_PRIM ((char)1),
        U_BATT_AUX  ((char)2),
        U_3         ((char)3),
        U_4         ((char)4),
        U_5         ((char)5),
        U_6         ((char)6),
        U_7         ((char)7);

        public char value;
        private eTensionType(char value) {
            this.value = value;
        }
    }

    float myTension;

    public TensionItem(float tension){
        myTension = tension;
    }

    public float getTension(){
        return myTension;
    }
}
