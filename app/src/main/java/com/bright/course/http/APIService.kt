package com.bright.course.http

import com.bright.course.BuildConfig
import com.bright.course.http.response.*
import com.classroom.constant.Constant
import com.cxz.wanandroid.utils.SPUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Created by kim on 2018/7/13.
 *
 */
interface APIService {


    @GET("login")
    fun login(@Query("name") name: String,
              @Query("password") password: String,
              @Query("type") type: String,
              @Query("ostype") ostype: String): Call<ResponseLogin>

    @FormUrlEncoded
    @POST("student/fetchquestion")
    fun getQuizQuestion(@Field("ques_id") ques_id: String): Call<ResponseDataT<ResponseQuiz>>

    @FormUrlEncoded
    @POST("student/answer")
    fun postQuestion(@Field("ques_id") ques_id: String,
                     @Field("answer_type") answer_type: String,
                     @Field("answer") answer: String): Call<ResponseDataT<Any?>>


    @Multipart
    @POST("upload")
    fun uploadImage(@Part file: MultipartBody.Part,
                    @Part("type") type: RequestBody = RequestBody.create(MediaType.parse("text/plain"), "jpg"))
            : Call<ResponseDataT<ResponseUploadImage>>


    @FormUrlEncoded
    @POST("student/fetchtest")
    fun fetchExam(@Field("code") code: String): Call<ResponseDataT<ResponseExam>>


    @FormUrlEncoded
    @POST("student/fetchtestdone")
    fun fetchExamDone(@Field("code") code: String): Call<ResponseDataT<Any?>>


    /**
     * type:0为答题卡，1为答卷
     */
    @FormUrlEncoded
    @POST("student/fetchCard")
    fun fetchExamCard(@Field("TextNumber") code: String,
                      @Field("type") type: String = "0"): Call<ResponseDataT<ResponseExamCard>>

    @FormUrlEncoded
    @POST("student/submitTestAnswer")
    fun submitTestAnswer(@Field("code") code: String,
                         @Field("data") type: String): Call<ResponseDataT<Any?>>


    companion object {
        val PAGE_SIZE = 10

        //        val domain = "http://192.168.1.109:7009/"
//        var domain = "http://192.168.99.161:7009/"
        var domain: String = "http://${SPUtil.prefs.getString(Constant.TEACHER_IP_KEY, "")}:${SPUtil.prefs.getString(Constant.TEACHER_PORT_KEY, "")}/"

        private val client = OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(TokenVerify.DefaultInterceptor())
                .retryOnConnectionFailure(true)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                })
                .build()

        private val retrofit = Retrofit.Builder()
                .baseUrl(domain)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        fun create(): APIService {
            domain = "http://${SPUtil.prefs.getString(Constant.TEACHER_IP_KEY, "")}:${SPUtil.prefs.getString(Constant.TEACHER_PORT_KEY, "")}/"
            return retrofit.newBuilder().baseUrl(domain).build().create(APIService::class.java)
        }
    }
}