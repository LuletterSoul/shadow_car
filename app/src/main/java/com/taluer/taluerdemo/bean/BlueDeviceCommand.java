package com.taluer.taluerdemo.bean;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

;

/**
 * Created by 陈智杰 on 2016/9/22.
 */

public class BlueDeviceCommand {

    public final static byte[] KEY ={
            0x00, (byte) 0xEA, (byte) 0xAD,0x13,0x24,0x05,0x36,
            0x07, 0x28,0x09,0x0A, (byte) 0xFB,0x2C,0x1D,0x3E,
            0x0F, 0x10,0x22,0x00,0x45, (byte) 0x89,0x33,0x51,
            0x48};

    public final static byte[] FWFILE_KEY={
            (byte) 0xaa, (byte) 0x89, (byte) 0xE5, (byte) 0x83, (byte) 0xEC,
            0x18, (byte) 0xA1, 0x24, (byte) 0xBF, 0x04, 0x08, (byte) 0x85,
            (byte) 0xC0, 0x74, 0x12, (byte) 0xB8, (byte) 0x85, (byte) 0xC0,
            0x74, 0x09, (byte) 0xC7, (byte) 0xcd, (byte) 0xB3, 0x24};

    /* const 传输can数据的key,破解后需要升级为本key.例如该数组与目标版不一样 */
    public final static byte[] G_CAN_TRANSFER_KEY={
            (byte) 0xF3, 0x5C, 0x22, 0x20, 0x41, 0x75, 0x74, 0x6F, 0x6D,
            0x61, 0x74, 0x69, 0x63, 0x61, 0x6C, 0x6C, 0x79, 0x20, 0x67,
            0x65, 0x6E, 0x65, 0x72, (byte) 0xA5
    };


    public static final int DOWNLOAD_HEAD_MAGIC = 0xA536F78E;
    public static final int DEVICE_HEAD_MAGIC = 0xA163580A;
    public static final byte JUMP_TO_BOOT_MODE = 0x11;
    public static final byte JUMP_TO_POWERDOWN_OR_RESET = 0x22;


    private static final String KEY_ALGORITHM = "DESede";
    private static final String DEFAULT_CIPHER_ALGORITHM = "DESede/ECB/Nopadding";

    public static final byte START = (byte) 0xAA;
    public static final byte END = 0x55;
    public static final byte IDENTIFY = (byte) 0xA5;

    public static final byte STANDARD = 0x00;
    public static final byte EXTENDED_IDENTIFIER = 0x01;

    public static final byte DATA_FRAME = 0x00;
    public static final byte REMOTE_FRAME = 0x01;

    public static final int ID = 0x00AA0000;

    public static final int APP_BLE_CH = 0x02;
    public static final int APP_SET_ALIVE = 1; /* 激活功能 */
    public static final int APP_SET_MODE = 2; /* 设置当前模式:新手-普通 */
    public static final int APP_HEARTBEAT = 3; /* 心跳包申请，回复app显示所需信息 */
    public static final int APP_APPLY_REMOTE = 4; /* 申请进入或者退出遥控模式 */
    public static final int APP_SPORT_CTRL = 5; /* 控制运动 */
    public static final int APP_SET_SPEED = 6; /* 设置速度限制 */
    public static final int APP_SET_VOL = 7; /* 设置音量 */
    public static final int APP_PLAY_MUSIC = 8; /* 播放音乐 */
    public static final int APP_LOCK_CAR = 9; /* 锁车 */
    public static final int APP_UNLOCK_CAR = 10; /* 解锁 */
    public static final int APP_SET_BLE_NAME = 11; /* 修改设备名称 */
    public static final int APP_SET_FORLIGHT_SWITCH = 12; /* 控制前灯开关 */
    public static final int APP_SET_BAKLIGHT_SWITCH = 13; /* 控制后灯开关 */
    public static final int APP_SET_DESLIGHT_SWITCH = 14; /* 控制装饰灯开关 */
    public static final int APP_SET_HMI_LIGHT_VAL = 15; /* 设置指示灯亮度 */
    public static final int APP_SET_FOWARD_LIGHT_VAL = 16; /* 设置前照明灯亮度 */
    public static final int APP_SET_DESCR_LIGHT_VAL = 17; /* 设置装饰灯亮度 */
    public static final int APP_SET_BAKLIGHT_CARLOR = 18; /* 设置装饰灯颜色 */
    public static final int APP_STUDY_STATUS = 19; /* 学习模式返回状态*/
    public static final int APP_ENTER_STUDY_MODE = 21; /* 进入学习模式 */
    public static final int APP_GET_VOICE_STATUS = 22; /* 进入学习模式 */

    public static final int SYS_CH = 0x80;
    public static final int SYS_RESET = 1;
    public static final int SYS_BOOT_HW_VER = 4;
    public static final int SYS_GET_CAR_STATUS = 6;
    public static final int SYS_GET_COM_VER = 7;

    public static final int BOOT_DOWNLOAD_CH = 0x81;
    public static final int BOOT_GET_MAGIC = 1;
    public static final int BOOT_SET_FILEINFO = 4;
    public static final int BOOT_SEND_DATA = 5;
    public static final int BOOT_GET_CHECK_SUM = 7;
    public static final int BOOT_OP_END = 8;

    public static byte[] encrypt(byte[] bytes){
        byte[] data = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        if (bytes != null)
            System.arraycopy(bytes, 0, data, 0, bytes.length);
        try {
            Cipher c1 = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            c1.init(Cipher.ENCRYPT_MODE, toKey(KEY));
            return c1.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] bytes, byte[] key){
        byte[] data = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        if (bytes != null)
            System.arraycopy(bytes, 0, data, 0, bytes.length);
        try {
            Cipher c1 = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            c1.init(Cipher.ENCRYPT_MODE, toKey(key));
            return c1.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解密函数
    public static byte[] decryptMode(byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(KEY));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解密函数
    public static byte[] decryptModeFile(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换密钥
     *
     * @param key   二进制密钥
     * @return Key  密钥
     * @throws Exception
     */
    private static Key toKey(byte[] key) throws Exception{
        //实例化DES密钥规则
        DESedeKeySpec dks = new DESedeKeySpec(key);
        //实例化密钥工厂
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        //生成密钥
        return skf.generateSecret(dks);
    }
}
