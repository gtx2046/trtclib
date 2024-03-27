package com.byh.module.onlineoutser.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.byh.module.onlineoutser.BuildConfig
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.ActivityTitle
import com.byh.module.onlineoutser.base.BHBaseActivity
import com.byh.module.onlineoutser.fragment.ReceiveTRTCFragment
import com.byh.module.onlineoutser.im.callback.CallVideoListener
import com.byh.module.onlineoutser.im.callback.CancelCallListener
import com.byh.module.onlineoutser.im.entity.RecAnswerSelfEvent
import com.byh.module.onlineoutser.im.entity.TrTcExitEvent
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import kotlinx.android.synthetic.main.dial_activity.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * IM监听 其他医生发起的视频会话请求
 * 接到请求后打开视频接听界面，
 * 同意后 进入视频会话界面receiveTrtcfragment
 */
@ActivityTitle(showTitle = false)
class DialActivity : BHBaseActivity(), View.OnClickListener {

  companion object {

    private const val TAG = "DialActivity"
    const val OPEN_VIDEO = "open_video"

    lateinit var pClickListener :CallVideoListener

    fun launch(context: Context, callMsg: CallMsg,callBack:CallVideoListener) {
      pClickListener = callBack
      val intent = Intent(context, DialActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.putExtra("msg", callMsg)
      context.startActivity(intent)
    }

    fun launch(context: Context, callMsg: CallMsg, flag: String) {
      val intent = Intent(context, DialActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.putExtra("msg", callMsg)
      intent.putExtra(OPEN_VIDEO, flag)
      context.startActivity(intent)
    }
  }

  private val mMsg by lazy { intent.getParcelableExtra<CallMsg>("msg") }

  override fun getLayoutId() = R.layout.dial_activity

  override fun onCreate(savedInstanceState: Bundle?) {
    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    super.onCreate(savedInstanceState)
  }

  override fun afterViewCreated() {

    Utils.init(this.application)

//    val usbManager = getSystemService(USB_SERVICE) as UsbManager
//    usbManager.deviceList.forEach {
//      it.value.vendorId
//    }

//    if(!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
//      ToastUtils.center(this,"无摄像头")
//      return
//    }

//    if(!packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
//      ToastUtils.center(this,"无麦克风")
//      return
//    }

    val flag = intent.getStringExtra(OPEN_VIDEO)?:""
    if (OPEN_VIDEO == flag) {
      //视频会话界面
      CallMgr.stopRing()
      supportFragmentManager.beginTransaction().replace(R.id.frame_content,
        ReceiveTRTCFragment.newInstance(mMsg!!, flag))
        .commitAllowingStateLoss()
    } else {
      //视频接听界面
      root.visibility = View.VISIBLE
      CallMgr.startRing(mThis)
      CallMgr.setIsVideoing(true)
    }

    if (checkCancel()) {
      return
    }

    Glide.with(mThis).load(mMsg!!.avatar).placeholder(R.drawable.ic_user_header).circleCrop().into(avatar)

    doc_name.text = mMsg!!.name
    answer.setOnClickListener(this)
    tv_answer.setOnClickListener(this)
    hang.setOnClickListener(this)
    tv_hang.setOnClickListener(this)

    CallMgr.setCancelListener(CancelCallListener {
      CallMgr.setIsVideoing(false)
      finish()
    })

    answer.setOnFocusChangeListener { v, hasFocus ->
      if(hasFocus){
        ViewCompat.animate(answer).scaleX(1.5f).scaleY(1.5f).translationZ(1f).start()
      }else{
        ViewCompat.animate(answer).scaleX(1f).scaleY(1f).translationZ(0f).start()
      }
    }

    hang.setOnFocusChangeListener { v, hasFocus ->
      if(hasFocus){
        ViewCompat.animate(hang).scaleX(1.5f).scaleY(1.5f).translationZ(1f).start()
      }else{
        ViewCompat.animate(hang).scaleX(1f).scaleY(1f).translationZ(0f).start()
      }
    }
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.answer, R.id.tv_answer -> {
//        CallMgr.sendAnswerMsg(mMsg,object:TIMValueCallBack<TIMMessage>{
//          override fun onError(p0: Int, p1: String?) {}
//
//          override fun onSuccess(p0: TIMMessage?) {
            if (!supportFragmentManager.isDestroyed)
              supportFragmentManager.beginTransaction().replace(R.id.frame_content, ReceiveTRTCFragment.newInstance(mMsg,pClickListener))
                .commitAllowingStateLoss()
            answer.clearFocus()
            hang.clearFocus()
            CallMgr.stopRing()
            pClickListener.receiveListener()
//          }
//        })
      }
      R.id.hang, R.id.tv_hang -> {
//          CallMgr.refuse(mMsg!!, object : TIMValueCallBack<TIMMessage> {
//            override fun onSuccess(p0: TIMMessage?) {
              CallMgr.setIsVideoing(false)
              finish()
              pClickListener.rejectListener()
//            }
//            override fun onError(p0: Int, p1: String?) {
//              ToastUtils.center(mThis, "挂断失败")
//            }
//          })
      }
    }
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

  private fun checkCancel(): Boolean {
    val cancel = intent.getBooleanExtra("remoteCancel", false)
    if (cancel) {
      finish()
    }
    return cancel
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun recAnswerSelfEvent(event: RecAnswerSelfEvent) {
    finish()
  }
}
