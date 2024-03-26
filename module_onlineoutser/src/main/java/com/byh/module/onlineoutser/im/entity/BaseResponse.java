package com.byh.module.onlineoutser.im.entity;


/**
 * Created by poetr on 07/01/2018.
 */

public class BaseResponse<T> {

  private String errCode;
  private String code;
  private String msg;
  private T data;
  private String reqName;

  public String getErrCode() {
    return errCode;
  }

  public void setErrCode(String errCode) {
    this.errCode = errCode;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getReqName() {
    return reqName;
  }

  public void setReqName(String reqName) {
    this.reqName = reqName;
  }
}
