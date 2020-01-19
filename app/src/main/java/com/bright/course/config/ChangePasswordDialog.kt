package com.bright.course.config

import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bright.course.App
import com.bright.course.R
import com.bright.course.utils.ToastGlobal.customMsgToastShort
import kotlinx.android.synthetic.main.change_pssword_dialog.*

/**
 * Created by roztop10 on 2018/9/11.
 */
class ChangePasswordDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.change_pssword_dialog, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnCancelSave.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            if (validate()) {

            }
        }
    }

    /**
     * Check old/new PassWord
     */
    private fun validate(): Boolean {
        var valid = true
        val edtOldPwd: String = edtOldPwd.text.toString()
        val edtNewPwd: String = edtNewPwd.text.toString()
        val edtSurePwd: String = edtSurePwd.text.toString()

        if (edtNewPwd.isEmpty() || edtSurePwd.isEmpty() || edtOldPwd.isEmpty()) {
            customMsgToastShort(App.instance, "原/新密码输入错误请重新输入！")
            valid = false
        }

        if (edtSurePwd != edtNewPwd) {
            customMsgToastShort(App.instance, "两次密码不一致，请重新设置")
            valid = false
        }
        return valid

    }
}