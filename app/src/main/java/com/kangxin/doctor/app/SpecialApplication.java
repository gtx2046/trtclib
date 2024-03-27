package com.kangxin.doctor.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.blankj.utilcode.util.Utils;
import com.byh.module.onlineoutser.BHServiceImp;

/**
 * Created by Admin on 2016/10/24.
 */
public class SpecialApplication extends Application {

  public static Context mContext;

  @Override
  public void onCreate() {
    super.onCreate();

    mContext = getApplicationContext();
    Utils.init(mContext);

    new BHServiceImp().init(mContext);//初始化IM

    //注册无数据等状态
    registerState();

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
    builder.detectFileUriExposure();
  }

  private void registerState() {
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
  }

}

