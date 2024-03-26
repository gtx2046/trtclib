package com.byh.module.onlineoutser.im.video

object IMConstants {

  const val KEY_CMD = "UserAction"
  const val KEY_ROOM_ID = "roomId"
  const val KEY_PEER = "accoutsdk"
  const val KEY_AVATAR = "invitingAvatar"
  const val KEY_BSID = "appointmentId"
  const val KEY_NAME = "invitingName"
  const val KEY_BSCODE = "businessCode"
  const val KEY_MSGTYPE = "msgType"
  const val KEY_CallType = "CallType"
  const val KEY_TEXT = "text"
  const val KEY_APPLICATION_CODE = "applicationCode"
  const val KEY_GROUPID = "groupId"

  //实时视频通话
  const val CMD_CALL = 129
  const val CMD_ANSWER = 130//多学科接听
  const val CMD_CANCEL = 131
  const val CMD_REFUSE = 133//拒绝
  const val CMD_BUSY = 135//占线中
  const val CMD_END = 200

  //实时语音通话
  const val CMD_AUDIO_END = 134//挂断

  //药店扫码-语音通话-开具处方
  const val CMD_DRUG_STORE_ANSWER = 140//接听
  const val CMD_DRUG_STORE_CANCEL = 141//远端取消

}
