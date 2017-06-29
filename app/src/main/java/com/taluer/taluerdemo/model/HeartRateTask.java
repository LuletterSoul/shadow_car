package com.taluer.taluerdemo.model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;
import android.util.Log;

import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;
import com.taluer.taluerdemo.utils.Utils;
import com.vise.baseble.ViseBluetooth;
import com.vise.baseble.callback.data.ICharacteristicCallback;
import com.vise.baseble.exception.BleException;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by 陈智杰 on 2017/6/6 0006.
 */

public class HeartRateTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = HeartRateTask.class.getSimpleName();
    private BlockingQueue<byte[]> mQueue = new ArrayBlockingQueue<>(2);
    private boolean isStop = false;

    @Override
    protected Void doInBackground(Void... params) {
        CanMsg canMsg = new CanMsg();
        canMsg.setHeader((byte) 0x00);
        canMsg.setId((byte) 0xAA);
        canMsg.setCh((byte) BlueDeviceCommand.APP_BLE_CH);
        canMsg.setFunction((byte) BlueDeviceCommand.APP_HEARTBEAT);
        canMsg.setData(BlueDeviceCommand.encrypt(null));
        canMsg.setLen((byte) 8);
        canMsg.setFormat(BlueDeviceCommand.EXTENDED_IDENTIFIER);
        canMsg.setType(BlueDeviceCommand.REMOTE_FRAME);
        byte[] heartRateData = Utils.getSendData(canMsg);

        ViseBluetooth.getInstance().enableCharacteristicNotification(mNotificationCallback,false);

        while (!isCancelled()){
            if (!isStop) {
                Log.w(TAG, "++++++++++++++++++++++++++++心跳中++++++++++++++++++++++++++++++");
                if (heartRateData.length > 20)
                    sendContinueData(heartRateData);
                else
                    ViseBluetooth.getInstance().writeCharacteristic(heartRateData, mWriteCallback);

            }else{
                Log.w(TAG, "++++++++++++++++++++++++++++心跳停止++++++++++++++++++++++++++++++");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
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
        isStop = true;
        ViseBluetooth.getInstance().writeCharacteristic(mQueue.poll(),mWriteCallback);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViseBluetooth.getInstance().writeCharacteristic(mQueue.poll(),mWriteCallback);
        isStop = false;
    }

    private ICharacteristicCallback mNotificationCallback = new ICharacteristicCallback() {
        @Override
        public void onSuccess(BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "心跳包收到通知: ");
            byte[] data = characteristic.getValue();
            Log.i(TAG, "data: " + Utils.byte2HexStr(data));
        }


        @Override
        public void onFailure(BleException exception) {
            Log.e(TAG, "心跳包通知失败" + exception.getDescription());
        }
    };

    private ICharacteristicCallback mWriteCallback = new ICharacteristicCallback() {
        @Override
        public void onSuccess(BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "心跳包写入成功: ");
            byte[] data = characteristic.getValue();
            Log.i(TAG, "data: " + Utils.byte2HexStr(data));
        }

        @Override
        public void onFailure(BleException exception) {
            Log.e(TAG, "心跳包写入失败: " + exception.getDescription());
        }
    };

    public void setStop(boolean isStop) {
        Log.i(TAG, "setStop: " + isStop);
        this.isStop = isStop;
    }
}
