package com.bright.course.config

import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bitanswer.library.BitAnswer
import com.bright.course.App
import com.bright.course.R
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.utils.ToastGlobal
import com.classroom.activity.ScanQRCodeActivity
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import kotlinx.android.synthetic.main.activity_add_license.*
import kotlinx.android.synthetic.main.activity_config.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by kim on 2018/8/27.
 *
 */
class AddLicenseDialog : DialogFragment() {
    var bitAnswer: BitAnswer?=null
    var code by SPUtil(Constant.SYSTEM_POWER_CODE_KEY, "")
    var loginCode by SPUtil(Constant.AUTHORIZATION_CODE,"")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)

        return inflater.inflate(R.layout.activity_add_license, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this);
        etLicense.setText(code)
        etLicense.addTextChangedListener(mTextWatcher)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        btnScanQrCode.setOnClickListener {
            activity?.let { ScanQRCodeActivity.start(it) }
        }

        btnCancel.setOnClickListener {
            var code = etLicense.text.toString().trim()
            if (TextUtils.isEmpty(code)){
                ToastGlobal.customMsgToastShort(App.instance, "授权码无效，请输入授权码。")
            }else{
                bitAnswer = BitAnswer(activity)
                var loginMsg =  bitAnswer?.ysLogin(null, code, BitAnswer.LoginMode.AUTO)
                Log.e("TAG","loginMsg : "+loginMsg+ "code : " +code)
                if (loginMsg.equals("执行成功")){
                    ToastGlobal.showToast("授权码可以使用，请点击注册")
                }else{
                    ToastGlobal.showToast(loginMsg)
                }
            }
        }

        btnRegister.setOnClickListener {
            //修改
            bitAnswer = BitAnswer(activity)
            var code = etLicense.text.toString().trim()
            Log.e("tag","code : "+ code)
            if (!TextUtils.isEmpty(code)){
            var loginMsg =  bitAnswer?.ysLogin(null, code, BitAnswer.LoginMode.AUTO)
                if (loginMsg.equals("执行成功")){
                    EventBus.getDefault().post(EventMessage(BCMessage.MSG_SCANQRCODE, code))
                    loginCode = loginMsg!!
                    ToastGlobal.customMsgToastShort(App.instance, "注册成功。")
                    dismiss()
                }else{
                    Log.e("TAG","loginMsg : "+loginMsg)
                    ToastGlobal.showToast(loginMsg)
                }
            }else{
                ToastGlobal.showToast("授权码不可为空，请输入授权码")
            }
        }
    }

    private val mTextWatcher = object : TextWatcher {
        var beforeLength: Int = 0

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            beforeLength = s.length
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        //一般我们都是在这个里面进行我们文本框的输入的判断，上面两个方法用到的很少
        override fun afterTextChanged(s: Editable) {
            editLengthChange(beforeLength, s)
        }
    }

    /**
     * 每隔四位插入一个“-”
     */
    private fun editLengthChange(beforeLength: Int, s: Editable) {
        val length = s.toString().length
        val b = s.toString().endsWith("-")
        if (beforeLength < length) {//判断输入状态
            if (length == 4 || length == 9 || length == 14) {
                etLicense.setText(StringBuffer(s).insert(length, "-").toString())
            } else if (length == 5 || length == 10 || length == 15) { //另一种情况，手动删除空格再次输入后
                if (!b) etLicense.setText(StringBuffer(s).insert(length - 1, "-").toString())
            }
        } else { //删除状态
            if (b) etLicense.setText(StringBuffer(s).delete(length - 1, length).toString())
        }
        //设置指针选中位置
        etLicense.setSelection(etLicense.text.toString().length)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvenMsg(msg: EventMessage){
        etLicense.setText(msg.msg)
    }
    override fun onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView()
    }
}