package com.bright.course.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bright.course.R
import com.bright.course.utils.WIFIUtils
import kotlinx.android.synthetic.main.wifi_info_dialog.*

/**
 * Created by kim on 2018/7/12.
 *
 */
class WifiInfoDialog : DialogFragment() {

    lateinit var scanResult: ScanResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanResult = arguments!!.getParcelable("scanResult")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        getDialog().getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        return inflater.inflate(R.layout.wifi_info_dialog, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wifiManager = activity?.getApplicationContext()?.getSystemService(Context.WIFI_SERVICE) as WifiManager


        tvWifiName.text = scanResult.SSID
        tvWifiSecret.text = "WPA/WPA2 PSK"
        tvWifiXinDao.text = "${scanResult.channelWidth}"
        tvWifiMacAddress.text = WIFIUtils.getMacAddress()
        tvWifiIpAddress.text = WIFIUtils.long2ip(wifiManager.dhcpInfo.ipAddress)
        tvWifiWangGuanAddress.text = WIFIUtils.long2ip(wifiManager.dhcpInfo.gateway)
        tvWifiZiWangYanMa.text = WIFIUtils.long2ip(wifiManager.dhcpInfo.netmask)
        tvWifiBSSID.text = scanResult.BSSID
        tvWifiDNS.text = WIFIUtils.long2ip(wifiManager.dhcpInfo.dns1)


        btnCloseDialog.setOnClickListener {
            dismiss()
        }
    }
}