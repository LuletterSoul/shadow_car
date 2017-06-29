package com.taluer.taluerdemo.utils;


import com.google.common.primitives.Bytes;
import com.taluer.taluerdemo.bean.BlueDeviceCommand;
import com.taluer.taluerdemo.bean.CanMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhijie on 16/6/16.
 */
public class Utils {


    private static final String TAG = "Utils";

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        stmp = Integer.toHexString(b & 0xFF);
        sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        sb.append(" ");
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int b = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
            ret[i] = (byte) (b & 0xFF);
        }
        return ret;
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2BytesHightLow(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        byte[] ret2 = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int b = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
            ret[i] = (byte) (b & 0xFF);
        }
        int j = l-1;
        for (int i = 0; i < l; i++) {
            ret2[i] = ret[j--];
        }
        return ret2;
    }

//    public static String sha(String src) {
//        StringBuilder builder = new StringBuilder();
//        builder.append(src);
//        builder.append("57a4115a9f468f8bcef03d8cfc5e0e33");
//        byte[] digesta = null;
//        try {
//            // 得到一个SHA-1的消息摘要
//            MessageDigest alga = MessageDigest.getInstance("SHA-1");
//            // 添加要进行计算摘要的信息
//            alga.update(builder.toString().getBytes());
//            // 得到该摘要
//            digesta = alga.digest();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        // 将摘要转为字符串
//        return MD5.toHexString(digesta);
//    }


    /**
     * 将一个整数转换位字节数组(4个字节)，b[0]存储高位字符，大端
     *
     * @param value
     *            整数
     * @return 代表整数的字节数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将一个整数转换位字节数组(4个字节)，b[0]存储高位字符，大端
     *
     * @param value
     *            整数
     * @return 代表整数的字节数组
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value>>24) & 0xFF);
        src[2] = (byte) ((value>>16)& 0xFF);
        src[1] = (byte) ((value>>8)&0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * Byte数组转换Int
     *
     * @param buf
     *            需转化的Byte数组
     * @param offset
     *            起点
     * @return int 转化后的Int
     */
    public static int bytesToInt(byte[] buf, int offset) {
        int out = 0, temp;
        if (buf.length >= (4 + offset)) {
            temp = buf[offset];
            temp &= 0xFF;
            out += temp;
            temp = buf[offset + 1];
            temp &= 0xFF;
            temp <<= 8;
            out |= temp;
            temp = buf[offset + 2];
            temp &= 0xFF;
            temp <<= 16;
            out |= temp;
            temp = buf[offset + 3];
            temp &= 0xFF;
            temp <<= 24;
            out |= temp;
        }
        return out;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF);
        return value;
    }

    /**
     * 将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
     *
     * @return
     */
    static public long bytesToUnsignedInt(byte[] buf, int offset) {

        long out = 0, temp;
        if (buf.length >= (4 + offset)) {
            temp = buf[offset + 0];
            temp &= 0xFF;
            out += temp;
            temp = buf[offset + 1];
            temp &= 0xFF;
            temp <<= 8;
            out |= temp;
            temp = buf[offset + 2];
            temp &= 0xFF;
            temp <<= 16;
            out |= temp;
            temp = buf[offset + 3];
            temp &= 0xFF;
            temp <<= 24;
            out |= temp;
        }

        return out;
    }

    //long类型转成byte数组
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static byte[] getSendData(CanMsg canMsg) {
        List<Byte> canSend = new ArrayList<>();
        //header
        canSend.add(BlueDeviceCommand.START);
        canSend.add(BlueDeviceCommand.START);
        //id
        byte[] deviceId = new byte[]{canMsg.getFunction(),canMsg.getCh(),canMsg.getId(),canMsg.getHeader()};
        for (byte b : deviceId) {
            if (b == BlueDeviceCommand.START || b == BlueDeviceCommand.END)
                canSend.add(BlueDeviceCommand.IDENTIFY);
            canSend.add(b);
        }
        //date[8]
        if (canMsg.getData() != null) {
            for (byte b : canMsg.getData()) {
                if (b == BlueDeviceCommand.START || b == BlueDeviceCommand.END)
                    canSend.add(BlueDeviceCommand.IDENTIFY);
                canSend.add(b);
            }
            //date[8].length
            canSend.add(canMsg.getLen());
        }

        //chanel
        canSend.add(canMsg.getCh());
        //format
        canSend.add(canMsg.getFormat());
        //type
        canSend.add(canMsg.getType());
        //sum
        byte sum = 0;
        for (int i = 2; i < canSend.size(); i++) {
            sum += canSend.get(i);
        }

        canSend.add(sum);

        canSend.add(BlueDeviceCommand.END);
        canSend.add(BlueDeviceCommand.END);
        return Bytes.toArray(canSend);
    }
}
