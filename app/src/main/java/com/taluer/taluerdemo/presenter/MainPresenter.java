package com.taluer.taluerdemo.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;
import com.taluer.taluerdemo.service.BluetoothLeService;
import com.taluer.taluerdemo.utils.Utils;
import com.taluer.taluerdemo.view.IMainView;
import com.vise.baseble.model.BluetoothLeDevice;

/**
 * Created by chenzhijie on 2017/6/5.
 */

public class MainPresenter extends BasePresenter<IMainView>
        implements ServiceConnection {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private BluetoothLeService mBluetoothLeService;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (getView() != null) {
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    getView().hideLoading();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    getView().showMsg("蓝牙已断开");
                    getView().hideLoading();
                    getView().onDisconnect();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED: ");
                    mBluetoothLeService.startHeartBeatTimer();
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    Log.i(TAG, "ACTION_DATA_AVAILABLE: ");
                    CanMsg canMsg = intent.getParcelableExtra(BluetoothLeService.EXTRA_DATA);
                    getView().onDataGet(canMsg);
                    mBluetoothLeService.startHeartBeatTimer();
                }
            }
        }
    };

    @Override
    public void attachView(IMainView view) {
        super.attachView(view);
        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
        getContext().bindService(gattServiceIntent, this, Context.BIND_AUTO_CREATE);
        getContext().registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
    }


    private IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    public void sendData(byte[] data) {
        mBluetoothLeService.stopHeartBeatTimer();
        CanMsg canMsg = new CanMsg();
        canMsg.setHeader(data[0]);
        canMsg.setId(data[1]);
        canMsg.setCh(data[2]);
        canMsg.setFunction(data[3]);
        byte[] dataTemp = null;
        if (data.length > 7 && data.length <= 15) {//data
            dataTemp = new byte[data.length - 7];
            System.arraycopy(data, 4, dataTemp, 0, data.length - 3 - 4);
        }
        canMsg.setData(BlueDeviceCommand.encrypt(dataTemp));
        canMsg.setLen(data[data.length - 3]);
        canMsg.setFormat(data[data.length - 2]);
        canMsg.setType(data[data.length - 1]);
        byte[] canSend = Utils.getSendData(canMsg);
        Log.i(TAG, "sendData() called with: data = [" + Utils.byte2HexStr(canSend) + "]");
        mBluetoothLeService.writeValue(canSend);
    }


    public void connectDevice(BluetoothLeDevice bluetoothLeDevice) {
        Log.i(TAG, "name: " + bluetoothLeDevice.getName());
        Log.i(TAG, "address: " + bluetoothLeDevice.getAddress());
        if (getView() != null) {
            if (mBluetoothLeService != null) {
                if (mBluetoothLeService.getConnectionState() == BluetoothLeService.STATE_DISCONNECTED) {
//                    getView().showLoading(mContext.getString(R.string.connecting));
                    mBluetoothLeService.connect(bluetoothLeDevice.getAddress());
                    getView().setConnectSuccess(bluetoothLeDevice.getName(),bluetoothLeDevice.getAddress());
                } else if (mBluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED) {
                    getView().setConnectSuccess(bluetoothLeDevice.getName(),bluetoothLeDevice.getAddress());
                } else {
                    getView().onDisconnect();
                }
            }
        }
    }


    @Override
    public void detachView() {
        getContext().unregisterReceiver(mGattUpdateReceiver);
        getContext().unbindService(this);
        super.detachView();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (getView() != null) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getView().showMsg("不支持蓝牙设备");
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBluetoothLeService = null;
    }

    public void disconnect() {
        mBluetoothLeService.disconnect();
    }
}
