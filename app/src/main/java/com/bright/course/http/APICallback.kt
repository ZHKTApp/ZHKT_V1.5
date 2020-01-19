package com.bright.course.http

import android.util.Log
import android.util.MalformedJsonException
import com.bright.course.http.response.ResponseDataT
import com.bright.course.http.response.ResponseRelevanceLogin
import com.google.gson.JsonSyntaxException
import com.bright.course.utils.ToastGlobal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class APICallback<T> : Callback<T> {
    override fun onFailure(call: Call<T>, t: Throwable?) {

        if (call.isCanceled) {
        } else {
            val msg: String = when (t) {
                is UnknownHostException -> "请检查网络"
                is ConnectException -> "请检查网络"
                is SocketTimeoutException -> "连接超时"
                is JsonSyntaxException -> "json解析异常"
                is MalformedJsonException -> "json解析异常"
                is IllegalStateException -> "非法状态"
                else -> t?.message.toString()
            }
            ToastGlobal.showToast(msg)
            onFinish(msg)
        }
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (call.isCanceled) return

        if (response.isSuccessful) {
            when (response.body()) {
                is ResponseDataT<*> -> {
                    val baseResponse = response.body() as ResponseDataT<*>
                    if (baseResponse.code == 1) {
                        onSuccess(response.body())
                    } else {
                        onFailure(call, Exception(baseResponse.msg))
                    }
                }
                else -> {
                    Log.e("apicall", "apicall else->" + response.body().toString())
                    if (response.body() != null) onSuccess(response.body()) else onFinish("返回结果错误")
                }
            }
        } else {
            onFailure(call, Exception("服务器异常：" + response.code()))
        }

        onFinish("finish")
    }

    abstract fun onSuccess(response: T?)
    abstract fun onFinish(msg: String)

}