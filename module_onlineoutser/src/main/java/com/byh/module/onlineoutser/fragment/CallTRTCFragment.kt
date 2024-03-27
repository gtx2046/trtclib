package com.byh.module.onlineoutser.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.BHBaseFragment
import com.byh.module.onlineoutser.im.callback.OnWindowPermissionListener
import com.byh.module.onlineoutser.im.IMManager
import com.byh.module.onlineoutser.im.callback.RemoteBusyListener
import com.byh.module.onlineoutser.im.callback.RemoteRefuseListener
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.activity.CallActivity
import com.byh.module.onlineoutser.activity.DialActivity
import com.byh.module.onlineoutser.activity.DialActivity.Companion.OPEN_VIDEO
import com.byh.module.onlineoutser.im.entity.RecTrtcBackEvent
import com.byh.module.onlineoutser.im.entity.StartPageEvent
import com.byh.module.onlineoutser.im.entity.TrTcExitEvent
import com.byh.module.onlineoutser.utils.*
import com.permissionx.guolindev.PermissionX
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.liteav.TXLiteAVCode
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener
import kotlinx.android.synthetic.main.online_video_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 首页进入-----视频------（一对一）
 * 发起视频  CallActivity OnlineChatActivity
 */
class CallTRTCFragment : BHBaseFragment(),
  OnWindowPermissionListener {
  companion object {

    private const val CALL_TAG = "CallTRTCFragment"
    fun newInstance(props: CallMsg): CallTRTCFragment {
      val fg = CallTRTCFragment()
      val data = Bundle()
      data.putParcelable("props", props)
      fg.arguments = data
      return fg
    }

    fun start(ft: FragmentManager, props: CallMsg, @IdRes containerId: Int): CallTRTCFragment {
      val trtcFragment = newInstance(props)
      ft.beginTransaction().add(containerId, trtcFragment)
        .commitAllowingStateLoss()
      return trtcFragment
    }

    fun start(activity: AppCompatActivity, props: CallMsg): CallTRTCFragment {
      val trtcFragment = newInstance(props)
      //将fragment添加到容器中
      FloatHelper.floatFragment(activity, trtcFragment)
      return trtcFragment
    }

  }

  private val trtcCloud: TRTCCloud by lazy {
    TRTCCloud.sharedInstance(activity)
  }

  private val mProps by lazy {
    arguments?.getParcelable("props") ?: CallMsg()
  }

  private val param by lazy {
    TRTCCloudDef.TRTCRenderParams().apply {
      rotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0
      fillMode = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT
    }
  }

  override fun initEvent() {

    bigw_name.text = mProps.name
    sub_name.text = mProps.peerName

    local.setOnClickListener { changeScreen(if (FloatServiceHelpter.hasDoctorBigScreen) remote.userId else local.userId) }

    RxPermissions.getInstance(context)
      .request(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
      .subscribe { aBoolean: Boolean ->
        if (aBoolean) {
          enterRoom()
        }else{
          finishFragment()
        }
      }


    FloatServiceHelpter.mCloudView = remote
    FloatServiceHelpter.mLocalView = local
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  fun receiveBackEvent(event: RecTrtcBackEvent) {
    context?.let {
      DialActivity.launch(it, mProps, OPEN_VIDEO)
      FloatServiceHelpter.stopService()
      activity?.finish()
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun startDiaPageEvent(event: StartPageEvent) {
    activity?.finish()
  }

  private fun changeScreen(userId: String) {
    trtcCloud.stopRemoteView(userId)
    trtcCloud.stopLocalPreview()
    FloatServiceHelpter.hasDoctorBigScreen = !(FloatServiceHelpter.hasDoctorBigScreen)

    if (FloatServiceHelpter.hasDoctorBigScreen) {
      local.userId = IMManager.getUserId()
      if (FloatServiceHelpter.audioSwitch) {
        trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
        trtcCloud.startLocalPreview(true, local)
        trtcCloud.startLocalAudio()
      }
      remote.userId = userId
      trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      trtcCloud.startRemoteView(userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remote)

      sub_name.text = mProps.peerName
      bigw_name.text = mProps.name
      FloatServiceHelpter.mCloudView = remote
      FloatServiceHelpter.mLocalView = local
    } else {
      remote.userId = IMManager.getUserId()
      if (FloatServiceHelpter.audioSwitch) {
        trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
        trtcCloud.startLocalPreview(true, remote)
        trtcCloud.startLocalAudio()
      }
      local.userId = userId
      trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      trtcCloud.startRemoteView(local.userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, local)

      sub_name.text = mProps.name
      bigw_name.text = mProps.peerName
      FloatServiceHelpter.mCloudView = local
      FloatServiceHelpter.mLocalView = remote

    }
  }

  private fun enterRoom() {
    //展示呼叫等待界面
    addCallFragment()

    initViewListener()
    trtcCloud.setListener(TRTCCloudListenerImpl())
    val trtcParams = TRTCCloudDef.TRTCParams()
    trtcParams.sdkAppId = IMManager.APP_ID
    trtcParams.userId = IMManager.getUserId()
    trtcParams.userSig = IMManager.getSig()
    trtcParams.roomId = mProps.roomId
    FloatServiceHelpter.mRoomId = mProps.roomId
    Thread(Runnable {
      //每个端在应用场景 appScene 上必须要进行统一，否则会出现一些不可预料的问题。
      //TRTC_APP_SCENE_VIDEOCALL = 0; 视频
      //TRTC_APP_SCENE_LIVE = 1;//直播
      //TRTC_APP_SCENE_AUDIOCALL = 2;//语音
      trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
    }).start()

    arguments?.let { arg ->
      mLaunFlag = arg.getString("userId")
      if (!TextUtils.isEmpty(mLaunFlag)) {
        root.visibility = View.VISIBLE
        FloatServiceHelpter.startLocalVideoView(local, DisplayUtil.dp2px(context, 100f), DisplayUtil.dp2px(context, 178f))
        FloatServiceHelpter.startRemoteVide(remote, ScreenUtils.getScreenWidth(context), ScreenUtils.getScreenHeight(context))
      }
    }
  }


  @RequiresApi(Build.VERSION_CODES.M)
  fun initViewListener() {
    online_toggle.setOnClickListener(mToggleCameraClickListener)
    tv_toggle.setOnClickListener(mToggleCameraClickListener)
    tv_hang.setOnClickListener(mHangClickListener)
    hang_iv.setOnClickListener(mHangClickListener)
    minimize.setOnClickListener {
      context?.let {
        activity?.let { page ->
          PermissionX.init(activity)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .request { allGranted, grantedList, deniedList ->
              if (allGranted) {
                ////悬浮权限
                if (checkPermission(mThis)) {
//                  WindowPermissionCheck.checkPermission(mThis)
                  FloatServiceHelpter.init(it).bindService(mProps)
                  if (activity != null) {
                    if (activity is CallActivity) {
                      activity!!.finish()
                    } else {
                      finishFragment()
                    }
                  }
                } else {
                  //悬浮权限
                  startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + mThis.getPackageName())), 109)
                }
              } else {
//                ViviPermissionUtil.gotoPermission(mThis)
                ToastUtils.center(mThis,"请打开悬浮窗权限")
              }
            }
        }
      }
    }

    CallMgr.setIsVideoing(true)
    CallMgr.setCallListener(RemoteRefuseListener {
      //  CallActivity.cancel(mThis)
      exitRoom()
      FloatServiceHelpter.valumeSwitch = true
      if (activity != null) {
        if (activity is CallActivity) {
          activity!!.finish()
        } else {
          CallFragment.remove(this@CallTRTCFragment)
        }
      }
    })

    CallMgr.setRemoteBusyListener(RemoteBusyListener {
      ToastUtils.center(mThis,"对方正在通话中...")
      exitRoom()
      if (activity != null) {
        if (activity is CallActivity) {
          activity!!.finish()
        } else {
          CallFragment.remove(this@CallTRTCFragment)
        }
      }
    })

    FloatServiceHelpter.startVideoTimer {
      if (chronometer != null) {
        chronometer.text = it
      }
    }
  }

  private fun checkPermission(activity: Activity): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
      return false
    }
    return true
  }

  private val mHangClickListener = View.OnClickListener {
    exitRoom()
  }

  private val mToggleCameraClickListener = View.OnClickListener {
    trtcCloud.switchCamera()
  }

  private fun exitRoom() {
    trtcCloud.exitRoom()
    CallMgr.setIsVideoing(false)
    FloatServiceHelpter.stopService()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    //悬浮权限
    if (requestCode == 109) {
      if (checkPermission(mThis)) {
        context?.let { FloatServiceHelpter.init(it).bindService(mProps) }
        if (activity != null) {
          if (activity is CallActivity) {
            activity!!.finish()
          } else {
            finishFragment()
          }
        }
      }
    }

    if (resultCode == Activity.RESULT_OK) {
      exitRoom()
    }
  }

  private fun finishFragment() {
    fragmentManager?.beginTransaction()
      ?.remove(this@CallTRTCFragment)?.commitAllowingStateLoss()

    FloatHelper.dismiss(activity as AppCompatActivity?)
  }

  private var mLaunFlag: String? = null

  @Subscribe(threadMode = ThreadMode.POSTING)
  fun getCallBack(callFragment: CallFragment) {
    exitRoom()
  }

  inner class TRTCCloudListenerImpl : TRTCCloudListener() {

    override fun onNetworkQuality(p0: TRTCCloudDef.TRTCQuality?, arrayList: ArrayList<TRTCCloudDef.TRTCQuality>?) {
      super.onNetworkQuality(p0, arrayList)
      if (arrayList != null) {
        if (arrayList.size == 0) {
//          ToastUtils.center(mThis,"未定义")
          return
        }
//        ToastUtils.center(mThis,"网络流畅度"+arrayList.get(0).quality)
      } else {
//        ToastUtils.center(mThis,"未定义")
      }
    }

    override fun onEnterRoom(p0: Long) {//result大于0时表示进房成功，具体数值为加入房间所消耗的时间
      //startActivityForResult(CallActivity.newIntent(mThis, mProps), 11)
//      if(mProps.groupDiagnosisChat == 1){
//        if(p0>0){
//        }
//      }
    }

    override fun onUserEnter(userId: String?) {
      //CallActivity.cancel(mThis)
      //删除呼叫等待界面，展示实时视频
      CallFragment.remove(this@CallTRTCFragment)
      root.visibility = View.VISIBLE
      local.userId = IMManager.getUserId()
      trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)//fill全屏模式 fit自适应
      trtcCloud.startLocalPreview(true, local)//开启本地的摄像头，并将采集到的画面编码并发送出去
      trtcCloud.startLocalAudio()//开启本地的麦克风采集，并将采集到的声音编码并发送出去
      remote.visibility = View.VISIBLE
      remote.userId = userId
      trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      //订阅视频流 - 将远端用户的视频数据和显示view关联起来
      trtcCloud.startRemoteView(userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remote)

      initViewListener()
    }

    override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
      if (remote != null) {
        remote.visibility = View.GONE
      }
      trtcCloud.stopRemoteView(userId)
      exitRoom()
    }

    //当房间中有其他用户在上行音频数据时
    override fun onUserAudioAvailable(userId: String?, available: Boolean) {
      super.onUserAudioAvailable(userId, available)
    }

    //当房间中有其他用户在上行视频数据时
    override fun onUserVideoAvailable(userId: String?, available: Boolean) {
      super.onUserVideoAvailable(userId, available)
    }

    override fun onError(errCode: Int, errMsg: String?, p2: Bundle?) {
      if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
        //进入房间失败
        ToastUtils.center(context!!, "呼叫失败")
        exitRoom()
      }
    }

    override fun onExitRoom(p0: Int) {
      CallMgr.setIsVideoing(false)
      CallMgr.setCallListener(null)
      CallMgr.setRemoteBusyListener(null)
      if (activity != null) {
        if (activity is CallActivity) {
          activity!!.finish()
        } else {
          finishFragment()
        }
      }

      FloatServiceHelpter.destroyVideoLive()
      EventBus.getDefault().post(TrTcExitEvent())
    }
  }

  fun addCallFragment() {
    childFragmentManager.beginTransaction().add(R.id.callContainer,
      CallFragment.newSelf(mThis, mProps)
    )
      .commitAllowingStateLoss()
  }

  override fun onSuccess() {
    Log.e("tag", "onSuccess")
  }

  override fun onFailure() {
    Log.e("tag", "onFailure")
  }

  override fun getLayoutId() = R.layout.online_video_layout

}
