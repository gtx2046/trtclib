package com.byh.module.onlineoutser.base

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import com.byh.module.onlineoutser.R
import me.yokeyword.fragmentation.SupportActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class NewBHBaseActivity : SupportActivity() {

  protected val mThis by lazy { this }
  private var mTitleTv: TextView? = null
  private var mSubTitleTv: TextView? = null
  protected var mToolbar: Toolbar? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    //设置状态栏颜色
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val window: Window = getWindow()
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) //允许页面可以拉伸到顶部状态栏并且定义顶部状态栏透名
      window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = Color.TRANSPARENT //设置状态栏为透明
      window.navigationBarColor = Color.TRANSPARENT //设置虚拟键为透明
    }

    EventBus.getDefault().register(this)
    val activityTitle = this.javaClass.getAnnotation(ActivityTitle::class.java)
    if (activityTitle == null || activityTitle.showTitle) {
      val rootView = layoutInflater.inflate(R.layout.new_title_toolbar, null) as ViewGroup
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
