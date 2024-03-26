package com.byh.module.onlineoutser.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.activity.DialActivity
import com.byh.module.onlineoutser.base.BHBaseFragment
import com.byh.module.onlineoutser.im.IMManager
import com.byh.module.onlineoutser.im.callback.CallVideoListener
import com.byh.module.onlineoutser.im.callback.RemoteRefuseListener
import com.byh.module.onlineoutser.im.entity.RecTrtcBackEvent
import com.byh.module.onlineoutser.im.entity.TrTcExitEvent
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.utils.FloatHelper
import com.byh.module.onlineoutser.utils.FloatServiceHelpter
import com.byh.module.onlineoutser.utils.ToastUtils
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.liteav.TXLiteAVCode
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener
import kotlinx.android.synthetic.main.dial_activity.*
import kotlinx.android.synthetic.main.online_video_layout.*
import kotlinx.android.synthetic.main.online_video_layout.root
import kotlinx.android.synthetic.main.online_video_layout.tv_hang
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 接收视频画面 DiaActivity
 */
class ReceiveTRTCFragment : BHBaseFragment() {

  companion object {

    private const val REC_TAG = "ReceiveTRTCFragment"
    lateinit var pClickListener :CallVideoListener

    fun newInstance(props: CallMsg,callBack: CallVideoListener): ReceiveTRTCFragment {
      pClickListener = callBack

      val fg = ReceiveTRTCFragment()
      val data = Bundle()
      data.putParcelable("props", props)
      fg.arguments = data
      return fg
    }

    fun newInstance(props: CallMsg, flag: String): ReceiveTRTCFragment {
      val fg = ReceiveTRTCFragment()
      val data = Bundle()
      data.putParcelable("props", props)
      data.putString(DialActivity.OPEN_VIDEO, flag)
      fg.arguments = data
      return fg
    }
  }

  private var mLaunFlag: String? = null
  private var oldTime:Long = System.currentTimeMillis()
  private var isLookReport:Boolean = false

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

  private val param2 by lazy {
    TRTCCloudDef.TRTCVideoEncParam().apply {
      videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE
      videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360
    }
  }

  override fun getLayoutId():Int{
    return R.layout.online_video_layout
  }

  override fun initEvent() {

    bigw_name.text = mProps.name//医生
    sub_name.text = mProps.peerName//患者名字

    RxPermissions.getInstance(requireActivity())
      .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      .subscribe({ isPermission ->
        if (isPermission) {
          enterRoom()
        } else {
          ToastUtils.center(mThis,"未获取到相应权限")
        }
      }, { finishFragment() })


    FloatServiceHelpter.mCloudView = remote
    FloatServiceHelpter.mLocalView = local

    initViewListener()

    local?.setOnClickListener {//点击互换位置
      if(System.currentTimeMillis() - oldTime >= 1000){
        changeScreen(if (FloatServiceHelpter.hasDoctorBigScreen) remote.userId else local.userId)
        oldTime = System.currentTimeMillis()
      }
    }
  }

  private fun enterRoom() {

    val trtcParams = TRTCCloudDef.TRTCParams()
    trtcParams.sdkAppId = IMManager.APP_ID
    trtcParams.userId = mProps.account
    trtcParams.userSig = mProps.userSign
    trtcParams.roomId = mProps.roomId
    FloatServiceHelpter.mRoomId = mProps.roomId

    trtcCloud.setListener(TRTCCloudListenerImpl())
    trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)

    arguments?.let { arg ->
      mLaunFlag = arg.getString(DialActivity.OPEN_VIDEO)
      if (!TextUtils.isEmpty(mLaunFlag)) {
        root.visibility = View.VISIBLE

        FloatServiceHelpter.mCloudView?.let { bffRemoteView ->
          val userId = bffRemoteView.userId
          loadVideoView(userId)
        }
      }
    }
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

    override fun onEnterRoom(p0: Long) {
      if (root == null) {
        val rootView = findView<View>(R.id.root)
        rootView?.visibility = View.VISIBLE
      } else {
        root.visibility = View.VISIBLE
      }

      initTrtcVideo()
    }

    override fun onUserVideoAvailable(p0: String?, available: Boolean) {
      if (!available) {
        trtcCloud.stopRemoteView(p0)
        trtcCloud.startRemoteView(p0, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,remote)
      } else {
        trtcCloud.startRemoteView(p0, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,remote)
      }
    }

    override fun onUserEnter(userId: String?) {
      userId?.let {
        startRemoteView(it)
      }
    }

    override fun onUserExit(userId: String?, p1: Int) {}//废弃

    override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
      trtcCloud.stopRemoteView(userId)
      exitRoom()
    }

    override fun onError(errCode: Int, errMsg: String, p2: Bundle?) {
      if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
        //进入房间失败
        ToastUtils.center(context!!, "呼叫失败")
        exitRoom()
      }else{
        if(errMsg.contains("camera") || errMsg.contains("mic")){
          ToastUtils.center(context!!, "请打开摄像头/麦克风")
        }
      }
    }

    override fun onExitRoom(p0: Int) {
      CallMgr.setIsVideoing(false)
      CallMgr.setCallListener(null)
      activity?.finish()
      FloatServiceHelpter.destroyVideoLive()
      EventBus.getDefault().post(TrTcExitEvent())
    }
  }

  private fun initTrtcVideo() {
    initViewListener()
    root.visibility = View.VISIBLE
    local.userId = mProps.account
    trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
    trtcCloud.startLocalPreview(true, local)
    trtcCloud.startLocalAudio()
  }

  private fun startRemoteView(userId: String) {
    view ?: return
    remote.userId = userId
    trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
    trtcCloud.setVideoEncoderParam(param2)
    trtcCloud.startRemoteView(userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remote)
  }

  private fun exitRoom() {
    trtcCloud.exitRoom()
    CallMgr.setIsVideoing(false)
    FloatServiceHelpter.stopService()
  }

  private fun initViewListener() {
    online_toggle?.setOnClickListener{//切换摄像头
      trtcCloud.switchCamera()
    }
    tv_toggle?.setOnClickListener{//切换摄像头
      trtcCloud.switchCamera()
    }

    tv_hang?.setOnClickListener{//挂断
      exitRoom()
      pClickListener.videoEndListener()
    }
    hang_iv?.setOnClickListener{//挂断
      exitRoom()
      pClickListener.videoEndListener()
    }

    hang_iv.requestFocus()
    hang_iv.setOnFocusChangeListener { v, hasFocus ->
      if(hasFocus){
        ViewCompat.animate(hang_iv).scaleX(1.5f).scaleY(1.5f).translationZ(1f).start()
      }else{
        ViewCompat.animate(hang_iv).scaleX(1f).scaleY(1f).translationZ(0f).start()
      }
    }
    tv_hang.setOnFocusChangeListener { v, hasFocus ->
      if (hasFocus) {
        tv_hang.setTextColor(resources.getColor(R.color.red))
      } else {
        tv_hang.setTextColor(resources.getColor(R.color.white))
      }
    }

    minimize?.setOnClickListener {//缩小
      isLookReport = false
      startFloatVideoLive()
    }

    CallMgr.setIsVideoing(true)
    CallMgr.setCallListener(RemoteRefuseListener {
      FloatServiceHelpter.valumeSwitch = true
      exitRoom()
      activity?.finish()
    })

    FloatServiceHelpter.startVideoTimer {//计时
      if (chronometer != null) {
        chronometer.text = it
      }
    }
  }

  private fun startFloatVideoLive() {
    RxPermissions.getInstance(requireActivity())
      .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      .subscribe({ isPermission ->
        if (isPermission) {
          if(checkPermission(requireActivity())){
            showFlowVideoView()
          }else{
            //悬浮权限
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:" + mThis.getPackageName())), 1109)
          }
        } else {
          ToastUtils.center(mThis,"未获取到相应权限")
        }
      }, { })
  }

  private fun checkPermission(activity: Activity): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
      return false
    }
    return true
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    //悬浮权限
    if (requestCode == 1109) {
      if (checkPermission(mThis)) {
        showFlowVideoView()
      }
    }
  }

  private fun showFlowVideoView(){
    if(isLookReport) {
      //查看病例
    }
    FloatServiceHelpter.init(requireActivity()).bindService(mProps)
    activity?.finish()
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun receiveBackEvent(event: RecTrtcBackEvent) {
    //病例详情返回
    FloatServiceHelpter.stopService()

    FloatServiceHelpter.mCloudView?.let { bffRemoteView ->
      val userId = bffRemoteView.userId
      loadVideoView(userId)
    }
  }

  @SuppressLint("CheckResult")
  private fun loadVideoView(userId: String) {
    trtcCloud.stopRemoteView(userId)
    trtcCloud.stopLocalAudio()
    trtcCloud.stopLocalPreview()

    if (FloatServiceHelpter.hasDoctorBigScreen) {
      local.userId = mProps.account

      if (FloatServiceHelpter.audioSwitch) {
        trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
        trtcCloud.startLocalPreview(true, local)
        trtcCloud.startLocalAudio()
      }

      remote.userId = userId
      trtcCloud.setRemoteRenderParams(userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      trtcCloud.setVideoEncoderParam(param2)
      trtcCloud.startRemoteView(userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remote)

      sub_name.text = mProps.peerName
      bigw_name.text = mProps.name

      FloatServiceHelpter.mCloudView = remote
      FloatServiceHelpter.mLocalView = local
    } else {
      remote.userId = mProps.account
      if (FloatServiceHelpter.audioSwitch) {
        trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
        trtcCloud.startLocalPreview(true, remote)
        trtcCloud.startLocalAudio()
      }

      local.userId = userId
      trtcCloud.setRemoteRenderParams(local.userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      trtcCloud.setVideoEncoderParam(param2)
      trtcCloud.startRemoteView(local.userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,local)

      sub_name.text = mProps.name
      bigw_name.text = mProps.peerName

      FloatServiceHelpter.mCloudView = local
      FloatServiceHelpter.mLocalView = remote
    }
  }

  private fun changeScreen(userId: String) {

    trtcCloud.stopRemoteView(userId)
    trtcCloud.stopLocalPreview()

    FloatServiceHelpter.hasDoctorBigScreen = !(FloatServiceHelpter.hasDoctorBigScreen)

    if (FloatServiceHelpter.hasDoctorBigScreen) {
      local.userId = mProps.account
      if (FloatServiceHelpter.audioSwitch) {
        trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
        trtcCloud.startLocalPreview(true, local)
        trtcCloud.startLocalAudio()
      }
      remote.userId = userId
      trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      trtcCloud.setVideoEncoderParam(param2)
      trtcCloud.startRemoteView(userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remote)
      sub_name.text = mProps.peerName
      bigw_name.text = mProps.name
      FloatServiceHelpter.mCloudView = remote
      FloatServiceHelpter.mLocalView = local
    } else {
      local.userId = remote.userId
      remote.userId = mProps.account
      if (FloatServiceHelpter.audioSwitch) {
        trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL)
        trtcCloud.startLocalPreview(true, remote)
        trtcCloud.startLocalAudio()
      }
      // local.userId = userId
      trtcCloud.setRemoteRenderParams(local.userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      trtcCloud.setVideoEncoderParam(param2)
      trtcCloud.startRemoteView(local.userId,TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, local)
      sub_name.text = mProps.name
      bigw_name.text = mProps.peerName
      FloatServiceHelpter.mCloudView = local
      FloatServiceHelpter.mLocalView = remote
    }
  }

  private fun finishFragment() {
    fragmentManager?.beginTransaction()
      ?.remove(this@ReceiveTRTCFragment)?.commitAllowingStateLoss()

    FloatHelper.dismiss(activity as AppCompatActivity?)
  }
}
