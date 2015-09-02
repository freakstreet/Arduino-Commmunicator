/*
 * Copyright (C) 2012 Mathias Jeppsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package freakycamper.com.freaky.arduino_commmunicator;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ElectricalManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.HeatManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.LightManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.MainManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.TemperatureManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.ArduinoCommunicatorService;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyButton;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyGauge;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyRow;

public class ArduinoCommunicatorActivity extends Activity implements
        MainManager.SendTcListener,
        ElectricalManager.ListenerRelayModuleUpdate,
        LightManager.switchLightModule {

    private final static boolean SIMULATE_BOARD = true;

    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;

    private SQLDatasHelper dbHelper;
    private ElectricalManager managerElectrical;
    private LightManager managerLights;
    private WaterManager managerWater;
    private ColdManager managerCold;
    private TemperatureManager managerTemp;
    private HeatManager managerHeat;


    private final static String TAG = "ArduinoCommunicatorActivity";
    private final static boolean DEBUG = false;

    private void findDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        if (DEBUG) Log.d(TAG, "length: " + usbDeviceList.size());
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            UsbDevice tempUsbDevice = deviceIterator.next();

            // Print device information. If you think your device should be able
            // to communicate with this app, add it to accepted products below.
            if (DEBUG) Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
            if (DEBUG) Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
            if (DEBUG) Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
            if (DEBUG) Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
            if (DEBUG) Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
            if (DEBUG) Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
            if (DEBUG) Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
            if (DEBUG) Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

            if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) {
                if (DEBUG) Log.i(TAG, "FreakyCamper device found!");

                switch (tempUsbDevice.getProductId()) {
                case ARDUINO_UNO_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Uno " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
                    //Toast.makeText(getBaseContext(), "Arduino Mega 2560 R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Uno R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 ADK R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 ADK " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                    break;
                }
            }
        }

        if (SIMULATE_BOARD){
            Toast.makeText(getBaseContext(), getString(R.string.simulation_mode), Toast.LENGTH_LONG).show();
            onServiceConnected();
        }
        else{
            if (usbDevice == null) {
                if (DEBUG) Log.i(TAG, "No device found!");
                Toast.makeText(getBaseContext(), getString(R.string.no_device_found), Toast.LENGTH_LONG).show();
            } else {
                if (DEBUG) Log.i(TAG, "Device found!");
                Intent startIntent = new Intent(getApplicationContext(), ArduinoCommunicatorService.class);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, startIntent, 0);
                usbManager.requestPermission(usbDevice, pendingIntent);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (DEBUG) Log.d(TAG, "onCreate()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
        filter.addAction(ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
        filter.addAction(ArduinoCommunicatorService.SERVICE_CREATED);
        registerReceiver(mReceiver, filter);

        findDevice();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
        super.onNewIntent(intent);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
            if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
            findDevice();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void sendTC(char[] data) {
        if (!SIMULATE_BOARD) {
            Intent tx = new Intent(ArduinoCommunicatorService.SEND_DATA_INTENT);
            tx.putExtra(ArduinoCommunicatorService.DATA_EXTRA, data);
            //Toast.makeText(getBaseContext(), "send tc", Toast.LENGTH_LONG).show();
            sendBroadcast(tx);
        }
        else{
            simulateFromTC(data);
        }
    }

    private void onServiceConnected(){
        // **** Initialize functional components ****
        dbHelper = new SQLDatasHelper(getBaseContext());
        managerElectrical = ElectricalManager.initialiseElectricalManager(this);
        managerTemp = TemperatureManager.initialiseTemperatureManager(this, dbHelper);
        managerLights = LightManager.initialiseLightManager(this);
        managerWater = WaterManager.initialiseWaterManager(this, managerElectrical, dbHelper);
        managerCold = ColdManager.initialiseColdManager(this, this, managerElectrical, managerTemp, dbHelper);
        managerHeat = HeatManager.initialiseHeatManager(this, this, managerTemp);
        managerElectrical.addRelayModuleListener(this);
        managerWater.setGauge((FreakyGauge)findViewById(R.id.gaugeWater));
        managerLights.updateFromTM(new char[]{CampDuinoProtocol.TM_LIGHT, 0, LightItem.eLightTypes.NORMAL_ON_OFF.value, 0, 0, 0, 0});

        managerElectrical.setListenerSwitchLightModule(this);
        // **** Initialize GUI components ****
        initGuiComponents();
    }

    private void initGuiComponents(){
        // **** Configure main components ****
        FreakyGauge gaugeBattery, gaugeWater;
        gaugeBattery = (FreakyGauge)findViewById(R.id.gaugeBatterie);
        gaugeBattery.addIcon(R.drawable.icon_battery);
        gaugeBattery.setIconIdx(0);
        gaugeBattery.setActiveMode(true);
        gaugeBattery.set_percentFill(50);
        gaugeBattery.setLegend(getString(R.string.battery_lb_level) + " " + getString(R.string.battery_auxiliary));
        gaugeBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FreakyGauge g = (FreakyGauge) v;
                int val = g.getPercentFill();
                val -= 2;
                if (val < 0) val = 100;
                g.set_percentFill(val);
            }
        });

        gaugeWater = (FreakyGauge)findViewById(R.id.gaugeWater);
        gaugeWater.setActiveMode(false);
        gaugeWater.setLegend(getString(R.string.water_lb_relay_on));

        FreakyButton fb1, fb2;
        fb1 = (FreakyButton)findViewById(R.id.btPower);
        fb1.addIcon(R.drawable.icon_water);
        fb1.setIconIdx(0);

        fb2 = (FreakyButton)findViewById(R.id.btCold);
        fb2.addIcon(R.drawable.icon_battery);
        fb2.setIconIdx(0);
        fb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColdButtonClick();
            }
        });

        FreakyRow fr = (FreakyRow)findViewById(R.id.rawElec);
        fr.setLabel(getString(R.string.row_electricity));

        fr = (FreakyRow)findViewById(R.id.rawLights);
        fr.setLabel(getString(R.string.row_lightning));
        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLightButtonClick();
            }
        });
        fr.setActivationMode(false);

        fr = (FreakyRow)findViewById(R.id.rawHeater);
        fr.setLabel(getString(R.string.row_heater));
        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeaterButtonClick();
            }
        });
        fr.setActivationMode(false);

        fr = (FreakyRow)findViewById(R.id.rawParameters);
        fr.setMainRow();
        fr.setLabel(getString(R.string.row_electricity_dispatch));
        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onElecButtonClick();
            }
        });
        fr.setActivationMode(true);
    }

    private void onHeaterButtonClick() {
        managerHeat.showDialog(this);
    }
    private void onLightButtonClick(){
        managerLights.showDialog(this);
    }
    private void onElecButtonClick(){
        managerElectrical.showDialog(this, managerLights.getModuleIsAstive());
    }
    private void onColdButtonClick(){
        managerCold.showDialog(this);
    }


    @Override
    public void relayModuleUpdated() {
        // Etat switch pompe a eau
        boolean pumpStatus = managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER);
        FreakyGauge g = (FreakyGauge)findViewById(R.id.gaugeWater);
        g.setIconIdx((pumpStatus?0:1));
        g.setActiveMode(pumpStatus);
        if(pumpStatus)
            g.setLegend(getString(R.string.water_lb_relay_on));
        else
            g.setLegend(getString(R.string.water_lb_relay_off));

        // Etat module chauffage
        boolean heatStatus = managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER);
        FreakyRow fr = (FreakyRow)findViewById(R.id.rawHeater);
        fr.setActivationMode(heatStatus);

        // Etat module lumière
        boolean lightStatus = managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT);
        fr = (FreakyRow)findViewById(R.id.rawLights);
        fr.setActivationMode(lightStatus);
    }

    public void gotTM(char[] tm){
        if (tm.length==0) return;
        switch (tm[0]) {
            case CampDuinoProtocol.TM_LIGHT :
                managerLights.updateFromTM(tm);
                break;
            case CampDuinoProtocol.TM_WATER:
                managerWater.updateFromTM(tm);
                break;
            case CampDuinoProtocol.TM_CURRENT:
                managerElectrical.updateCurrents(tm);
                break;
            case CampDuinoProtocol.TM_TENSION:
                managerElectrical.updateTensions(tm);
                break;
            case CampDuinoProtocol.TM_RELAY:
                managerElectrical.updateRelays(tm);
                break;
            case CampDuinoProtocol.TM_ELEC_CONF:
                managerElectrical.updateAlimConfig(tm);
                break;
            case CampDuinoProtocol.TM_TEMPERATURE:
                managerTemp.updateTemperatures(tm);
                break;
            case CampDuinoProtocol.TM_COLD_HOT:
                managerCold.updateColdTm(tm);
                managerHeat.updateFromTM(tm);
                break;
            default :
                break;
        }
    }

    private void simulateFromTC(char[] tc){
        if (tc[0]== CampDuinoProtocol.PROT_TC_LIGHT){
            char[] tm = new char[7];
            tm[0] = CampDuinoProtocol.TM_LIGHT;
            tm[1] = tc[1];
            tm[2] = managerLights.getLight(tm[1]).getLightType().value;
            tm[3] = tc[2];
            tm[4] = tc[3];
            tm[5] = tc[4];
            tm[6] = tc[5];
            gotTM(tm);
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
            gotTM(tm);
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
            gotTM(tm);
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
            gotTM(tm);
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
            gotTM(tm);
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
            gotTM(tm);
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
            gotTM(tm);

            tm = new char[managerTemp.getTempsCount()+1];
            tm[0] = CampDuinoProtocol.TM_TEMPERATURE;
            char newTemp = (char)(tc[1] -2);
            tm[1] = newTemp;
            for (int i=1; i<tm.length-1; i++){
                tm[i+1] = CampDuinoProtocol.encodeTempToChar(managerTemp.getTempFromIdx(i));
            }
            gotTM(tm);
        }
        else if (tc[0] == CampDuinoProtocol.PROT_TC_HEATER){
            char[]tm = new char[6];
            tm[0] = CampDuinoProtocol.TM_COLD_HOT;
            tm[1] = CampDuinoProtocol.encodeTempToChar(managerCold.getTempConsigne());

            tm[2] = tc[1];
            tm[3] = tc[2];
            tm[4] = tc[3];
            tm[5] = tc[4];
            gotTM(tm);

            tm = new char[managerTemp.getTempsCount()+1];
            tm[0] = CampDuinoProtocol.TM_TEMPERATURE;
            char newTemp = (char)(tc[2] - 1);
            tm[2] = newTemp;
            tm[3] = newTemp;

            tm[1] = CampDuinoProtocol.encodeTempToChar(managerTemp.getTempFromIdx(0));
            for (int i=3; i<tm.length-1; i++){
                tm[i+1] = CampDuinoProtocol.encodeTempToChar(managerTemp.getTempFromIdx(i));
            }
            gotTM(tm);
        }

    }



    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        private void handleTransferedData(Intent intent, boolean receiving) {
            gotTM(intent.getCharArrayExtra(ArduinoCommunicatorService.DATA_EXTRA));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) Log.d(TAG, "onReceive() " + action);

            if (ArduinoCommunicatorService.DATA_RECEIVED_INTENT.equals(action)) {
                handleTransferedData(intent, true);
            } else if (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT.equals(action)) {
                handleTransferedData(intent, false);
            } else if (ArduinoCommunicatorService.SERVICE_CREATED.equals(action)){
                onServiceConnected();
            }
        }
    };


    @Override
    public boolean functionSwitch() {
        boolean ret = managerLights.switchModuleActivation();
        FreakyRow fr = (FreakyRow)findViewById(R.id.rawLights);
        fr.setLabel(getString(R.string.row_lightning));
        fr.setActivationMode(ret);
        return ret;
    }
}
