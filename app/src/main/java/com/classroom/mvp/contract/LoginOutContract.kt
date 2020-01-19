package com.rztop.classroom.classroom.mvp.contract

import com.classroom.base.IPresenter
import com.classroom.base.IView


/**
 * Created by wyq on 2018/9/25.
 */
interface LoginOutContract {

    interface View : IView {

        fun loginOutSuccess(data: String)

        fun loginOutFail()

    }

    interface Presenter : IPresenter<View> {

        fun loginout()

    }

}