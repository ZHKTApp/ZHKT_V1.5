package com.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.BaseFragment
import com.bright.course.R
import com.bright.course.utils.ToastGlobal
import com.classroom.mvp.model.bean.HandSupBean
import com.rztop.classroom.classroom.mvp.contract.HandSupContract
import com.rztop.classroom.presenter.HandSupPresenter
import kotlinx.android.synthetic.main.view_hans_up.*

/**
 * Created by kim on 2018/10/18.
 *
 */
class HandsUpFragment : BaseFragment(), HandSupContract.View {

    private val mPresenter: HandSupPresenter by lazy {
        HandSupPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_hans_up, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibHandsUp.setOnClickListener {
            mPresenter.handSup()//学生举手表示疑问
        }
    }


    override fun handSupSuccess(data: HandSupBean) {
        ToastGlobal.showToast(data.ID.toString())
    }

    override fun handSupFail() {
        showLoadingDialog("举手失败")
    }

    override fun showLoading() {
        showLoadingDialog("正在举手")
    }

    override fun hideLoading() {
        dismissLoadingDialog()
    }

    override fun showError(errorMsg: String) {
        ToastGlobal.showToast(errorMsg)
    }
}