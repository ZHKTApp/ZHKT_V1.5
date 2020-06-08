package com.classroom.mvp.presenter

import android.util.Log
import com.classroom.base.BasePresenter
import com.classroom.http.exception.ErrorStatus
import com.classroom.http.exception.ExceptionHandle
import com.classroom.http.function.RetryWithDelay
import com.classroom.mvp.model.LoginModel
import com.rztop.classroom.classroom.mvp.contract.LoginContract

/**
 * Created by wyq on 2018/9/25.
 */
class LoginPresenter : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    private val loginModel: LoginModel by lazy {
        LoginModel()
    }

    override fun login(name: String, password: String) {
        mView?.showLoading()
        val disposable = loginModel.login(name, password)
                .retryWhen(RetryWithDelay())
                .subscribe({ results ->
                    mView?.apply {
                        if (results.code != ErrorStatus.SUCCESS.toString()) {
                            showError(results.msg)
                        } else {
                            loginSuccess(results.data)
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