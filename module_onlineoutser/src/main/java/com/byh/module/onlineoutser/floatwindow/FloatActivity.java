package com.byh.module.onlineoutser.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于在内部自动申请权限
 * https://github.com/yhaolpz
 * <p>
 * Fix:bugly #10502
 *
 * @author mac
 */

public class FloatActivity extends Activity {

  private static List<PermissionListener> mPermissionListenerList;
  private static PermissionListener mPermissionListener;
  int KEY_PRE = 756232212;

  private static void onSuccessListener(PermissionListener permissionListener) {
    if (permissionListener != null) {
      permissionListener.onSuccess();
    }
  }

  private static void onFailListener(PermissionListener permissionListener) {
    if (permissionListener != null) {
      permissionListener.onFail();
    }
  }

  static synchronized void request(Context context, PermissionListener permissionListener) {
    if (PermissionUtil.hasPermission(context)) {
      onSuccessListener(permissionListener);
      return;
    }
    if (mPermissionListenerList == null) {
      mPermissionListenerList = new ArrayList<>();
      mPermissionListener = new PermissionListener() {
        @Override
        public void onSuccess() {
          for (PermissionListener listener : mPermissionListenerList) {
            onSuccessListener(listener);
          }
          mPermissionListenerList.clear();
        }

        @Override
        public void onFail() {
          for (PermissionListener listener : mPermissionListenerList) {
            onFailListener(listener);
          }
          mPermissionListenerList.clear();
        }
      };
      Intent intent = new Intent(context, FloatActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }
    mPermissionListenerList.add(permissionListener);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestAlertWindowPermission();
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void requestAlertWindowPermission() {
    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
    intent.setData(Uri.parse("package:" + getPackageName()));
    startActivityForResult(intent, KEY_PRE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (KEY_PRE == requestCode) {
      if (PermissionUtil.hasPermissionOnActivityResult(this)) {
        onSuccessListener(mPermissionListener);
        EventModel eventModel =new EventModel();
        eventModel.setWhta(3333);
        eventModel.setMsg("3333");
        EventBus.getDefault().post(eventModel);
      } else {
        onFailListener(mPermissionListener);
        EventModel eventModel =new EventModel();
        eventModel.setWhta(3332);
        eventModel.setMsg("3332");
        EventBus.getDefault().post(eventModel);
      }
    }
    finish();
  }


}
