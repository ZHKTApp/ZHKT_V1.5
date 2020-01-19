package com.classroom.http

import com.bright.course.BuildConfig
import com.classroom.api.ApiService
import com.classroom.constant.Constant
import com.classroom.constant.HttpConstant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RelevanceRetrofitHelper {
    private var retrofit: Retrofit? = null

    fun create(): ApiService {
        return getRetrofit()!!.create(ApiService::class.java)
    }

    private fun getRetrofit(): Retrofit? {
        val client = OkHttpClient.Builder()
                .connectTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)// 错误重连
                .hostnameVerifier { hostname, session -> true }
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl(Constant.REL_BASE_URL)  // baseUrl
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return retrofit
    }


}