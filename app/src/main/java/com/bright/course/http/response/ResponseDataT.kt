package com.bright.course.http.response

/**
 * Created by kim on 12/10/2017.
 */

open class ResponseDataT<T> {
    var msg: String? = null
    var code: Int? = 0
    var data: T? = null
}
