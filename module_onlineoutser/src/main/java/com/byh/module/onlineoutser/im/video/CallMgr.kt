package com.byh.module.onlineoutser.im.video

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.byh.module.onlineoutser.BuildConfig
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.im.callback.CancelCallListener
import com.byh.module.onlineoutser.im.callback.RemoteBusyListener
import com.byh.module.onlineoutser.im.callback.RemoteDrugStoreCancelListener
import com.byh.module.onlineoutser.im.callback.RemoteRefuseListener
import com.byh.module.onlineoutser.activity.DialActivity
import com.byh.module.onlineoutser.activity.DialAudioActivity
import com.byh.module.onlineoutser.im.callback.CallVideoListener
import com.tencent.imsdk.*
import org.json.JSONObject
import org.json.JSONTokener
import java.lang.ref.WeakReference
import java.nio.charset.Charset

object CallMgr {

  private const val TAG = "CallMgr"
  private var mMediaPlayer: WeakReference<MediaPlayer>? = null
  private var mIsVideoing = false//语音 视频 在线咨询 复诊 远程会诊 多学科 是否占线
  private var mRefuseListener: RemoteRefuseListener? = null//远端拒绝
  private var mCancelCallback: CancelCallListener? = null//取消
  private var mBusyListener: RemoteBusyListener? = null//远端忙线
  private var mDrugStoreRemoteCancel: RemoteDrugStoreCancelListener? = null//药店远端取消

  fun setCallListener(listener: RemoteRefuseListener?) {
    mRefuseListener = listener
  }

  fun setCancelListener(listener: CancelCallListener?) {
    mCancelCallback = listener
  }

  fun setRemoteBusyListener(listener: RemoteBusyListener?){
    mBusyListener = listener
  }

  fun setRemoteDrugCancelListener(listener: RemoteDrugStoreCancelListener?){
    mDrugStoreRemoteCancel = listener
  }

  fun isVideoing() = mIsVideoing

  fun setIsVideoing(isVideo: Boolean) {
    mIsVideoing = isVideo
  }

  fun isVideoMsg(elem: TIMCustomElem): Boolean {
    val data = JSONTokener(String(elem.data,
      Charset.defaultCharset())).nextValue() as JSONObject
    val cmd = data.optInt(IMConstants.KEY_CMD)
    return cmd >= IMConstants.CMD_CALL && cmd < IMConstants.CMD_END
  }

  fun call(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    callMsg.userAction = IMConstants.CMD_CALL
    sendC2C(callMsg, callback)
  }

  fun sendAnswerMsg(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    callMsg.userAction = IMConstants.CMD_ANSWER
    callMsg.text = String.format(Utils.getApp().getString(R.string.online_video_received), callMsg.peerName)
    sendC2C(callMsg, callback)
  }

  fun dial(msg: CallMsg) {
    msg.peerName = "患者"
    DialActivity.launch(Utils.getApp(), msg,object:CallVideoListener{
      override fun receiveListener() {
        Log.v("back--","接听")
      }

      override fun rejectListener() {
        Log.v("back--","拒绝")
      }

      override fun videoEndListener() {
        Log.v("back--","结束")
      }
    })
  }

  fun dialDrugStoreAnswer(msg: CallMsg){
    msg.peerName = "医生姓名"
    DialAudioActivity.launch(Utils.getApp(), msg)
  }

  fun dial(msg: CallMsg, flag: String) {
    msg.peerName = "医生姓名"
    DialActivity.launch(Utils.getApp(), msg, flag)
  }

  fun cancel(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    callMsg.userAction = IMConstants.CMD_CANCEL
    sendC2C(callMsg, callback)
  }
  fun audioEnd(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    callMsg.userAction = IMConstants.CMD_AUDIO_END
    sendC2C(callMsg, callback)
  }

  fun remoteCancel() {
    mCancelCallback?.onCancel()
  }

  fun remoteRefuse() {
    mRefuseListener?.onRefuse()
  }

  fun remoteBusy(){
    mBusyListener?.busy()
  }

  fun remoteDrugStoreCancel(){
    mDrugStoreRemoteCancel?.remoteCancel()
  }

  fun refuse(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    callMsg.userAction = IMConstants.CMD_REFUSE
    sendC2C(callMsg, callback)
  }

  fun busy(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    callMsg.userAction = IMConstants.CMD_BUSY
    sendC2C(callMsg, callback)
  }

  private fun sendC2C(callMsg: CallMsg, callback: TIMValueCallBack<TIMMessage>) {
    val conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, callMsg.peer)
    val msg = TIMMessage()
    val customElem = TIMCustomElem()
    customElem.data = callMsg.toRemoteGsonString().toByteArray()
    msg.addElement(customElem)

    Log.i(TAG, "sendC2C msg data:${msg.toString()}")
    conversation.sendMessage(msg, callback)
  }

  fun startRing(context: Context) {
    if (!BuildConfig.BUILD_TYPE.contains("release", true)) return

    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
    if (mMediaPlayer == null) {
      val mediaPlayer = MediaPlayer.create(context, R.raw.phone_online)
      mediaPlayer.isLooping = true
      mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
      mediaPlayer.start()
      mMediaPlayer = WeakReference(mediaPlayer)
    }
  }

  fun stopRing() {
    if (mMediaPlayer != null) {
      mMediaPlayer!!.get()?.let {
        if (it.isPlaying) {
          it.stop()
        }
        it.release()
      }
      mMediaPlayer = null
    }
  }
}
