package com.bright.course.bean

/**
 * Created by kim on 2018/9/28.
 *
 */
interface BCMessage {
    companion object {
        val MSG_SUBSCRIPT: String = "subscript"
        val MSG_PUBLISH_TEACHER: String = "publish"
        val MSG_PUBLISH_QIANG_DA: String = "QiangDa"
        val MSG_UN_SUBSCRIPT: String = "un_subscript"
        val MSG_RECEIVED: String = "messageArrived"
        val MSG_CONNECT: String = "connectMQTT"
        val MSG_SCANQRCODE:String = "scanQRCode"
        val MSG_BIT_REGISTER:String = "register_bitanswer"
    }
}