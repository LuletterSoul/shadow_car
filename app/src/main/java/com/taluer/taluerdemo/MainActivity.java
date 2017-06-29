package com.taluer.taluerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.taluer.taluerdemo.Button.MySeekBar;
import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;
import com.taluer.taluerdemo.presenter.MainPresenter;
import com.taluer.taluerdemo.utils.Utils;
import com.taluer.taluerdemo.view.IMainView;
import com.vise.baseble.ViseBluetooth;
import com.vise.baseble.model.BluetoothLeDevice;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, IMainView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int[] canDataEdIds = new int[]{
            R.id.id_ed, R.id.data1_ed, R.id.data2_ed, R.id.data3_ed,
            R.id.data4_ed, R.id.data5_ed, R.id.data6_ed, R.id.data7_ed,
            R.id.data8_ed
    };

    private int[] canDataTvIds = new int[]{
            R.id.data1_tv, R.id.data2_tv, R.id.data3_tv,
            R.id.data4_tv, R.id.data5_tv, R.id.data6_tv, R.id.data7_tv,
            R.id.data8_tv
    };
    //遥控命令
    final byte[] remotecontroldata = new byte[]{
            0x00, (byte)
            0xAA,
            0x02,
            0x04,
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

    private TextView mBleName, mBleAddress, mIdTv, mFormatTv, typeTv,mLenTv;

    private Spinner mDataTypeSp;

    private EditText mDataEd;

    private View mOrContent, mCanContent;
    private byte[] data;

    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
        mDataTypeSp = (Spinner) findViewById(R.id.data_type_sp);
        mDataTypeSp.setOnItemSelectedListener(this);
        mDataEd = (EditText) findViewById(R.id.data_ed);
        mOrContent = findViewById(R.id.or_content);
        mCanContent = findViewById(R.id.can_cotent);
        mBleAddress = (TextView) findViewById(R.id.ble_address);
        mBleName = (TextView) findViewById(R.id.ble_name);
        mIdTv = (TextView) findViewById(R.id.id_tv);
        mFormatTv = (TextView) findViewById(R.id.format_tv);
        typeTv = (TextView) findViewById(R.id.type_tv);
        mLenTv = (TextView) findViewById(R.id.len_tv);

        //点击进入遥控模式
        final Button b1;
        b1=(Button)findViewById(R.id.button2);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"remote control mode");
                mPresenter.sendData(remotecontroldata);
                Intent intent =new Intent(MainActivity.this,ControlActivity.class);
                startActivity(intent);
            }
        });

        final Button b2;
        b2=(Button)findViewById(R.id.ctlbtn);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.sendData(remotecontroldata);
                Intent intent0=new Intent(MainActivity.this,RemoteActivity.class);
                startActivity(intent0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        ViseBluetooth.getInstance().disconnect();
        ViseBluetooth.getInstance().clear();
        mPresenter.detachView();
        super.onDestroy();
    }

    public void onSendClick(View view) {
        String dataStr = null;
        switch (mDataTypeSp.getSelectedItemPosition()) {
            case 0:
                dataStr = mDataEd.getText().toString().trim();
                break;
            case 1:
                StringBuilder builder = new StringBuilder();
                for (int canDataEdId : canDataEdIds) {
                    String temEdData = ((EditText) findViewById(canDataEdId)).getText().toString().trim();
                    if (!TextUtils.isEmpty(temEdData))
                        builder.append(temEdData);
                }
                builder.append("08");
                String format = ((Spinner) findViewById(R.id.format_sp)).getSelectedItem().toString();
                String type = ((Spinner) findViewById(R.id.type_sp)).getSelectedItem().toString();
                builder.append(format.equals("STANDARD") ? "00" : "01");
                builder.append(type.equals("DATA_FRAME") ? "00" : "01");
                dataStr = builder.toString();
                break;
        }
        try {
            if (dataStr != null) {
                data = Utils.hexStr2Bytes(dataStr);
                Log.i(TAG, "onSendClick: " + Utils.byte2HexStr(data));
                if (data == null || data.length < 6 || data.length > 15) {
                    showMsg("数据长度不正确请重新输入");
                } else {
                    mPresenter.sendData(data);//发送byte形数据
                }
            } else {
                showMsg("数据长度不正确请重新输入");
            }
        } catch (Exception e) {
            Log.e(TAG, "onSendClick: ", e);
            showMsg("数据不正确请重新输入");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mOrContent.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        mCanContent.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void showLoading(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showMsg(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    public void onConnectClick(View view) {
//        ViseBluetooth.getInstance().disconnect();
//        ViseBluetooth.getInstance().clear();
        mPresenter.disconnect();
        startActivityForResult(new Intent(this, DeviceScanActivity.class), 1001);
        //startActivity(new Intent(this,DeviceScanActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            BluetoothLeDevice bluetoothLeDevice = data.getParcelableExtra("device");
            mPresenter.connectDevice(bluetoothLeDevice);
        }
    }

    @Override
    public void setConnectSuccess(String name, String address) {
        mBleAddress.setText(address);
        mBleName.setText(TextUtils.isEmpty(name) ? getString(R.string.unknown_device) : name);
    }

    @Override
    public void onDisconnect() {
        mBleAddress.setText(null);
        mBleName.setText(null);
    }

    @Override
    public void onDataGet(CanMsg canMsg) {
        mIdTv.setText("00" + Utils.byte2HexStr(canMsg.getId()) + Utils.byte2HexStr(canMsg.getCh()) + Utils.byte2HexStr(canMsg.getFunction()));
        byte[] msg = BlueDeviceCommand.decryptMode(canMsg.getData());
        if (msg != null) {
            for (int i = 0; i < canDataTvIds.length; i++) {
                ((TextView) findViewById(canDataTvIds[i])).setText(Utils.byte2HexStr(msg[i]));
            }
        }
        mLenTv.setText("8");
        mFormatTv.setText(Utils.byte2HexStr(canMsg.getFormat()));
        typeTv.setText(Utils.byte2HexStr(canMsg.getType()));
    }
}
