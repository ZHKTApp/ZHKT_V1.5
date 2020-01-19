package com.rztop.classroom.classroom.mvp.contract

import com.classroom.base.IPresenter
import com.classroom.base.IView
import com.classroom.mvp.model.bean.HandSupBean


/**
 * Created by wyq on 2018/9/25.
 */
interface HandSupContract {

    interface View : IView {

        fun handSupSuccess(data: HandSupBean)

        fun handSupFail()

    }

    interface Presenter : IPresenter<View> {

        fun handSup()

    }

}