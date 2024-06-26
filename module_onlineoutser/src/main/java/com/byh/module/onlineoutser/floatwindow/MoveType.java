package com.byh.module.onlineoutser.floatwindow;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author yhao
 * @date 2017/12/22
 * https://github.com/yhaolpz
 */

public class MoveType {
  public static final int inactive = 1;
  public static final int active = 2;
  public static final int slide = 3;
  public static final int back = 4;
  static final int fixed = 0;

  @IntDef({fixed, inactive, active, slide, back})
  @Retention(RetentionPolicy.SOURCE)
  @interface MOVE_TYPE {
  }
}
