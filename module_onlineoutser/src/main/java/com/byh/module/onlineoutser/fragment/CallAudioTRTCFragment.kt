package com.byh.module.onlineoutser.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.BHBaseFragment
import com.byh.module.onlineoutser.im.IMManager
import com.byh.module.onlineoutser.im.callback.RemoteBusyListener
import com.byh.module.onlineoutser.im.callback.RemoteRefuseListener
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.utils.FloatServiceHelpter
import com.byh.module.onlineoutser.utils.ToastUtils
import com.byh.module.onlineoutser.utils.FloatHelper
import com.kangxin.common.Pretty
import com.kangxin.common.imageloader.Style
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.imsdk.TIMMessage
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.liteav.TXLiteAVCode
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener
import kotlinx.android.synthetic.main.online_audio_layout.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CallAudioTRTCFragment : BHBaseFragment(){

  companion object {

    private const val CALL_TAG = "CallAudioTRTCFragment"
    fun newInstance(props: CallMsg): CallAudioTRTCFragment {
      val fg = CallAudioTRTCFragment()
      val data = Bundle()
      data.putParcelable("props", props)
      fg.arguments = data
      return fg
    }

    fun start(ft: FragmentManager, props: CallMsg, @IdRes containerId: Int): CallAudioTRTCFragment {
      val trtcFragment = newInstance(props)
      ft.beginTransaction().add(containerId, trtcFragment)
        .commitAllowingStateLoss()
      return trtcFragment
    }

    fun start(activity: AppCompatActivity, props: CallMsg): CallAudioTRTCFragment {
      val trtcFragment = newInstance(props)
      FloatHelper.floatFragment(activity, trtcFragment)//将fragment置于悬浮窗中
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
    Log.i(CALL_TAG, "peerName:${mProps.peerName}, name:${mProps.name}")
    p_name.text = "患者"+mProps.peerName
    var deat = 0
    if(mProps.getpSex() == 1){
      //男
      deat = R.drawable.ic_user_header
    }else{
      deat = R.drawable.ic_pation_girl
    }
    Pretty.create().loadImage("").bitmapTransform(Style.CIRCLE)
      .placeholder(deat).into(p_head)

    RxPermissions.getInstance(context)
      .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      .subscribe { aBoolean: Boolean ->
        if (aBoolean) {
          enterRoom()
        }else{
          finishFragment()
        }
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
      trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_AUDIOCALL)
    }).start()

  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun initViewListener() {
    tv_hang.setOnClickListener(mHangClickListener)
    hang.setOnClickListener(mHangClickListener)

    CallMgr.setIsVideoing(true)
    CallMgr.setCallListener(RemoteRefuseListener {
      exitRoom()
      FloatServiceHelpter.valumeSwitch = true
      CallFragment.remove(this@CallAudioTRTCFragment)
    })

    CallMgr.setRemoteBusyListener(RemoteBusyListener {
      ToastUtils.center(mThis,"对方正在通话中...")
      CallFragment.remove(this@CallAudioTRTCFragment)
      exitRoom()
    })

  }

  private fun checkPermission(activity: Activity): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
      return false
    }
    return true
  }

  private val mHangClickListener = View.OnClickListener {
    FloatHelper.dismiss(activity as AppCompatActivity?)
    exitRoom()
  }

  private fun exitRoom() {
//    if(mProps.groupDiagnosisChat == 1){
//      enterRoomStatus(0)
//    }
    if(mProps.callType == 1){
      CallMgr.audioEnd(mProps,object:TIMValueCallBack<TIMMessage>{
        override fun onError(p0: Int, p1: String?) {}

        override fun onSuccess(p0: TIMMessage?) {
          trtcCloud.exitRoom()
          CallMgr.setIsVideoing(false)
          FloatServiceHelpter.stopService()
        }
      })
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      exitRoom()
    }
  }

  private fun finishFragment() {
    fragmentManager?.beginTransaction()
      ?.remove(this@CallAudioTRTCFragment)?.commitAllowingStateLoss()
  }

  @Subscribe(threadMode = ThreadMode.POSTING)
  fun getCallBack(callFragment: CallFragment) {
//    if(mProps.groupDiagnosisChat == 1){
//      enterRoomStatus(0)
//    }
    FloatHelper.dismiss(activity as AppCompatActivity?)
    trtcCloud.exitRoom()
    CallMgr.setIsVideoing(false)
    FloatServiceHelpter.stopService()
  }

  inner class TRTCCloudListenerImpl : TRTCCloudListener() {

    override fun onNetworkQuality(p0: TRTCCloudDef.TRTCQuality?, arrayList: ArrayList<TRTCCloudDef.TRTCQuality>?) {
      super.onNetworkQuality(p0, arrayList)
      if (arrayList != null) {
        if (arrayList.size == 0) {
          //直播源不可用
          return
        }
      } else {
        //直播源不可用
      }
    }

    override fun onEnterRoom(p0: Long) {//result大于0时表示进房成功，具体数值为加入房间所消耗的时间
//      if(mProps.groupDiagnosisChat == 1){
//        if(p0>0){
//          enterRoomStatus(1)
//        }else{
//          enterRoomStatus(0)
//        }
//      }
    }

    override fun onUserEnter(userId: String?) {
      //删除呼叫等待界面，展示实时视频
      CallFragment.remove(this@CallAudioTRTCFragment)
      trtcCloud.startLocalAudio()//开启本地的麦克风采集，并将采集到的声音编码并发送出去
      trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)

      initViewListener()

      FloatServiceHelpter.startVideoTimer {
        if (audio_time != null) {
          audio_time.text = it
        }
      }

    }

    override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
      trtcCloud.muteRemoteAudio(userId,true)
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
      try {
        FloatHelper.dismiss(activity as AppCompatActivity?)
        CallMgr.setIsVideoing(false)
        CallMgr.setCallListener(null)
        CallMgr.setRemoteBusyListener(null)
        callContainer?.removeAllViews()
        FloatServiceHelpter.destroyVideoLive()
        finishFragment()
      }catch (e:Exception){
        e.printStackTrace()
      }
    }
  }

  fun addCallFragment() {
    childFragmentManager.beginTransaction().add(R.id.callContainer,
      CallFragment.newSelf(mThis, mProps)
    )
      .commitAllowingStateLoss()
  }

  override fun getLayoutId() = R.layout.online_audio_layout

  fun enterRoomStatus(status:Int){
  }

  //华为手机左滑退出activity.onDestroy()
  //如果在onDestroy中写 会先调用activity的onDestroy
  // 使fragment finish时空指针
  override fun onStop() {
    try {
      exitRoom()
    }catch (e :Exception){
    }
    super.onStop()
  }
}
