package com.byh.module.onlineoutser.floatwindow;

/**
 * Created by yhao on 2018/5/5
 * https://github.com/yhaolpz
 * @author mac
 */
public interface ViewStateListener {
  void onPositionUpdate(int x, int y);

  void onShow();

  void onHide();

  void onDismiss();

  void onMoveAnimStart();

  void onMoveAnimEnd();

  void onBackToDesktop();
}
