package com.rztop.classroom.presenter


import com.classroom.base.BasePresenter
import com.classroom.http.exception.ErrorStatus
import com.classroom.http.exception.ExceptionHandle
import com.classroom.http.function.RetryWithDelay
import com.classroom.mvp.model.HandSupModel
import com.rztop.classroom.classroom.mvp.contract.HandSupContract

/**
 * Created by wyq on 2018/9/25.
 */
class HandSupPresenter : BasePresenter<HandSupContract.View>(), HandSupContract.Presenter {

    private val handSupMode: HandSupModel by lazy {
        HandSupModel()
    }

    override fun handSup() {
        mView?.showLoading()
        val disposable = handSupMode.handSup()
                .retryWhen(RetryWithDelay())
                .subscribe({ results ->
                    mView?.apply {
                        if (results.code != ErrorStatus.SUCCESS.toString()) {
                            showError(results.msg)
                            handSupFail()
                        } else {
                            handSupSuccess(results.data)
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