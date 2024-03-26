package com.byh.module.onlineoutser.utils;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.lang.reflect.Method;
import java.util.List;

public class AppUtils {
  /**
   * 获取应用程序名称
   */
  public static synchronized String getAppName(Context context) {
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageInfo(
        context.getPackageName(), 0);
      int labelRes = packageInfo.applicationInfo.labelRes;
      return context.getResources().getString(labelRes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 获取图标 bitmap
   * @param context
   */
  public static synchronized Bitmap getAppIcon(Context context) {
    PackageManager packageManager = null;
    ApplicationInfo applicationInfo = null;
    try {
      packageManager = context.getApplicationContext()
        .getPackageManager();
      applicationInfo = packageManager.getApplicationInfo(
        context.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      applicationInfo = null;
    }
    Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
    BitmapDrawable bd = (BitmapDrawable) d;
    Bitmap bm = bd.getBitmap();
    return bm;
  }


  /**
   * 判断程序是否在后台运行
   *
   * @return true 表示在后台运行

   */

  public static boolean isRunBackground(Context context) {

    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

    String packageName = context.getPackageName();

    //获取Android设备中所有正在运行的App

    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

    if (appProcesses == null) {
      return true;
    }

    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {

      // The name of the process that this object is associated with.

      if (appProcess.processName.equals(packageName)

        && appProcess.importance ==

        ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

        return false;

      }

    }

    return true;

  }


  public static boolean isAllowed(Context context) {
    AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    try {
      int op = 10021;
      Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class});
      Integer result = (Integer) method.invoke(ops, op, android.os.Process.myUid(), context.getPackageName());
      return result == AppOpsManager.MODE_ALLOWED;

    } catch (Exception e) {
    }
    return false;
  }

}
