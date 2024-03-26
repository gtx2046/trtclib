package com.byh.module.onlineoutser.im.video;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.byh.module.onlineoutser.im.IMManager;
import com.byh.module.onlineoutser.im.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author mac
 */
public class CallMsg implements Parcelable {

  public static final Creator<CallMsg> CREATOR = new Creator<CallMsg>() {
    @Override
    public CallMsg createFromParcel(Parcel source) {
      return new CallMsg(source);
    }

    @Override
    public CallMsg[] newArray(int size) {
      return new CallMsg[size];
    }
  };

  private static final String TAG = "CallMsg";
  private String peer;//医生账号
  private int roomId;//im 房间号
  private int UserAction;
  private String name;//医生名字
  private String avatar;//头像
  private String businessId;
  private String peerName;//患者姓名
  private String peerAvatar;
  private String businessCode;
//  private String msgType;
  private String text;
  private String applicationCode;
  private int callType;
  private int pSex;
  private long CallDate;

  private String account;//im账号
  private String userSign;//im账号 sign


  public CallMsg() {}

  protected CallMsg(Parcel in) {
    this.peer = in.readString();
    this.roomId = in.readInt();
    this.UserAction = in.readInt();
    this.name = in.readString();
    this.avatar = in.readString();
    this.businessId = in.readString();
    this.peerName = in.readString();
    this.peerAvatar = in.readString();
    this.businessCode = in.readString();
//    this.msgType = in.readString();
    this.text = in.readString();
    this.applicationCode = in.readString();
    this.callType = in.readInt();
    this.account = in.readString();
    this.userSign = in.readString();
  }

  public long getCallDate() {
    return CallDate;
  }

  public void setCallDate(long callDate) {
    CallDate = callDate;
  }

  public int getpSex() {
    return pSex;
  }

  public void setpSex(int pSex) {
    this.pSex = pSex;
  }

//  public String getMsgType() {
//    return msgType;
//  }

//  public void setMsgType(String msgType) {
//    this.msgType = msgType;
//  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getApplicationCode() {
    return applicationCode;
  }

  public void setApplicationCode(String applicationCode) {
    this.applicationCode = applicationCode;
  }

  public String getBusinessCode() {
    return businessCode;
  }

  public void setBusinessCode(String businessCode) {
    this.businessCode = businessCode;
  }

  public String getPeerName() {
    return peerName;
  }

  public void setPeerName(String peerName) {
    this.peerName = peerName;
  }

  public String getPeerAvatar() {
    return peerAvatar;
  }

  public void setPeerAvatar(String peerAvatar) {
    this.peerAvatar = peerAvatar;
  }

  public String getBusinessId() {
    return businessId;
  }

  public void setBusinessId(String businessId) {
    this.businessId = businessId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public String getPeer() {
    return peer;
  }

  public void setPeer(String peer) {
    this.peer = peer;
  }

  public int getRoomId() {
    return roomId;
  }

  public void setRoomId(int roomId) {
    this.roomId = roomId;
  }

  public int getUserAction() {
    return UserAction;
  }

  public void setUserAction(int userAction) {
    UserAction = userAction;
  }

  public int getCallType() {
    return callType;
  }

  public void setCallType(int callType) {
    this.callType = callType;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getUserSign() {
    return userSign;
  }

  public void setUserSign(String userSign) {
    this.userSign = userSign;
  }

  @Override
  public String toString() {
    return JsonUtil.INSTANCE.getGson().toJson(this);
  }

  public String toRemoteGsonString() {
    String result = "";
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put(IMConstants.KEY_CMD, UserAction);
      jsonObject.put(IMConstants.KEY_ROOM_ID, String.valueOf(roomId));
      jsonObject.put(IMConstants.KEY_BSID, businessId);
      jsonObject.put(IMConstants.KEY_PEER, account);
      jsonObject.put(IMConstants.KEY_AVATAR, avatar);
      if ("YCHZ" == businessCode) {
        jsonObject.put(IMConstants.KEY_NAME, peerName);
      } else {
        jsonObject.put(IMConstants.KEY_NAME, name);
      }

      jsonObject.put(IMConstants.KEY_BSCODE, businessCode);
      jsonObject.put(IMConstants.KEY_TEXT, text);
//      jsonObject.put(IMConstants.KEY_GROUPID, groupId);
//      jsonObject.put(IMConstants.KEY_MSGTYPE, msgType);
      jsonObject.put(IMConstants.KEY_APPLICATION_CODE, applicationCode);
      if(callType == 0){
        jsonObject.put("CallType", "video");//小程序端判断接收的是何种消息
      }else if(callType == 1){
        jsonObject.put("CallType", "audio");//小程序端判断接收的是何种消息
      }
      jsonObject.put("CallDate", System.currentTimeMillis() / 1000);
      result = jsonObject.toString();
      Log.i(TAG, "toRemoteGsonString: result:" + result);
    } catch (JSONException ignored) {
    }
    return result;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.peer);
    dest.writeInt(this.roomId);
    dest.writeInt(this.UserAction);
    dest.writeString(this.name);
    dest.writeString(this.avatar);
    dest.writeString(this.businessId);
    dest.writeString(this.peerName);
    dest.writeString(this.peerAvatar);
    dest.writeString(this.businessCode);
//    dest.writeString(this.msgType);
    dest.writeString(this.text);
    dest.writeString(this.applicationCode);
    dest.writeInt(this.callType);
    dest.writeString(this.account);
    dest.writeString(this.userSign);
  }
}
