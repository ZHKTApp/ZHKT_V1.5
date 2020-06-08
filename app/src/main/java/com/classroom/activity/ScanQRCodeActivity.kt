package com.classroom.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.bright.course.App
import com.bright.course.R
import com.bright.course.bean.BCMessage
import com.bright.course.bean.EventMessage
import com.bright.course.utils.ToastGlobal
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import kotlinx.android.synthetic.main.activity_config.*
import kotlinx.android.synthetic.main.activity_qrcode_scan.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.intentFor

/**
 * @auther wyq
 *
 * @time Create by 2018/09/15
 */
class ScanQRCodeActivity : AppCompatActivity(), QRCodeView.Delegate {

    private val TAG = "ScanQRCodeActivity"

    companion object {

        fun start(context: Context) {
            context.startActivity(context.intentFor<ScanQRCodeActivity>())
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scan)

        zxingview.setDelegate(this)
    }

    override fun onStart() {
        super.onStart()

        zxingview.startCamera() // 打开后置摄像头开始预览，但是并未开始识别
        //        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别

        zxingview.startSpotAndShowRect() // 显示扫描框，并且延迟0.5秒后开始识别
    }

    override fun onStop() {
        zxingview.stopCamera() // 关闭摄像头预览，并且隐藏扫描框
        super.onStop()
    }

    override fun onDestroy() {
        zxingview.onDestroy() // 销毁二维码扫描控件
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    override fun onScanQRCodeSuccess(result: String) {
        Log.i(TAG, "result:$result")
        //修改
        if (result.contains("?")){
            val newStr = result.substring(result.indexOf("?"), result.length)

            val newArray = arrayOfNulls<String>(4)
            val split = newStr.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in split.indices) {
                val split1 = split[i].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (j in split1.indices) {
                    if (j + 1 >= split1.size) {
                        continue
                    }
                    newArray[i] = split1[j + 1]
                    Log.i("TAG", "initView: key   " + i + "<>" + j + split1[j + 1])
                }
            }
            Log.i("TAG", "initView: key   " + newArray[0] + "," + newArray[1])
            setSpContent(result, newArray[0], newArray[1])
            ToastGlobal.customMsgToastShort(App.instance, "更新成功！")
            EventBus.getDefault().post(EventMessage(BCMessage.MSG_CONNECT, ""))
        }else{
            var code by SPUtil(Constant.SYSTEM_POWER_CODE_KEY, "")
            code=result
            EventBus.getDefault().post(EventMessage(BCMessage.MSG_SCANQRCODE, result))
        }
        finish()
    }

    override fun onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错")
    }

    /**
     * 扫描成功拿到Result,IP,Port
     */
    private fun setSpContent(result: String?, ip: String?, port: String?) {
        var qrResult by SPUtil(Constant.QR_CODE_RESULT_KEY, result)
        var name by SPUtil(Constant.TEACHER_IP_KEY, ip)
        var pwd by SPUtil(Constant.TEACHER_PORT_KEY, port)
        qrResult = result
        name = ip
        pwd = port

    }
}