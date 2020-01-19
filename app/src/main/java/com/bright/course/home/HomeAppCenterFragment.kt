package com.bright.course.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bright.course.BaseFragment
import com.bright.course.R
import com.bright.course.utils.DialogUtil.Companion.initHiddenMainDialog
import com.bright.course.utils.FileUtils
import com.bright.course.utils.NetWorkUtil.Companion.isNetworkConnected
import com.bright.course.utils.ToastGlobal
import com.bumptech.glide.Glide
import com.classroom.activity.WisdomClassRoomActivity
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.fragment_answer_subjective.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.E


/**
 * Created by kim on 2018/8/27.
 *
 */
class HomeAppCenterFragment : BaseFragment() {
    val TAG = HomeAppCenterFragment::class.java.simpleName
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_app_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        context?.let { it ->
            val adapter = HomeAppItemAdapter(it) { pos ->

                if (isNetWorkDialog()) return@HomeAppItemAdapter
                when (pos) {
                    0 -> activity?.let { WisdomClassRoomActivity.launch(it) }
                    1 -> {
//                        it.startService(Intent(it, FloatingNotes::class.java))
//                        val startHoverIntent = Intent(it, SingleSectionHoverMenuService::class.java)
//                        it.startService(startHoverIntent)
                        luanch("com.zwyl.guide", "com.zwyl.guide.main.MainActivity")
                    }
                    2 -> luanch("com.zwyl.myhomework", "com.zwyl.myhomework.main.MainActivity")
                    3 -> luanch("com.zwyl.homeworkhelp", "com.zwyl.homeworkhelp.main.MainActivity")
                    4 -> luanch("com.zwyl.wronglist", "com.zwyl.wronglist.main.MainActivity")
                    5 -> luanch("com.zwyl.course", "com.zwyl.course.main.MainActivity")
                    6 -> luanch("com.zwyl.myfile", "com.zwyl.myfile.main.filebrower.FileBrowerApp")
                    7 -> dispatchTakePictureIntent()
                }
            }
            val layoutManager = GridLayoutManager(context, 4)
            layoutManager.let { x ->
                recyclerView.layoutManager = x
            }
            recyclerView.adapter = adapter
        }
    }

    private fun luanch(packageName: String, className: String) {
        var token: String  by SPUtil(Constant.REL_STUID, "")
        var service_ip:String by SPUtil(Constant.SYSTEM_SERVER_IP_KEY,"")
        Log.e(TAG, "token bright : $token service ip : $service_ip" )
        if (TextUtils.isEmpty(token)) {
            ToastGlobal.showToast("请先点击系统管理 保存用户名密码")
        } else {
            var intent = Intent()
            intent.setClassName(packageName, className)
            var mBundle = Bundle()
            mBundle.putString("token", token)
            mBundle.putString("service_ip",service_ip)
            intent.putExtras(mBundle)
            startActivity(intent)
        }
    }

    /**
     * 无网络时弹窗
     */
    private fun isNetWorkDialog(): Boolean {
        //App无网络进入弹框
        if (!isNetworkConnected(context!!)) {
            initHiddenMainDialog(context!!)

            return true
        }
        return false
    }


    var mCurrentPhotoPath: String? = null
    val REQUEST_TAKE_PHOTO = 1

    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context?.packageManager) != null) {
//            // Create the File where the photo should go
            var photoFile: File? = null;
            try {
                photoFile = createImageFile();
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                val photoURI = context?.let {
//                    FileProvider.getUriForFile(it,
//                            "com.bright.course.fileprovider",
//                            photoFile)
//                }
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
//            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
            takePictureIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp
        //修改
        val image = FileUtils.getFilePath(Constant.PACKAGE_NAME, imageFileName + ".jpg")
//                val imageFileName = "JPEG_" + timeStamp + "_"
//
//        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        val image = File.createTempFile(
//                imageFileName, /* prefix */
//                ".jpg", /* suffix */
//                storageDir      /* directory */
//        )

        mCurrentPhotoPath = image.getAbsolutePath()
        Log.e("http", "mCurrentPhotoPath : " + mCurrentPhotoPath)
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Crop.of(Uri.fromFile(File(mCurrentPhotoPath)), Uri.fromFile(File(mCurrentPhotoPath))).start(context, this)
            ToastGlobal.showToast("长按边角可控制截图范围")
        }
    }

}