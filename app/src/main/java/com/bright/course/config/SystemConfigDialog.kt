package com.bright.course.config

import android.content.Context
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
import android.widget.CompoundButton
import com.bright.course.App
import com.bright.course.BaseDialogFragment
import com.bright.course.R
import com.bright.course.http.APICallback
import com.bright.course.http.response.ResponseRelevanceLogin
import com.bright.course.utils.CleanCatcheUtils
import com.bright.course.utils.DialogUtil
import com.bright.course.utils.ToastGlobal
import com.bright.course.utils.ToastGlobal.customMsgToastShort
import com.classroom.activity.ScanQRCodeActivity
import com.classroom.constant.Constant
import com.classroom.dialog.AuthValidationDialog
import com.classroom.http.RelevanceRetrofitHelper
import com.cxz.wanandroid.utils.SPUtil
import com.bitanswer.library.BitAnswer
import com.bright.course.bean.EventMessage
import kotlinx.android.synthetic.main.activity_config.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.getStackTraceString
import retrofit2.Call


/**
 * Created by kim on 2018/7/15.
 *
 */
class SystemConfigDialog : BaseDialogFragment(), View.OnClickListener {

    private var TAG = SystemConfigDialog::class.java.simpleName
    var mOnGetDataListener: onGetDataListener? = null
    var studentName by SPUtil(Constant.STU_NO, "")
    var bitAnswer: BitAnswer?=null
    var loginCode by SPUtil(Constant.AUTHORIZATION_CODE,"")
    var service_ip:String by SPUtil(Constant.SERVICE_IP,"")

    //修改
    private fun login(url:String) {
        val call = RelevanceRetrofitHelper.create(url).relevanceLogin(edtUserName.text.toString(), edtPassWord.text.toString())
        call.enqueue(object : APICallback<ResponseRelevanceLogin>() {
            override fun onSuccess(response: ResponseRelevanceLogin?) {
                Log.e(TAG, "token : " + response?.resultInfo?.token + " studentName : " + response?.resultInfo?.studentName)
                if (response != null) {
                    if (TextUtils.isEmpty(response?.resultInfo?.token)) {
                        ToastGlobal.showToast(response.resultMsg)
                        service_ip=""
                    } else {
                        var relevanceToken by SPUtil(Constant.REL_TOKEN, response.resultInfo.token)
                        relevanceToken = response?.resultInfo.token
                        var relevanceStuId by SPUtil(Constant.REL_STUID, response.resultInfo.cmStudentId)
                        relevanceStuId = response?.resultInfo.cmStudentId
                        studentName = response?.resultInfo.studentName
                        service_ip=url
                        Log.e(TAG, "token systemconfig : $relevanceStuId service_ip : $service_ip")
                        customMsgToastShort(App.instance, "保存成功。")
                        dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseRelevanceLogin>, t: Throwable?) {
                super.onFailure(call, t)
                Log.e("tag","error : ${t?.getStackTraceString()}")
                service_ip=""
            }
            override fun onFinish(msg: String) {
                mDialog.dismiss()
            }
        })
    }


    private val mDialog by lazy {
        DialogUtil.getWaitDialog(activity, "处理中...")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)

        return inflater.inflate(R.layout.activity_config, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this);
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        getEdtContent()
        initListener()
    }

    override fun onCurrentAttach(mContext: Context?) {
        super.onCurrentAttach(mContext)
        mOnGetDataListener = mContext as onGetDataListener
    }

    override fun onDestroy() {
        if (mOnGetDataListener != null) {
            mOnGetDataListener!!.onDataChanged(studentName)
        }
        super.onDestroy()

    }

    private fun initListener() {
        btnSave.setOnClickListener(this)
        btnCancels.setOnClickListener(this)
        tvQrConfig.setOnClickListener(this)
        tvChangePwd.setOnClickListener(this)
        llForgetPwd.setOnClickListener(this)
        tvClearCache.setOnClickListener(this)
        btnAddLicense.setOnClickListener(this)
        edtPowerCode.addTextChangedListener(mTextWatcher)
        swhNetwork.setOnCheckedChangeListener(mOnCheckedChange)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            //添加授权
            R.id.btnAddLicense -> AddLicenseDialog().show(childFragmentManager, "addLicenseDialog")
            //关闭弹窗
            R.id.btnCancels -> dismiss()
            //修改密码
            R.id.tvChangePwd -> ChangePasswordDialog().show(childFragmentManager, "changePasswordDialog")
            //扫码
            R.id.tvQrConfig -> activity?.let { ScanQRCodeActivity.start(it) }
            //忘记密码
            R.id.llForgetPwd -> AuthValidationDialog().show(childFragmentManager, "forgetPasswordDialog")
            //清理垃圾文件
            R.id.tvClearCache -> clearCache()
            //保存
            R.id.btnSave -> setEdtContent()
        }
    }

    /**
     * 保存输入框数据
     */
    private fun setEdtContent() {
        //修改
        var ip by SPUtil(Constant.SYSTEM_SERVER_IP_KEY, edtIpAddress.text.toString().trim())
        var name by SPUtil(Constant.SYSTEM_USER_NAME_KEY, edtUserName.text.toString().trim())
        var pwd by SPUtil(Constant.SYSTEM_PASS_WORD_KEY, edtPassWord.text.toString().trim())
//        var code by SPUtil(Constant.SYSTEM_POWER_CODE_KEY, "")
        ip = edtIpAddress.text.toString()
        name = edtUserName.text.toString()
        pwd = edtPassWord.text.toString()
//        code = edtPowerCode.text.toString()

        //修改
        mDialog.show()
        bitAnswer = BitAnswer(activity)
        var loginMsg =  bitAnswer?.ysLogin(null, edtPowerCode.text.toString().trim(), BitAnswer.LoginMode.AUTO)
        Log.e("tag","CODE ： "+  edtPowerCode.text.toString().trim())
        if (loginMsg.equals("执行成功")){
            loginCode = loginMsg!!
            login(ip)
        }else{
            Log.e("TAG","loginMsg : "+loginMsg)
            ToastGlobal.showToast(loginMsg)
        }
//        login()
//        customMsgToastShort(App.instance, "保存成功。")

    }

    /**
     * 清理垃圾
     */
    private fun clearCache() {
        val cacheSize = CleanCatcheUtils.getCacheSize(activity)
        if (cacheSize == "0 B") {
            customMsgToastShort(App.instance, "没有垃圾文件需要清理。")
        } else {
            customMsgToastShort(App.instance, "正在清理垃圾文件。 $cacheSize…")
            CleanCatcheUtils.clear(activity)

            tvClearCache.postDelayed({
                customMsgToastShort(App.instance, "清理完毕。")
            }, 2018)
        }
    }

    private val mOnCheckedChange = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        if (isChecked) {
            //打开时 do some thing
            customMsgToastShort(App.instance, "ON")
        } else {
            //关闭时 do some thing
            customMsgToastShort(App.instance, "OFF")
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
                edtPowerCode.setText(StringBuffer(s).insert(length, "-").toString())
            } else if (length == 5 || length == 10 || length == 15) { //另一种情况，手动删除空格再次输入后
                if (!b) edtPowerCode.setText(StringBuffer(s).insert(length - 1, "-").toString())
            }
        } else { //删除状态
            if (b) edtPowerCode.setText(StringBuffer(s).delete(length - 1, length).toString())
        }
        //设置指针选中位置
        edtPowerCode.setSelection(edtPowerCode.text.toString().length)
    }

    /**
     * 设置本地保存数据
     */
    private fun getEdtContent() {
        //取本地(sp)服务器-ip数据
        val strServerIp: String by SPUtil(Constant.SYSTEM_SERVER_IP_KEY, "")
        //取本地(sp)用户名数据
        val strUserName: String by SPUtil(Constant.SYSTEM_USER_NAME_KEY, "")
        //取本地(sp)密码数据
        val strPassWord: String by SPUtil(Constant.SYSTEM_PASS_WORD_KEY, "")
        //取本地(sp)授权码数据
        val strPowerCode: String by SPUtil(Constant.SYSTEM_POWER_CODE_KEY, "")

        if (TextUtils.isEmpty(strPowerCode)){
            edtPowerCode.setText(strPowerCode)
        }else{
            edtPowerCode.setTag("请输入授权码")
        }
        edtIpAddress?.setText(strServerIp)
        edtUserName?.setText(strUserName)
        edtPassWord?.setText(strPassWord)
        edtPowerCode?.setText(strPowerCode)
        edtIpAddress?.setSelection(edtIpAddress.text.toString()?.length)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvenMsg(msg: EventMessage){
        edtPowerCode.setText(msg.msg)
    }
    public interface onGetDataListener {
        fun onDataChanged(studenName: String)
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView()
    }
}