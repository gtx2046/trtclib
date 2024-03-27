package com.byh.module.onlineoutser.im

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.byh.module.onlineoutser.BuildConfig
import com.byh.module.onlineoutser.im.callback.EmptyTIMCallBack
import com.tencent.imsdk.*
import com.tencent.imsdk.TIMManager.TIM_STATUS_LOGINED
import com.tencent.imsdk.session.SessionWrapper

@SuppressLint("StaticFieldLeak")
object IMManager : TIMUserStatusListener {

  private const val TAG = "IMManager"
  val APP_ID: Int = 1400088738
  lateinit var mContext: Context
  private lateinit var mSig: String

  private var mUserId: String? = null
  private var isRegister: Boolean = false


  fun init(context: Context) {
    mContext = context.applicationContext
    if (SessionWrapper.isMainProcess(mContext)) {

      if (TIMManager.getInstance().loginStatus != TIM_STATUS_LOGINED) {

        val config = TIMSdkConfig(APP_ID)
          .enableLogPrint(BuildConfig.DEBUG)
          .setLogLevel(TIMLogLevel.VERBOSE)

        TIMManager.getInstance().init(mContext, config)

        val userConfig = TIMUserConfig().setUserStatusListener(this)
        userConfig.connectionListener = TIMConnectListener()
        TIMManager.getInstance().userConfig = userConfig
        //IM消息监听
        TIMManager.getInstance().addMessageListener(TXMsgListener)
        Log.v("patientIM","IM初始化")
      }
    }
  }

  fun loginWithRun(userId: String, sig: String) {

    if (TIMManager.getInstance().loginStatus == TIM_STATUS_LOGINED) {
      return
    }

    if (!TextUtils.isEmpty(mUserId) && !TextUtils.equals(userId, mUserId)) {
      isRegister = true
    }

    mUserId = userId
    mSig = sig

    //1762018180135387136*EHOS_PATIENT
    //1679409592439668736*EHOS_PATIENT
    TIMManager.getInstance().login(userId, sig, object : TIMCallBack {
      override fun onError(code: Int, desc: String) {
        Log.v("patientIM","$code ---- $desc")

        if (code == 6012 || code == 6014) {
//          BHManger.imLoginWithRun(run)
        } else if (code == 6208) {//其他终端登录同一个账号，引起已登录的账号被踢，需重新登录
//          ToastUtils.showShort("IM被其他终端登录")
        }
      }

      override fun onSuccess() {
        val settings = TIMOfflinePushSettings()
        settings.isEnabled = true
        TIMManager.getInstance().setOfflinePushSettings(settings)
        Log.v("patientIM","IM成功")
      }
    })
  }

  fun logout() {
    TIMManager.getInstance().logout(EmptyTIMCallBack())
  }

  fun getUserId(): String {
    if (!TextUtils.isEmpty(mUserId)) {
      return mUserId!!
    }
    return ""
  }

  fun getSig(): String {
    if (!TextUtils.isEmpty(mSig)) {
      return mSig
    }
    return ""
  }

  override fun onUserSigExpired() {//用户票据过期
    Log.v("patientIM", "==========onUserSigExpired=========")
//    ToastUtils.showShort("IM票据过期")
  }

  override fun onForceOffline() {//被踢下线时回调
    Log.v("patientIM", "==========onForceOffline=========")
//    ToastUtils.showShort("IM被踢下线")
  }
}
