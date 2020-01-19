package com.bright.course.http

import okhttp3.*
import java.io.IOException

/**
 * Created by kim on 16/05/2018.
 */
class TokenVerify {

    class DefaultInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var formBody: RequestBody? = null
            val url = request.url().toString()


            if (request.method() === "POST") {
                if (request.body() is FormBody) {
                    formBody = request.body() as FormBody?
                } else if (request.body() is MultipartBody) {
                    formBody = request.body() as MultipartBody?
                } else {
                    formBody = FormBody.Builder().build()
                }
            }

            var requestBuilder: Request.Builder = request.newBuilder().url(url)

            if (null != formBody) {
                requestBuilder = requestBuilder.post(formBody)
            }

//            if (!UserInfoInstance.instance.isGuestUser) {
//                val token = UserInfoInstance.instance.userInfo.session_id
//                requestBuilder.addHeader("Cookie", "sessionid=$token")
            if(!UserInfoInstance.instance.isGuestUser){
                requestBuilder.addHeader("token", UserInfoInstance.instance.token)
//                requestBuilder.addHeader("token", "897b98af956348d392cda5f441cdf321")
            }
//            }
            return chain.proceed(requestBuilder.build())
        }
    }

}
