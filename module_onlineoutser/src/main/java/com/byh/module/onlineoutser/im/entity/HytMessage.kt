package com.byh.module.onlineoutser.im.entity

import android.text.TextUtils
import com.byh.module.onlineoutser.db.*
import com.byh.module.onlineoutser.im.IMManager
import com.google.gson.Gson
import com.tencent.imsdk.TIMMessage
import java.util.*

class HytMessage( var id: String){

  var conversionId: Int = 0
  var msgId: String? = null
  var userId = IMManager.getUserId()
  var applicationCode: String = "zxzx"
  var subId: String? = null
  var sender: String? = null
  var receiver: String? = null
  var date: Date? = null//聊天记录排序用
  var body: String? = null//聊天内容
  var path: String? = null
  var url: String? = null
  var converId: String? = null
  var sendStatus: HytSendStatusType? = null
  var messageType: HytMessageType? = null
    get() {
      return if (field == HytMessageType.System
        && !TextUtils.isEmpty(body)) {
        when {
            else -> {
              HytMessageType.System
            }
        }
      } else {
        field
      }
    }

  var direction: HytDirectionType? = null
  var readStatus: HytReadStatusType? = null

  var data: HytData? = null
  var duration: Int = 0

//  @Ignore 发送者名字
  var name: String? = ""
  //职称
  var professional: String? = ""

//  @Ignore
  var img: String? = ""

  var portrait = Portrait.Default

  var timMsg: TIMMessage? = null

  override fun toString(): String {
    return Gson().toJson(this)
  }

  override fun equals(other: Any?): Boolean {
    other?.let {
      if (it is HytMessage) {
        return id == it.id
      }
    }
    return super.equals(other)
  }
}
