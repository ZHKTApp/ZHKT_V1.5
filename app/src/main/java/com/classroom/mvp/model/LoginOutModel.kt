package com.classroom.mvp.model


import com.classroom.base.BaseModel
import com.classroom.http.RetrofitHelper
import com.classroom.mvp.model.bean.HttpResult
import com.rztop.classroom.rx.SchedulerUtils
import io.reactivex.Observable

/**
 * Created by wyq on 2018/9/25.
 */
class LoginOutModel : BaseModel() {

    fun loginout(): Observable<HttpResult<Object>> {
        return RetrofitHelper.service.logout()
                .compose(SchedulerUtils.ioToMain())
    }

}