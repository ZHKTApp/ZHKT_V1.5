package com.bright.course.http.response

/**
 * Created by kim on 2018/9/10.
 *
 */


data class ResponseLogin(
        val token: String,
        val profile: Profile
)

data class Profile(
        val ID: String,
        val Name: String,
        val Code: String
)