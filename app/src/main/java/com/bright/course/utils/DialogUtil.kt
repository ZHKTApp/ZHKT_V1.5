package com.bright.course.utils

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.TextView
import com.bright.course.R
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import org.jetbrains.anko.runOnUiThread
import java.util.*

/**
 * Created by on 2018/9/16.
 */
class DialogUtil {

    companion object {
        var dialog: Dialog? = null

        //弹出对话框
        fun initHiddenMainDialog(context: Context) {
            dialog = Dialog(context, R.style.dialog)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.alert_reply_prompt, null)
            val tvCancel = view.findViewById<TextView>(R.id.alert_tv_timer)
            dialog?.setContentView(view)
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.show()
            //计时
            setTimer(context, tvCancel)
        }

        /**
         * 设置timer倒计时
         */
        private fun setTimer(context: Context, tvCancel: TextView) {
            var num = 6  //时长
            val timer = Timer()
            val mTimerTask = object : TimerTask() {
                override fun run() {
                    --num
                    context.runOnUiThread {
                        if (num == 0) {
                            timer.cancel()
                            dialog?.dismiss()
                        }
                        tvCancel.text = num.toString() + "（s）后自动关闭..."
                        var firatValue: Boolean by SPUtil(Constant.FIRST_ENTER_MAIN_KEY, true)
                        firatValue = true
                    }
                }
            }

            timer.schedule(mTimerTask, 0, 1000)
        }

        /**
         * 获取一个耗时的对话框 ProgressDialog
         *
         * @param context
         * @param message
         * @return
         */
        fun getWaitDialog(context: Context, message: String): ProgressDialog {
            val waitDialog = ProgressDialog(context)
            if (!TextUtils.isEmpty(message)) {
                waitDialog.setMessage(message)
            }
            return waitDialog
        }
    }
}
