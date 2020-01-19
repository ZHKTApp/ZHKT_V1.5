package com.classroom.http


import com.bright.course.http.UserInfoInstance
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import okhttp3.HttpUrl
import okhttp3.Interceptor
import java.io.IOException

class ChangeUrlIntercept : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        //获取request
        val request = chain.request()

        //获取request的创建者builder
        val builder = request.newBuilder()

        //从request中获取headers，通过给定的键url_name
        val oldHost = request.url().host()
        val oldPort = request.url().port()

        //取本地(sp)教师端-ip数据
        val strTeacherIp: String by SPUtil(Constant.TEACHER_IP_KEY, "")
        //取本地(sp)教师端-端口数据
        val strTeacherPort: String by SPUtil(Constant.TEACHER_PORT_KEY, "")

        if (!UserInfoInstance.instance.isGuestUser) {
            builder.addHeader("token", UserInfoInstance.instance.token)
        }

        if (strTeacherIp != oldHost || strTeacherPort != oldPort.toString()) {
            //从request中获取原有的HttpUrl实例oldHttpUrl
            val oldHttpUrl = request.url()

            var newBaseUrl: HttpUrl? = HttpUrl.parse("http://$strTeacherIp:$strTeacherPort/")

//            var newBaseUrl: HttpUrl? = HttpUrl.parse("http://jt4mwq.natappfree.cc/")

            //重建新的HttpUrl，修改需要修改的url部分
            val newFullUrl: HttpUrl? = oldHttpUrl
                    .newBuilder()
                    .scheme(newBaseUrl?.scheme())
                    .host(newBaseUrl?.host())
                    .port(newBaseUrl?.port()!!)
                    .build()

            //重建这个request，通过builder.url(newFullUrl).build()；
            //然后返回一个response至此结束修改
            return chain.proceed(builder.url(newFullUrl).build())
        } else {
            return chain.proceed(request)
        }
    }

}