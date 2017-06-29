package com.taluer.taluerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.taluer.taluerdemo.Button.MySeekBar;
import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;
import com.taluer.taluerdemo.presenter.MainPresenter;
import com.taluer.taluerdemo.utils.Utils;
import com.taluer.taluerdemo.view.IMainView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhumingqing on 2017/6/28.
 */

public class RemoteActivity extends Activity implements IMainView{
    public static String TGA=RemoteActivity.class.getSimpleName();

    MainPresenter mainPresenter=new MainPresenter();
    Timer timer=new Timer();

    TextView textview1;
    TextView textview2;
    public byte[] Data4 = new byte[]{
            0x00, (byte)
            0xAA,
            //BlueDeviceCommand.APP_BLE_CH,
            //BlueDeviceCommand.APP_HEARTBEAT,
            0x02,
            0x05,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            8,
            BlueDeviceCommand.EXTENDED_IDENTIFIER,
            BlueDeviceCommand.DATA_FRAME};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remotectl);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mainPresenter.attachView(this);
        MySeekBar myleftSeekBar,myrightSeekBar;
        myleftSeekBar=(MySeekBar)findViewById(R.id.leftbar);
        myrightSeekBar=(MySeekBar)findViewById(R.id.rightbar);

        textview1=(TextView)findViewById(R.id.textView1);
        textview2=(TextView)findViewById(R.id.textView2);
        Log.i(TGA,"welcome");
        //Timer timer=new Timer();


        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                doActionHandler.sendMessage(message);
                //Log.d(TGA,"send data4 "+Data4);
                //mainPresenter.sendData(Data4);
            }
        },1000,500);

        myleftSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                Data4[6]= (byte) ((10*i)/256);
                Data4[7]=(byte)((10*i)%256);

                Log.d(TGA,"the changed data 3 "+Data4[6]);
                Log.d(TGA,"the changed data 4 "+Data4[7]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        myrightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Data4[10]=(byte)((10*i)/256);
                Data4[11]=(byte)((10*i)%256);
                Log.d(TGA,"the changed data 7"+Data4[10]);
                Log.d(TGA,"the changed data 8"+Data4[11]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//         Handler doActionHandler = new Handler()
//         {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                int msgId = msg.what;
//                switch (msgId) {
//                    case 1:
//                        Log.i(TGA,"send data ");
//                        mainPresenter.sendData(Data4);
//                        textView1.setText(Utils.byte2HexStr(Data4[6])+" "+Utils.byte2HexStr(Data4[7]));
//                        textView2.setText(Utils.byte2HexStr(Data4[10])+" "+Utils.byte2HexStr(Data4[11]));
//                        break;
//                    default:
//                        break;
//                }
//            }
//        };

    }
    private Handler doActionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgId = msg.what;
            switch (msgId) {
                case 1:
                    Log.i(TGA,"send data IS "+Utils.byte2HexStr(Data4));
                    mainPresenter.sendData(Data4);
                    textview1.setText(Utils.byte2HexStr(Data4[6])+" "+Utils.byte2HexStr(Data4[7]));
                    textview2.setText(Utils.byte2HexStr(Data4[10])+" "+Utils.byte2HexStr(Data4[11]));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TGA,"timer GG");
        timer.cancel();
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
