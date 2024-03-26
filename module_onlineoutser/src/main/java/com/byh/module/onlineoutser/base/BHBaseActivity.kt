package com.byh.module.onlineoutser.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import com.byh.module.onlineoutser.R
import me.yokeyword.fragmentation.SupportActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class BHBaseActivity : SupportActivity() {

  protected val mThis by lazy { this }
  private var mTitleTv: TextView? = null
  private var mSubTitleTv: TextView? = null
  protected var mToolbar: Toolbar? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    EventBus.getDefault().register(this)
    val activityTitle = this.javaClass.getAnnotation(ActivityTitle::class.java)
    if (activityTitle == null || activityTitle.showTitle) {
      val rootView = layoutInflater.inflate(R.layout.online_toolbar_layout, null) as ViewGroup
      val toolbar = rootView.findViewById<Toolbar>(R.id.tool_bar)
      mToolbar = toolbar
      setSupportActionBar(toolbar)
      supportActionBar?.setDisplayShowTitleEnabled(false)
      toolbar.setNavigationOnClickListener { onBackPressed() }
      val container = rootView.findViewById<FrameLayout>(R.id.view_container)
      val acView = layoutInflater.inflate(getLayoutId(), container, false);
      container.addView(acView)
      setContentView(rootView)
      mTitleTv = rootView.findViewById(R.id.tool_bar_title)
      mSubTitleTv = rootView.findViewById(R.id.sub_title)
    } else {
      setContentView(getLayoutId())
    }
    afterViewCreated()
  }

  @Subscribe
  fun sub(s: String) {

  }

  override fun onDestroy() {
    EventBus.getDefault().unregister(this)
    super.onDestroy()
  }

  abstract fun getLayoutId(): Int

  abstract fun afterViewCreated()

  override fun setTitle(titleId: Int) {
    mTitleTv?.text = resources.getString(titleId)
  }

  override fun setTitle(title: CharSequence?) {
    mTitleTv?.text = title
  }

  protected fun setSubTitle(title: CharSequence?) {
    mSubTitleTv?.visibility = if (TextUtils.isEmpty(title)) View.GONE else View.VISIBLE
    mSubTitleTv?.text = title
  }

  protected fun setHeadColor(@ColorInt color: Int) {
    mTitleTv?.setTextColor(color)
  }

  protected fun startActivity(cls: Class<out Activity>) {
    startActivity(Intent(mThis, cls))
  }


}

