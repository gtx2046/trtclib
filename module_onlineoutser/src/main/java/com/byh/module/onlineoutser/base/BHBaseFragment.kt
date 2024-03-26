package com.byh.module.onlineoutser.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class BHBaseFragment : Fragment() {

  protected lateinit var mThis: Activity
  private lateinit var mView: View

  override fun onAttach(context: Context) {
    super.onAttach(context)
    mThis = context as Activity
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    mThis = activity as Activity
    mView = inflater.inflate(getLayoutId(), container, false)
    EventBus.getDefault().register(this)
    initViews()
    return mView
  }


  open fun initViews() {}
  override fun onDestroyView() {
    EventBus.getDefault().unregister(this)
    super.onDestroyView()
  }

  @Subscribe
  fun receiveTestEvent(test: String) {

  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    initEvent()
  }

  abstract fun getLayoutId(): Int

  abstract fun initEvent()

  protected fun <T : View> findView(id: Int): T? {
    return mView.findViewById<T>(id)
  }

  protected fun startActivity(cls: Class<out Activity>) {
    startActivity(Intent(mThis, cls))
  }

  override fun onHiddenChanged(hidden: Boolean) {
    super.onHiddenChanged(hidden)
  }
}
