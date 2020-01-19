package com.bright.course.home

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.home_app_center_item.view.*


/**
 * Created by jinbangzhu on 25/04/2017.
 */

class HomeAppItemAdapter(val context: Context, val listener: (Int) -> Unit) : RecyclerView.Adapter<HomeAppItemAdapter.ViewHolder>() {

    var appNames: Array<String>
    var appIcons: TypedArray
    var appBgColors: IntArray

    init {
        appNames = context.resources.getStringArray(R.array.homeApp)
        appIcons = context.resources.obtainTypedArray(R.array.homeAppIcon)
        appBgColors = context.resources.getIntArray(R.array.homeAppBackground)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_app_center_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        holder.bind(position, listener, appNames[position], appIcons.getResourceId(position, -1), appBgColors[position])
    }

    override fun getItemCount(): Int {
        return appNames.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int, listener: (Int) -> Unit, appName: String, icon: Int, color: Int) = with(itemView) {
            tvAppName.text = appName
            Glide.with(context).load(icon).into(ivAppIcon)
            llItemBg.setBackgroundColor(color)
            setOnClickListener { listener(position) }
        }
    }

}
