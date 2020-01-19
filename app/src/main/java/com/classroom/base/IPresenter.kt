package com.classroom.base


/**
 * Created by wyq on 2018/9/25.
 */
interface IPresenter<in V : IView> {

    fun attachView(mView: V)

    fun detachView()

}