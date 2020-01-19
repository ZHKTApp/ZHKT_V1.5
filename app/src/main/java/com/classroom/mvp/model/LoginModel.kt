package com.classroom.mvp.model


import android.util.Log
import com.bright.course.http.response.ResponseLogin
import com.classroom.base.BaseModel
import com.classroom.http.RetrofitHelper
import com.classroom.mvp.model.bean.HttpResult
import com.rztop.classroom.rx.SchedulerUtils
import io.reactivex.Observable

/**
 * Created by wyq on 2018/9/25.
 */
class LoginModel : BaseModel() {

    fun login(name: String, password: String): Observable<HttpResult<ResponseLogin>> {
        return RetrofitHelper.service.login(name, password)
                .compose(SchedulerUtils.ioToMain())
    }

}