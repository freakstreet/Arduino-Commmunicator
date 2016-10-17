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

package freakycamper.com.freaky.arduino_commmunicator.campduinoservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.utils.CharCircularFifoBuffer;

public class ArduinoCommunicatorService extends Service {

    private final static String TAG = "ArduinoCommunicatorService";
    private final static boolean DEBUG = false;

    private boolean mIsRunning = false;
    private SenderThread mSenderThread;

    private SQLDatasHelper db;

    private volatile UsbDevice mUsbDevice = null;
    private volatile UsbDeviceConnection mUsbConnection = null;
    private volatile UsbEndpoint mInUsbEndpoint = null;
    private volatile UsbEndpoint mOutUsbEndpoint = null;

    public final static String DATA_RECEIVED_INTENT = "primavera.arduino.intent.action.DATA_RECEIVED";
    public final static String SEND_DATA_INTENT = "primavera.arduino.intent.action.SEND_DATA";
    public final static String DATA_SENT_INTERNAL_INTENT = "primavera.arduino.internal.intent.action.DATA_SENT";
    public final static String DATA_EXTRA = "primavera.arduino.intent.extra.DATA";
    public final static String SERVICE_CREATED = "primavera.arduino.intent.extra.SERVICE_CREATED";

    private static int TC_SEND_ACTION = 10;
    private static int TC_SENDER_QUIT = 11;

    private final CharCircularFifoBuffer rxBuffer = new CharCircularFifoBuffer(CampDuinoProtocol.RX_BUFFER_SIZE);

    private PowerManager.WakeLock wl;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "onCreate()");
        super.onCreate();
        db = new SQLDatasHelper(getBaseContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SEND_DATA_INTENT);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand() " + intent + " " + flags + " " + startId);

        if (mIsRunning) {
            if (DEBUG) Log.i(TAG, "Service already running.");
            return Service.START_REDELIVER_INTENT;
        }

        mIsRunning = true;

        if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (DEBUG) Log.i(TAG, "Permission denied");
            Toast.makeText(getBaseContext(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        if (DEBUG) Log.d(TAG, "Permission granted");
        mUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (!initDevice()) {
            if (DEBUG) Log.e(TAG, "Init of device failed!");
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        Intent sendIntent = new Intent(SERVICE_CREATED);
        sendBroadcast(sendIntent);

        if (DEBUG) Log.i(TAG, "Receiving!");
        Toast.makeText(getBaseContext(), getString(R.string.receiving), Toast.LENGTH_SHORT).show();



        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Tag");
        wl.acquire();


        startReceiverThread();
        startSenderThread();

        activateBoard();

        return Service.START_REDELIVER_INTENT;
    }

    private void activateBoard(){
    //public static void activateBoard(){

        Thread thread = new Thread() {
            @Override
            public void run() {
                byte[] activate = CampDuinoProtocol.prepareTC(CampDuinoProtocol.PROT_TC_ACTIVATE_BOARD);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                mUsbConnection.bulkTransfer(mOutUsbEndpoint, activate, activate.length, 0);
            }
        };
        thread.run();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.i(TAG, "onDestroy()");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mUsbDevice = null;
        if (mUsbConnection != null) {
            mUsbConnection.close();
        }
    }

    private byte[] getLineEncoding(int baudRate) {
        final byte[] lineEncodingRequest = { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 };
        //Get the least significant byte of baudRate, and put it in first byte of the array being sent
        lineEncodingRequest[0] = (byte)(baudRate & 0xFF);
        //Get the 2nd byte of baudRate, and put it in second byte of the array being sent
        lineEncodingRequest[1] = (byte)((baudRate >> 8) & 0xFF);
        //ibid, for 3rd byte (my guess, because you need at least 3 bytes to encode your 115200+ settings)
        lineEncodingRequest[2] = (byte)((baudRate >> 16) & 0xFF);
        return lineEncodingRequest;
    }

    private boolean initDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mUsbConnection = usbManager.openDevice(mUsbDevice);
        if (mUsbConnection == null) {
            if (DEBUG) Log.e(TAG, "Opening USB device failed!");
            Toast.makeText(getBaseContext(), "ArduinoCommunicatorService.initDevice() openDevice. failed!", Toast.LENGTH_LONG).show();
            return false;
        }
        UsbInterface usbInterface = mUsbDevice.getInterface(1);
        if (!mUsbConnection.claimInterface(usbInterface, true)) {
            if (DEBUG) Log.e(TAG, "Claiming interface failed!");
            Toast.makeText(getBaseContext(), getString(R.string.claimning_interface_failed), Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return false;
        }

        // Arduino USB serial converter setup
        // Set control line state
        mUsbConnection.controlTransfer(0x21, 0x22, 0, 0, null, 0, 0);
        // Set line encoding.
        mUsbConnection.controlTransfer(0x21, 0x20, 0, 0, getLineEncoding(115200), 7, 0);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    mInUsbEndpoint = usbInterface.getEndpoint(i);
                } else if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                    mOutUsbEndpoint = usbInterface.getEndpoint(i);
                }
            }
        }

        if (mInUsbEndpoint == null) {
            if (DEBUG) Log.e(TAG, "No in endpoint found!");
            Toast.makeText(getBaseContext(), getString(R.string.no_in_endpoint_found), Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return false;
        }

        if (mOutUsbEndpoint == null) {
            if (DEBUG) Log.e(TAG, "No out endpoint found!");
            Toast.makeText(getBaseContext(), getString(R.string.no_out_endpoint_found), Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return false;
        }

        return true;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) Log.d(TAG, "onReceive() " + action);

            if (SEND_DATA_INTENT.equals(action)) {
                final char[] dataToSend = intent.getCharArrayExtra(DATA_EXTRA);
                if (dataToSend == null) {
                    if (DEBUG) Log.i(TAG, "No " + DATA_EXTRA + " extra in intent!");
                    String text = String.format(getResources().getString(R.string.no_extra_in_intent), DATA_EXTRA);
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(context, "SEND_TC: " + CampDuinoProtocol.getCharArrayValsHexString(dataToSend), Toast.LENGTH_LONG).show();
                Log.d(TAG, "TC: " + CampDuinoProtocol.getCharArrayValsHexString(dataToSend));

                if (mSenderThread == null)
                    Toast.makeText(context, "mSenderThread is null", Toast.LENGTH_LONG).show();
                else{
                    mSenderThread.mHandler.obtainMessage(TC_SEND_ACTION, dataToSend).sendToTarget();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Toast.makeText(context, getString(R.string.device_detaches), Toast.LENGTH_LONG).show();
                mSenderThread.mHandler.sendEmptyMessage(TC_SENDER_QUIT);
                stopSelf();
            }
        }
    };

    private void startReceiverThread() {
        new Thread("USB_receiver") {
            public void run() {
                byte[] inBuffer = new byte[CampDuinoProtocol.RX_BUFFER_SIZE/2];
                while(mUsbDevice != null ) {
                    if (DEBUG) Log.d(TAG, "calling bulkTransfer() in");
                    final int len = mUsbConnection.bulkTransfer(mInUsbEndpoint, inBuffer, inBuffer.length, 0);

                    if (len > 0) {
 /*                       char[] dbg = new char[len];
                        for (int i=0; i<len; i++) dbg[i] = (char)inBuffer[i];
                           Log.d(TAG, "TM: " + CampDuinoProtocol.charArraytoString(dbg));
*/

                        for (int i=0; i<len; i++)
                            rxBuffer.add(CampDuinoProtocol.decodeSignedByte(inBuffer[i]));
                        gotUsbData();
                    } else {
                        if (DEBUG) Log.i(TAG, "zero data read!");
                    }

                }

                if (DEBUG) Log.d(TAG, "receiver thread stopped.");
            }
        }.start();
    }

    private void gotUsbData(){
        int i, count;
        int len = rxBuffer.getDataCount();
        int footerCount = 0;
        int headerCount = 0;
        String s;

        if (len == 0) return;

        // removes all the datas before meeting the first header char
        while(len > 0 && headerCount == 0){
            if (rxBuffer.charAt(0) == CampDuinoProtocol.FRAME_HEADER[0])
                headerCount++;
            else {
                rxBuffer.get(0);
                len = rxBuffer.getDataCount();
            }
        }
        if (len<CampDuinoProtocol.FRAME_HEADER.length){
            rxBuffer.getFrame(0, rxBuffer.getDataCount());
            return;
        }
        // check the 4 start header bytes
        for (i = 0; i < CampDuinoProtocol.FRAME_HEADER.length; i++) {
            if (rxBuffer.get(0) != CampDuinoProtocol.FRAME_HEADER[i]) {
                gotUsbData();
                return;
            }
        }
        // retrieve datas until th 4 end footer chars
        count = 0;
        len = rxBuffer.getDataCount();

        while (len > 0 && footerCount < CampDuinoProtocol.FRAME_FOOTER.length){
            if (count >= rxBuffer.getDataCount())
                return;
            char c = rxBuffer.charAt(count);
            count ++;

            if (c == CampDuinoProtocol.FRAME_FOOTER[footerCount]) footerCount++;
            else footerCount = 0;

            if (footerCount == CampDuinoProtocol.FRAME_FOOTER.length){
                // got all the footer bytes, the TM is complete
                char[] tm = rxBuffer.getFrame(0, count-footerCount);
                rxBuffer.getFrame(0, footerCount);

                // debug display tm
                s = "[TM] identified: " + CampDuinoProtocol.getCharArrayValsHexString(tm);
                Log.d(TAG, s);

                // add tm to sgl database
                manageTm(tm);
                // diffuse tm data
                Intent intent = new Intent(DATA_RECEIVED_INTENT);
                intent.putExtra(DATA_EXTRA, tm);
                sendBroadcast(intent);

                if (rxBuffer.getDataCount() > 0) {
                    gotUsbData();
                }
                return;
            }
            len = rxBuffer.getDataCount();
        }
    }

    private void manageTm(char[] tm){
        if (tm.length==0) return;
        switch (tm[0]) {
            case CampDuinoProtocol.TM_LIGHT :
                break;
            case CampDuinoProtocol.TM_WATER:
                db.registerTmWater(tm[2], tm[4]);
                break;
            case CampDuinoProtocol.TM_CURRENT:
                db.registerTmCurrents(CampDuinoProtocol.decodeFloatOnlyTm(tm));
                break;
            case CampDuinoProtocol.TM_TENSION:
                db.registerTmTensions(CampDuinoProtocol.decodeFloatOnlyTm(tm));
                break;
            case CampDuinoProtocol.TM_RELAY:
                break;
            case CampDuinoProtocol.TM_ELEC_CONF:
                break;
            case CampDuinoProtocol.TM_TEMPERATURE:
                db.registerTmTemp(CampDuinoProtocol.decodeFloatOnlyTm(tm));
                break;
            case CampDuinoProtocol.TM_COLD_HOT:
                break;
            default :
                break;
        }
    }


    private void startSenderThread() {
        mSenderThread = new SenderThread("arduino_sender");
        mSenderThread.start();
    }



    private class SenderThread extends Thread {
        public Handler mHandler;

        public SenderThread(String string) {
            super(string);
        }

        public void run() {

            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (DEBUG) Log.i(TAG, "handleMessage() " + msg.what);
                    //Toast.makeText(getBaseContext(), "handleMessage() ", Toast.LENGTH_LONG).show();
                    if (msg.what == TC_SEND_ACTION) {
                       final char[] dataToSend = (char[]) msg.obj;
                        byte[] data = CampDuinoProtocol.prepareTC(dataToSend);
                        if (DEBUG) Log.d(TAG, "calling bulkTransfer() out");
                        mUsbConnection.bulkTransfer(mOutUsbEndpoint, data, data.length, 0);
                        String s = "SenderThread TC sent: " + CampDuinoProtocol.getCharArrayValsHexString(dataToSend) + ", encoded :" + CampDuinoProtocol.getByteArrayValsHexString(data);
                        Log.d(TAG, s);
                    } else if (msg.what == TC_SENDER_QUIT) {
                        restoreSleepMode();
                        Looper.myLooper().quit();
                    }
                }
            };

            Looper.loop();
            if (DEBUG) Log.i(TAG, "sender thread stopped");
        }
    }

    private void restoreSleepMode(){
        //do what you need to do
        wl.release();
    }
}
