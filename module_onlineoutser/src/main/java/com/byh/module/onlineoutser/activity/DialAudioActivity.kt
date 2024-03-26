package com.byh.module.onlineoutser.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import com.byh.module.onlineoutser.BuildConfig
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.ActivityTitle
import com.byh.module.onlineoutser.base.BHBaseActivity
import com.byh.module.onlineoutser.im.callback.RemoteDrugStoreCancelListener
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.fragment.ReceiveAudioTRTCFragment
import com.byh.module.onlineoutser.im.entity.TrTcExitEvent
import com.byh.module.onlineoutser.utils.ToastUtils
import com.tencent.imsdk.TIMMessage
import com.tencent.imsdk.TIMValueCallBack
import kotlinx.android.synthetic.main.activity_receive_audio.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ActivityTitle(showTitle = false)
class DialAudioActivity : BHBaseActivity(){

  private val mMsg by lazy { intent.getParcelableExtra<CallMsg>("msg") }

  companion object {

    const val OPEN_VIDEO = "open_video"

    fun launch(context: Context, callMsg: CallMsg) {
      val intent = Intent(context, DialAudioActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.putExtra("msg", callMsg)
      context.startActivity(intent)
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    window.addFlags(
      WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
      or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
      or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    super.onCreate(savedInstanceState)
  }

  override fun getLayoutId(): Int {
    return R.layout.activity_receive_audio
  }

  override fun afterViewCreated() {

    CallMgr.startRing(mThis)
    CallMgr.setIsVideoing(true)

    iv_agree.setOnClickListener{

      if (!supportFragmentManager.isDestroyed)
        supportFragmentManager.beginTransaction().replace(R.id.frame_content, ReceiveAudioTRTCFragment.newInstance(mMsg!!))
          .commitAllowingStateLoss()
    }

    iv_reject.setOnClickListener {
      CallMgr.refuse(mMsg!!, object : TIMValueCallBack<TIMMessage> {
        override fun onSuccess(p0: TIMMessage?) {
          finish()
          CallMgr.setIsVideoing(false)
        }

        override fun onError(p0: Int, p1: String?) {
          ToastUtils.center(mThis, "挂断失败")
        }
      })
    }

    CallMgr.setRemoteDrugCancelListener(RemoteDrugStoreCancelListener {
      CallMgr.setIsVideoing(false)
      finish()
    })
  }

  override fun onBackPressedSupport() {
    if (CallMgr.isVideoing()) {
      return
    }
    super.onBackPressedSupport()
  }

  override fun onStop() {
    super.onStop()
    CallMgr.stopRing()
  }

  override fun onResume() {
    super.onResume()
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    val wl = pm.newWakeLock(
      PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
      BuildConfig.LIBRARY_PACKAGE_NAME + ":screenlock")
    wl.acquire(10000) // 点亮屏幕
    wl.release()
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun receiveExitEvent(event: TrTcExitEvent) {
    finish()
  }
}
