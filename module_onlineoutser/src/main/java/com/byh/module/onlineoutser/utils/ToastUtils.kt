package com.byh.module.onlineoutser.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {

  fun center(ctx: Context, content: String) {
    show(ctx, content, Gravity.CENTER)
  }


  fun show(ctx: Context, content: String, gravity: Int) {
    val toast = Toast.makeText(ctx, content, Toast.LENGTH_SHORT)
    toast.setGravity(gravity, 0, 0)
    toast.show()
  }


  fun showShort(mContext:Context,res: String) {
    Toast.makeText(mContext, res, Toast.LENGTH_LONG).show()
  }

}
