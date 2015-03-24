package freakycamper.com.freaky.arduino_commmunicator.campdatas;

/**
 * Created by lsa on 08/12/14.
 */
public class ElectricalItem {
    public static int   TENSION_COUNT       = 2;
    public static int   CURRENT_COUNT       = 7;
    public static char  PROT_RELAYS_COUNT   = 6;

    public enum eRelayType{
        R_COLD      ((char)0),
        R_WATER     ((char)1),
        R_HEATER    ((char)2),
        R_LIGHT     ((char)3),
        R_AUX       ((char)4),
        R_SPARE     ((char)5);

        public char value;
        private eRelayType(char value) {
            this.value = value;
        }
    }

    public enum eAlimSource{
        ALIM_AUXILIARY ((char)1),
        ALIM_PRIMARY ((char)2);

        public char value;
        private eAlimSource(char value) {
            this.value = value;
        }
    }

}
