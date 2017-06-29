package com.taluer.taluerdemo.view;

import com.taluer.taluerdemo.bean.CanMsg;

/**
 * Created by chenzhijie on 2017/6/5.
 */

public interface IMainView extends BaseView {
    void setConnectSuccess(String name, String address);

    void onDisconnect();

    void onDataGet(CanMsg canMsg);
}
