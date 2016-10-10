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

    int counter = 0;

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

        switch (counter++ % 4)
        {
            case 0 :    // generate random Tensions
                tm = new char[7];
                float u1, u2, u3;
                u1 = 11.5f +2.5f * r.nextFloat();
                u2 = 11.5f +2.5f * r.nextFloat();
                u3 = 5f * r.nextFloat();
                tm[pos++] = CampDuinoProtocol.TM_TENSION;
                tmp = CampDuinoProtocol.encodeEncodedFloatToTm(u1);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                tmp = CampDuinoProtocol.encodeEncodedFloatToTm(u2);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];
                tmp = CampDuinoProtocol.encodeEncodedFloatToTm(u3);
                tm[pos++] = tmp[0];
                tm[pos++] = tmp[1];

                generateTM(tm);
                break;

            case 1 :    // generate random currents
                tm = new char[11];
                tm[pos++] = CampDuinoProtocol.TM_CURRENT;
                for (int i=0;i<5;i++)
                {
                    tmp = CampDuinoProtocol.encodeEncodedFloatToTm(10f * r.nextFloat());
                    tm[pos++] = tmp[0];
                    tm[pos++] = tmp[1];
                }
                generateTM(tm);
                break;

            case 2:     // generate random temps

                break;
        }
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
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[2] = (tc[1]==1? (char)1:0);
            tm[3] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_AUX_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[3] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            tm[5] = (tc[1]==1? (char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_COLD_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (tc[1]==1? (char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[3] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)?(char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_HEAT_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[3] = (tc[1]==1? (char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0]== CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_HEAT_MODULE.value){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_RELAY;
            tm[1] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_COLD)?(char)1:0);
            tm[2] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER)?(char)1:0);
            tm[3] = (tc[1]==1? (char)1:0);
            tm[4] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)?(char)1:0);
            tm[5] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_AUX)?(char)1:0);
            tm[6] = (managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)?(char)1:0);
            generateTM(tm);
        }
        else if (tc[0] == CampDuinoProtocol.PROT_TC_COLD){
            char[]tm = new char[6];
            tm[0] = CampDuinoProtocol.TM_COLD_HOT;
            // cold temp consigne value, from java to java there is no issue with signed/unsigned as seen with arduino so compensating TM with +128 when simulating
            char tempCorrected = tc[1];
            tempCorrected = (char) (tempCorrected + (char)128);
            tm[1] = tempCorrected;
            // TODO : retrieve HOT parameters from heat manager
            tm[2] = (char)(managerHeat.getHeatParams().getStatus().value);
            tm[3] = CampDuinoProtocol.encodeTempToChar(managerHeat.getHeatParams().getTempConsigne());
            tm[4] = (char)(managerHeat.getHeatParams().getFanSpeed(0));
            tm[5] = (char)(managerHeat.getHeatParams().getFanSpeed(1));
            generateTM(tm);

            tm = new char[managerTemp.getTempsCount()+1];
            tm[0] = CampDuinoProtocol.TM_TEMPERATURE;
            char newTemp = (char)(tc[1] -2);
            tm[1] = newTemp;
            for (int i=1; i<tm.length-1; i++){
                tm[i+1] = CampDuinoProtocol.encodeTempToChar(managerTemp.getTempFromIdx(i));
            }
            generateTM(tm);
        }
        else if (tc[0] == CampDuinoProtocol.PROT_TC_HEATER){
            char[]tm = new char[6];
            tm[0] = CampDuinoProtocol.TM_COLD_HOT;
            tm[1] = CampDuinoProtocol.encodeTempToChar(managerCold.getTempConsigne());

            tm[2] = tc[1];
            tm[3] = tc[2];
            tm[4] = tc[3];
            tm[5] = tc[4];
            generateTM(tm);

            tm = new char[managerTemp.getTempsCount()+1];
            tm[0] = CampDuinoProtocol.TM_TEMPERATURE;
            char newTemp = (char)(tc[2] - 1);
            tm[2] = newTemp;
            tm[3] = newTemp;

            tm[1] = CampDuinoProtocol.encodeTempToChar(managerTemp.getTempFromIdx(0));
            for (int i=3; i<tm.length-1; i++){
                tm[i+1] = CampDuinoProtocol.encodeTempToChar(managerTemp.getTempFromIdx(i));
            }
            generateTM(tm);
        }

    }

    private void generateTM(char[] tm){
        if (tmListener != null)
            tmListener.onReceivedRawTM(tm);
    }
}
