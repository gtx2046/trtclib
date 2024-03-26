package com.byh.module.onlineoutser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.byh.module.onlineoutser.im.IMManager

@SuppressLint("StaticFieldLeak")
object BHManger {

  private var currtenActivity: Activity? = null

  private const val TECENT_IM_TAG = "BHManger"
  fun init(application: Application) {
    IMManager.init(application)
    application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
      override fun onActivityResumed(p0: Activity) {
        currtenActivity = p0
      }

      override fun onActivityPaused(p0: Activity) {
      }

      override fun onActivityStarted(p0: Activity) {
      }

      override fun onActivityDestroyed(p0: Activity) {
      }

      override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
      }

      override fun onActivityStopped(p0: Activity) {
      }

      override fun onActivityCreated(p0: Activity, p1: Bundle?) {
      }
    })
  }

  fun imLoginWithRun(account:String,sign:String) {
    IMManager.loginWithRun(account, sign)
  }

  fun imLogout() {
    IMManager.logout()
  }

}
