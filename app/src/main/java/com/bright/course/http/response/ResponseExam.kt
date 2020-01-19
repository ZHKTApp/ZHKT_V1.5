package com.bright.course.http.response

/**
 * Created by kim on 2018/9/26.
 *
 */

data class ResponseExam(
    val id: Int,
    val code: String,
    val beginTime: String,
    val endTime: String,
    val duration: String,
    val path: String
)