package com.bright.course.http.response

/**
 * Created by kim on 2018/9/26.
 *
 */

data class ResponseExamCard(
        val TextNumber: String,
        val data: List<ExamCardData>
)

data class ExamCardData(
        val type: String,
        val num: Int,
        val answer: List<ExamCardAnswer>
)

data class ExamCardAnswer(
        val type: Int,
        val answer_id: Int,
        val answer: String = "",
        var user_answer: String = ""
)