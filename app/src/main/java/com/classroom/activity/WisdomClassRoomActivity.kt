package com.classroom.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bright.course.App
import com.bright.course.BaseActivity
import com.bright.course.BaseEventBusActivity
import com.bright.course.R
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.http.UserInfoInstance
import com.bright.course.http.response.ResponseLogin
import com.bright.course.utils.DialogUtil
import com.bright.course.utils.FileUtils
import com.bright.course.utils.StreamUtils
import com.bright.course.utils.ToastGlobal
import com.bright.course.utils.ToastGlobal.customMsgToastShort
import com.classroom.constant.Constant
import com.classroom.mvp.presenter.LoginOutPresenter
import com.classroom.mvp.presenter.LoginPresenter
import com.cxz.wanandroid.utils.SPUtil
import com.rztop.classroom.classroom.mvp.contract.LoginContract
import com.rztop.classroom.classroom.mvp.contract.LoginOutContract
import kotlinx.android.synthetic.main.activity_classroom_login.*
import org.eclipse.paho.client.mqttv3.internal.CommsTokenStore
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.uiThread
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class WisdomClassRoomActivity : BaseEventBusActivity(), View.OnClickListener, LoginContract.View, LoginOutContract.View {
    var name by SPUtil(Constant.SYSTEM_USER_NAME_KEY, "")
    var pwd by SPUtil(Constant.SYSTEM_PASS_WORD_KEY, "")
    var teacherIp by SPUtil(Constant.TEACHER_IP_KEY, "")
    var teacherPort by SPUtil(Constant.TEACHER_PORT_KEY, "")
    var isFirst:Boolean = true
    companion object {
        fun launch(context: Context) {
            context.startActivity(context.intentFor<WisdomClassRoomActivity>())
        }
    }

    /**
     * 加载框
     */
    private val mDialog by lazy {
        DialogUtil.getWaitDialog(this, "处理中...")
    }

    private val mPresenter: LoginPresenter by lazy {
        LoginPresenter()
    }

    private val nPresenter: LoginOutPresenter by lazy {
        LoginOutPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_login)

        initNetWork()
        init()
    }

    private fun initNetWork() {
        mPresenter.attachView(this)
        nPresenter.attachView(this)
    }

    /**
     * 设置本地保存数据
     */
    private fun getEdtContent() {
        //修改
//        var name by SPUtil(Constant.STUDENT_ACCOUNT_KEY, "")
//        var pwd by SPUtil(Constant.STUDENT_PASSWORD_KEY, "")
//        var name by SPUtil(Constant.SYSTEM_USER_NAME_KEY, "")
//        var pwd by SPUtil(Constant.SYSTEM_PASS_WORD_KEY, "")
        //取本地(sp)教师端-ip数据
        val strTeacherIp: String by SPUtil(Constant.TEACHER_IP_KEY, "")
        //取本地(sp)教师端-端口数据
        val strTeacherPort: String by SPUtil(Constant.TEACHER_PORT_KEY, "")

        edtUName?.setText(name)
        edtPwd?.setText(pwd)
        edtUName?.setSelection(edtUName.text.toString()?.length)
        edtPwd?.setSelection(edtPwd.text.toString()?.length)
        edtTeacherIp?.setText(strTeacherIp)
        edtTeacherPort?.setText(strTeacherPort)
        edtTeacherIp?.setSelection(edtTeacherIp.text.toString()?.length)
        edtTeacherPort?.setSelection(edtTeacherPort.text.toString()?.length)
        if (isFirst){
            selfMotionLogin()
            isFirst=false
        }
    }

    override fun onResume() {
        super.onResume()
        getEdtContent()
    }

    private fun init() {
        tvBack.setOnClickListener(this)
        btnOut.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        btnSure.setOnClickListener(this)
        btnLogin.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
        tvQrClass.setOnClickListener(this)
        tvNetConfig.setOnClickListener(this)
    }

    /**
     * Login
     */
    private fun login() {
        if (validate()) {
            mPresenter.login(edtUName.text.toString(), edtPwd.text.toString())
        }
    }

    /**
     * LoginOut
     */
    private fun loginOut() {
//        if (validate()) {
//            nPresenter.loginout()
//        }
        finish()
    }

    //修改
    var file = File("/data/data/com.bright.course/databaes/mqttAndroidService.db-journal")

    override fun onClick(v: View?) {
        when (v?.id) {
            //网络配置
            R.id.tvNetConfig -> {
                setInvisible()
                cvNetConfig.visibility = View.VISIBLE
            }
            //关闭弹窗
            R.id.ivClose -> {
                setVisible()
                cvNetConfig.visibility = View.INVISIBLE
            }
            //扫码更新配置
            R.id.tvQrClass -> this.let { ScanQRCodeActivity.start(it) } // this.startActivity(this.intentFor<TestScanActivity>())
            //登录
            R.id.btnLogin -> {
                UserInfoInstance.instance.updateUserInfo(null)
                login()
                //修改
                file.deleteRecursively()
            }
            //退出
            R.id.btnOut -> loginOut()
            //配置取消
            R.id.btnCancel -> {
                setVisible()
                cvNetConfig.visibility = View.INVISIBLE
            }
            //配置确定
            R.id.btnSure -> setEdtContent()
            R.id.edtNewPwd -> setInvisible()
            R.id.tvTitle -> setVisible()
            //返回
            R.id.tvBack -> this.finish()
        }
    }

    var TAG = WisdomClassRoomActivity::class.java.simpleName

    override fun loginSuccess(data: ResponseLogin) {
        Log.e("login", "login success : " + data)
        UserInfoInstance.instance.updateUserInfo(data)
        var name by SPUtil(Constant.STUDENT_ACCOUNT_KEY, edtUName.text.toString())
        var pwd by SPUtil(Constant.STUDENT_PASSWORD_KEY, edtPwd.text.toString())
        name = edtUName.text.toString()
        pwd = edtPwd.text.toString()
        this?.let { WisdomInClassActivity.launch(it) }
    }

    override fun loginFail() {
        tvLoginError.visibility = View.VISIBLE
    }

    override fun loginOutSuccess(data: String) {
        UserInfoInstance.instance.updateUserInfo(null)
        customMsgToastShort(App.instance, "退出登录成功")
        this.finish()
    }

    override fun loginOutFail() {
        customMsgToastShort(App.instance, "退出登录失败")
    }

    override fun showLoading() {
        mDialog.show()
    }

    override fun hideLoading() {
        mDialog.dismiss()
    }

    override fun showError(errorMsg: String) {
        customMsgToastShort(App.instance, errorMsg)
    }

    /**
     * 保存输入框数据
     */
    private fun setEdtContent() {
        teacherIp = edtTeacherIp.text.toString()
        teacherPort = edtTeacherPort.text.toString()
        EventBus.getDefault().post(EventMessage(BCMessage.MSG_CONNECT, ""))
        setVisible()
        //关闭配置弹框
        cvNetConfig.visibility = View.INVISIBLE
    }

    /**
     * 设置显示
     */
    private fun setVisible() {
        edtPwd.visibility = View.VISIBLE
        edtUName.visibility = View.VISIBLE
    }

    /**
     * 设置隐藏
     */
    private fun setInvisible() {
        edtPwd.visibility = View.INVISIBLE
        edtUName.visibility = View.INVISIBLE
    }

    /**
     * Check UserName and PassWord
     */
    private fun validate(): Boolean {
        var valid = true
        val username: String = edtUName.text.toString()
        val password: String = edtPwd.text.toString()

        if (username.isEmpty()) {
            edtUName.error = "用户名不能为空"
            valid = false
        }
        if (password.isEmpty()) {
            edtPwd.error = "密码不能为空"
            valid = false
        }
        return valid

    }
    fun selfMotionLogin(){
        //登录账号密码 教师IP不为空自动登录
        if (!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(pwd)&&!TextUtils.isEmpty(teacherIp)){
            UserInfoInstance.instance.updateUserInfo(null)
            login()
            //修改
            file.deleteRecursively()
        }
    }
    fun downloadFile(url: String) {
        showLoadingDialog("正在下载文件请稍等")
        val fileName = url.substring(url.lastIndexOf("/") + 1, url.length)

        doAsync {
            var file: File? = null
            try {
                val inputStream = BufferedInputStream(URL(url).openStream(), StreamUtils.IO_BUFFER_SIZE)
//                val downloadPath = File(Environment.getExternalStorageDirectory(), "bc_downloads")
//                if (!downloadPath.exists()) downloadPath.mkdirs()

//                val inputStream = BufferedInputStream(URL("http://foooooot.com/media/upload/selected_route/%E6%A1%83%E8%8A%B1%E5%B2%9B.jpg").openStream(), IO_BUFFER_SIZE)
//                file = File(downloadPath, fileName)
                file = FileUtils.getFilePath(Constant.PACKAGE_NAME, fileName)
                val outputStream = FileOutputStream(file, false)
                val out = BufferedOutputStream(outputStream, StreamUtils.IO_BUFFER_SIZE)
                StreamUtils.copy(inputStream, out)
                out.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            uiThread {
                dismissLoadingDialog()
                if (file != null) {
                    ToastGlobal.showToast("下载成功$fileName")
                } else {
                    ToastGlobal.showToast("下载失败")
                }
            }
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    fun turnOnScreen() {
        // turn on screen
        val mPowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag")
        mWakeLock.acquire()
        mWakeLock.release()
    }
}