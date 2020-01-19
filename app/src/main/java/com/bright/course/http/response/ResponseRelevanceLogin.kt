package com.bright.course.http.response

data class ResponseRelevanceLogin(
        val resultMsg: String,
        val resultCode: Int,
        val len: Int,
        val resultInfo: ResultInfo,
        val token: String,
        val existsData: Boolean,
        val success: Boolean,
        val dataLength: Int
)

data class ResultInfo(
        val token: String,
        val cmStudentId: String,
        val studentNo: String,
        val adminSchoolId: String,
        val studentPhone: String,
        val studentName: String
)