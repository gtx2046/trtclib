package com.byh.module.onlineoutser.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;

import com.byh.module.onlineoutser.R;
import com.byh.module.onlineoutser.im.entity.StartPageEvent;
import com.byh.module.onlineoutser.im.video.CallMsg;
import com.byh.module.onlineoutser.activity.DialActivity;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.byh.module.onlineoutser.floatwindow.FloatWindow;
import com.byh.module.onlineoutser.floatwindow.MoveType;
import com.byh.module.onlineoutser.floatwindow.PermissionListener;
import com.byh.module.onlineoutser.floatwindow.Screen;
import com.byh.module.onlineoutser.floatwindow.ViewStateListener;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.byh.module.onlineoutser.activity.DialActivity.OPEN_VIDEO;

/**
 * 视频悬浮窗服务
 */
public class FloatVideoWindowServiceIM extends Service {

  private static final String TAG = "FloatVideoWindowService";
  //容器父布局
  private TXCloudVideoView mTXCloudVideoView;
  private TXCloudVideoView mLocalVideoView;
  private View floatView;
  private CallMsg mProps;
  private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
  private PermissionListener mPermissionListener = new PermissionListener() {

    @Override
    public void onSuccess() {

      Log.i(TAG, "onSuccess: create float view ok");
    }

    @Override
    public void onFail() {
      Log.i(TAG, "onSuccess: create float view err");

    }
  };
  private ViewStateListener mViewStateListener = new ViewStateListener() {

    @Override
    public void onPositionUpdate(int i, int i1) {

      Log.i(TAG, "onPositionUpdate: =========");
    }

    @Override
    public void onShow() {

      Log.i(TAG, "onShow: float view");
    }

    @Override
    public void onHide() {

      Log.i(TAG, "onHide: ========");
    }

    @Override
    public void onDismiss() {

      Log.i(TAG, "onDismiss: ==========");
    }

    @Override
    public void onMoveAnimStart() {

      Log.i(TAG, "onMoveAnimStart: =======");
    }

    @Override
    public void onMoveAnimEnd() {

      Log.i(TAG, "onMoveAnimEnd: =========");
    }

    @Override
    public void onBackToDesktop() {

      Log.i(TAG, "onBackToDesktop: ===========");
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "onCreate: ===========");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.i(TAG, "onBind: =============");
//    mProps = intent.getParcelableExtra("msg");
//    initFloating();//悬浮框点击事件的处理
//    initVideo();

    return new MyBinder();
  }

  private void initVideo() {

    FloatServiceHelpter.INSTANCE.startLocalVideoView(mLocalVideoView, 1, 1);
    FloatServiceHelpter.INSTANCE.startRemoteVide(mTXCloudVideoView, DisplayUtil.dp2px(getBaseContext(), 80),
      DisplayUtil.dp2px(getBaseContext(), 120));

  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand: =========");
    if (intent != null)
      mProps = intent.getParcelableExtra("msg");

    initFloating();//悬浮框点击事件的处理
    initVideo();
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDestroy() {
    dismissFloatView();
    mExecutorService.shutdown();
    super.onDestroy();
  }

  private void dismissFloatView() {
    FloatWindow.destroy();
  }

  private void initFloating() {
    if (FloatWindow.get() != null) {
      return;
    }

    floatView = LayoutInflater.from(getBaseContext()).inflate(R.layout.float_video_window_im_layout, null);
    mTXCloudVideoView = floatView.findViewById(R.id.remote_videoView_float);
    mLocalVideoView = floatView.findViewById(R.id.local_videoView_float);
    floatView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!AppUtils.isRunBackground(getBaseContext()) || AppUtils.isAllowed(getBaseContext())) {

          if (mProps != null) {
            DialActivity.Companion.launch(getBaseContext(), mProps, OPEN_VIDEO);
            if(null!=floatView){
              floatView.post(new Runnable() {
                @Override
                public void run() {
                  EventBus.getDefault().post(new StartPageEvent());
                  FloatServiceHelpter.INSTANCE.stopService();
                }
              });
            }
          }
        }
      }
    });

    FloatWindow
      .with(getApplicationContext())
      .setView(floatView)
      .setX(100)                                   //设置控件初始位置
      .setY(Screen.height, 0.3f)
      .setDesktopShow(true)                        //桌面显示
      .setMoveType(MoveType.slide)
      .setMoveStyle(500, new AccelerateInterpolator())
      .setViewStateListener(mViewStateListener)    //监听悬浮控件状态改变
      .setPermissionListener(mPermissionListener)  //监听权限申请结果
      .build();
    FloatWindow.get().show();
  }

  public class MyBinder extends Binder {
    public FloatVideoWindowServiceIM getService() {
      return FloatVideoWindowServiceIM.this;
    }
  }

}
