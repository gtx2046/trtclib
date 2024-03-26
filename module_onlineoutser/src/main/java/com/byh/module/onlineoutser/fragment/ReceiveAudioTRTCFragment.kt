package com.byh.module.onlineoutser.fragment

import android.Manifest
import android.os.Bundle
import android.view.View
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.BHBaseFragment
import com.byh.module.onlineoutser.im.IMManager
import com.byh.module.onlineoutser.im.callback.RemoteRefuseListener
import com.byh.module.onlineoutser.im.entity.TrTcExitEvent
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.utils.FloatServiceHelpter
import com.byh.module.onlineoutser.utils.ToastUtils
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.liteav.TXLiteAVCode
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudListener
import kotlinx.android.synthetic.main.fragment_receive_audio_trtc.audio_time
import kotlinx.android.synthetic.main.fragment_receive_audio_trtc.hang
import kotlinx.android.synthetic.main.fragment_receive_audio_trtc.p_name
import kotlinx.android.synthetic.main.fragment_receive_audio_trtc.tv_hang
import org.greenrobot.eventbus.EventBus

class ReceiveAudioTRTCFragment : BHBaseFragment(){

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

  companion object {
    fun newInstance(props: CallMsg): ReceiveAudioTRTCFragment {
      val fg = ReceiveAudioTRTCFragment()
      val data = Bundle()
      data.putParcelable("props", props)
      fg.arguments = data
      return fg
    }
  }

  override fun getLayoutId(): Int {
    return R.layout.fragment_receive_audio_trtc
  }

  override fun initEvent() {

    p_name.text = "患者"+mProps.name

    RxPermissions.getInstance(requireActivity())
      .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      .subscribe({ isPermission ->
        if (isPermission) {
          enterRoom()
        } else {
          ToastUtils.center(mThis,"未获取到相应权限")
        }
      }, { finishFragment() })

  }

  private fun enterRoom() {
    trtcCloud.setListener(TRTCCloudListenerImpl())
    val trtcParams = TRTCCloudDef.TRTCParams()
    trtcParams.sdkAppId = IMManager.APP_ID
    trtcParams.userId = IMManager.getUserId()
    trtcParams.userSig = IMManager.getSig()
    trtcParams.roomId = mProps.roomId
    trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
  }

  private val mHangClickListener = View.OnClickListener {
    exitRoom()
  }

  private fun exitRoom() {
    trtcCloud.exitRoom()
    CallMgr.setIsVideoing(false)
    FloatServiceHelpter.stopVideoTimer()
  }

  private fun finishFragment() {
    fragmentManager?.beginTransaction()
      ?.remove(this@ReceiveAudioTRTCFragment)?.commitAllowingStateLoss()
  }

  private fun initViewListener() {
    tv_hang?.setOnClickListener(mHangClickListener)
    hang?.setOnClickListener(mHangClickListener)

    CallMgr.setIsVideoing(true)
    CallMgr.setCallListener(RemoteRefuseListener {
      exitRoom()
      activity?.finish()
    })
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
      initViewListener()
      CallMgr.stopRing()
    }

    override fun onUserAudioAvailable(userId: String?, available: Boolean) {
      super.onUserAudioAvailable(userId, available)
    }

    override fun onUserEnter(userId: String?) {
      trtcCloud.startLocalAudio()
      trtcCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,param)
      CallMgr.stopRing()
      FloatServiceHelpter.startVideoTimer {
        if (audio_time != null) {
          audio_time.text = it
        }
      }
    }

    override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
      exitRoom()
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
      FloatServiceHelpter.destroyVideoLive()
      activity?.finish()
      EventBus.getDefault().post(TrTcExitEvent())
    }
  }

}
