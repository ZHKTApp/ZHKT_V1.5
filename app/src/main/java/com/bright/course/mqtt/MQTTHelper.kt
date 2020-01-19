package com.bright.course.mqtt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.Display
import com.bright.course.MainActivity
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.filemanage.LearnBySelfActivity
import com.bright.course.http.UserInfoInstance
import com.bright.course.utils.CommandUtils
import com.bright.course.utils.ToastGlobal
import com.bright.course.video.VideoActivity
import com.classroom.activity.WisdomClassRoomActivity
import com.classroom.activity.WisdomInClassActivity
import com.exam.ExamMainActivity
import com.exam.QuizActivity
import com.google.gson.Gson
import com.holoview.smcscreenshare.ScreenShareMgr
import com.screen.ScreenShareActivity
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.intentFor
import java.io.File
import java.util.*


/**
 * Created by kim on 2018/9/28.
 *
 */
class MQTTHelper {

    companion object {

        val MIN_OPE_CLOAS_SCREEN = 2000
        var lastScreenTime: Long = 0
        fun subscribeToTopic(topic: String) {
            EventBus.getDefault().post(EventMessage(BCMessage.MSG_SUBSCRIPT, topic))
        }

        fun unSubscribeToTopic(topic: String) {
            EventBus.getDefault().post(EventMessage(BCMessage.MSG_UN_SUBSCRIPT, topic))
        }

        fun processMessage(topic: String?, msg: String?, activity: Activity) {
            if (topic == null || msg == null) {
                return
            }
            if (UserInfoInstance.instance.isGuestUser) {
                WisdomClassRoomActivity.launch(activity)
                return
            }
            val currentTime = Calendar.getInstance().getTimeInMillis()
            val msgJson = Gson().fromJson(msg, MQTTMessageJSON::class.java)
            when (msgJson.data.status) {
                //开启互动
                "StartInteraction" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("开启互动")
                }
                //关闭互动
                "EndInteraction" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("关闭互动")
                }
                //教师开始讲评
                "StartAComment" -> {
//                    ToastGlobal.showToast("教师开始讲评")
                    ExamMainActivity.launch(activity, msgJson.data.TextNumber)
                }
                //教师结束讲评
                "EndAComment" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("教师结束讲评")
                    if (activity is ExamMainActivity) {
                        activity.finish()
                    }
                }
                //开启屏幕投影
                "StartScreenProjection" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("开启屏幕投影")
                    activity.startActivity(activity.intentFor<VideoActivity>())
                }
                //关闭屏幕投影
                "EndScreenProjection" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("关闭屏幕投影")
                    if (activity is VideoActivity) {
                        activity.finish()
                    }
                }
                //开启视频投影
                "StartVideoProjection" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("开启视频投影")
                    activity.startActivity(activity.intentFor<VideoActivity>())
                }
                //关闭视频投影
                "EndVideoProjection" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("关闭视频投影")
                    if (activity is VideoActivity) {
                        activity.finish()
                    }
                }

                //开始课堂提问
                "StartClassroomQuestioning" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("开始课堂提问")
                    if (activity is QuizActivity) {
                        activity.finish()
                    }
                    QuizActivity.launch(activity, msgJson.data.ques_id)
//                    ClassQuestionActivity.launch(activity, msgJson.data.ques_id)

                }
                //结束课堂提问
                "EndClassroomQuestioning" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("结束课堂提问")
                    if (activity is QuizActivity) {
                        activity.finish()
                    }
                }
                //准备开始课堂测验
                "PrepareForTheTest" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("准备开始课堂测验")
                    if (activity is ExamMainActivity) {
                        activity.finish()
                    }
                    ExamMainActivity.launch(activity)
                }
                //取消课堂测验
                "CancelTheTest" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("取消课堂测验")
                    if (activity is ExamMainActivity) {
                        activity.finish()
                    }
                }
                //拉取课堂测验试卷
                "GetTextPaper" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
                    ToastGlobal.showToast("拉取课堂测验试卷")
                    if (activity is ExamMainActivity) {
                        activity.prepareForExam(msgJson.data.TextNumber)
                    }
                }
                //开始课堂测验
                "StartClassTests" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("开始课堂测验")
                    if (activity is ExamMainActivity) {
                        activity.startExam(msgJson.data.time.toInt())
                    }
                }
                // 结束课堂测验
                "EndClassTests" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("结束课堂测验")
                    if (activity is ExamMainActivity) {
                        activity.endExam()
                        //修改结束课堂测验逻辑 PC发送消息会变更
//                        activity.finish()
                    }
                }
//                //开始抢答
//                "StartScrambleForAnswer" -> {
//                    ToastGlobal.showToast("开始抢答")
//                }
                //开启自主学习
                "StartAutonomousLearning" -> {
//                    LearnBySelfActivity.launch(activity)
                    var intent = Intent()
                    intent.setClassName("com.zwyl.myfile", "com.zwyl.myfile.main.filebrower.FileBrowerApp")
                    activity.startActivity(intent)
//                    ToastGlobal.showToast("开启自主学习")
                }
                //关闭自主学习
                "EndAutonomousLearning" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("关闭自主学习")
                    if (activity is LearnBySelfActivity) {
                        activity.finish()
                    }
                }
                //开启Pad屏幕
                "OpenPadScreen" -> {
//                    ToastGlobal.showToast("开启Pad屏幕")
//                    if (activity is MainActivity) {
                    if (activity is WisdomClassRoomActivity) {
                        activity.turnOnScreen()
                        CommandUtils.exec(82)
                    }
                }
                //关闭Pad屏幕
                "ClosePadScreen" -> {
//                    ToastGlobal.showToast("关闭Pad屏幕")
//                        if (isScreenOn(activity)) {
                    if (currentTime - lastScreenTime > MIN_OPE_CLOAS_SCREEN) {
                        lastScreenTime = currentTime
                        turnOffScreenWithCommand()
                    }
//                    turnOffScreen(activity)
                }
//                //开启网络
//                "ConnectToExternalNetwork" -> {
//                    WebActivity.launch(activity)
//                    ToastGlobal.showToast("开启网络")
//                }
//                //关闭网络
//                "UnconnectToExternalNetwork" -> {
//                    ToastGlobal.showToast("关闭网络")
//                    if (activity is WebActivity) {
//                        activity.finish()
//                    }
//                }
                //开启学生演示
                "PadCast" -> {
                    val ids = msgJson.data.ids
                    //比对ids和学生ID

                    val userId = UserInfoInstance.instance.userInfo.profile.ID
                    if (ids.isNotEmpty() && ids.split(",").contains(userId)) {
//                        ToastGlobal.showToast("开启屏幕投影")
                        ScreenShareActivity.launch(activity)
                    } else {
//                        ToastGlobal.showToast("开启学生演示")
                    }
                    //然后开启反投屏
                }
                //关闭学生演示
                "ClosePadCast" -> {
//                    ToastGlobal.showToast("关闭学生演示")
                    if (activity is ScreenShareActivity) {
                        activity.finish()
                    }
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
                }
                //下载文件
                "sendFileToPad" -> {
                    //修改* 8U*
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
                    val targetstudents = msgJson.data.targetstudents
                    val userId = UserInfoInstance.instance.userInfo.profile.ID
                    if (targetstudents.isNotEmpty() && targetstudents.contains(userId)) {
                        if (activity is WisdomClassRoomActivity) {
                            ToastGlobal.showToast("正在接收文件${msgJson.data.url}")
                            activity.downloadFile(msgJson.data.url)
                        }
                    }
                }
                "PCLogout" -> {
                    //修改
                    ScreenShareMgr.GetInstance().StopScreenCapture(activity)
//                    ToastGlobal.showToast("退出登录")
//                    if (activity is MainActivity) {
                    if (activity is WisdomInClassActivity) {
                        activity.logout()
                    } else {
                        activity.finish()
                    }
                    //修改
                    file.deleteRecursively()
                }
            }
        }

        //修改
        var file = File("/data/data/com.bright.course/databaes/mqttAndroidService.db-journal")

        private fun turnOffScreenWithCommand() {
            CommandUtils.exec(26)
        }

        private fun isScreenOn(activity: Activity): Boolean {
            val dm = activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            for (display in dm.displays) {
                if (display.state != Display.STATE_OFF) {
                    return true
                }
            }
            return false
        }

        private fun turnOffScreen(activity: Activity) {
            try {
                val c = Class.forName("android.os.PowerManager")
                val mPowerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager

                for (m in c.declaredMethods) {
                    if (m.name.equals("goToSleep")) {
                        m.isAccessible = true
                        if (m.parameterTypes.size == 1) {
                            m.invoke(mPowerManager, SystemClock.uptimeMillis() - 2)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}