package com.bright.course.config

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bright.course.R
import com.bright.course.wifi.OnClickWLANItemListener
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.floating.view.*


/**
 * Created by jinbangzhu on 25/04/2017.
 */

class AppCenterItemAdapter : RecyclerView.Adapter<AppCenterItemAdapter.ViewHolder>() {


    private var clickWLANItemListener: OnClickWLANItemListener? = null
//    lateinit var resultList: List<PackageInfo>
    lateinit var resultList:List<ResolveInfo>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_center_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val info = resultList[position]
        holder.tvAppName.text = info.loadLabel(context.getPackageManager()).toString();
        Glide.with(context).load(info.loadIcon(context.getPackageManager())).into(holder.ivAppIcon)
//        holder.tvAppName.text = info.applicationInfo.loadLabel(context.getPackageManager()).toString();
//        Glide.with(context).load(info.applicationInfo.loadIcon(context.getPackageManager())).into(holder.ivAppIcon)
        holder.itemView.setOnClickListener {

            // val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", info.packageName, null))
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            holder.itemView.context.startActivity(intent);
            //修改
            var cn = ComponentName(info.activityInfo.packageName,info.activityInfo.name);
            var intent = Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            intent.setComponent(cn);
            holder.itemView.context.startActivity(intent)
//            AppInfoActivity.launch(holder.itemView.context, info)
        }
    }

    override fun getItemCount(): Int {
        return resultList.size
    }


    fun setClickWLANItemListener(clickWLANItemListener: OnClickWLANItemListener) {
        this.clickWLANItemListener = clickWLANItemListener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvAppName: TextView
        var ivAppIcon: ImageView

        init {
            tvAppName = itemView.findViewById(R.id.tvAppName)
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon)
        }
    }
}
