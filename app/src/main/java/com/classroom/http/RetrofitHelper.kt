package com.classroom.http

import com.bright.course.BuildConfig
import com.classroom.api.ApiService
import com.classroom.constant.Constant
import com.classroom.constant.HttpConstant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by wyq on 2018/9/25.
 */
object RetrofitHelper {

    private var retrofit: Retrofit? = null

    val service: ApiService by lazy { getRetrofit()!!.create(ApiService::class.java) }

    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            synchronized(RetrofitHelper::class.java) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                            .baseUrl(Constant.BASE_URL)  // baseUrl
                            .client(client)
                            .addConverterFactory(MoshiConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build()
                }
            }
        }
        return retrofit
    }

    private val client = OkHttpClient().newBuilder()
            .addInterceptor(ChangeUrlIntercept())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // 错误重连
            .build()


}