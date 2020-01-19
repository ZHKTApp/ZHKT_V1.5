package com.bright.course.config

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageStatsObserver
import android.content.pm.PackageInfo
import android.content.pm.PackageStats
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bright.course.R
import com.bright.course.utils.HumanReadableUnit
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.app_item.view.*
import java.io.FileInputStream
import java.lang.reflect.InvocationTargetException
import android.content.Context
import android.content.IntentFilter
import android.widget.TextView
import com.bright.course.utils.CacheClearHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * Created by jinbangzhu on 25/04/2017.
 */

class AppItemAdapter(private val listener: (PackageInfo) -> Unit) : RecyclerView.Adapter<AppItemAdapter.ViewHolder>() {

    lateinit var resultList: List<PackageInfo>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val info = resultList[position]
        holder.bind(info, position, listener)
    }

    override fun getItemCount(): Int {
        return resultList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(info: PackageInfo, position: Int, listener: (PackageInfo) -> Unit) = with(itemView) {
            //            if (position == 0) btnAllUpdate.visibility = View.VISIBLE else btnAllUpdate.visibility = View.GONE

            tvAppName.text = info.applicationInfo.loadLabel(context.packageManager).toString();
            tvAppVersion.text = info.versionName

            updateApp(position)// 更新

            val sourceSize = FileInputStream(info.applicationInfo.sourceDir).channel.size();

            val dataSize = try {
                FileInputStream(info.applicationInfo.dataDir).getChannel().size();
            } catch (e: Exception) {
                0L
            }

            val publicSourceSize = FileInputStream(info.applicationInfo.publicSourceDir).channel.size();
//            Log.e("size", " sourceSize : " + sourceSize + " dataSize : " + dataSize + " publicSourceSize : " + publicSourceSize + " info : " + info.activities)
            getpkginfo(info.packageName, context, tvAppSize)

//            tvAppSize.text = HumanReadableUnit.ByteWithUnitSuffixes(sourceSize + dataSize + publicSourceSize)

            Glide.with(context).load(info.applicationInfo.loadIcon(context.packageManager)).into(ivAppIcon)

//            setOnClickListener { listener(info) }


            btnAppClean.setOnClickListener {
                //                                CacheClearHelper.clearCache(context)
                clearCache()
                getpkginfo(info.packageName, context, tvAppSize)
            }
            btnUnInstall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:${info.packageName}")
                context.startActivity(intent)
                listener(info)
//
            }
        }

        /**
         * 更新按钮
         */
        private fun View.updateApp(position: Int) {
            when (position) {
                0 -> {
                    cirProgress.setProgress(50)
                    btnUpgrade?.text = "50%"
                    btnUpgrade?.letterSpacing = 0f
                }
                1 -> {
                    cirProgress?.setProgress(100)
                    btnUpgrade?.text = "更新"
                    btnUpgrade?.letterSpacing = 0.4f
                }
                else -> {
                    cirProgress?.setProgress(0)
                    btnUpgrade?.text = "更新"
                    btnUpgrade?.letterSpacing = 0.4f
                }
            }
        }

        fun getpkginfo(pkg: String, mContext: Context, tvAppSize: TextView) {
            val pm = mContext.getPackageManager()
            try {
                val getPackageSizeInfo = pm.javaClass.getMethod(
                        "getPackageSizeInfo", String::class.java,
                        IPackageStatsObserver::class.java)
                getPackageSizeInfo.invoke(pm, pkg, GetPackageDataObserver(tvAppSize))
            } catch (e: Exception) {
            }

        }



        private fun View.clearCache() {
            val mClearCacheObserver = CachePackageDataObserver()

            val mPM = context.packageManager
            val localLong = java.lang.Long.valueOf(Long.MAX_VALUE)
            val classes = arrayOf(java.lang.Long.TYPE, IPackageDataObserver::class.java)
//            val arrayOfObject = arrayOfNulls<Any>(2)
//            var localLong = java.lang.Long.MAX_VALUE
//            arrayOfObject[0] = localLong
//            arrayOfObject[1] = mClearCacheObserver
//            var methods = mPM.javaClass.methods
//
//            for (method in methods) {
//                if ("freeStorageAndNotify" == method.name) {
//                    try {
//                        Log.e("size", " method IPackageDataObserver : " + method.name)
//                        method.invoke(mPM, arrayOfObject[0],arrayOfObject[1])
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                    return
//                }
//            }

            try {
                val localMethod = mPM.javaClass.getMethod("freeStorageAndNotify", *classes)
//                /*
//                     * Start of inner try-catch block
//                     */
                try {
                    localMethod.invoke(mPM, localLong, mClearCacheObserver)
                } catch (e: IllegalArgumentException) {
//                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
//                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
//                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
//
//                /*
//                     * End of inner try-catch block
//                     */
            } catch (e1: NoSuchMethodException) {
//                // TODO Auto-generated catch block
                e1.printStackTrace()
            }
        }
    }

    private class GetPackageDataObserver constructor(tvAppSize: TextView) : IPackageStatsObserver.Stub() {
        private var tvAppSize: TextView

        init {
            this.tvAppSize = tvAppSize
        }

        override fun onGetStatsCompleted(pStats: PackageStats?, succeeded: Boolean) {
            doAsync {
                uiThread {
                    Log.e("size", " pStats cacheSize : " + pStats?.cacheSize?.let { formatFileSize(it) } + " pStats dataSize : " + pStats?.dataSize?.let { formatFileSize(it) } + " pStats codeSize : " + pStats?.codeSize?.let { formatFileSize(it) })
                    var pStatsSize = pStats?.cacheSize!! + pStats.dataSize + pStats.codeSize
                    tvAppSize.text = formatFileSize(pStatsSize)
                }
            }
        }

        /**
         * 获取文件大小
         */
        fun formatFileSize(length: Long): String {
            var result: String? = null
            var sub_string = 0
            if (length >= 1073741824) {
                sub_string = (length.toFloat() / 1073741824).toString().indexOf(
                        ".")
                result = ((length.toFloat() / 1073741824).toString() + "000").substring(0,
                        sub_string + 3) + "GB"
            } else if (length >= 1048576) {
                sub_string = (length.toFloat() / 1048576).toString().indexOf(".")
                result = ((length.toFloat() / 1048576).toString() + "000").substring(0,
                        sub_string + 3) + "MB"
            } else if (length >= 1024) {
                sub_string = (length.toFloat() / 1024).toString().indexOf(".")
                result = ((length.toFloat() / 1024).toString() + "000").substring(0,
                        sub_string + 3) + "KB"
            } else if (length < 1024)
                result = java.lang.Long.toString(length) + "B"
            return result.toString()
        }
    }

    private class CachePackageDataObserver : IPackageDataObserver.Stub() {
        override fun onRemoveCompleted(packageName: String, succeeded: Boolean) {
            Log.e("size", "packageName : " + packageName + " succeeded : " + succeeded)

        }//End of onRemoveCompleted() method
    }//End of CachePackageDataObserver instance inner class
}
