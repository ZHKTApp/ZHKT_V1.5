package com.classroom.mvp.model.bean

import com.squareup.moshi.Json

/**
 * Created by wyq on 2018/9/25.
 */
//PAD-BaseResult
data class HttpResult<T>(@Json(name = "data") val data: T,
                         @Json(name = "code") val code: String,
                         @Json(name = "msg") val msg: String
)

//PAD-学生登录
data class LoginBean(
        @Json(name = "token") val token: String,
        @Json(name = "profile") val profile: ProfileBean
)

//PAD-学生举手表示疑问
data class ProfileBean(
        @Json(name = "ID") val ID: String,
        @Json(name = "Name") val Name: String,
        @Json(name = "Code") val Code: String
)

//PAD-学生举手表示疑问
data class HandSupBean(
        @Json(name = "ID") val ID: Int,
        @Json(name = "StudentID") val StudentID: Any,
        @Json(name = "CreateTime") val CreateTime: String
)