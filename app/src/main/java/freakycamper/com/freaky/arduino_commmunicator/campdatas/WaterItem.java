package freakycamper.com.freaky.arduino_commmunicator.campdatas;

import java.util.Date;

/**
 * Created by lsa on 08/12/14.
 */
public class WaterItem {

    int _level;
    float _flow;
    Date _date;

    public interface ToggleSwitchWaterPumpRelay{
        public void actionToggleSwitchPumpRelay(boolean newStatus);
    }

    public WaterItem(int newLevel, float newFlow, Date newDate){
        _level = newLevel;
        _flow = newFlow;
        _date = newDate;
    }

    public WaterItem(int newLevel, float newFlow){
        this(newLevel, newFlow, new Date());
    }

    public Date getDateTag(){
        return _date;
    }

    public int getWaterLevel(){
        return _level;
    }

    public float getFlow(){
        return _flow;
    }

}
