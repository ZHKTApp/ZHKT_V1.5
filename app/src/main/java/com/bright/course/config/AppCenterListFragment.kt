package com.bright.course.config

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.BaseFragment
import kotlinx.android.synthetic.main.activity_app_list.*
import android.content.pm.ApplicationInfo
import com.bright.course.R


/**
 * Created by kim on 2018/8/27.
 *
 */
class AppCenterListFragment : BaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_app_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var pm = activity?.getPackageManager()
        var filter = Intent(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_LAUNCHER);
        var pkgAppsList = pm?.queryIntentActivities(filter, PackageManager.GET_RESOLVED_FILTER)
        var list:ArrayList<ResolveInfo> =ArrayList<ResolveInfo>()
        for(info in pkgAppsList!!){
            if (ApplicationInfo.FLAG_SYSTEM and info.activityInfo.applicationInfo.flags !== 0) {
                continue
            }
            Log.e("app"," info : " + info.loadLabel(pm) + " activity info : " + info.activityInfo.applicationInfo.className)
            if (info.loadLabel(pm)!="课程资源"&&info.loadLabel(pm)!="导学本"&&info.loadLabel(pm)!="作业辅导"
                    &&info.loadLabel(pm)!="我的文件"&&info.loadLabel(pm)!="我的作业"&&info.loadLabel(pm)!="错题集"&&info.loadLabel(pm)!="智慧课堂"){
                list.add(info)
            }
        }
//        val pkgAppsList = activity?.getPackageManager()?.getInstalledPackages(0)!!
        val adapter = AppCenterItemAdapter()
//        adapter.resultList = pkgAppsList
        adapter.resultList = list!!
        val layoutManager = GridLayoutManager(context, 5)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

    }

}