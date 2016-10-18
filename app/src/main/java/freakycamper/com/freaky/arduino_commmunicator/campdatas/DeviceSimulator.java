package freakycamper.com.freaky.arduino_commmunicator.campdatas;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ElectricalManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.HeatManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.LightManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.TemperatureManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.GotTmListener;

/**
 * Created by lsa on 10/10/16.
 */

public class DeviceSimulator {

    private GotTmListener tmListener = null;

    private ElectricalManager managerElectrical = null;
    private LightManager managerLights = null;
    private HeatManager managerHeat = null;
    private TemperatureManager managerTemp = null;
    private ColdManager managerCold = null;
    private WaterManager managerWater = null;

    short counter = 0;

    public DeviceSimulator(GotTmListener listener)
    {
        tmListener = listener;
    }

    public void startSimulator()
    {
        // prepare timer to call tm reception simulation
        Timer timer;
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                generateTm();
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void generateTm()
    {
        char[] tm, tmp;
        char pos=0;
        Random r = new Random();

        switch (counter++ % 5)
        {
            case 0 :    // generate random Tensions
                tm = new char[7];
                float u1, u2, u3;
                u1 = 11.5f +2.5f * r.nextFloat();
                u2 = 11.5f +2.5f * r.nextFloat();
                u3 = 5f * r.nextFloat();
                tm[pos++] = CampDuinoProtocol.TM_TENSION;
                tmp = CampDuinoProtocol.encodeFloatToTm(u1);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                tmp = CampDuinoProtocol.encodeFloatToTm(u2);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                tmp = CampDuinoProtocol.encodeFloatToTm(u3);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];

                generateTM(tm);
                break;

            case 1 :    // generate random currents
                tm = new char[11];
                tm[pos++] = CampDuinoProtocol.TM_CURRENT;
                for (int i=0;i<5;i++)
                {
                    tmp = CampDuinoProtocol.encodeFloatToTm(10f * r.nextFloat());
                    tm[pos++] = tmp[0];
                    tm[pos++] = tmp[1];
                }
                generateTM(tm);
                break;

            case 2:     // generate random temps
                tm = new char[15];
                tmp = new char[2];
                tm[pos++] = CampDuinoProtocol.TM_TEMPERATURE;
                // Fridge
                float t = managerCold.getTempConsigne();
                if (t < -50) t = 8;
                t = t - 1 + 2*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                // Inside 1
                t = 15 + 15*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                // Inside 2
                t = t -1 + 2*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                // T Water primary
                t = 19 + 2*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                // T Water secondary
                t = 60 + 10*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                // T Heated air
                t = 30 + 15*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                // T Outside
                t = 2 + 5*r.nextFloat();
                tmp = CampDuinoProtocol.encodeFloatToTm(t);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                generateTM(tm);
                break;

            case 3 :    // generate Relays TM
                tm = new char[8];
                tm[pos++] = CampDuinoProtocol.TM_RELAY;
                tm[pos++] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
                tm[pos++] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
                tm[pos++] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
                tm[pos++] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
                tm[pos++] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
                tm[pos++] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
                generateTM(tm);
                break;

            case 4 :    // generate water TM
                tm = new char[8];
                tm[pos++] = CampDuinoProtocol.TM_WATER;
                // is pump active ?
                if (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER))
                {
                    tm[pos++] = (char)(100*r.nextFloat() >= 50?1:0);
                }
                else
                    tm[pos++] = 0;

                tm[pos++] = (char)Math.round(100*r.nextFloat()); // tank level
                tm[pos++] = (char)(100*r.nextFloat() >= 50?1:0);    // grey water tank full
                tmp = CampDuinoProtocol.encodeFloatToTm(16*r.nextFloat());  // water flow
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                tm[pos++] = (char)(100*r.nextFloat() >= 50?1:0);    // evier openedl
                tm[pos++] = (char)(100*r.nextFloat() >= 50?1:0);    // shower opened
                generateTM(tm);
                break;

        }

        tm = new char[3];
        tm[0] = CampDuinoProtocol.TM_IS_ALIVE;
        tm[1] = (char) (counter >>> 8);
        tm[2] = (char) counter;
        generateTM(tm);
    }

    public void setElectricalManager(ElectricalManager mgr)
    {
        managerElectrical = mgr;
    }
    public void setLightManager(LightManager mgr)
    {
        managerLights = mgr;
    }
    public void setHeatManager(HeatManager mgr)
    {
        managerHeat = mgr;
    }
    public void setTemperatureManager(TemperatureManager mgr)
    {
        managerTemp = mgr;
    }
    public void setColdManager(ColdManager mgr)
    {
        managerCold = mgr;
    }

    public void setWaterManager(WaterManager mgr)
    {
        managerWater = mgr;
    }

    public void simulateFromTC(char[] tc){
        if (tc[0]== CampDuinoProtocol.PROT_TC_LIGHT){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_LIGHT;
            tm[1] = tc[1];
            tm[2] = managerLights.getLight(tm[1]).getLightType().value;
            tm[3] = tc[2];
            tm[4] = tc[3];
            tm[5] = tc[4];
            tm[6] = tc[5];
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_WATER_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (tc[1]==1? (char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[3] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_AUX_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[3] = (tc[1]==1? (char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_COLD_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[2] = (tc[1]==1? (char)1:0);
            tm[3] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_HEAT_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[3] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[4] = (tc[1]==1? (char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0] == CampDuinoProtocol.PROT_TC_COLD){
            char[]tm = new char[8];
            char tmp[];
            tm[0] = CampDuinoProtocol.TM_COLD_HOT;
            tm[1] = tc[1];
            tm[2] = tc[2];
            HeatItem h = managerHeat.getHeatParams();
            tm[3] = (char)(h.getStatus().value);
            tmp = CampDuinoProtocol.encodeFloatToTm(h.getTempConsigne());
            tm[4] = tmp[0];
            tm[5] = tmp[1];
            tm[6] = (char)(h.getFanSpeed(0));
            tm[7] = (char)(h.getFanSpeed(1));
            generateTM(tm);
        }
        else if (tc[0] == CampDuinoProtocol.PROT_TC_HEATER){
            char[]tm = new char[8];
            char tmp[];
            tm[0] = CampDuinoProtocol.TM_COLD_HOT;
            tmp = CampDuinoProtocol.encodeFloatToTm(managerCold.getTempConsigne());
            tm[1] = tmp[0];
            tm[2] = tmp[1];
            tm[3] = tc[1];
            tm[4] = tc[2];
            tm[5] = tc[3];
            tm[6] = tc[4];
            tm[7] = tc[5];
            generateTM(tm);
        }

    }

    private void generateTM(char[] tm){
        if (tmListener != null)
            tmListener.onReceivedRawTM(tm);
    }
}
