package com.exam

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bright.course.BaseEventBusActivity
import com.bright.course.R
import com.bright.course.http.APICallback
import com.bright.course.http.APIService
import com.bright.course.http.UserInfoInstance
import com.bright.course.http.response.ExamCardData
import com.bright.course.http.response.ResponseDataT
import com.bright.course.http.response.ResponseExam
import com.bright.course.http.response.ResponseExamCard
import com.bright.course.utils.StreamUtils
import com.bright.course.utils.StreamUtils.IO_BUFFER_SIZE
import com.bright.course.utils.ToastGlobal
import com.bright.course.utils.Utils
import com.bright.course.views.GlideSimpleLoader
import com.bright.course.views.ImageWatcherHelper
import com.bumptech.glide.Glide
import com.classroom.activity.WisdomInClassActivity
import com.google.gson.Gson
import com.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_exam.*
import kotlinx.android.synthetic.main.exam_template_filling.*
import me.panavtec.drawableview.DrawableViewConfig
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import retrofit2.Call
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.util.ArrayList
import java.util.concurrent.Executors


/**
 * Created by kim on 2018/9/10.
 *
 */
class ExamMainActivity : BaseEventBusActivity(), View.OnClickListener {

    private var subjectiveFragment: Fragment? = null
    private lateinit var examCode: String
    private lateinit var examInfo: ResponseExam
    lateinit var examCard: ResponseExamCard
    private var isPreparedExamCard: Boolean = false
    private var timeLeft: Int = 0
    private var timeCountDownTimer: CountDownTimer? = null
    lateinit var paintConfig: DrawableViewConfig

    private val wrongColor: Int = Color.RED
    private val rightColor: Int = Color.GREEN

    private val TAG = ExamMainActivity::class.java.simpleName

    private val NUMBER_UNIT = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")

    companion object {
        fun launch(context: Context) {
            context.startActivity(context.intentFor<ExamMainActivity>())
        }

        //老师讲评
        fun launch(context: Context, examCode: String) {
            context.startActivity(context.intentFor<ExamMainActivity>("isTeacherGuide" to true, "examCode" to examCode))
        }

    }

    private var isShowSubjective: Boolean = false
    private var isTeacherGuide: Boolean = false //是否为老师讲评

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)
        UserInfoInstance.instance.userInfo?.let {
            tvStudentInfo.text = "姓名：${it.profile.Name} 学号：${it.profile.Code}"
        }
        val isTranslucentStatus = false
        iwHelper = ImageWatcherHelper.with(this, GlideSimpleLoader()) // 一般来讲， ImageWatcher 需要占据全屏的位置
                .setTranslucentStatus(if (!isTranslucentStatus) Utils.calcStatusBarHeight(this) else 0) // 如果不是透明状态栏，你需要给ImageWatcher标记 一个偏移值，以修正点击ImageView查看的启动动画的Y轴起点的不正确

        paintConfig = DrawableViewConfig()
        paintConfig.strokeColor = resources.getColor(android.R.color.black)
        paintConfig.isShowCanvasBounds = true
        paintConfig.strokeWidth = DrawableViewConfig.DEFAULT_LINE_SIZE
        paintConfig.minZoom = 1.0f
        paintConfig.maxZoom = 2.0f
        paintConfig.canvasHeight = resources.displayMetrics.heightPixels
        paintConfig.canvasWidth = resources.displayMetrics.widthPixels



        initListener()

        if (null != intent && null != intent.extras && !intent.extras.isEmpty) {
            isTeacherGuide = intent.getBooleanExtra("isTeacherGuide", false)
            examCode = intent.getStringExtra("examCode")
            prepareForExam(examCode)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            //恢复到实初始状态
            tvExamStatus.text = "等待接收试卷…"
            isPreparedExamCard = false
            frameWaitForPaper.visibility = View.VISIBLE
            tvExamTitleStatus.visibility = View.GONE
        }
    }

    /**
     * 准备试卷
     * 1.获取试卷图片
     * 2.下载图片
     * 3.获取答题卡
     * 4.通知获取完成
     */
    public fun prepareForExam(examCode: String) {
        this.examCode = examCode
        tvExamStatus.text = "正在获取试卷内容"

        val call = APIService.create().fetchExam(examCode)
        call.enqueue(object : APICallback<ResponseDataT<ResponseExam>>() {
            override fun onSuccess(response: ResponseDataT<ResponseExam>?) {
                if (null == response?.data) {
                    ToastGlobal.showToast("获取考试信息失败，请联系老师")
                    return
                }
                examInfo = response.data!!
                val imgPath = response.data!!.path

                downloadImage(imgPath)
            }

            override fun onFinish(msg: String) {
            }

        })
        addCallQueue(call)
    }

    /**
     * 下载试卷
     */
    private fun downloadImage(imgPath: String?) {
        tvExamStatus.text = "正在下载试卷…"
        val fileName = imgPath!!.substring(imgPath!!.lastIndexOf("/") + 1, imgPath.length)

        doAsync {
            var file: File? = null
            try {
                val inputStream = BufferedInputStream(URL(imgPath).openStream(), IO_BUFFER_SIZE)
                val downloadPath = File(Environment.getExternalStorageDirectory(), "bc_downloads")
                if (!downloadPath.exists()) downloadPath.mkdirs()
                file = File(downloadPath, fileName)

//                val inputStream = BufferedInputStream(URL("http://foooooot.com/media/upload/selected_route/%E6%A1%83%E8%8A%B1%E5%B2%9B.jpg").openStream(), IO_BUFFER_SIZE)
//                file = File(cacheDir, "examPaper_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file, false)
                val out = BufferedOutputStream(outputStream, IO_BUFFER_SIZE)
                StreamUtils.copy(inputStream, out)
                out.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (null != file) {
                val bitmapHeight = ImageUtils.getBitmapHeight(file.absolutePath)
                var height = resources.displayMetrics.heightPixels
                val width = resources.displayMetrics.widthPixels
                height = if (bitmapHeight > height) bitmapHeight else height
                val bitmap = ImageUtils.decodeFile(file.absolutePath, width, height)
                var drawable = BitmapDrawable(resources, bitmap)
//                ivSubject.setClearModel(true)
//                ivSubject.setBitmap(bitmap)
//                ivSubject.setConfig(paintConfig)
//                ivSubject.setJustBitmapModel(true)
                //修改 以中心点手势放大
                uiThread {
                    pvSubject.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
                    pvSubject.setImageDrawable(drawable)
                    pvSubject.invalidate()
                }
            }
            uiThread {
                //                pvSubject.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
//                pvSubject.setImageURI(Uri.fromFile(file))
//                pvSubject.invalidate()
                if (file != null) {
//                    Glide.with(this@ExamMainActivity).load(file).into(ivSubject)
                    tvExamStatus.text = "正在下载答题卡…"
                    fetchExamCard()
                } else {
                    tvExamStatus.text = "下载答题卡失败\n$imgPath"
                }
            }
        }
    }

    /**
     * 获取试卷以及答题卡完毕
     */
    fun fetchExamCardDone() {
        val call = APIService.create().fetchExamDone(examCode)
        call.enqueue(object : APICallback<ResponseDataT<Any?>>() {
            override fun onSuccess(response: ResponseDataT<Any?>?) {
                // show the examView
                isPreparedExamCard = true
                tvExamStatus.text = "获取试卷完成，请等待老师开始"

                //显示卷子，但不允计答题
                frameWaitForPaper.visibility = View.GONE
                tvExamTitleStatus.visibility = View.VISIBLE
                disableViews(llBottom)

                if (isTeacherGuide) {
                    startExam(0)
                }
            }

            override fun onFinish(msg: String) {
            }

        })
        addCallQueue(call)
    }

    /**
     * 获取答题卡
     */
    private fun fetchExamCard() {
        val call = APIService.create().fetchExamCard(examInfo.code, if (isTeacherGuide) "1" else "0")

        call.enqueue(object : APICallback<ResponseDataT<ResponseExamCard>>() {
            override fun onSuccess(response: ResponseDataT<ResponseExamCard>?) {
                // startExam()
                isSubmit = false
                renderExamCard(response)
                fetchExamCardDone()
            }

            override fun onFinish(msg: String) {

            }

        })
    }

    /**
     * 渲染答题卡
     */
    @SuppressLint("SetTextI18n")
    private fun renderExamCard(response: ResponseDataT<ResponseExamCard>?) {
        if (null == response?.data) {
            ToastGlobal.showToast("答题卡为空！请联系老师")
            return
        }
        examCard = response.data!!
        renderExamCardView()
    }

    private fun getAnswerPosition(answer: String): Int {
        val array = listOf("A", "B", "C", "D", "E", "F")
        return array.indexOf(answer)
    }

    @SuppressLint("SetTextI18n")
    fun renderExamCardView() {
        llExamBody.removeAllViews()
        var titleNumber = 0
        //遍历data
        //修改
        Log.e(TAG, "examCard : ${examCard.data}")
        for ((i, cardData) in examCard.data.withIndex()) {
            when (cardData.type) {
                "单选题" -> {
                    titleNumber = renderExamCardTitleAndIncreasePosition(titleNumber, cardData)
                    renderSingleChoseView(cardData)
                }
                "多选题" -> {
                    titleNumber = renderExamCardTitleAndIncreasePosition(titleNumber, cardData)
                    renderMultipleChoseView(cardData)
                }
                "判断题" -> {
                    titleNumber = renderExamCardTitleAndIncreasePosition(titleNumber, cardData)
                    renderTrueOrFalseView(cardData)
                }
                "简答题", "填空题", "计算题" -> {
                    titleNumber = renderExamCardTitleAndIncreasePosition(titleNumber, cardData)
                    renderSimpleAnswer(cardData, i)
                }
            }
        }
    }

    private fun renderExamCardTitleAndIncreasePosition(titleNumber: Int, cardData: ExamCardData): Int {
        var titleNumber1 = titleNumber
        addTitleIntoCard("${NUMBER_UNIT[titleNumber1]}、${cardData.type}")
        titleNumber1++
        return titleNumber1
    }

    var isSubmit: Boolean = false
    private var iwHelper: ImageWatcherHelper? = null

    private fun renderSimpleAnswer(cardData: ExamCardData, i: Int) {
        Log.e("examCard", "renderSimpleAnswer : " + cardData + " i : " + i)
        for (position in 0 until cardData.num) {
            val view = LayoutInflater.from(this@ExamMainActivity).inflate(R.layout.exam_template_filling, null)
            val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
            tvPosition.text = "${position + 1}、"

            val ivFilling = view.findViewById<ImageView>(R.id.ivFilling)
            if (isSubmit) {
                ivFilling.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val longPictureList = ArrayList<String>()
                        if (!TextUtils.isEmpty(cardData.answer[position].user_answer)) {
                            longPictureList.add(cardData.answer[position].user_answer)
                        }
                        val mappingViews = SparseArray<ImageView>()
                        mappingViews.put(0, v as ImageView?)
                        iwHelper?.show(v, mappingViews, Utils.convertList(longPictureList))
                    }
                })
            } else {
                ivFilling.setOnClickListener {
                    Log.e("examCard", "点击的当前题目位置 : " + i + " 点击的题目小题位置 : " + position)
                    showSubjectiveView(i, position)
                    if (llBottom.layoutParams.height != 0) {
                        llBottom.layoutParams.height = 0
                        btnHideOrShowAnswer.text = "打开"
                        btnHideOrShowAnswer.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exam_answer_show, 0, 0, 0)
                    }
                }
            }
            if (cardData.answer.isNotEmpty()) {
                if (!TextUtils.isEmpty(cardData.answer[position].user_answer)) {
                    Glide.with(this@ExamMainActivity).load(cardData.answer[position].user_answer).into(ivFilling)
                }
            }


            llExamBody.addView(view)
        }
    }

    /**
     * 是非题
     */
    @SuppressLint("SetTextI18n")
    private fun renderTrueOrFalseView(cardData: ExamCardData) {
        for (position in 0 until cardData.num) {
            val view = LayoutInflater.from(this@ExamMainActivity).inflate(R.layout.exam_template_true_or_false, null)
            val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
            tvPosition.text = "${position + 1}、"
            val rdgTrueOrFalse = view.findViewById<RadioGroup>(R.id.rdgTrueOrFalse)



            if (cardData.answer.isNotEmpty()) {
                val userAnswer = cardData.answer[position].user_answer
                val rightAnswer = cardData.answer[position].answer
                //修改
                if (isSubmit) {
                    if ("yes" == userAnswer) {
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).isChecked = true
                    } else if ("no" == userAnswer) {
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).isChecked = true
                    }
                    rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).isEnabled = false
                    rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).isEnabled = false
                } else {
                    if ("yes" == userAnswer) {
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).isChecked = true
                    } else if ("no" == userAnswer) {
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).isChecked = true
                    }

                    if (isTeacherGuide) {
                        renderWrongOrRightTF(rightAnswer, userAnswer, rdgTrueOrFalse)
                    }

                }
                rdgTrueOrFalse.setOnCheckedChangeListener { _, _ ->
                    cardData.answer[position].user_answer = if (rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).isChecked) "yes" else "no"
                }
            }

            llExamBody.addView(view)
        }
    }


    /**
     * 多选题
     */
    @SuppressLint("SetTextI18n")
    private fun renderMultipleChoseView(cardData: ExamCardData) {
        for (position in 0 until cardData.num) {
            val view = LayoutInflater.from(this@ExamMainActivity).inflate(R.layout.exam_template_multip_chose, null)
            val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
            tvPosition.text = "${position + 1}、"

            val llMultipleView = view.findViewById<LinearLayout>(R.id.llMultipleChose)


            if (cardData.answer.isNotEmpty()) {
                val userAnswer = cardData.answer[position].user_answer
                val rightAnswer = cardData.answer[position].answer
                //修改
                if (isSubmit) {
                    renderMultiple(userAnswer, llMultipleView)
                } else {
                    if (!TextUtils.isEmpty(userAnswer)) {
                        for (c in userAnswer.toCharArray()) {
                            (llMultipleView.getChildAt(getAnswerPosition(c.toString())) as CheckBox).isChecked = true
                        }
                    }
                    if (isTeacherGuide) {
                        renderWrongOrRightMultiple(userAnswer, llMultipleView, rightAnswer)
                    }

                    // set listener
                    for (viewPosition in 0 until llMultipleView.childCount) {
                        (llMultipleView.getChildAt(viewPosition) as CheckBox).setOnCheckedChangeListener { _, _ ->
                            val checkedItems = getMultipleItemCheckedValues(llMultipleView)
                            cardData.answer[position].user_answer = checkedItems
                        }
                    }
                }
            }
            llExamBody.addView(view)
        }
    }

    /**
     * 单选题
     */
    @SuppressLint("SetTextI18n")
    private fun renderSingleChoseView(cardData: ExamCardData) {
        Log.e(TAG, "singleChose : $cardData")

        for (position in 0 until cardData.num) {
            val view = LayoutInflater.from(this@ExamMainActivity).inflate(R.layout.exam_template_single_chose, null)
            val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
            tvPosition.text = "${position + 1}、"

            val radioGroup = view.findViewById<RadioGroup>(R.id.rdgSingleChose)
            //修改
            if (isSubmit) {
                val userAnswer = cardData.answer[position].user_answer
                renderSingle(radioGroup, userAnswer)

            } else {
                if (cardData.answer.isNotEmpty()) {
                    val userAnswer = cardData.answer[position].user_answer
                    if (!TextUtils.isEmpty(userAnswer)) {
                        (radioGroup.getChildAt(getAnswerPosition(userAnswer)) as RadioButton).isChecked = true
                    }

                    if (isTeacherGuide) {
                        renderWrongRightSingle(radioGroup, userAnswer, cardData, position)
                    }
                    //仅考试的时候添加事件
                    radioGroup.setOnCheckedChangeListener { _, checkedId ->
                        cardData.answer[position].user_answer = radioGroup.findViewById<RadioButton>(checkedId).text.toString()
                    }
                }
            }
            llExamBody.addView(view)
        }
    }

    private fun setRightOrWrongUnChecked() {
    }

    /**
     * 设置单选题正确、错误的颜色
     */
    private fun renderWrongRightSingle(radioGroup: RadioGroup, userAnswer: String, cardData: ExamCardData, position: Int) {
        radioGroup.isEnabled = false
        val rightAnswer = cardData.answer[position].answer

        if (!TextUtils.isEmpty(userAnswer)) {
            val userAnswerRadioButton = (radioGroup.getChildAt(getAnswerPosition(userAnswer)) as RadioButton)
            if (rightAnswer == userAnswer) {
                userAnswerRadioButton.textColor = rightColor
            } else {
                userAnswerRadioButton.textColor = wrongColor
                if (!TextUtils.isEmpty(rightAnswer)) {
                    (radioGroup.getChildAt(getAnswerPosition(rightAnswer)) as RadioButton).textColor = rightColor
                }
            }
        } else {
            if (!TextUtils.isEmpty(rightAnswer)) {
                (radioGroup.getChildAt(getAnswerPosition(rightAnswer)) as RadioButton).textColor = rightColor
            }
        }

    }

    private fun renderSingle(radioGroup: RadioGroup, userAnswer: String) {
        if (!TextUtils.isEmpty(userAnswer)) {
            (radioGroup.getChildAt(getAnswerPosition(userAnswer)) as RadioButton).isChecked = true
        }
        for (index in 0 until radioGroup.childCount) {
            (radioGroup.getChildAt(index) as RadioButton).isEnabled = false
        }
    }

    /**
     * 设置多选题正确、错误的颜色
     */
    private fun renderWrongOrRightMultiple(userAnswer: String, llMultipleView: LinearLayout, rightAnswer: String) {
        if (!TextUtils.isEmpty(userAnswer)) {
            for (c in userAnswer.toCharArray()) {
                val userCheckBox = (llMultipleView.getChildAt(getAnswerPosition(c.toString())) as CheckBox)
                if (rightAnswer.contains(c)) {
                    userCheckBox.textColor = rightColor
                } else {
                    userCheckBox.textColor = wrongColor
                }
            }
        }


        if (!TextUtils.isEmpty(rightAnswer)) {
            for (c in rightAnswer.toCharArray()) {
                if (TextUtils.isEmpty(userAnswer) || !userAnswer.contains(c)) {
                    (llMultipleView.getChildAt(getAnswerPosition(c.toString())) as CheckBox).textColor = wrongColor
                }
            }
        }

    }

    private fun renderMultiple(userAnswer: String, llMultipleView: LinearLayout) {
        llMultipleView.isEnabled = false
        if (!TextUtils.isEmpty(userAnswer)) {
            for (c in userAnswer.toCharArray()) {
                (llMultipleView.getChildAt(getAnswerPosition(c.toString())) as CheckBox).isChecked = true
            }
        }
        for (index in 0 until llMultipleView.childCount) {
            (llMultipleView.getChildAt(index) as CheckBox).isEnabled = false
        }
    }

    private fun renderWrongOrRight(userAnswer: String, rdgTrueOrFalse: RadioGroup) {
        rdgTrueOrFalse.isEnabled = false
        if ("yes" == userAnswer) {
            rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).isChecked = true
        } else {
            rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).isChecked = true

        }
    }

    /**
     * 设置是非题正确、错误的颜色
     */
    private fun renderWrongOrRightTF(rightAnswer: String, userAnswer: String, rdgTrueOrFalse: RadioGroup) {
        if (rightAnswer == userAnswer) {
            if ("yes" == rightAnswer) {
                rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).textColor = rightColor
            } else if ("no" == rightAnswer) {
                rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).textColor = rightColor
            }
        } else {
            if (TextUtils.isEmpty(userAnswer)) {
                if ("yes" == rightAnswer) {
                    rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).textColor = rightColor
                } else if ("no" == rightAnswer) {
                    rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).textColor = rightColor
                }
            } else {
                when (userAnswer) {
                    "yes" -> {
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).textColor = wrongColor
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).textColor = rightColor
                    }
                    "no" -> {
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).textColor = rightColor
                        rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).textColor = wrongColor
                    }
                    else -> {
                        if ("yes" == rightAnswer) {
                            rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbTrue).textColor = rightColor
                        } else if ("no" == rightAnswer) {
                            rdgTrueOrFalse.findViewById<RadioButton>(R.id.rdbFalse).textColor = rightColor
                        }
                    }
                }
            }

        }

    }

    private fun getMultipleItemCheckedValues(llMultipleView: LinearLayout): String {
        val checkedItems = StringBuilder()
        for (subViewPosition in 0 until llMultipleView.childCount) {
            val cbx = (llMultipleView.getChildAt(subViewPosition) as CheckBox)
            if (cbx.isChecked) {
                checkedItems.append(cbx.text.toString())
            }
        }
        return checkedItems.toString()
    }

    private fun addTitleIntoCard(title: String) {
        val view = LayoutInflater.from(this@ExamMainActivity).inflate(R.layout.part_exam_card_title, null)
        (view as TextView).text = title
        llExamBody.addView(view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }


    private fun initListener() {
//        llFilling.setOnClickListener(this)

        btnHideOrShowAnswer.setOnClickListener {
            if (llBottom.layoutParams.height != 0) {
                llBottom.layoutParams.height = 0
                btnHideOrShowAnswer.text = "打开"
                btnHideOrShowAnswer.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exam_answer_show, 0, 0, 0)
                subjectiveContainer.visibility = View.GONE
//
            } else {
                llBottom.layoutParams.height = resources.getDimensionPixelSize(R.dimen.answerViewHeight)
                btnHideOrShowAnswer.text = "收起"
                btnHideOrShowAnswer.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exam_answer_hide, 0, 0, 0)

                if (isShowSubjective) {
                    subjectiveContainer.visibility = View.VISIBLE
                }

            }
        }

        btnSubmitExam.setOnClickListener {
            submitAnswer()
        }

    }

    /**
     * 提交答案
     */
    private fun submitAnswer() {
        showLoadingDialog("正在提交答案，请稍等")
        val dataJson = Gson().toJson(examCard.data)
        Log.d("dataJson:", dataJson)
        val call = APIService.create().submitTestAnswer(examCode, dataJson)
        call.enqueue(object : APICallback<ResponseDataT<Any?>>() {
            override fun onSuccess(response: ResponseDataT<Any?>?) {
                ToastGlobal.showToast("提交成功")
                isSubmit = true
                //修改
                disableViews(llBottom)
                renderExamCardView()
                timeCountDownTimer?.cancel()
//                WisdomInClassActivity.launch(this@ExamMainActivity, "考试结束，等待评卷…")
//                finish()
                timeCountDownTimer?.cancel()
                //清空答题板内容
//                llExamBody.removeAllViews()
            }

            override fun onFailure(call: Call<ResponseDataT<Any?>>, t: Throwable?) {
                super.onFailure(call, t)

            }

            override fun onFinish(msg: String) {
                dismissLoadingDialog()
            }

        })
    }


    override fun onClick(v: View?) {
        when (v) {
//            llFilling -> showSubjectiveView()
        }
    }

    /**
     * 显示主观题
     * @param position 当前题目的位置
     * @param subPosition 题目下面的第几题
     */
    public fun showSubjectiveView(position: Int, subPosition: Int) {
        isShowSubjective = true
        subjectiveContainer.visibility = View.VISIBLE
        val tag = "subjective_${position}_$subPosition"

        if (null != subjectiveFragment) {
            supportFragmentManager.beginTransaction().hide(subjectiveFragment).commit()
        }

        subjectiveFragment = supportFragmentManager.findFragmentByTag(tag)
        if (subjectiveFragment == null) {
            subjectiveFragment = ExamAnswerSubjectiveFragment.instance(position, subPosition)
            supportFragmentManager.beginTransaction().add(R.id.subjectiveContainer, subjectiveFragment, tag).commit()
        } else {
            subjectiveFragment = supportFragmentManager.findFragmentByTag(tag)
            val bundle = Bundle()
            bundle.putInt("p", position)
            bundle.putInt("subP", subPosition)
            subjectiveFragment?.arguments = bundle
            supportFragmentManager.beginTransaction().show(subjectiveFragment).commit()
//            supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag(tag)).commit()
        }
    }

    /**
     * 隐藏主观题
     */
    fun hideSubjectiveView() {
        llBottom.layoutParams.height = resources.getDimensionPixelSize(R.dimen.answerViewHeight)
        btnHideOrShowAnswer.text = "收起"
        btnHideOrShowAnswer.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exam_answer_hide, 0, 0, 0)
        isShowSubjective = false
        subjectiveContainer.visibility = View.GONE
        supportFragmentManager.beginTransaction().hide(subjectiveFragment).commit()
    }

    /**
     * 开始测验
     */
    fun startExam(timeLeft: Int) {
        if (isPreparedExamCard) {
            if (isTeacherGuide) {
                //老师讲评
            } else {
                this.timeLeft = timeLeft
                startCountDown(timeLeft * 1L, 1000)
            }
            enableViews(llBottom)
        } else {
            ToastGlobal.showToast("答题卡下载失败，请联系老师")
        }
    }

    private fun enableViews(view: ViewGroup) {
        for (position in 0 until view.childCount) {

            val currentView = view.getChildAt(position)
            if (currentView is ViewGroup) {
                enableViews(currentView)
            } else {
                currentView.isEnabled = true
            }
        }
    }

    private fun disableViews(view: ViewGroup) {
        for (position in 0 until view.childCount) {
            val currentView = view.getChildAt(position)
            if (currentView is ViewGroup) {
                disableViews(currentView)
            } else {
                currentView.isEnabled = false
            }
        }
    }


    private fun startCountDown(millisInFuture: Long, countDownInterval: Long) {
        timeCountDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimeLeft.text = "剩余时间\n${convertSecondsToHMmSs(millisUntilFinished / 1000)}"
            }

            override fun onFinish() {
                ToastGlobal.showToast("时间到了！")
                submitAnswer()
            }
        }
        timeCountDownTimer?.start()
    }

    fun convertSecondsToHMmSs(seconds: Long): String {
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        return String.format("%d:%02d:%02d", h, m, s)
    }


    override fun onDestroy() {
        super.onDestroy()
        timeCountDownTimer?.cancel()
    }

    /**
     * 结束测验
     */
    public fun endExam() {
        submitAnswer()
    }

}