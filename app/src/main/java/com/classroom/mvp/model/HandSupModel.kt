package com.classroom.mvp.model

import com.classroom.base.BaseModel
import com.classroom.http.RetrofitHelper
import com.classroom.mvp.model.bean.HandSupBean
import com.classroom.mvp.model.bean.HttpResult
import com.rztop.classroom.rx.SchedulerUtils
import io.reactivex.Observable

/**
 * Created by wyq on 2018/9/25.
 */
class HandSupModel : BaseModel() {

    fun handSup(): Observable<HttpResult<HandSupBean>> {
        return RetrofitHelper.service.handsup()
                .compose(SchedulerUtils.ioToMain())
    }

}