package com.bright.course.config

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bright.course.BaseActivity
import com.bright.course.R
import com.bright.course.utils.HumanReadableUnit
import kotlinx.android.synthetic.main.activity_app_info.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.intentFor
import java.io.FileInputStream


/**
 * Created by kim on 2018/7/17.
 *
 */
class AppInfoActivity : BaseActivity() {
    companion object {
        fun launch(context: Context, packageInfo: PackageInfo) {
            context.startActivity(context.intentFor<AppInfoActivity>("packageInfo" to packageInfo))
        }
    }

    lateinit var info: PackageInfo
    lateinit var receiver: AppUninstallReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)

        receiver = AppUninstallReceiver()
        info = intent.getParcelableExtra("packageInfo")


        btnBack.setOnClickListener { onBackPressed() }

        registerReceiver(receiver, IntentFilter("android.intent.action.PACKAGE_REMOVED"))

        tvAppName.text = info.applicationInfo.loadLabel(getPackageManager()).toString();
        tvTitle.text = tvAppName.text
        tvAppVersion.text = info.versionName

        try {
            val sourceSize = FileInputStream(info.applicationInfo.sourceDir).getChannel().size();
            val dataSize = FileInputStream(info.applicationInfo.dataDir).getChannel().size();
            val publicSourceSize = FileInputStream(info.applicationInfo.publicSourceDir).getChannel().size();
            tvTotalSize.text = HumanReadableUnit.ByteWithUnitSuffixes(sourceSize + dataSize + publicSourceSize)
            tvApplicationSize.text = HumanReadableUnit.ByteWithUnitSuffixes(sourceSize)
            tvDataSize.text = HumanReadableUnit.ByteWithUnitSuffixes(dataSize)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun onClickRemove(view: View) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:${info.packageName}")
        startActivity(intent)

    }

    class AppUninstallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context is AppInfoActivity) {
                context.finish()
            }
        }
    }


}