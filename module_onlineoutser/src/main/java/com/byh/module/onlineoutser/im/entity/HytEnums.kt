package com.byh.module.onlineoutser.db

import com.byh.module.onlineoutser.R

enum class HytMessageType {
  System;//二维码
}


enum class HytDirectionType {
  Send,
  Receive
}

enum class HytSendStatusType {
  Success,
  Sending,
  Fail
}

enum class HytReadStatusType {
  None,
  Had
}

enum class Portrait(val resId: Int) {
  Default(R.drawable.ic_user_header),
}


enum class People(val resId: Int) {
  Female(R.drawable.ic_pation_girl),
  Male(R.drawable.ic_user_header),
}
