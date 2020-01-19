package com.bright.course

import android.app.ProgressDialog
import android.support.v4.app.Fragment
import retrofit2.Call

/**
 * Created by kim on 20/05/2018.
 *
 */
open class BaseFragment : Fragment() {
    var callList: ArrayList<Call<*>> = ArrayList<Call<*>>()

    private var loadingDialog: ProgressDialog? = null

    override fun onDestroyView() {
        super.onDestroyView()
        for (call in callList) {
            if (!call.isCanceled) {
                call.cancel()
            }
        }
    }

    fun addCallQueue(call: Call<*>) {
        callList.add(call)
    }


    fun showLoadingDialog(msg: String = "加载中") {
        if (null == loadingDialog) {
            loadingDialog = ProgressDialog.show(activity, "", msg, false, false)
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

}