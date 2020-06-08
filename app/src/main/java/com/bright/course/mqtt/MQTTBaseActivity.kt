package com.bright.course.mqtt

import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.bright.course.App
import com.bright.course.BaseActivity
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.utils.ToastGlobal
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by kim on 2018/7/26.
 *
 */
abstract class MQTTBaseActivity : BaseActivity() {
    var mqttClient: MqttAndroidClient? = null
    //    private val serverUri = "tcp://node3.yanxishe.cc:1883"

    lateinit var clientId: String
    private val subscriptionTopic = "room1"
    private val teacherTopic = "tcRoom1"
    var hasLostConnect: Boolean = false
    var mText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        getMQTTServerIPAndConnect()
    }

    override fun onResume() {
        super.onResume()
    }


    fun getMQTTServerIPAndConnect() {
        Log.e("mqtt", " isMQTTServer start")
        mqttClient?.let {
            if (it.isConnected) {
                it.disconnect()
            }
        }

        val ip = SPUtil.prefs.getString(Constant.TEACHER_IP_KEY, "")
//        val ip = "node3.yanxishe.cc"

        if (TextUtils.isEmpty(ip)) {
            return
        }

        val serverUri = "tcp://$ip:1883"
        clientId = "Android_${Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)}"

        mqttClient = MqttAndroidClient(App.instance.applicationContext, serverUri, clientId)
        mqttClient?.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                onMessageArrived(topic, message)
                EventBus.getDefault().post(EventMessage(BCMessage.MSG_RECEIVED, message.toString()))
            }

            override fun connectionLost(cause: Throwable?) {
                onConnectionLost()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                onDeliveryComplete()
            }
        })

        connectToServer()
    }


    fun reconnectToServer() {

        mqttClient?.let {
            if (it.isConnected) {
//                if (hasLostConnect) {
//                    hasLostConnect = false
                Log.e("MQTT", "reconnectToServer")
                subscribeToTopic(subscriptionTopic)
//                }
                return
            }
        }
        getMQTTServerIPAndConnect()
    }

    fun connectToServer() {
        //修改
        val mqttConnectOptions = MqttConnectOptions()
//        mqttConnectOptions.isAutomaticReconnect = true
//        mqttConnectOptions.keepAliveInterval = 30
//        mqttConnectOptions.connectionTimeout = 10
        mqttConnectOptions.isCleanSession = false
//        mqttConnectOptions.maxInflight = 50

        try {
            //addToHistory("Connecting to " + serverUri);
            mqttClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttClient?.setBufferOpts(disconnectedBufferOptions)

                    onConnectionSuccess()
                    subscribeToTopic(subscriptionTopic)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
//                        addToHistory("Failed to connect to: $serverUri")
                    onConnectionFailure()
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }


    fun subscribeToTopic(topic: String?) {
        if (null == mqttClient || !mqttClient!!.isConnected) {
            ToastGlobal.showToast("未成功连接服务器")
            return
        }

        try {
            mqttClient?.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    onSubscribeToTopicSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    onSubscribeToTopicOnFailure()
                }
            })

            // THIS DOES NOT WORK!
//            mqttClient.subscribe(subscriptionTopic, 0, IMqttMessageListener { topic, message ->
//                // message Arrived!
//                println("Message: " + topic + " : " + String(message.payload))
//            })

        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }

    }

    fun unSubscribeToTopic(topic: String?) {
        if (null == mqttClient || !mqttClient!!.isConnected) {
            ToastGlobal.showToast("未成功连接服务器")
            return
        }

        try {
            mqttClient?.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                }
            })


        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }

    }


    abstract fun onConnectionLost()
    abstract fun onConnectionSuccess()
    abstract fun onConnectionFailure()
    abstract fun onDeliveryComplete()
    abstract fun onMessageArrived(topic: String?, message: MqttMessage?)
    fun onSubscribeToTopicSuccess() {
        Log.d("MQTT", "onSubscribeToTopicSuccess")
    }

    fun onSubscribeToTopicOnFailure() {
        Log.d("MQTT", "onSubscribeToTopicOnFailure")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mqttClient) {
            mqttClient?.disconnect()
        }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventMessage) {
        when (event.msgType) {
            BCMessage.MSG_SUBSCRIPT -> {
                subscribeToTopic(event.msg)
            }
            BCMessage.MSG_UN_SUBSCRIPT -> {
            }
            BCMessage.MSG_PUBLISH_TEACHER -> {
                publishMessage("teacher", event.msg)
            }
            BCMessage.MSG_PUBLISH_QIANG_DA -> {
                publishMessage("QiangDa", event.msg)
            }
            BCMessage.MSG_CONNECT -> {
                getMQTTServerIPAndConnect()
            }
        }
    }

    fun publishMessage(type: String, msg: String) {

        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            mqttClient?.publish(type, message)
        } catch (e: MqttException) {
            System.err.println("Error Publishing: " + e.message)
            e.printStackTrace()
        }

    }
}