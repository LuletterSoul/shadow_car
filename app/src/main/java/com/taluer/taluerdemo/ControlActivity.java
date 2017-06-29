package com.taluer.taluerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.taluer.taluerdemo.Button.LongClickButton;
import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;
import com.taluer.taluerdemo.presenter.MainPresenter;
import com.taluer.taluerdemo.utils.Utils;
import com.taluer.taluerdemo.view.IMainView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhumingqing on 2017/6/26.
 */

public class ControlActivity extends Activity implements IMainView{
    private static final String TAG = ControlActivity.class.getSimpleName();
    private MainPresenter mainPresenter=new MainPresenter();
    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);

        LongClickButton button01=(LongClickButton)findViewById(R.id.button01);
        LongClickButton button02=(LongClickButton)findViewById(R.id.button02);
        LongClickButton button03=(LongClickButton)findViewById(R.id.button03);
        Button button04=(Button)findViewById(R.id.button04);
        //HeartRateTask mHeartBeatTask;


        mainPresenter.attachView(this);

        //final Timer timer=new Timer();

        Log.i(TAG,"control activity");
        final byte[] Data1 = new byte[]{
                0x00, (byte)
                0xAA,
                0x02,
                0x05,
                (byte) 0x01,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                8,
                BlueDeviceCommand.EXTENDED_IDENTIFIER,
                BlueDeviceCommand.DATA_FRAME};
        final byte[] Data2 = new byte[]{
                0x00, (byte)
                0xAA,
                0x02,
                0x05,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0x01,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                8,
                BlueDeviceCommand.EXTENDED_IDENTIFIER,
                BlueDeviceCommand.DATA_FRAME};
        final byte[] Data3 = new byte[]{
                0x00, (byte)
                0xAA,
                //BlueDeviceCommand.APP_BLE_CH,
                //BlueDeviceCommand.APP_HEARTBEAT,
                0x02,
                0x05,
                (byte) 0x08,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0x08,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                8,
                BlueDeviceCommand.EXTENDED_IDENTIFIER,
                BlueDeviceCommand.DATA_FRAME};
        final byte[] Data4 = new byte[]{
                0x00, (byte)
                0xAA,
                //BlueDeviceCommand.APP_BLE_CH,
                //BlueDeviceCommand.APP_HEARTBEAT,
                0x02,
                0x05,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                (byte) 0xff,
                8,
                BlueDeviceCommand.EXTENDED_IDENTIFIER,
                BlueDeviceCommand.DATA_FRAME};


        button01.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public boolean repeatAction() {
                //numberTV.setText(String.valueOf(Integer.parseInt(numberTV.getText().toString()) - 1));
                Log.i(TAG, "enter  data3 "+ Utils.byte2HexStr(Data3));
                mainPresenter.sendData(Data3);
                return false;
            }
        }, 1000);

        button01.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == R.id.button01){

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        Log.d("test", " button ---> up");
//                        timer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                Log.i(TAG,"01 data 4 : "+Utils.byte2HexStr(Data4));
//                                mainPresenter.sendData(Data4);
//                            }
//                        },1000,3000);
                        mainPresenter.sendData(Data4);

                    }
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                      Log.d("test", " button ---> down");
                        Log.i(TAG,"touch data 3 : "+Utils.byte2HexStr(Data3));
                        //timer.cancel();
                        mainPresenter.sendData(Data3);
                    }

                }
                return false;
            }
        });
//        button01.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(view.getId() == R.id.button01){
//
//                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
//                        Log.d("test", " button ---> down");
//                        Log.i(TAG,"touch data 4 : "+Utils.byte2HexStr(Data1));
//                        mainPresenter.sendData(Data1);
//                    }
//                }
//                return false;
//            }
//        });
        button02.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public boolean repeatAction() {
                //numberTV.setText(String.valueOf(Integer.parseInt(numberTV.getText().toString()) + 1));
                Log.i(TAG, "enter  data1 "+ Utils.byte2HexStr(Data1));
                mainPresenter.sendData(Data1);
                return false;
            }
        }, 1000);
        button02.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == R.id.button02){

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        Log.i(TAG,"02 data 4 : "+Utils.byte2HexStr(Data4));
                        mainPresenter.sendData(Data4);
                    }
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        Log.i(TAG,"02 data 1 : "+Utils.byte2HexStr(Data1));
                        mainPresenter.sendData(Data1);
                    }

                }
                return false;
            }
        });


       button03.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
           @Override
           public boolean repeatAction() {
               Log.i(TAG, "enter  data2 "+ Utils.byte2HexStr(Data2));
               mainPresenter.sendData(Data2);
               return false;
           }
       },1000);
        button03.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == R.id.button03){

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        Log.i(TAG,"03 data 4 : "+Utils.byte2HexStr(Data4));
                        mainPresenter.sendData(Data4);
                    }
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        Log.i(TAG,"02 data 2 : "+Utils.byte2HexStr(Data2));
                        mainPresenter.sendData(Data2);
                    }
                }
                return false;
            }
        });


        button04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"data4 "+Data4);
                mainPresenter.sendData(Data4);
            }
        });


        /*
//加1
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTV.setText(String.valueOf(Integer.parseInt(numberTV.getText().toString()) + 1));
            }
        });*/


    }

    @Override
    protected void onDestroy() {
        mainPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void showLoading(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showMsg(String errorMsg) {

    }

    @Override
    public void setConnectSuccess(String name, String address) {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onDataGet(CanMsg canMsg) {

    }

}

