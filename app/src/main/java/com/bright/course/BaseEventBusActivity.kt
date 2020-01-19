package com.bright.course

import android.os.Bundle
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.mqtt.MQTTHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by kim on 2018/10/10.
 *
 */
open class BaseEventBusActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventMessage) {
        when (event.msgType) {
            BCMessage.MSG_RECEIVED -> {
                MQTTHelper.processMessage(event.msgType, event.msg, this)
            }
        }
    }

}