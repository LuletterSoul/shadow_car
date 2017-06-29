package com.taluer.taluerdemo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 陈智杰 on 2016/12/2.
 */

public class CanMsg implements Parcelable {

    private byte id;                      /* 29 bit identifier                               */
    private byte function;
    private byte header;
    private byte[]  data;            /* Data field                                      */
    private byte  len;                    /* Length of data field in bytes                   */
    private byte  ch;                  /* Object channel                                  */
    private byte  format;              /* 0 - STANDARD, 1- EXTENDED IDENTIFIER            */
    private byte  type;                  /* 0 - DATA FRAME, 1 - REMOTE FRAME                */

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public byte getFunction() {
        return function;
    }

    public void setFunction(byte function) {
        this.function = function;
    }

    public byte getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte getLen() {
        return len;
    }

    public void setLen(byte len) {
        this.len = len;
    }

    public byte getCh() {
        return ch;
    }

    public void setCh(byte ch) {
        this.ch = ch;
    }

    public byte getFormat() {
        return format;
    }

    public void setFormat(byte format) {
        this.format = format;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.id);
        dest.writeByte(this.function);
        dest.writeByte(this.header);
        dest.writeByteArray(this.data);
        dest.writeByte(this.len);
        dest.writeByte(this.ch);
        dest.writeByte(this.format);
        dest.writeByte(this.type);
    }

    public CanMsg() {
    }

    protected CanMsg(Parcel in) {
        this.id = in.readByte();
        this.function = in.readByte();
        this.header = in.readByte();
        this.data = in.createByteArray();
        this.len = in.readByte();
        this.ch = in.readByte();
        this.format = in.readByte();
        this.type = in.readByte();
    }

    public static final Creator<CanMsg> CREATOR = new Creator<CanMsg>() {
        @Override
        public CanMsg createFromParcel(Parcel source) {
            return new CanMsg(source);
        }

        @Override
        public CanMsg[] newArray(int size) {
            return new CanMsg[size];
        }
    };
}
