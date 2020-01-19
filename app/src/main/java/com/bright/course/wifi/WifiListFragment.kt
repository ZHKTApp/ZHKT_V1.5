package com.bright.course.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bright.course.R
import com.bright.course.bean.EventWifiRefresh
import com.farproc.wifi.connecter.Wifi
import kotlinx.android.synthetic.main.activity_connect_wlan.*
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference


/**
 * Created by kim on 2018/7/9.
 *
 */
class WifiListFragment : Fragment(), OnClickWLANItemListener {


    private val MIN_TIME_INTERVAL = 2000
    private var lastUpdateTime: Long = 0

    private var currentWLAN: ScanResult? = null
    private val isOpenNetwork: Boolean = false

    lateinit var adapter: WiFiItemAdapter
    lateinit var wifiManager: WifiManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = WiFiItemAdapter()
        wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_connect_wlan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setWifiManager(wifiManager);
        adapter.setClickWLANItemListener(this);


        val linearLayoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.wifi_manager_divider))
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(dividerItemDecoration)

        wifiSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            wifiManager.isWifiEnabled = isChecked

            if (isChecked) {
            } else {
                adapter.setResultList(null)
                adapter.notifyDataSetChanged()
            }
        }

        wifiSwitch.isChecked = wifiManager.isWifiEnabled

    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        activity?.registerReceiver(mReceiver, filter)
        wifiManager.startScan()
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(mReceiver);
    }

    override fun onClickWLANItem(scanResult: ScanResult?) {
        currentWLAN = scanResult
//        tvWlanName.setText(currentWLAN.SSID)

//        tvWlanName.performClick()

        val isOpenNetwork = Wifi.ConfigSec.isOpenNetwork(Wifi.ConfigSec.getScanResultSecurity(scanResult))
        val mNumOpenNetworksKept = Settings.Secure.getInt(activity?.contentResolver, Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10)
        val security = Wifi.ConfigSec.getScanResultSecurity(scanResult)
        val config = Wifi.getWifiConfiguration(wifiManager, scanResult, security)
        if (config == null) {
            if (isOpenNetwork) {
                //不需要密码的wifi
                Wifi.connectToNewNetwork(activity, wifiManager, currentWLAN, null, mNumOpenNetworksKept);
                Toast.makeText(context, "连接成功", Toast.LENGTH_LONG).show()

                hideFragment()
            } else {
                // 没有连接过的有密码wifi
                showInputPasswordDialog(scanResult)
            }
        } else {
            val isCurrentNetwork_ConfigurationStatus = config.status == WifiConfiguration.Status.CURRENT
            val info = wifiManager.connectionInfo
            val isCurrentNetwork_WifiInfo = (info != null
                    && TextUtils.equals(info.ssid, scanResult?.SSID)
                    && TextUtils.equals(info.bssid, scanResult?.BSSID))
            if (isCurrentNetwork_ConfigurationStatus || isCurrentNetwork_WifiInfo) {
                //当前已连接的wifi
                showCurrentWifiDialog(scanResult)
            } else {
                //连接过的wifi
                var connResult = false
                connResult = Wifi.connectToConfiguredNetwork(context, wifiManager, config, false)
                if (!connResult) {
                    Toast.makeText(context, R.string.toastFailed, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "连接成功", Toast.LENGTH_LONG).show()
                    hideFragment()
                }
                EventBus.getDefault().post(EventWifiRefresh())
            }
        }
    }

    private fun hideFragment() {
        fragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun showInputPasswordDialog(scanResult: ScanResult?) {
        val bundle = Bundle()
        bundle.putParcelable("scanResult", scanResult)
        val dialog = InputWifiPasswordDialog()
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "p")
    }

    private fun showCurrentWifiDialog(scanResult: ScanResult?) {
        val bundle = Bundle()
        bundle.putParcelable("scanResult", scanResult)
        val dialog = WifiInfoDialog()
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "p")
    }


    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {

                if (System.currentTimeMillis() - lastUpdateTime > MIN_TIME_INTERVAL) {
                    lastUpdateTime = System.currentTimeMillis()
                    updateList(wifiManager.scanResults)
                }
                wifiManager.startScan()

            }

        }
    }

    fun updateList(resultList: List<ScanResult>) {
        val finalList = ArrayList<ScanResult>()

        var isContains: Boolean = false

        var connectedResult: ScanResult? = null

        for (scanResult in resultList) {
            if (scanResult.BSSID == wifiManager.connectionInfo.bssid) {
                connectedResult = scanResult
                continue
            }
            isContains = false
            for (result in finalList) {
                if (scanResult.SSID == result.SSID || connectedResult?.SSID.equals(result.SSID)) {
                    isContains = true
                }
            }
            if (!isContains) {
                finalList.add(scanResult)
            }
        }


        if (null != connectedResult) {

            for (scanResult in finalList) {
                if (scanResult.SSID == connectedResult.SSID) {
                    finalList.remove(scanResult)
                    break
                }
            }
            finalList.add(0, connectedResult)
        }

        adapter.setResultList(finalList)
        adapter.notifyDataSetChanged()
    }

    private class MainHandler : Handler() {
        private var dialogWeakReference: WeakReference<WifiListFragment>? = null

        private fun setDialogWeakReference(dialogWeakReference: WifiListFragment) {
            this.dialogWeakReference = WeakReference(dialogWeakReference)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (null != msg.obj) {
                if (null != dialogWeakReference && dialogWeakReference!!.get() != null) {
                    val resultList = msg.obj as List<ScanResult>
                    dialogWeakReference!!.get()?.updateList(resultList)
                }

            }
        }
    }
}