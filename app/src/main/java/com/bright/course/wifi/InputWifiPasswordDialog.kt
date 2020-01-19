package com.bright.course.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.bright.course.R
import com.bright.course.bean.EventWifiRefresh
import com.farproc.wifi.connecter.Wifi
import kotlinx.android.synthetic.main.wifi_input_password_dialog.*
import org.greenrobot.eventbus.EventBus


/**
 * Created by kim on 2018/7/12.
 *
 */
class InputWifiPasswordDialog : DialogFragment() {

    lateinit var scanResult: ScanResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanResult = arguments!!.getParcelable("scanResult")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE);


        return inflater.inflate(R.layout.wifi_input_password_dialog, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvWifiName.text = scanResult.SSID
        tvWifiSecret.text = "安全性\n${scanResult.capabilities}"


        btnCancel.setOnClickListener {
            dismiss()
        }


        btnConnect.setOnClickListener {
            if (etWifiPassword.text.isEmpty()) {
                Toast.makeText(context, "请输入密码", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (etWifiPassword.text.length < 8) {
                Toast.makeText(context, "密码不能小于8位", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val wifiManager: WifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val security = Wifi.ConfigSec.getScanResultSecurity(scanResult)
            val config = Wifi.getWifiConfiguration(wifiManager, scanResult, security)
            val mNumOpenNetworksKept = Settings.Secure.getInt(activity?.contentResolver, Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10)

            val connectResult = Wifi.connectToNewNetwork(activity, wifiManager, scanResult
                    , etWifiPassword.text.toString()
                    , mNumOpenNetworksKept)

            if (connectResult) {
                dismiss()
                EventBus.getDefault().post(EventWifiRefresh())
                Toast.makeText(context, "正在连接", Toast.LENGTH_LONG).show()
            } else {
                EventBus.getDefault().post(EventWifiRefresh())
                Toast.makeText(context, "连接失败", Toast.LENGTH_LONG).show()
            }
        }

    }

}