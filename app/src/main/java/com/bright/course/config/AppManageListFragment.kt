package com.bright.course.config

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.BaseFragment
import com.bright.course.R
import kotlinx.android.synthetic.main.activity_app_list.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import kotlinx.android.synthetic.main.exam_template_filling.*
import kotlinx.android.synthetic.main.floating.*


/**
 * Created by kim on 2018/8/27.
 *
 */
class AppManageListFragment : BaseFragment() {
    var mUpdataReceiver: updataReceiver? = null
    var adapter: AppItemAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_app_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAppsList = activity?.getPackageManager()?.getInstalledPackages(0)
        var pkgAppsList: ArrayList<PackageInfo> = ArrayList<PackageInfo>()
        for (packageInfo in mAppsList!!) {
            if (ApplicationInfo.FLAG_SYSTEM and packageInfo.applicationInfo.flags !== 0) {
                continue
            }
            pkgAppsList.add(packageInfo)
        }
         adapter = AppItemAdapter(listener = {
            //            context?.let { context -> AppInfoActivity.launch(context, it) }
             Log.e("receiver", " receiver : " + it.packageName)
             mUpdataReceiver = updataReceiver()
             var intentFilter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED)
             intentFilter.addDataScheme("package:com.zwyl.myfile")
             getContext()!!.registerReceiver(mUpdataReceiver, intentFilter)
        })
        pkgAppsList?.let { list ->
            adapter!!.resultList = list
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }
    }

    class updataReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("receiver", " receiver : " + " 执行")
            var app: AppManageListFragment = AppManageListFragment()
            app.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        if (mUpdataReceiver != null) {
            context!!.unregisterReceiver(mUpdataReceiver)
        }
        super.onDestroy()
    }
}