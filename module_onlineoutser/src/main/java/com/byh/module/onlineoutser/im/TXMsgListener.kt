package com.byh.module.onlineoutser.im

import android.annotation.SuppressLint
import android.util.Log
import com.byh.module.onlineoutser.db.*
import com.byh.module.onlineoutser.im.entity.HytMessage
import com.byh.module.onlineoutser.im.entity.RecAnswerSelfEvent
import com.byh.module.onlineoutser.im.video.CallMgr
import com.byh.module.onlineoutser.im.video.CallParser
import com.tencent.imsdk.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 数据处理
 */
object TXMsgListener : TIMMessageListener {

    const val TXTTAG = "TXMsgListener"

    override fun onNewMessages(list: List<TIMMessage>?): Boolean {
        list?.let {
            for (msg in it) {
                parse(msg)
                Log.i(TXTTAG, "tim==" + msg.toString())
            }
        }
        return false
    }


    //IM腾讯发来的消息进行转换成HytMessage处理
    @SuppressLint("CheckResult")
    fun parse(msg: TIMMessage) {
        val message = HytMessage(msg.msgId)

        val conversation = msg.conversation
        val isGroup = conversation.type == TIMConversationType.Group
        val sender = msg.sender
        val peer = conversation.peer//C2C类型为对方账号，Group类型为群聊账号
        val loginUser = IMManager.getUserId()
        message.sender = sender
        when {
            msg.isSelf -> message.receiver = peer
            isGroup -> message.receiver = loginUser
            else -> message.receiver = if (peer == sender) loginUser else peer
        }

        if (IMManager.getUserId() == message.sender) {
            message.direction = HytDirectionType.Send
        } else {
            message.direction = HytDirectionType.Receive
        }

        message.date = Date(msg.timestamp() * 1000)//聊天记录排序用

        if (loginUser == null) {
            return
        }

        if (msg.elementCount == 0)
            return

        for (i in 0 until msg.elementCount.toInt()) {
            val e = msg.getElement(i)
            e?.let {
                when (it) {
                    is TIMTextElem -> message.body = it.text
                    is TIMCustomElem -> {
                        //发起方 发来的视频会话邀请
                        if (CallMgr.isVideoMsg(it)) {
                            if (IMManager.getUserId() == message.sender) {
                                EventBus.getDefault().post(RecAnswerSelfEvent())
                                return
                            }
                            //数据转换并调起视频会话界面DialActivity
                            CallParser.parse(it, peer)
                        }
                    } else -> {
                        return
                    }
                }
            }
        }
    }
}
