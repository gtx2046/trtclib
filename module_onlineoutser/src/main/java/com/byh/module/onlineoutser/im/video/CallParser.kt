package com.byh.module.onlineoutser.im.video

import android.Manifest
import com.blankj.utilcode.util.Utils
import com.byh.module.onlineoutser.activity.DialActivity.Companion.OPEN_VIDEO
import com.byh.module.onlineoutser.im.IMManager
import com.byh.module.onlineoutser.utils.FloatServiceHelpter
import com.byh.module.onlineoutser.utils.ToastUtils
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.imsdk.TIMCustomElem
import com.tencent.imsdk.TIMMessage
import com.tencent.imsdk.TIMValueCallBack
import org.json.JSONObject
import org.json.JSONTokener
import java.nio.charset.Charset

object CallParser {

  fun parse(elem: TIMCustomElem, converPeer: String) {
    val data = JSONTokener(String(elem.data,
      Charset.defaultCharset())).nextValue() as JSONObject
    val callMsg = CallMsg()
    callMsg.peer = converPeer
    callMsg.roomId = data.optString(IMConstants.KEY_ROOM_ID).toInt()
    callMsg.avatar = data.optString(IMConstants.KEY_AVATAR)
    callMsg.businessId = data.optString(IMConstants.KEY_BSID)
    callMsg.businessCode = data.optString(IMConstants.KEY_BSCODE)
    callMsg.name = data.optString(IMConstants.KEY_NAME)
    callMsg.userAction = data.optInt(IMConstants.KEY_CMD)
    callMsg.account = "1679409592439668736*EHOS_PATIENT"
    callMsg.userSign = "eJw1js0KgkAAhN9ljxG2un*u0MGDYCRWukV0Cc3NtihMRcvo3du0jjPzzTAvIILYkI9ClRI4JiHEghCOe7eRJXCAZUAw6Cq7JEWhMs1hDdk2Q-aQqEzeanVUfcGkjGPICbcw4pRqio48fxHvl66YeaH4r6lcw9u5bPDGzVfB4Xlec8Hup7DtqiSKd2kkUrPDW*Ul0p*0*fRXrNX1e5VBjKCJGHt-AA5jN8c_"
    if("video".equals(data.optString(IMConstants.KEY_CallType))){
      callMsg.callType = 0
    }else if("audio".equals(data.optString(IMConstants.KEY_CallType))){
      callMsg.callType = 1
    }

    when (callMsg.userAction) {
      IMConstants.CMD_CALL -> {
        if (CallMgr.isVideoing()) {
          if (callMsg.roomId == FloatServiceHelpter.mRoomId) {
            //直接进入视频通话
            CallMgr.dial(callMsg, OPEN_VIDEO)
          } else {
            //发占线消息
            CallMgr.busy(callMsg, object : TIMValueCallBack<TIMMessage> {
              override fun onError(p0: Int, p1: String?) {
              }

              override fun onSuccess(p0: TIMMessage?) {
              }
            })
          }
          return
        }

        RxPermissions.getInstance(Utils.getApp())
          .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
          .subscribe { aBoolean: Boolean ->
            if (aBoolean) {
              CallMgr.dial(callMsg)
            }else{
               ToastUtils.center(IMManager.mContext,"权限未开启")
            }
          }
      }
      IMConstants.CMD_CANCEL -> {
        CallMgr.remoteCancel()
      }
      IMConstants.CMD_REFUSE -> {
        CallMgr.remoteRefuse()
      }
      IMConstants.CMD_BUSY -> {
        CallMgr.remoteBusy()
      }
      IMConstants.CMD_DRUG_STORE_CANCEL -> {
        CallMgr.remoteDrugStoreCancel()
      }
      IMConstants.CMD_DRUG_STORE_ANSWER -> {
        if (CallMgr.isVideoing()) {
          CallMgr.busy(callMsg, object : TIMValueCallBack<TIMMessage> {
            override fun onError(p0: Int, p1: String?) {}

            override fun onSuccess(p0: TIMMessage?) {}
          })
        }else{
          try {
            //超时不接听
            val time = data.optLong("CallDate")
            if(time + 60*1000 < System.currentTimeMillis()){
                return
            }
          }catch (e:Exception){}
          callMsg.name = data.optString("name")
          CallMgr.dialDrugStoreAnswer(callMsg)
        }
      }
    }
  }
}
