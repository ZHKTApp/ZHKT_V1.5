package com.bright.course.mqtt

/**
 * Created by kim on 2018/10/10.
 *
 */

data class MQTTMessageJSON(
        val id: Int,
        val data: Data
)

data class Data(
        val status: String,
        val TextNumber: String,
        val ids: String,
        val ques_id: String,
        val userIds: ArrayList<String>,
        val targetstudents: ArrayList<String>,
        val url: String,
        val time: String//考试时间，单位分钟
)