package com.byh.module.onlineoutser.im.entity;

public class VideoCallHangReq {

  String admId;
  String type;//type 参数,string型 1.视频  2.语音

  public VideoCallHangReq(String admId,String types) {
    this.admId = admId;
    this.type = types;
  }

  public String getAdmId() {
    return admId;
  }

}
