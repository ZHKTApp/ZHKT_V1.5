package com.screen

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.bright.course.R
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.http.UserInfoInstance
import com.bright.course.mqtt.MQTTHelper
import com.bright.course.utils.ToastGlobal
import com.holoview.smcscreenshare.ScreenShareLib
import kotlinx.android.synthetic.main.activity_screen_share.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.intentFor

/**
 * Created by kim on 2018/10/14.
 *
 */
class ScreenShareActivity : ScreenShareLib(), ScreenShareLib.OnScreenShareStateChange {

    companion object {
        fun launch(context: Context) {
            context.startActivity(context.intentFor<ScreenShareActivity>())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_screen_share)

        this.InitRtspServer(this)
        RtspAddress = GetIpAddress()
        val ipOnly = RtspAddress.replace("rtsp://", "").replace(":8554", "")
        EventBus.getDefault().post(EventMessage(BCMessage.MSG_PUBLISH_TEACHER, "{\"uid\":\"${UserInfoInstance.instance.userInfo.profile.ID}\", \"ip\":\"$ipOnly\"}"))

        tvInfo.text = "${tvInfo.text}\nIP:${RtspAddress}"

        StartScreenCapture()

    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventMessage) {
        when (event.msgType) {
            BCMessage.MSG_RECEIVED -> {
                ToastGlobal.showToast("已开启屏幕分享")
                MQTTHelper.processMessage(event.msgType, event.msg, this)
            }
        }
    }


    override fun OnStateChange(p0: ScreenShareState?) {
        p0?.let {
            Log.e(TAG, "OnStateChange\n" + p0);
            when (it) {
                ScreenShareState.ScreenShareState_Started -> {
                    finish()
                }

                else -> {

                }
            }
        }

    }

}