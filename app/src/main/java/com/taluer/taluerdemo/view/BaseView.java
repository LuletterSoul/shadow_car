package com.taluer.taluerdemo.view;


/**
 * Created by yiwei on 16/4/1.
 */
public interface BaseView {
    void showLoading(String msg);

    void hideLoading();

    void showMsg(String errorMsg);

}