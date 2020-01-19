package com.bright.course

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import retrofit2.Call
import java.util.*


/**
 * Created by kim on 14/05/2018.
 *
 */
open class BaseActivity : AppCompatActivity() {
    private var loadingDialog: ProgressDialog? = null
    var callList: ArrayList<Call<*>> = ArrayList<Call<*>>()


    override fun onDestroy() {
        super.onDestroy()
        for (call in callList) {
            if (!call.isCanceled) {
                call.cancel()
            }
        }
    }

    fun showLoadingDialog(msg: String = "加载中") {
        if (null == loadingDialog) {
            loadingDialog = ProgressDialog.show(this, "", msg, false, true)
        }
        loadingDialog?.setMessage(msg)

        if (loadingDialog?.isShowing!!) {
        } else {
            loadingDialog?.show()
        }
    }

    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }


    fun addCallQueue(call: Call<*>) {
        callList.add(call)
    }

}