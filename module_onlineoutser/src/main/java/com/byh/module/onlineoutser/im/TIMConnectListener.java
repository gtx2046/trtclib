package com.byh.module.onlineoutser.im;

import android.util.Log;

import com.tencent.imsdk.TIMConnListener;

public class TIMConnectListener implements TIMConnListener {

  private static final String TAG = "TIMConnectListener";

  @Override
  public void onConnected() {
    Log.i(TAG, "=========onConnected: connect ok===========");
  }

  @Override
  public void onDisconnected(int i, String s) {
    Log.i(TAG, "-----------onDisconnected: disconnected --------------");
  }

  @Override
  public void onWifiNeedAuth(String s) {

  }
}
