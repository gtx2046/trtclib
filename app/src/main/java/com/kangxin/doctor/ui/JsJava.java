package com.kangxin.doctor.ui;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.byh.module.onlineoutser.activity.DialActivity;
import com.byh.module.onlineoutser.im.callback.CallVideoListener;
import com.byh.module.onlineoutser.im.video.CallMgr;
import com.byh.module.onlineoutser.im.video.CallMsg;

public class JsJava {

    @JavascriptInterface
    public void jsCallJava(){
        /*DialActivity.Companion.launch(Utils.getApp(), new CallMsg(), new CallVideoListener() {
            @Override
            public void receiveListener() {

            }

            @Override
            public void rejectListener() {

            }

            @Override
            public void videoEndListener() {

            }
        });*/
        Log.v("js----","js--java");
    }
}
