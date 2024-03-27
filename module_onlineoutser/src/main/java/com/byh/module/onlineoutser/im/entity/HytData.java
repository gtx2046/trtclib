package com.byh.module.onlineoutser.im.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

public class HytData implements Parcelable {

  public static final int DEF_VALUE = -1;
  public static final Creator<HytData> CREATOR = new Creator<HytData>() {
    @Override
    public HytData createFromParcel(Parcel source) {
      return new HytData(source);
    }

    @Override
    public HytData[] newArray(int size) {
      return new HytData[size];
    }
  };
  private String UserAction;
  private String msgId;
  private String doctorName;
  private String appointmentId;
  private String hospitalCode;
  private String businessCode;
  private String msgType;
  private String messageType;
  private String applicationCode;
  private String toApplicationCode;
  private String attacheUrl;
  private String duration;
  private Boolean team = false;
  private String doctorHeadUrl;
  private String groupId;
  private String groupName;
  private String groupIcon;
  private String senderName;
  private String text;
  private String receiverUrl;
  private String receiverName;
  private String senderProfessional;//发送者职称
  private int teamFlag;//1团队诊疗
  private String msgRandom;//社区群聊msgId

  protected HytData(Parcel in) {
    this.UserAction = in.readString();
    this.msgId = in.readString();
    this.doctorName = in.readString();
    this.appointmentId = in.readString();
    this.hospitalCode = in.readString();
    this.businessCode = in.readString();
    this.msgType = in.readString();
    this.applicationCode = in.readString();
    this.toApplicationCode = in.readString();
    this.attacheUrl = in.readString();
    this.duration = in.readString();
    this.team = in.readByte() != 0;
    this.doctorHeadUrl = in.readString();
    this.groupId = in.readString();
    this.groupIcon = in.readString();
    this.groupName = in.readString();
    this.messageType = in.readString();
    this.text = in.readString();
    this.receiverUrl = in.readString();
    this.receiverName = in.readString();
    this.senderProfessional = in.readString();
    this.teamFlag = in.readInt();
    this.msgRandom = in.readString();
  }

  public String getMsgRandom() {
    return msgRandom;
  }

  public void setMsgRandom(String msgRandom) {
    this.msgRandom = msgRandom;
  }

  public int getTeamFlag() {
    return teamFlag;
  }

  public void setTeamFlag(int teamFlag) {
    this.teamFlag = teamFlag;
  }

  public String getReceiverUrl() {
    return receiverUrl;
  }

  public void setReceiverUrl(String receiverUrl) {
    this.receiverUrl = receiverUrl;
  }

  public String getReceiverName() {
    return receiverName;
  }

  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
  }

  public String getGroupIcon() {
    return groupIcon;
  }

  public void setGroupIcon(String groupIcon) {
    this.groupIcon = groupIcon;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getMessageType() {
    if (messageType == null) {
      return DEF_VALUE;
    }

    return Integer.parseInt(messageType);
  }

  public void setMessageType(int messageType) {
    this.messageType = messageType + "";
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public String getSenderProfessional() {
    return senderProfessional;
  }

  public void setSenderProfessional(String senderProfessional) {
    this.senderProfessional = senderProfessional;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getDoctorHeadUrl() {
    return doctorHeadUrl;
  }

  public void setDoctorHeadUrl(String doctorHeadUrl) {
    this.doctorHeadUrl = doctorHeadUrl;
  }

  public int getUserAction() {
    if (UserAction == null) {
      return DEF_VALUE;
    }

    return Integer.parseInt(UserAction);
  }

  public void setUserAction(int userAction) {
    UserAction = userAction + "";
  }

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public void setDoctorName(String doctorName) {
    this.doctorName = doctorName;
  }

  public String getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(String appointmentId) {
    this.appointmentId = appointmentId;
  }

  public String getHospitalCode() {
    return hospitalCode;
  }

  public void setHospitalCode(String hospitalCode) {
    this.hospitalCode = hospitalCode;
  }

  public String getBusinessCode() {
    return businessCode;
  }

  public void setBusinessCode(String businessCode) {
    this.businessCode = businessCode;
  }

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public String getApplicationCode() {
    return applicationCode;
  }

  public void setApplicationCode(String applicationCode) {
    this.applicationCode = applicationCode;
  }

  public String getToApplicationCode() {
    return toApplicationCode;
  }

  public void setToApplicationCode(String toApplicationCode) {
    this.toApplicationCode = toApplicationCode;
  }

  public String getAttacheUrl() {
    return attacheUrl;
  }

  public void setAttacheUrl(String attacheUrl) {
    this.attacheUrl = attacheUrl;
  }

  public int getDuration() {
    if (duration == null) {
      return DEF_VALUE;
    }

    return Integer.parseInt(duration);
  }

  public void setDuration(int duration) {
    this.duration = duration + "";
  }

  public boolean isTeam() {
    if (team == null) {
      return false;
    }
    return team;
  }

  public void setTeam(boolean team) {
    this.team = team;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.UserAction);
    dest.writeString(this.msgId);
    dest.writeString(this.doctorName);
    dest.writeString(this.appointmentId);
    dest.writeString(this.hospitalCode);
    dest.writeString(this.businessCode);
    dest.writeString(this.msgType);
    dest.writeString(this.applicationCode);
    dest.writeString(this.toApplicationCode);
    dest.writeString(this.attacheUrl);
    dest.writeString(this.duration);
    dest.writeByte(this.team ? (byte) 1 : (byte) 0);
    dest.writeString(this.doctorHeadUrl);
    dest.writeString(this.groupId);
    dest.writeString(this.groupName);
    dest.writeString(this.groupIcon);
    dest.writeString(this.messageType);
    dest.writeString(this.text);
    dest.writeString(this.receiverUrl);
    dest.writeString(this.receiverName);
    dest.writeString(this.senderProfessional);
    dest.writeInt(this.teamFlag);
    dest.writeString(this.msgRandom);
  }

}
