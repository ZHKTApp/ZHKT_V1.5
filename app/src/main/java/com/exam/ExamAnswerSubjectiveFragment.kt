package com.exam

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.BaseFragment
import com.bright.course.R
import com.bright.course.http.APICallback
import com.bright.course.http.APIService
import com.bright.course.http.response.ExamCardAnswer
import com.bright.course.http.response.ResponseDataT
import com.bright.course.http.response.ResponseExamCard
import com.bright.course.http.response.ResponseUploadImage
import com.bright.course.utils.ToastGlobal
import com.bright.course.utils.ViewHelper
import com.bumptech.glide.Glide
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.fragment_answer_subjective.*
import me.panavtec.drawableview.DrawableViewConfig
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.configuration
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kim on 2018/9/11.
 *
 */
class ExamAnswerSubjectiveFragment : BaseFragment(), View.OnClickListener {

    val REQUEST_TAKE_PHOTO = 1

    var mCurrentPhotoPath: String? = null
    var position: Int = 0
    var subPosition: Int = 0
    var examCardData: List<ExamCardAnswer>? = null

    companion object {
        fun instance(position: Int = -1, subPosition: Int = -1): ExamAnswerSubjectiveFragment {
            val fragment = ExamAnswerSubjectiveFragment()
            val bundle = Bundle()
            bundle.putInt("p", position)
            bundle.putInt("subP", subPosition)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null != arguments) {
            position = arguments!!.getInt("p")
            subPosition = arguments!!.getInt("subP")
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            if (null != arguments) {
                position = arguments!!.getInt("p")
                subPosition = arguments!!.getInt("subP")
            }
        } else {
            if (null != arguments) {
                position = arguments!!.getInt("p")
                subPosition = arguments!!.getInt("subP")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_answer_subjective, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnPaintEmpty.setOnClickListener(this)
        btnPaintErase.setOnClickListener(this)
        btnPaintRedo.setOnClickListener(this)
        btnPaintUndo.setOnClickListener(this)
        btnSubjectiveCommit.setOnClickListener(this)
        btnSubjectiveBack.setOnClickListener(this)

        rdbAnswerPaint.isSelected = true

        if (activity is ExamMainActivity) {
            btnSubjectiveCommit.text = "保存"
            tvSubjectIndex.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
            btnPrevious.visibility = View.VISIBLE
            renderSubjectIndex()
        } else {
            btnSubjectiveCommit.text = "提交"
            tvSubjectIndex.visibility = View.GONE
            btnNext.visibility = View.GONE
            btnPrevious.visibility = View.GONE
        }

        rdbAnswerPaint.setOnClickListener {
            AlertDialog.Builder(context!!).setTitle("提示").setMessage("切换输入法将会清除当前答案！")
                    .setPositiveButton("确定") { _, _ ->
                        fingerView.clear()
                        etSubjectiveText.setText("")
                        showAnswerPaintView()
                        ivSubjectiveCover.setImageDrawable(null)
                        rdbAnswerPaint.isSelected = true
                        rdbAnswerText.isSelected = false
                        rdbAnswerTakePhoto.isSelected = false
                    }.setNegativeButton("取消", null).show()
        }
        rdbAnswerText.setOnClickListener {
            AlertDialog.Builder(context!!).setTitle("提示").setMessage("切换输入法将会清除当前答案！")
                    .setPositiveButton("确定") { _, _ ->
                        fingerView.clear()
                        etSubjectiveText.setText("")
                        ivSubjectiveCover.setImageDrawable(null)
                        showAnswerInputTextView()
                        rdbAnswerPaint.isSelected = false
                        rdbAnswerText.isSelected = true
                        rdbAnswerTakePhoto.isSelected = false
                    }.setNegativeButton("取消", null).show()

        }
        rdbAnswerTakePhoto.setOnClickListener {
            AlertDialog.Builder(context!!).setTitle("提示").setMessage("切换输入法将会清除当前答案！")
                    .setPositiveButton("确定") { _, _ ->
                        fingerView.clear()
                        etSubjectiveText.setText("")
                        showAnswerTakePhotoPreview()
                        dispatchTakePictureIntent()
                        rdbAnswerPaint.isSelected = false
                        rdbAnswerText.isSelected = false
                        rdbAnswerTakePhoto.isSelected = true
                    }.setNegativeButton("取消", null).show()

        }


        val config = DrawableViewConfig()

        config.strokeColor = resources.getColor(android.R.color.black)
        config.isShowCanvasBounds = true
        config.strokeWidth = DrawableViewConfig.DEFAULT_LINE_SIZE
        config.minZoom = 1.0f
        config.maxZoom = 2.0f
        config.canvasHeight = resources.displayMetrics.heightPixels
        config.canvasWidth = resources.displayMetrics.widthPixels
        fingerView.setConfig(config)
        btnNext.setOnClickListener {
            val examCard = getExamCard()
            var examCardData = examCard!!.data.get(position).answer
            examCard?.let {
                var examCardType = examCard!!.data.get(position).type;
                if ((examCardType == "简答题" || examCardType == "填空题" || examCardType == "计算题") && subPosition + 1 == examCardData!!.size) {
                    uploadImageWithLogic(false, null)
                    ToastGlobal.showToast("没有下一题")
                } else {
                    if (!isAnswerEmpty()) {
                        uploadImageWithLogic(true, object : ICallback {
                            override fun doFinish() {
                                //修改
                                if (subPosition + 1 >= examCardData!!.size) {
                                    subPosition = 0;
                                    position = position + 1
                                    (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                                } else {
                                    subPosition = subPosition + 1
                                    (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                                }
                            }
                        })
                    } else {
                        if (subPosition + 1 >= examCardData!!.size) {
                            subPosition = 0;
                            position = position + 1
                            (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                        } else {
                            subPosition = subPosition + 1
                            (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                        }
                    }
                }
//                else {
//                    uploadImageWithLogic(false, null)
//                }
//                    if (subPosition + 1 > examCardData.size) {
//                        ToastGlobal.showToast("没有下一题。")
//                    } else {
//                        if (!isAnswerEmpty()) {
//                            uploadImageWithLogic(true, object : ICallback {
//                                override fun doFinish() {
//                                    //修改
////                                (activity as ExamMainActivity).showSubjectiveView(position + 1, subPosition)
//                                    (activity as ExamMainActivity).showSubjectiveView(position, subPosition + 1)
//                                }
//                            })
//                        } else {
////                        (activity as ExamMainActivity).showSubjectiveView(position + 1, subPosition)
//                            (activity as ExamMainActivity).showSubjectiveView(position, subPosition + 1)
//                        }
//                    }
            }


        }

        var mPreviouspos = position - getNotJianDaCount()
        btnPrevious.setOnClickListener { _ ->
            val examCard = getExamCard()
            examCard?.let {
                Log.e("exam", " previouspos : " + mPreviouspos + " positon : " + position + " subposition : " + subPosition)
                if (position == 0) {
                    ToastGlobal.showToast("没有上一题")
                    return@let
                }
                //修改
                if ((examCard.data[position - 1].type != "简答题"
                                && examCard.data[position - 1].type != "填空题"
                                && examCard.data[position - 1].type != "计算题")) {
                    ToastGlobal.showToast("没有上一题")
                } else {
                    if (!isAnswerEmpty()) {
                        uploadImageWithLogic(true, object : ICallback {
                            override fun doFinish() {
                                if (subPosition == 0) {
                                    if (mPreviouspos == 0) {
                                        ToastGlobal.showToast("没有上一题")
                                    } else {
                                        position = position - 1
                                        subPosition = examCard!!.data.get(position).answer.size - 1
                                        (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                                    }
                                } else {
                                    subPosition = subPosition - 1
                                    (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                                }
                            }
                        })
                    } else {
                        if (subPosition == 0) {
                            if (mPreviouspos == 0) {
                                ToastGlobal.showToast("没有上一题")
                            } else {
                                position = position - 1
                                subPosition = examCard!!.data.get(position).answer.size - 1
                                (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                            }
                        } else {
                            subPosition = subPosition - 1
                            (activity as ExamMainActivity).showSubjectiveView(position, subPosition)
                        }
                    }
                }
            }
            //修改
//            examCard?.let {
//                if (position == 0 || (examCard.data[position - 1].type != "简答题"
//                        && examCard.data[position - 1].type != "填空题"
//                        && examCard.data[position - 1].type != "计算题")) {
//                    ToastGlobal.showToast("没有上一题。")
//                } else {
////                    position -= 1
////                    renderSubjectIndex()
//                    //如果没有写答案就直接切换到下一题，如果写了就先提交当前题目的答案
//                    if (!isAnswerEmpty()) {
//                        uploadImageWithLogic(true, object : ICallback {
//                            override fun doFinish() {
//                                (activity as ExamMainActivity).showSubjectiveView(position - 1, subPosition)
//                            }
//                        })
//                    } else {
//                        (activity as ExamMainActivity).showSubjectiveView(position - 1, subPosition)
//                    }
//                }
//            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderSubjectIndex() {
//        tvSubjectIndex.text = "${position - getNotJianDaCount() + 1}"
        val exmaCard = getExamCard()
        tvSubjectIndex.text = "${exmaCard!!.data.get(position).type + " 第" + "${subPosition + 1}" + "小题"}"
    }

    fun getNotJianDaCount(): Int {
        val examCard = getExamCard()
        var count = 0
        examCard?.let {
            for (datum in examCard.data) {
                if (datum.type != "简答题" && datum.type != "填空题" && datum.type != "计算题") {
                    count++
                }
            }
        }

        return count
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Crop.of(Uri.fromFile(File(mCurrentPhotoPath)), Uri.fromFile(File(mCurrentPhotoPath))).start(context, this)
            ToastGlobal.showToast("长按边角可控制截图范围")
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(mCurrentPhotoPath).into(ivSubjectiveCover)
        }
    }

    var TAG = ExamAnswerSubjectiveFragment::class.java.simpleName

    var isEraseModel = false
    override fun onClick(v: View?) {
        when (v) {
            btnPaintUndo -> fingerView.undo()
            btnPaintEmpty -> fingerView.clear()
            btnPaintRedo -> fingerView.redo()
            btnPaintErase -> {
                isEraseModel = !isEraseModel
                fingerView.setClearModel(isEraseModel)
                btnPaintErase.setBackgroundResource(if (isEraseModel) R.drawable.shape_erase_selected else R.drawable.shape_erase_un_selected)
            }
            btnSubjectiveCommit -> {
                if (isAnswerEmpty()) return
                uploadImageWithLogic(false, null)
            }
            btnSubjectiveBack -> {
                android.support.v7.app.AlertDialog.Builder(context!!).setTitle("提示").setMessage("此操作将清空全部答案！")
                        .setPositiveButton("清空") { _, which ->
                            //修改
                            hideSubjectiveView(null, instance())
                        }.setNegativeButton("取消") { dialog, which ->
                        }.show()
            }

        }
    }

    private fun uploadImageWithLogic(justUpload: Boolean, callback: ICallback?) {
        doAsync {

            val filePath =
                    if (fingerView.visibility == View.VISIBLE) {
                        fingerView.save()
                    } else if (etSubjectiveText.visibility == View.VISIBLE) {
                        val file = File(Environment.getExternalStorageDirectory(), "editText_${System.currentTimeMillis()}.jpg")
                        ViewHelper.captureViewWithDrawingCache(etSubjectiveText, file.absolutePath)
                        file.absolutePath
                    } else {
                        mCurrentPhotoPath
                    }


            uiThread {
                //                        fingerView.clear()
                //                        hideSubjectiveView(filePath)

                if (TextUtils.isEmpty(filePath)) {
                    ToastGlobal.showToast("请先作答")
                } else {
                    uploadImage(File(filePath), justUpload, callback)
                }
            }
        }
    }

    private fun isAnswerEmpty(): Boolean {
        if (fingerView.visibility == View.VISIBLE) {
            if (fingerView.isEmpty) {
                ToastGlobal.showToast("请先作答")
                return true
            }

        } else if (etSubjectiveText.visibility == View.VISIBLE) {
            if (etSubjectiveText.text.isEmpty()) {
                ToastGlobal.showToast("请先作答")
                return true
            }
        } else if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            ToastGlobal.showToast("请先作答")
            return true
        }
        return false
    }

    fun getExamCard(): ResponseExamCard? {
        if (activity is ExamMainActivity) {
            return (activity as ExamMainActivity).examCard
        }
        return null
    }

    /**
     * 上传图片
     */
    private fun uploadImage(tempImageFile: File, justUpload: Boolean, callback: ICallback?) {
        showLoadingDialog("正在处理，请稍等…")
        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), tempImageFile)
        val body = MultipartBody.Part.createFormData("file", tempImageFile.getName(), requestBody)

        val call = APIService.create().uploadImage(body)
        call.enqueue(object : APICallback<ResponseDataT<ResponseUploadImage>>() {
            override fun onSuccess(response: ResponseDataT<ResponseUploadImage>?) {
                if (!TextUtils.isEmpty(response?.data?.url)) {
                    if (activity is ExamMainActivity) {
                        val examCard = getExamCard()
                        examCard?.let {
                            it.data[position].answer[subPosition].user_answer = response?.data?.url!!
//                        it.data[position].answer[subPosition].user_answer = tempImageFile.absolutePath
                            if (!justUpload) {
                                //修改
                                hideSubjectiveView(null, instance())
                                (activity as ExamMainActivity).renderExamCardView()
                            } else {
                                callback?.doFinish()
                            }

                        }
                    } else if (activity is QuizActivity) {
                        //修改
                        (activity as QuizActivity).postAnswerFromSubjectiveView(response?.data?.url!!)
                        hideSubjectiveView(null, instance())

                    }

                } else {
                    ToastGlobal.showToast("上传图片失败")
                }
            }

            override fun onFinish(msg: String) {
                dismissLoadingDialog()
            }
        })
        callList.add(call)
    }


    interface ICallback {
        fun doFinish()
    }

    /**
     * 显示主观题
     */
    private fun showSubjectiveView() {
        llSubjectiveOptions.visibility = View.VISIBLE
        llAnswerSubjective.visibility = View.VISIBLE
    }

    /**
     * 隐藏主观题
     */
    private fun hideSubjectiveView(filePath: String?, fragment: ExamAnswerSubjectiveFragment) {
        if (activity is ExamMainActivity) {
            (activity as ExamMainActivity).hideSubjectiveView()
        } else if (activity is QuizActivity) {
            (activity as QuizActivity).hideSubjectiveView(fragment)
//            filePath?.let { (activity as QuizActivity).postAnswerFromSubjectiveView(it) }
        }
    }


    private fun showAnswerPaintView() {
        etSubjectiveText.visibility = View.GONE
        fingerView.visibility = View.VISIBLE
        llPaintTools.visibility = View.VISIBLE
        ivSubjectiveCover.visibility = View.GONE
    }

    private fun showAnswerInputTextView() {
        etSubjectiveText.visibility = View.VISIBLE
        fingerView.visibility = View.GONE
        llPaintTools.visibility = View.GONE
        ivSubjectiveCover.visibility = View.GONE
    }

    private fun showAnswerTakePhotoPreview() {
        etSubjectiveText.visibility = View.GONE
        fingerView.visibility = View.GONE
        llPaintTools.visibility = View.GONE
        ivSubjectiveCover.visibility = View.VISIBLE
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath()
        return image
    }

    public fun disableSubmitButton() {
        btnSubjectiveCommit.isEnabled = false
    }

    public fun enableSubmitButton() {
        btnSubjectiveCommit.isEnabled = false
    }

    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context?.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null;
            try {
                photoFile = createImageFile();
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = context?.let {
                    FileProvider.getUriForFile(it,
                            "com.bright.course.fileprovider",
                            photoFile)
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }
}