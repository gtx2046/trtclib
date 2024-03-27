package com.byh.module.onlineoutser.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.byh.module.onlineoutser.R
import com.byh.module.onlineoutser.base.BHBaseFragment
import com.byh.module.onlineoutser.im.callback.RemoteRefuseListener
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallMsg
import com.byh.module.onlineoutser.activity.CallActivity
import com.byh.module.onlineoutser.utils.FloatServiceHelpter
import com.byh.module.onlineoutser.utils.ToastUtils
import com.byh.module.onlineoutser.utils.FloatHelper
import com.tencent.imsdk.TIMMessage
import com.tencent.imsdk.TIMValueCallBack
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dial_activity.*
import kotlinx.android.synthetic.main.online_call_activity.*
import kotlinx.android.synthetic.main.online_call_activity.avatar
import kotlinx.android.synthetic.main.online_call_activity.hang
import kotlinx.android.synthetic.main.online_call_activity.hint
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

/**
 * 仅仅是个呼叫等待界面，展示及接通视频，没有视频操作
 */
class CallFragment : BHBaseFragment() {

  var mMsg: CallMsg = CallMsg()

  companion object {

    fun newSelf(context: Context, callMsg: CallMsg): Fragment {
      var fragment = CallFragment()
      val args = Bundle()
      args.putParcelable("msg", callMsg)
      fragment.arguments = args
      return fragment
    }


    fun remove(parent: Fragment) {
      val transaction = parent.childFragmentManager.beginTransaction();
      parent.childFragmentManager.fragments.forEach {
        transaction.remove(it)
      }
      transaction.commitAllowingStateLoss()
    }

  }

  override fun initEvent() {
    mMsg = arguments?.getParcelable<CallMsg>("msg")!!
    afterViewCreated()
    startTimer()
  }

  override fun onStop() {
    super.onStop()
    CallMgr.stopRing()
    //结束计时任务
    disposable.dispose()
    disposable1.dispose()
  }

  override fun getLayoutId(): Int {
    return R.layout.online_call_activity
  }

  fun afterViewCreated() {
    if (mMsg == null) {
      back()
      return
    }

    if (mMsg.businessCode == "YCHZ") {
      val nameStr = FloatServiceHelpter.mCallName
      val headerUrl = FloatServiceHelpter.mCallHeaderUrl
      Glide.with(mThis).load(headerUrl).placeholder(R.drawable.icon_minimize).circleCrop().into(avatar)

      if (name != null && headerUrl != null) {
        Glide.with(mThis).load(headerUrl).placeholder(R.drawable.ic_user_header).circleCrop().into(userIcon)
        name.text = nameStr
      } else {
        Glide.with(mThis).load(headerUrl).placeholder(R.drawable.ic_user_header).circleCrop().into(userIcon)
      }

      hint.text = "您向${nameStr}发起了视频问诊请耐心等待医生接听"
    } else {
      Glide.with(mThis).load(mMsg.peerAvatar).placeholder(R.drawable.icon_minimize).circleCrop().into(avatar)
      Glide.with(mThis).load(mMsg.peerAvatar).placeholder(R.drawable.ic_user_header).circleCrop().into(userIcon)
      hint.text = "您向${mMsg.peerName}患者发起了视频问诊请耐心等待患者接听"
      name.text = mMsg.peerName

      if(mMsg.callType == 1){
        avatar.visibility = View.GONE
        hint.text = "您向${mMsg.peerName}患者发起了语音问诊请耐心等待患者接听"
      }
    }
    CallMgr.setCallListener(RemoteRefuseListener {
      if (mMsg.callType == 0) {
//        mHttp.silencePost(
//          VideoCallHangReq(
//            mMsg.businessId,
//            "1"
//          )
//        )
      } else if(mMsg.callType == 1) {
//        mHttp.silencePost(
//          VideoCallHangReq(
//            mMsg.businessId,
//            "2"
//          )
//        )
      }
      EventBus.getDefault().post(this@CallFragment)
      if (activity != null) {
        if (activity is CallActivity) {
          (activity as CallActivity).finish()
        }
      }
    })

    hang.setOnClickListener {
      if (mMsg.businessCode == "YCHZ") {//远程会诊

      } else {
        CallMgr.cancel(mMsg, object : TIMValueCallBack<TIMMessage> {
          override fun onSuccess(p0: TIMMessage?) {
            if(mMsg.callType == 0){
//              mHttp.silencePost(
//                VideoCallHangReq(
//                  mMsg.businessId,
//                  "1"
//                )
//              )
            }else if(mMsg.callType == 1){
//              mHttp.silencePost(
//                VideoCallHangReq(
//                  mMsg.businessId,
//                  "2"
//                )
//              )
            }

            EventBus.getDefault().post(this@CallFragment)
            back()
          }

          override fun onError(p0: Int, p1: String?) {
            ToastUtils.center(mThis, "取消失败，请重试")
          }
        })
      }
    }

    CallMgr.startRing(mThis)
    avatar.setOnClickListener {
      FloatHelper.setFloatMin(activity as AppCompatActivity?)
    }
  }


  lateinit var disposable: Disposable
  lateinit var disposable1: Disposable

  @SuppressLint("CheckResult")
  fun startTimer() {

    Observable.timer(20, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY) ))
      .subscribe(object : Observer<Long> {
        override fun onSubscribe(d: Disposable) {
          this@CallFragment.disposable1 = d
        }

        override fun onNext(t: Long) {
          ToastUtils.center(mThis,"对方可能不在手机旁，请稍后再试")
        }

        override fun onError(e: Throwable) {}

        override fun onComplete() {}
      })

    Observable.timer(60, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY) ))
      .subscribe(object : Observer<Long> {
        override fun onSubscribe(d: Disposable) {
          this@CallFragment.disposable = d
        }

        override fun onNext(t: Long) {
          ToastUtils.center(mThis,"对方长时间未接听，请稍后再试")
          if (mMsg.businessCode == "YCHZ") {

          } else {
            CallMgr.cancel(mMsg, object : TIMValueCallBack<TIMMessage> {
              override fun onSuccess(p0: TIMMessage?) {
                EventBus.getDefault().post(this@CallFragment)
                back()
              }

              override fun onError(p0: Int, p1: String?) {
                ToastUtils.center(mThis, "取消失败，请重试")
              }
            })
          }
        }

        override fun onError(e: Throwable) {}

        override fun onComplete() {}
      })
  }

  fun back() {
    fragmentManager?.beginTransaction()
      ?.remove(this)
      ?.commitAllowingStateLoss()
  }

}
