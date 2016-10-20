package freakycamper.com.freaky.arduino_commmunicator.campduinoservice;

/**
 * Created by lsa on 05/12/14.
 */
public class CampDuinoProtocol {

    public static char[] FRAME_HEADER = {0xAA, 0xAA, 0xAA, 0xAA};
    public static char[] FRAME_FOOTER = {0x55, 0x55, 0x55, 0x55};
    public static int RX_BUFFER_SIZE = 40960;

    public static final int MSG_REGISTER_CLIENT     = 1;
    public static final int MSG_REGISTERED_CLIENT   = 2;
    public static final int MSG_UNREGISTER_CLIENT   = 3;
    public static final int MSG_SEND_TC             = 4;
    public static final int MSG_GOT_TM              = 5;
    public static final int MSG_LOST_LINK           = 6;
    public static final int MSG_GOT_LINK            = 7;
    public static final int MSG_LOG                 = 8;

    public static final String KEY_DATA_TC = "TelecommandDatas";
    public static final String KEY_DATA_TM = "TelemetryDatas";

    public enum eProtTcSwitch {
        PROT_SWITCH_COLD_MODULE     ((char)10),
        PROT_SWITCH_WATER_MODULE    ((char)11),
        PROT_SWITCH_HEAT_MODULE     ((char)12),
        PROT_SWITCH_LIGHT_MODULE    ((char)13),
        PROT_SWITCH_AUX_MODULE      ((char)14),
        PROT_SWITCH_SPARE_MODULE    ((char)15);

        public char value;

        private eProtTcSwitch(char value) {
            this.value = value;
        }
    };

    public static char PROT_TC_ACTIVATE_BOARD   = 8;
    public static char PROT_TC_LIGHT            = 13;
    public static char PROT_TC_COLD             = 20;
    public static char PROT_TC_HEATER           = 40;

    // TELEMETRY
    public static final char	TM_IS_ALIVE 	    = 9;
    public static final char	TM_MIRROR_TC	    = 10;
    public static final char	TM_CURRENT		    = 70;
    public static final char	TM_TENSION		    = 71;
    public static final char	TM_TEMPERATURE	    = 72;
    public static final char	TM_WATER		    = 73;
    public static final char	TM_RELAY		    = 74;
    public static final char	TM_LIGHT			= 75;
    public static final char	TM_COLD_HOT		    = 76;
    public static final char    TM_ELEC_CONF        = 77;

    public static char[] buildSwitchRelayTC(eProtTcSwitch relay, boolean status){

        char[] ret = new char[2];
        ret[0] = relay.value;
        ret[1] = (status?(char)1:0);
        return ret;
    }

    public static char[] buildSwitchLightTC(int lightId, int dimm, int r, int g,int b){
        char[] ret = new char[6];
        ret[0] = PROT_TC_LIGHT;
        ret[1] = (char)lightId;
        ret[2] = (char)dimm;
        ret[3] = (char)r;
        ret[4] = (char)g;
        ret[5] = (char)b;
        return ret;
    }

    public static char[] buildSetFridgeConsigne(float temp){
        char[] ret = new char[3];
        char tmp[];
        ret[0] = PROT_TC_COLD;
        tmp = encodeFloatToTm(temp);
        ret[1] = tmp[0];
        ret[2] = tmp[1];
        return ret;
    }

    public static byte[] prepareTC(char tc){
        char[] ctc = new char[1];
        ctc[0] = tc;
        return prepareTC(ctc);
    }

    public static byte[] prepareTC(char[] tc) {
        byte[] ret = new byte[tc.length+CampDuinoProtocol.FRAME_FOOTER.length+CampDuinoProtocol.FRAME_HEADER.length];
        int pos = 0;
        // write header
        for (int i=0; i<CampDuinoProtocol.FRAME_HEADER.length; i++) ret[pos++] = (byte)CampDuinoProtocol.FRAME_HEADER[i];
        // write TC
        for (int i=0; i<tc.length; i++) ret[pos++] = (byte)tc[i];
        // write footer
        for (int i=0; i<CampDuinoProtocol.FRAME_FOOTER.length; i++) ret[pos++] = (byte)CampDuinoProtocol.FRAME_FOOTER[i];
        return ret;
    }

    public static char[] encodeFloatToTm(float val){
        //
        //  the value is multiplied by 100
        //  then rounded
        //  and coded to 15bits
        //  the 16th bit is 0 if pisitive, 1 if negative
        //
        //  12.7 -->    1270 -->    0x04 0xF6
        //  -12.7 -->   1270 -->    0x84 0xF6
        //
        char[] ret = new char[2];
        boolean positive  = val >= 0;
        if (!positive) val = -val;
        short s = (short) (Math.round(val*100));

        char lsb = (char)(s & 0xFF);
        char msb = (char)((s & 0xFF00)>> 8);

        if (!positive) msb = (char)(msb | 0x80);
        ret[0] = msb;
        ret[1] = lsb;

        return ret;
    }

    public static float decodeFloatFromTm(char msb, char lsb){
        //
        //  the value is multiplied by 100
        //  then rounded
        //  and coded to 15bits
        //  the 16th bit is 0 if positive, 1 if negative
        //
        //  12.7 -->    1270 -->    0x04 0xF6
        //  -12.7 -->   1270 -->    0x84 0xF6
        //

        char sign = (char)(msb & 0x80);
        char msb7bit = (char)(msb & 0x7F);
        short tmpVal = (short)( (msb7bit<<8) | lsb);
        float val = tmpVal;
        val = val / 100;
        if (sign == 0)
            return val;
        else{
            return -val;
        }
    }

    public static float[] decodeFloatOnlyTm(char[] tm){
        float[] ret = new float[(tm.length-1)/2];
        int i=1;
        for (float fT:ret){
            float cVal = decodeFloatFromTm(tm[2 * i - 1], tm[2 * i]);
            ret[i-1] = cVal;
            i++;
        }
        return ret;
    }

    public static String charArraytoString(char[] data){
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[data.length * 5];
        for ( int j = 0; j < data.length; j++ ) {
            int v = data[j] & 0xFF;

            hexChars[j * 5 + 0] = hexArray[0];
            hexChars[j * 5 + 1] = 'x';
            hexChars[j * 5 + 2] = hexArray[v >>> 4];
            hexChars[j * 5 + 3] = hexArray[v & 0x0F];
            hexChars[j * 5 + 4] = ' ';
        }
        return new String(hexChars);
    }


    public static char decodeSignedByte(byte b)
    {
        return (char)(b<0?0x80+128+b:b);
    }

    public static String getCharArrayValsHexString(char tcCode){
        return getCharArrayValsHexString(new char[tcCode]);
    }

    public static String getCharArrayValsHexString(char[] data){
        String s = "";
        int i = 0;
        for (char c : data){
            int val = c;
            if (i>0) s+= " ";
            s += "0x" + String.format("%02X", val);
            i++;
        }
        return s;
    }

    public static String getByteArrayValsHexString(byte[] data){
        char[] chs = new char[data.length];
        for (int i=0;i<data.length; i++)
            chs[i] = (char)data[i];
        return getCharArrayValsHexString(chs);
    }

    public static String getStringFromTmAlive(char[] tm)
    {
        int val = (tm[1] << 8) + tm[2];
        return "TM alive, cnt: " + val;
    }

}
