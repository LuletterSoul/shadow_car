/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.taluer.taluerdemo.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.common.primitives.Bytes;
import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;
import com.taluer.taluerdemo.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private Timer timer;
    private HeartBeatTask mHeartBeatTask;
//    private Handler mHandler;



    private int mConnectionState = STATE_DISCONNECTED;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.taluertek.taluer.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.taluertek.taluer.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.taluertek.taluer.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.taluertek.taluer.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.taluertek.taluer.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public BluetoothGattCharacteristic mNotifyCharacteristic;


    byte[] heartRateData = new byte[]{
            0x00, (byte)
            0xAA,
            BlueDeviceCommand.APP_BLE_CH,
            BlueDeviceCommand.APP_HEARTBEAT,
            8,
            BlueDeviceCommand.EXTENDED_IDENTIFIER,
            BlueDeviceCommand.REMOTE_FRAME};

    private Queue<byte[]> mQueue = new LinkedList<>();
    private final List<Byte> mBytes = new ArrayList<>();
    private enumRxStatus NextStatus = enumRxStatus.初始状态;
    private boolean isSendFinish = false;
//    private Runnable mHeartBreak = new Runnable() {
//        @Override
//        public void run() {
//            mHandler.postDelayed(mHeartBreak,3000);
//        }
//    };


    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
    }

    public void writeValue(byte[] data) {
        if (mNotifyCharacteristic != null && data != null && mConnectionState == STATE_CONNECTED) {
            if (data.length >= 20) {
                sendContinueData(data);
                return;
            }
            mNotifyCharacteristic.setValue(data);
            mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            Log.i(TAG, "writeValue: " + Utils.byte2HexStr(mNotifyCharacteristic.getValue()));
            if (mBluetoothGatt != null)
                mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
        }
    }

    private void sendContinueData(byte[] data) {
        int tmpLen = data.length;
        int start = 0;
        int end = 0;

        while (tmpLen > 0) {
//            Log.i(TAG, "sendContinueData: start");
            byte[] sendData;
            if (tmpLen >= 20) {
                end += 20;
                sendData = Arrays.copyOfRange(data, start, end);
                start += 20;
                tmpLen -= 20;
            } else {
                end += tmpLen;
                sendData = Arrays.copyOfRange(data, start, end);
                tmpLen = 0;
            }
            mQueue.add(sendData);
        }
        byte[] ready = mQueue.poll();
        if (ready != null) {
            isSendFinish = true;
            Log.i(TAG, "sendContinueData: " + Utils.byte2HexStr(ready));
            mNotifyCharacteristic.setValue(ready);
            mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            if (mBluetoothGatt != null)
                mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
        }
//        try {
//            wait();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        while (isSendFinish) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            Log.i(TAG, "sending");
        }
    }

//    //存储待发送的数据队列
//    private Queue<byte[]> dataInfoQueue = new LinkedList<>();
//
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    };
//
//    private Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            send();
//        }
//    };
//
//    //外部调用发送数据方法
//    public void send(byte[] data) {
//        if (dataInfoQueue != null) {
//            dataInfoQueue.clear();
//            dataInfoQueue = splitPacketFor20Byte(data);
//            handler.post(runnable);
//        }
//    }
//
//    //实际发送数据过程
//    private void send() {
//        if (dataInfoQueue != null && !dataInfoQueue.isEmpty()) {
//            //检测到发送数据，直接发送
//            if (dataInfoQueue.peek() != null) {
//                mNotifyCharacteristic.setValue(dataInfoQueue.poll());
//                mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//                Log.i(TAG, "writeValue: " + Utils.byte2HexStr(mNotifyCharacteristic.getValue()));
//                if (mBluetoothGatt != null)
//                    mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
//            }
//            //检测还有数据，延时后继续发送，一般延时100毫秒左右
//            if (dataInfoQueue.peek() != null) {
//                handler.postDelayed(runnable, 100);
//            }
//        }
//    }
//
//    //数据分包处理
//    private Queue<byte[]> splitPacketFor20Byte(byte[] data) {
//        Queue<byte[]> dataInfoQueue = new LinkedList<>();
//        if (data != null) {
//            int index = 0;
//            do {
//                byte[] surplusData = new byte[data.length - index];
//                byte[] currentData;
//                System.arraycopy(data, index, surplusData, 0, data.length - index);
//                if (surplusData.length <= 20) {
//                    currentData = new byte[surplusData.length];
//                    System.arraycopy(surplusData, 0, currentData, 0, surplusData.length);
//                    index += surplusData.length;
//                } else {
//                    currentData = new byte[20];
//                    System.arraycopy(data, index, currentData, 0, 20);
//                    index += 20;
//                }
//                dataInfoQueue.offer(currentData);
//            } while (index < data.length);
//        }
//        return dataInfoQueue;
//    }

    public void findService(List<BluetoothGattService> gattServices) {
    	Log.i(TAG, "Count is:" + gattServices.size());
    	for (BluetoothGattService gattService : gattServices) {
    		Log.i(TAG, gattService.getUuid().toString());
			Log.i(TAG, UUID_SERVICE.toString());
    		if(gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
    			List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
    			Log.i(TAG, "Count is:" + gattCharacteristics.size());
    			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
    				if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString())) {
    					Log.i(TAG, gattCharacteristic.getUuid().toString());
    					Log.i(TAG, UUID_NOTIFY.toString());
    					mNotifyCharacteristic = gattCharacteristic;
    					setCharacteristicNotification(gattCharacteristic, true);
//                        mBluetoothGatt.requestMtu(103);
    					return;
    				}
    			}
    		}
    	}
    }

	  public int getConnectionState() {
        return mConnectionState;
    }


//    public void setBluetoothDevice(Device bluetoothDevice) {
//        mBluetoothDevice = bluetoothDevice;
//    }
//
//    public Device getBluetoothDevice() {
//        return mBluetoothDevice;
//    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.i(TAG, "oldStatus=" + status + " NewStates=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                		mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                stopHeartBeatTimer();
//                dataInfoQueue.clear();
                mQueue.clear();
        	}
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	Log.w(TAG, "onServicesDiscovered received: " + status);
            	findService(gatt.getServices());
            } else {
            	if(mBluetoothGatt.getDevice().getUuids() == null)
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e(TAG, "onMtuChanged: mtu: " + mtu +", status: " +status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            Log.e(TAG, "onCharacteristicChanged");
        }
        
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        	Log.e(TAG, "OnCharacteristicWrite");
            byte[] ready = mQueue.poll();
            if (ready != null) {
                Log.i(TAG, "sendContinueData: " + Utils.byte2HexStr(ready));
                mNotifyCharacteristic.setValue(ready);
                if (mBluetoothGatt != null)
                    mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
            } else {
                isSendFinish = false;
            }
        }
        
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
        	Log.e(TAG, "onDescriptorRead");
        }
        
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
        	Log.e(TAG, "onDescriptorWrite");
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        }
        
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
        	Log.e(TAG, "onReadRemoteRssi");
        }
        
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
        	Log.e(TAG, "onReliableWriteCompleted");
        }
        
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        Log.i(TAG, "broadcastUpdate: " + Utils.byte2HexStr(data));
        for (byte aData : data) {
            processInputData(aData, intent);
        }
    }

    private enum enumRxStatus {
        初始状态, 收到特殊字段前导, 正在接收ID字段, 正在接收Data, 收到Len, 收到ch, 收到format, 收到type, 收到crc, 收到551, 收到552
    }

    private void processInputData(byte data, Intent intent) {
        synchronized (mBytes) {
            mBytes.add(data);
            switch (NextStatus) {
                case 初始状态:
                    if (BlueDeviceCommand.START == data) {
                        NextStatus = enumRxStatus.收到特殊字段前导;
                    }else{
                        mBytes.clear();
                    }
                    break;
                case 收到特殊字段前导:
                    // 紧接着前导的应该是1个字节的0xaa
                    if (data == BlueDeviceCommand.START) {
                        NextStatus = enumRxStatus.正在接收ID字段;
                    } else {
                        NextStatus = enumRxStatus.初始状态;
                        mBytes.clear();
                    }
                    break;
                default:
                    if (data == BlueDeviceCommand.END) {
                        NextStatus = enumRxStatus.收到552;
                    }
                    break;
                case 收到552:
                    if (data == BlueDeviceCommand.END) {
                        for (int i = 0; i < mBytes.size() ; i++) {
                            if (mBytes.get(i) == (byte) 0xA5) {
                                mBytes.remove(i);
                            }
                        }
                        int index = 2;
                        CanMsg canMsg = new CanMsg();
                        canMsg.setFunction(mBytes.get(index++));
                        canMsg.setCh(mBytes.get(index++));
                        canMsg.setId(mBytes.get(index++));
                        canMsg.setHeader(mBytes.get(index++));
                        byte[] dataByte = new byte[8];
                        for (int i = 0; i < 8; i++) {
                            dataByte[i] = mBytes.get(index++);
                        }
                        canMsg.setData(dataByte);
                        index ++;
                        canMsg.setFormat(mBytes.get(index++));
                        canMsg.setType(mBytes.get(index));
                        if (canMsg.getId() != (byte) 0xAB && canMsg.getFunction() != BlueDeviceCommand.APP_HEARTBEAT) {
                            Log.i(TAG, "processInputData: " + Utils.byte2HexStr(Bytes.toArray(mBytes)));
                            intent.putExtra(EXTRA_DATA, canMsg);
                            sendBroadcast(intent);
                        }
                        NextStatus = enumRxStatus.初始状态;
                        mBytes.clear();
                    }else{
                        NextStatus = enumRxStatus.正在接收Data;
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        stopHeartBeatTimer();
        super.onDestroy();
    }

    public void startHeartBeatTimer() {
        if (timer != null && mConnectionState == STATE_CONNECTED) {
            if (mHeartBeatTask != null)
            mHeartBeatTask.cancel();
            mHeartBeatTask = new HeartBeatTask();
            timer.schedule(mHeartBeatTask, 0, 1000);
        }
    }

    public void stopHeartBeatTimer() {
        if (timer != null) {
            if (mHeartBeatTask != null)
                mHeartBeatTask.cancel();
            else
                mHeartBeatTask = null;
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
/*
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
*/
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if(mBluetoothGatt != null) {
        	mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //mBluetoothGatt.connect();
        
        Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDevice.setBlueAddress(address);
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private class HeartBeatTask extends TimerTask {

        @Override
        public void run() {
            Log.i(TAG, "run: HeartBeatTask");
            sendData(heartRateData);
//            writeValue(Utils.getSendData(BlueDeviceCommand.APP_HEARTBEAT,
//                    BlueDeviceCommand.APP_BLE_CH, BlueDeviceCommand.ID, BlueDeviceCommand.REMOTE_FRAME,
//                    BlueDeviceCommand.encrypt(null),8));
        }
    }

    public void sendData(byte[] data) {
        CanMsg canMsg = new CanMsg();
        canMsg.setHeader(data[0]);
        canMsg.setId(data[1]);
        canMsg.setCh(data[2]);
        canMsg.setFunction(data[3]);
        byte[] dataTemp = null;
        if (data.length > 7 && data.length <= 15) {
            dataTemp = new byte[data.length - 7];
            System.arraycopy(data, 4, dataTemp, 0, data.length - 3 - 4);
        }
        canMsg.setData(BlueDeviceCommand.encrypt(dataTemp));
        canMsg.setLen(data[data.length - 3]);
        canMsg.setFormat(data[data.length - 2]);
        canMsg.setType(data[data.length - 1]);
        byte[] canSend = Utils.getSendData(canMsg);
        Log.i(TAG, "sendData() called with: data = [" + Utils.byte2HexStr(canSend) + "]");
        writeValue(canSend);
    }
}
