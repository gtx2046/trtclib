package com.byh.module.onlineoutser.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewGroup
import com.byh.module.onlineoutser.im.video.CallMsg
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import java.util.*

object FloatServiceHelpter {

  private const val VIDEO_TIME_VALUE = 1
  private var mContext: Context? = null
  var mCloudView: TXCloudVideoView? = null
  var mLocalView: TXCloudVideoView? = null
  var mTrtcCloudContr: TRTCCloud? = null
  var mRoomId: Int? = null

  var valumeSwitch = true
  var audioSwitch = true
  var cameraSwitch = false

  var hasDoctorBigScreen = true

  var mVideoTimer: Timer? = null

  var mCallName: String? = null
  var mCallHeaderUrl: String? = null

  var isRun = false;

  val mDelayHandler = Handler()
  private fun getStringTime(cnt: Int): String {
    val hour = cnt / 3600
    val min = cnt % 3600 / 60
    val second = cnt % 60
    return String.format(Locale.CHINESE, "%02d:%02d:%02d", hour, min, second)
  }


  private val mTimerHandler = object : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
      super.handleMessage(msg)
      if (VIDEO_TIME_VALUE == msg?.what) {
        val timeStr = msg.obj as String
        mBlock?.invoke(timeStr)
      }
    }
  }

  private var mBlock: ((String) -> Unit)? = null
  fun startVideoTimer(block: (String) -> Unit) {
    mBlock = block
    if (mVideoTimer == null) {
      mVideoTimer = Timer()
      mVideoTimer!!.schedule(object : TimerTask() {
        var cnt = 0
        override fun run() {
          val msg = mTimerHandler.obtainMessage()
          msg.obj = getStringTime(cnt++)
          msg.what = VIDEO_TIME_VALUE
          mTimerHandler.sendMessage(msg)
        }
      }, 0, 1000)
    }
  }


  fun stopVideoTimer() {
    if (mVideoTimer != null) {
      mVideoTimer!!.cancel()
      mVideoTimer = null
    }
    mBlock = null
  }


  fun destroyVideoLive() {
    mContext = null
    mCloudView = null
    mLocalView = null
    valumeSwitch = true
    audioSwitch = true
    cameraSwitch = false
    hasDoctorBigScreen = true
    stopVideoTimer()
    TRTCCloud.destroySharedInstance()
  }

  fun startLocalVideoView(localVideoView: TXCloudVideoView, width: Int, height: Int) {

    mDelayHandler.postDelayed({
      mLocalView?.let {
        val glSurfaceView = it.glSurfaceView
        if (glSurfaceView?.parent != null) {
          (glSurfaceView.parent as ViewGroup).removeView(glSurfaceView)
          val layoutParams = glSurfaceView.layoutParams
          layoutParams.width = width
          layoutParams.height = height
          glSurfaceView.layoutParams = layoutParams
          glSurfaceView.visibility = View.VISIBLE
          localVideoView.visibility = View.VISIBLE
          localVideoView.addVideoView(glSurfaceView)
        }
      }
    }, 80)


  }

  fun startRemoteVide(remoteView: TXCloudVideoView, width: Int, height: Int) {

    mDelayHandler.postDelayed({
      mCloudView?.let {
        val videoView = it.videoView
        if (videoView?.parent != null) {
          (videoView.parent as ViewGroup).removeView(videoView)
          val layoutParams = videoView.layoutParams
          layoutParams.width = width
          layoutParams.height = height
          videoView.layoutParams = layoutParams
          remoteView.addVideoView(videoView)
        }
      }
    }, 80)

  }

  private val mIntent by lazy {
    Intent(mContext,
      FloatVideoWindowServiceIM::class.java)
  }

  fun init(context: Context): FloatServiceHelpter {
    if (mContext == null) {
      mContext = context
    }
    return this
  }

  fun bindService(msg: CallMsg) {
    mIntent.putExtra("msg", msg)
    mContext?.startService(mIntent)
    isRun = true
  }

  fun stopService() {
    mContext?.stopService(mIntent)
    isRun = false
  }

  fun isServiceRun() = isRun

}
