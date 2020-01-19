package com.bright.course.config

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by kim on 2018/8/27.
 *
 */
class AppManagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AppCenterListFragment()
            1 -> AppManageListFragment()
            else -> AppUpgradeListFragment()
        }
    }


    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "应用中心"
            1 -> "应用管理"
            else -> "新增应用"
        }
    }
}