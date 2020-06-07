package com.screen

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.opengl.Visibility
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import com.bright.course.App
import com.bright.course.R
import com.bright.course.bean.EventMessage
import com.bright.course.http.UserInfoInstance
import com.bright.course.mqtt.MQTTMessageJSON
import com.bright.course.utils.EasySharePreference
import com.bright.course.utils.ToastGlobal
import com.bright.course.web.WebActivity
import com.classroom.mvp.model.bean.HandSupBean
import com.google.gson.Gson
import com.rztop.classroom.classroom.mvp.contract.HandSupContract
import com.rztop.classroom.presenter.HandSupPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.intentFor

class FloatingFinger : Service(), HandSupContract.View {

    private lateinit var frameLayout: LinearLayout
    private lateinit var mWindowManager: WindowManager
    private lateinit var browserButton: ImageButton
    private lateinit var answerButton: ImageButton
    private lateinit var firstRow: LinearLayout
    private var loadingDialog: ProgressDialog? = null
    var paramsF = WindowManager.LayoutParams()

    private val mPresenter: HandSupPresenter by lazy {
        HandSupPresenter()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)

        mPresenter.attachView(this)

        frameLayout = LinearLayout(this)
        val firstRow = LinearLayout(this)
        val secondRow = LinearLayout(this)
        firstRow.orientation = LinearLayout.HORIZONTAL
        secondRow.orientation = LinearLayout.HORIZONTAL
        frameLayout.orientation = LinearLayout.VERTICAL

        val handsUpButton = ImageButton(this)
        handsUpButton.setImageResource(R.drawable.icon_raise_hand)
        handsUpButton.setBackgroundColor(Color.TRANSPARENT)
        handsUpButton.tag = "handsUpButton"

        answerButton = ImageButton(this)
        answerButton.setImageResource(R.drawable.ic_exam_race)
        answerButton.setBackgroundColor(Color.TRANSPARENT)
        answerButton.visibility = View.GONE
        answerButton.tag = "answerButton"

        firstRow.addView(handsUpButton)
        firstRow.addView(answerButton)


        val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        param.gravity = Gravity.RIGHT

        browserButton = ImageButton(this)
        browserButton.setImageResource(R.drawable.ic_browser)
        browserButton.setBackgroundColor(Color.TRANSPARENT)
        browserButton.visibility = View.GONE
        browserButton.tag = "browserButton"
        firstRow.addView(browserButton)
        frameLayout.addView(firstRow)



        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        paramsF = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        paramsF.gravity = Gravity.TOP
        paramsF.x = 850
        paramsF.y = 220


        var touchListener = object : View.OnTouchListener {
            internal var paramsT: WindowManager.LayoutParams = paramsF
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = paramsF.x
                        initialY = paramsF.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d("actionUp", "${event.rawX - initialTouchX}")
                        if (null != v.tag && Math.abs(event.rawX - initialTouchX) < 4) {
                            val tag: String = v.tag as String
                            when (tag) {
                                "handsUpButton" -> {
                                    handsUp()
                                }
                                "browserButton" -> {
                                    startActivity(intentFor<WebActivity>())
//                                    var intent = Intent()
//                                    intent.setAction("android.intent.action.VIEW")
//                                    var content_url = Uri.parse("https://www.baidu.com")
//                                    intent.setData(content_url)
//                                    startActivity(intent)
                                }
                                "answerButton" -> {
                                    answerQuestion()
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        paramsF.x = initialX + (event.rawX - initialTouchX).toInt()
                        paramsF.y = initialY + (event.rawY - initialTouchY).toInt()
                        mWindowManager.updateViewLayout(frameLayout, paramsF)
                    }
                }
                return true
            }
        }
        try {
            handsUpButton.setOnTouchListener(touchListener)
            frameLayout.setOnTouchListener(touchListener)
            browserButton.setOnTouchListener(touchListener)
            answerButton.setOnTouchListener(touchListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun answerQuestion() {
        val userInfoStr = EasySharePreference.getPrefInstance(App.instance).getString(UserInfoInstance.KEY_FOR_USER_INFO, null)
        EventBus.getDefault().post(EventMessage("QiangDa", userInfoStr))
        answerButton.visibility = View.GONE
        ToastGlobal.showToast("已抢答")
    }

    fun handsUp() {
        val handsUpTime = EasySharePreference.getPrefInstance(this).getLong("handsUpTime", 0)
        val gapTime = System.currentTimeMillis() - handsUpTime
        if (gapTime < 1000 * 60 * 1) {
            ToastGlobal.showToast("1分钟之内只能举手一次")
        } else {
            mPresenter.handSup()//学生举手表示疑问
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventMessage) {
        val msgJson = Gson().fromJson(event.msg, MQTTMessageJSON::class.java)
        when (msgJson.data.status) {
            //开启网络
            "ConnectToExternalNetwork" -> {
                ToastGlobal.showToast("开启网络")
                browserButton.visibility = View.VISIBLE
            }
            //关闭网络
            "UnconnectToExternalNetwork" -> {
                ToastGlobal.showToast("关闭网络")
                browserButton.visibility = View.GONE
            }
            //开始抢答
            "StartScrambleForAnswer" -> {
                answerButton.visibility = View.VISIBLE
            }
            //开启学生演示的时候关闭抢答
            "PadCast" -> {
                answerButton.visibility = View.GONE
            }
        }

    }


    override fun handSupSuccess(data: HandSupBean) {
        ToastGlobal.showToast("举手成功")
        EasySharePreference.getEditorInstance(this).putLong("handsUpTime", System.currentTimeMillis()).apply()
    }

    override fun handSupFail() {
        ToastGlobal.showToast("举手失败")
    }

    override fun showLoading() {
        ToastGlobal.showToast("正在举手")
    }

    override fun hideLoading() {
    }

    override fun showError(errorMsg: String) {
        ToastGlobal.showToast(errorMsg)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
//            mWindowManager.removeView(frameLayout)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var isShow = intent?.getBooleanExtra("isShow", true)
        if (frameLayout != null)
            when (isShow) {
                true -> mWindowManager.addView(frameLayout, paramsF)
                false -> mWindowManager.removeView(frameLayout)
            }
        return super.onStartCommand(intent, flags, startId)
    }
}