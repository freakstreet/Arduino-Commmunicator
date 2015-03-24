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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import freakycamper.com.freaky.arduino_commmunicator.utils.CharCircularFifoBuffer;

public class ArduinoCommunicatorActivity extends Activity implements
        MainManager.SendTcListener,
        ElectricalManager.ListenerRelayModuleUpdate {

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
                if (DEBUG) Log.i(TAG, "Arduino device found!");

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
                    Toast.makeText(getBaseContext(), "Arduino Mega 2560 R3 " + getString(R.string.found), Toast.LENGTH_SHORT).show();
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
        Intent tx = new Intent(ArduinoCommunicatorService.SEND_DATA_INTENT);
        tx.putExtra(ArduinoCommunicatorService.DATA_EXTRA, data);
        //Toast.makeText(getBaseContext(), "send tc", Toast.LENGTH_LONG).show();
        sendBroadcast(tx);
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
        // **** Initialize GUI components ****
        initGuiComponents();
    }

    private void initGuiComponents(){
        // **** Configure main components ****
        FreakyGauge gaugeBattery, gaugeWater;
        gaugeBattery = (FreakyGauge)findViewById(R.id.gaugeBatterie);
        gaugeBattery.addIcon(R.drawable.icon_battery);
        gaugeBattery.setIconIdx(0);
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
        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onElecButtonClick();
            }
        });
        fr = (FreakyRow)findViewById(R.id.rawLights);
        fr.setLabel(getString(R.string.row_lightning));
        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLightButtonClick();
            }
        });
        fr = (FreakyRow)findViewById(R.id.rawHeater);
        fr.setLabel(getString(R.string.row_heater));
        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeaterButtonClick();;
            }
        });
        fr = (FreakyRow)findViewById(R.id.rawParameters);
        fr.setMainRow();
        fr.setLabel(getString(R.string.row_parameters));
    }

    private void onHeaterButtonClick() {
        managerHeat.showDialog(this);
    }
    private void onLightButtonClick(){
        managerLights.showDialog(this);
    }
    private void onElecButtonClick(){
        managerElectrical.showDialog(this);
    }
    private void onColdButtonClick(){
        managerCold.showDialog(this);
    }

    @Override
    public void relayModuleUpdated(boolean[] relayList) {
        // Etat switch pompe ÃÂ  eau
        boolean pumpStatus = managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_WATER);
        FreakyGauge g = (FreakyGauge)findViewById(R.id.gaugeWater);
        g.setIconIdx((pumpStatus?0:1));
        if(pumpStatus)
            g.setLegend(getString(R.string.water_lb_relay_on));
        else
            g.setLegend(getString(R.string.water_lb_relay_off));
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
}
