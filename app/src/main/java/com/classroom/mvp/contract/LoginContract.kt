package com.rztop.classroom.classroom.mvp.contract

import com.bright.course.http.response.ResponseLogin
import com.classroom.base.IPresenter
import com.classroom.base.IView


/**
 * Created by wyq on 2018/9/25.
 */
interface LoginContract {

    interface View : IView {

        fun loginSuccess(data: ResponseLogin)

        fun loginFail()

    }

    interface Presenter : IPresenter<View> {

        fun login(username: String, password: String)

    }

}