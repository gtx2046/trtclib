package com.byh.module.onlineoutser;

import android.app.Application;
import android.content.Context;

public class BHServiceImp {

  public void init(Context context) {
    BHManger.INSTANCE.init((Application) context.getApplicationContext());
  }

  public void imLogin(String account,String sign) {
    BHManger.INSTANCE.imLoginWithRun(account,sign);
  }

  public void imLogout() {
    BHManger.INSTANCE.imLogout();
  }

}
