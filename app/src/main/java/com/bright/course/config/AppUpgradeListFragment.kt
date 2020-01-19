package com.bright.course.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.BaseFragment
import com.bright.course.R

/**
 * Created by kim on 2018/8/27.
 *
 */
class AppUpgradeListFragment : BaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_app_list, container, false)
    }

}