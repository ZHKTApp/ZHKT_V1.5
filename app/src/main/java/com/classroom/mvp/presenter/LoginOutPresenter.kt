package com.classroom.mvp.presenter

import com.classroom.base.BasePresenter
import com.classroom.http.exception.ErrorStatus
import com.classroom.http.exception.ExceptionHandle
import com.classroom.http.function.RetryWithDelay
import com.classroom.mvp.model.LoginOutModel
import com.rztop.classroom.classroom.mvp.contract.LoginOutContract

/**
 * Created by wyq on 2018/9/25.
 */
class LoginOutPresenter : BasePresenter<LoginOutContract.View>(), LoginOutContract.Presenter {

    private val loginoutModel: LoginOutModel by lazy {
        LoginOutModel()
    }

    override fun loginout() {
        mView?.showLoading()
        val disposable = loginoutModel.loginout()
                .retryWhen(RetryWithDelay())
                .subscribe({ results ->
                    mView?.apply {
                        if (results.code != ErrorStatus.SUCCESS.toString()) {
                            showError(results.msg)
                        } else {
                            loginOutSuccess(results.data.toString())
                        }
                        hideLoading()
                    }
                }, { t ->
                    mView?.apply {
                        hideLoading()
                        showError(ExceptionHandle.handleException(t))
                    }
                })
        addSubscription(disposable)
    }

}