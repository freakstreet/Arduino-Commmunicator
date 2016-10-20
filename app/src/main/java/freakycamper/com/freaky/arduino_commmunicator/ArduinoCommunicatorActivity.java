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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ElectricalManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.HeatManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.LightManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.MainManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.TemperatureManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.DeviceSimulator;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TensionItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.ArduinoCommunicatorService;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.GotTmListener;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogTelemetryView;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyButton;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyGauge;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyRow;
import freakycamper.com.freaky.arduino_commmunicator.gui.UIUpdater;
import freakycamper.com.freaky.arduino_commmunicator.utils.AcceleroMonitoring;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;

public class ArduinoCommunicatorActivity extends Activity implements
        MainManager.SendTcListener,
        ElectricalManager.ListenerRelayModuleUpdate,
        ElectricalManager.ListenerTensionUpdate,
        LightManager.switchLightModule,
        GotTmListener,
        AcceleroMonitoring.OnShakeDetected
{

    private final static String DEVICE_NAME = "Android SDK built for x86";

    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;

    GotTmListener tmListener = null;

    private SQLDatasHelper dbHelper;
    private ElectricalManager managerElectrical;
    private LightManager managerLights;
    private WaterManager managerWater;
    private ColdManager managerCold;
    private TemperatureManager managerTemp;
    private HeatManager managerHeat;

    private boolean use_simulator = false;
    private DeviceSimulator simulator = null;

    private UIUpdater mUIUpdater;
    private FreakyGauge gaugeBattery, gaugeWater;
    private DialogTelemetryView dlgConsoleTM;
    private TextView textTime;

    private final static String TAG = "ArduinoCommunicatorActivity";
    private final static boolean DEBUG = false;

    private AcceleroMonitoring acceleroMonitor;

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

        dlgConsoleTM = null;

        if (DEBUG) Log.d(TAG, "onCreate()");

        acceleroMonitor = new AcceleroMonitoring(getBaseContext());
        acceleroMonitor.setShakeListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
        filter.addAction(ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
        filter.addAction(ArduinoCommunicatorService.SERVICE_CREATED);
        registerReceiver(mReceiver, filter);
        findDevice();
        ImageView imgBtUSB = (ImageView)findViewById(R.id.img_usb_status);
        final Context ctx = this;
        imgBtUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swowTmConsoleDialog();
            }
        });

        if (Build.MODEL.compareTo(DEVICE_NAME) == 0)
        {
            use_simulator = true;
            Toast.makeText(getBaseContext(), getString(R.string.simulation_mode), Toast.LENGTH_LONG).show();
            simulator = new DeviceSimulator(this);
            onServiceConnected();
        }
        else findDevice();

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
        if (!use_simulator) {
            Intent tx = new Intent(ArduinoCommunicatorService.SEND_DATA_INTENT);
            tx.putExtra(ArduinoCommunicatorService.DATA_EXTRA, data);
            //Toast.makeText(getBaseContext(), "send tc", Toast.LENGTH_LONG).show();
            sendBroadcast(tx);
        }
        else{
            simulator.simulateFromTC(data);
        }
    }

    private void swowTmConsoleDialog()
    {
        if (dlgConsoleTM == null) {

            dlgConsoleTM = new DialogTelemetryView(this);
            tmListener = dlgConsoleTM;

            dlgConsoleTM.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!dlgConsoleTM.keepAlive()) {
                        tmListener = null;
                        dlgConsoleTM = null;
                    }
                }
            });

            dlgConsoleTM.setColdManager(managerCold);
            dlgConsoleTM.setElectricalManager(managerElectrical);
            dlgConsoleTM.setHeatManager(managerHeat);
            dlgConsoleTM.setLightManager(managerLights);
            dlgConsoleTM.setTemperatureManager(managerTemp);
            dlgConsoleTM.setWaterManager(managerWater);
        }

        dlgConsoleTM.showDialog();
    }

    private void onServiceConnected(){
        // **** Initialize functional components ****
        dbHelper = new SQLDatasHelper(getBaseContext());
        managerElectrical = ElectricalManager.initialiseElectricalManager(this);
        managerElectrical.addTensionListener(this);
        managerTemp = TemperatureManager.initialiseTemperatureManager(this, dbHelper);
        managerLights = LightManager.initialiseLightManager(this);
        managerWater = WaterManager.initialiseWaterManager(this, managerElectrical, dbHelper);
        managerCold = ColdManager.initialiseColdManager(this, this, managerElectrical, managerTemp, dbHelper);
        managerHeat = HeatManager.initialiseHeatManager(this, this, managerTemp);
        managerElectrical.addRelayModuleListener(this);
        managerWater.setGauge((FreakyGauge)findViewById(R.id.gaugeWater));

        managerElectrical.setListenerSwitchLightModule(this);

        // **** Initialize GUI components ****
        initGuiComponents();

        if (simulator != null)
        {
            simulator.setColdManager(managerCold);
            simulator.setElectricalManager(managerElectrical);
            simulator.setHeatManager(managerHeat);
            simulator.setLightManager(managerLights);
            simulator.setTemperatureManager(managerTemp);
            simulator.setWaterManager(managerWater);
            simulator.startSimulator();
        }

    }

    private void initGuiComponents(){
        // **** Configure main components ****
        gaugeBattery = (FreakyGauge)findViewById(R.id.gaugeBatterie);
        gaugeBattery.addIcon(R.drawable.icon_battery);
        gaugeBattery.setIconIdx(0);
        gaugeBattery.setActiveMode(true);
        gaugeBattery.setNumericalMode(11.5f, 12.7f);
        gaugeBattery.set_percentFill(50);
        gaugeBattery.setLegend(getString(R.string.battery_lb_level) + " " + getString(R.string.battery_auxiliary));

        gaugeWater = (FreakyGauge)findViewById(R.id.gaugeWater);
        gaugeWater.setActiveMode(false);
        gaugeWater.setLegend(getString(R.string.water_lb_relay_on));

        textTime = (TextView)findViewById(R.id.textTime);
        textTime.setTypeface(FontUtils.loadFontFromAssets(getBaseContext(), FontUtils.FONT_TRIPLE_DOT_DIGITAL));

        mUIUpdater = new UIUpdater(new Runnable() {
            @Override
            public void run() {
                updateGui();
            }
        });

        FreakyButton fb;
        fb = (FreakyButton)findViewById(R.id.btPower);
        fb.setIconSize(16);
        fb.addIcon(R.drawable.car_battery);
        fb.setIconIdx(0);

        fb = (FreakyButton)findViewById(R.id.btCold);
        fb.setIconSize(16);
        fb.addIcon(R.drawable.icon_snow);
        fb.setIconIdx(0);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColdButtonClick();
            }
        });

        FreakyRow fr = (FreakyRow)findViewById(R.id.rawElec);
        fr.setLabel(getString(R.string.row_electricity));
        fr.setActivationMode(true);
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
        fr.setLabel(getString(R.string.row_header));


        mUIUpdater.startUpdates();
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
        gaugeWater.setIconIdx((pumpStatus?0:1));
        gaugeWater.setActiveMode(pumpStatus);
        gaugeWater.invalidate();

        // Etat module chauffage
        boolean heatStatus = managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_HEATER);
        FreakyRow fr = (FreakyRow)findViewById(R.id.rawHeater);
        fr.setActivationMode(heatStatus);
        fr.invalidate();

        // Etat module lumière
        boolean lightStatus = managerElectrical.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT);
        fr = (FreakyRow)findViewById(R.id.rawLights);
        fr.setActivationMode(lightStatus);
        fr.invalidate();
    }

    @Override
    public void tensionUpdated(float[] tensionList)
    {
        float auxBatt = managerElectrical.getTension(TensionItem.eTensionType.U_BATT_AUX);
        gaugeBattery.set_Value(auxBatt);
        gaugeBattery.set_ValueText(Float.toString(auxBatt) + "V");
    }

    public void gotTM(char[] tm)
    {
        if (tm.length==0) return;

        switch (tm[0])
        {
            case CampDuinoProtocol.TM_MIRROR_TC:
                if (tmListener != null)
                    tmListener.onReceivedRawTM(tm);
                break;
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

        if (tmListener != null)
        {
            tmListener.onReceivedRawTM(tm);
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

    @Override
    public void onReceivedRawTM(char[] data) {
        gotTM(data);
    }

    private void updateGui()
    {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        if (managerElectrical.isDisplayingDialog()) managerElectrical.updateDialog();
        else if (managerCold.isDisplayingDialog()) managerCold.updateDialog();
        else if (managerHeat.isDisplayingDialog()) managerHeat.updateDialog();
        else if (managerLights.isDisplayingDialog()) managerLights.updateDialog();
        else if (managerWater.isDisplayingDialog()) managerWater.updateDialog();
        else if (dlgConsoleTM != null)
            dlgConsoleTM.refresh();
        else {
            String formattedDate = df.format(new Date());
            textTime.setText(formattedDate);
            gaugeBattery.invalidate();
            gaugeWater.invalidate();
            relayModuleUpdated();
        }

    }

    @Override
    public void WakeDisplay() {
        // wakes up the display
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn())
        {
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire();
        }
    }

}

