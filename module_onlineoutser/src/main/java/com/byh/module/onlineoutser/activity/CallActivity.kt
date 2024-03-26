package com.byh.module.onlineoutser.activity

import android.content.Context
import android.content.Intent
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.ActivityTitle
import com.byh.module.onlineoutser.base.BHBaseActivity
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.fragment.CallTRTCFragment
import com.byh.module.onlineoutser.im.entity.TrTcExitEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 发起视频的界面callTrtcFragment
 */
@ActivityTitle(showTitle = false)
class CallActivity : BHBaseActivity() {

  companion object {
    fun newIntent(context: Context, callMsg: CallMsg): Intent {
      val intent = Intent(context, CallActivity::class.java)
      intent.putExtra("msg", callMsg)
      return intent
    }

    fun cancel(context: Context) {
      val intent = Intent(context, CallActivity::class.java)
      context.startActivity(intent)
    }
  }

  private val mMsg by lazy { intent.getParcelableExtra<CallMsg>("msg") }

  override fun getLayoutId() = R.layout.frame_layout

  override fun afterViewCreated() {
    supportFragmentManager.beginTransaction().replace(R.id.frame_content,
      CallTRTCFragment.newInstance(mMsg!!)).commitAllowingStateLoss()
  }


  override fun onBackPressedSupport() {
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  fun receiveExitEvent(event: TrTcExitEvent) {
    finish()
  }
}
