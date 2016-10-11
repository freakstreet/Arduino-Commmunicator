package freakycamper.com.freaky.arduino_commmunicator.campdatas;

import java.util.Date;

/**
 * Created by lsa on 09/12/14.
 */
public class TemperatureItem {

    public static int   TEMP_SENSORS_COUNT = 7;

    public static String[] tempNames = {"T1", "T2", "T3", "T4", "T5", "T6", "T7"};

    float _temp;
    Date _date;

    public enum eTemperatureType{
        TEMP_FRIDGE     ((char)1),
        TEMP_INSIDE1    ((char)2),
        TEMP_INSIDE2    ((char)3),
        TEMP_WATER_PRIM ((char)4),
        TEMP_WATER_SEC  ((char)5),
        TEMP_HEAR_AIR   ((char)6),
        TEMP_OUTSIDE    ((char)7);

        public char value;
        private eTemperatureType(char value) {
            this.value = value;
        }
    }

    public static float getTemperatureFromType(float[] tempArray, eTemperatureType type){
        return tempArray[type.value-1];
    }

    public TemperatureItem(float newTemp){
        this(newTemp, new Date());

    }

    public TemperatureItem(float newTemp, Date date){
        _temp = newTemp;
        _date = date;
    }


    public float getTemperature(){
        return _temp;
    }

    public static String getTemperatureStr(float t){ return String.format("%.1f", t) + "°C"; }

    public Date getDateTag(){
        return _date;
    }
}
