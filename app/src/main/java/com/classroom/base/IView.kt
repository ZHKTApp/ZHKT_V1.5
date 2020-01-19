package com.classroom.base

/**
 * Created by wyq on 2018/9/25.
 */
interface IView {

    fun showLoading()

    fun hideLoading()

    fun showError(errorMsg: String)

}