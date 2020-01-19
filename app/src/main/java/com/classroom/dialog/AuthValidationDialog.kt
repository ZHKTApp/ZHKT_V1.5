package com.classroom.dialog

import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.bright.course.App
import com.bright.course.R
import com.bright.course.utils.RegularUtils
import com.bright.course.utils.ToastGlobal.customMsgToastShort
import kotlinx.android.synthetic.main.dialog_auth_validation.*

/**
 * 身份验证
 *
 * Created by kim on 2018/7/15.
 *
 */
class AuthValidationDialog : DialogFragment() {

    var edtOldPwd: EditText? = null
    var edtNewPwd: EditText? = null

    private val countDownTimer: MyCountDownTimer by lazy {
        MyCountDownTimer(60000, 1000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_auth_validation, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //确定
        btnSure?.setOnClickListener {
            dismiss()
            initResetPwdDialog()
        }

        //new倒计时对象,总共的时间,每隔多少秒更新一次时间

        //设置Button点击事件触发倒计时
        tvGetVarCode?.setOnClickListener {
            val strPhone = edtPhone.text.toString().trim()
            val mPhone = RegularUtils.isMobile(strPhone)
            if (mPhone || !strPhone.isEmpty()) else customMsgToastShort(App.instance, getString(R.string.str_input_right_pwd))
            //发起请求
        }
    }

    //倒计时函数
    inner class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
            CountDownTimer(millisInFuture, countDownInterval) {

        //计时过程
        override fun onTick(l: Long) {
            //防止计时过程中重复点击
            tvGetVarCode?.isClickable = false
            tvGetVarCode?.isFocusable = false
            tvGetVarCode?.text = (l / 1000).toString() + "秒"
        }

        //计时完毕的方法
        override fun onFinish() {
            //重新给Button设置文字
            tvGetVarCode?.text = getString(R.string.str_reget_code)
            //设置可点击
            tvGetVarCode?.isClickable = true
            tvGetVarCode?.isFocusable = true
        }
    }

    override fun dismiss() {
        countDownTimer?.cancel()
        super.dismiss()
    }

    /**
     * 弹出重置对话框
     */
    private fun initResetPwdDialog() {
        val dialog = Dialog(activity, R.style.dialog)
        val inflater = LayoutInflater.from(activity)
        val view = inflater?.inflate(R.layout.change_pssword_dialog, null)
        dialog?.setContentView(view)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        edtOldPwd = view?.findViewById(R.id.edtOldPwd)
        edtNewPwd = view?.findViewById(R.id.edtNewPwd)
        val btnSave = view?.findViewById<Button>(R.id.btnSave)
        val tvTitleName = view?.findViewById<TextView>(R.id.tvTitleName)
        val btnCancelSave = view?.findViewById<Button>(R.id.btnCancelSave)
        val llOldPwd = view?.findViewById<LinearLayout>(R.id.llOldPwd)

        //设置标题
        tvTitleName?.text = getString(R.string.str_reset_pwd)
        //隐藏原密码一行
        llOldPwd?.visibility = View.GONE

        //取消
        btnCancelSave?.setOnClickListener {
            dialog?.dismiss()
        }
        //保存
        btnSave?.setOnClickListener {
            if (validate()) {

            }
        }
    }

    /**
     * Check old/new PassWord
     */
    private fun validate(): Boolean {
        var valid = true
        val edtOldPwd: String = edtOldPwd?.text.toString().trim()
        val edtNewPwd: String = edtNewPwd?.text.toString().trim()

        if (edtNewPwd != edtOldPwd) {
            customMsgToastShort(App.instance, "两次密码不一致，请重新设置")
            valid = false
        }
        return valid

    }
}