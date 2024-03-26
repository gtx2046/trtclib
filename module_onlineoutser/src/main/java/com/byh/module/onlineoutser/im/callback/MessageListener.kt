package com.byh.module.onlineoutser.im.callback

import com.byh.module.onlineoutser.im.entity.HytMessage

interface MessageListener {
  fun onNewMessage(message: HytMessage)
}
