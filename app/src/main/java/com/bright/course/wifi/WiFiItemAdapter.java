package com.bright.course.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bright.course.R;
import com.farproc.wifi.connecter.Wifi;

import java.util.List;


/**
 * Created by jinbangzhu on 25/04/2017.
 */

public class WiFiItemAdapter extends RecyclerView.Adapter<WiFiItemAdapter.ViewHolder> {


    private OnClickWLANItemListener clickWLANItemListener;
    private List<ScanResult> resultList;

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    private WifiManager wifiManager;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wifi_manage_dialog_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ScanResult scanResult = resultList.get(position);
//        holder.tvName.setText(scanResult.SSID);
        holder.tvName.setText(scanResult.SSID);

        boolean isOpenNetwork = Wifi.ConfigSec.isOpenNetwork(Wifi.ConfigSec.getScanResultSecurity(scanResult));

//        holder.tvWifiDesc.setText(String.valueOf(scanResult.level));
//        holder.tvWifiDesc.setText(isOpenNetwork ? "开放网络" : "未知网络 " + scanResult.capabilities);
        holder.tvWifiDesc.setText(isOpenNetwork ? "开放网络" : "未知网络");

        final WifiConfiguration config = Wifi.getWifiConfiguration(wifiManager, scanResult, Wifi.ConfigSec.getScanResultSecurity(scanResult));

        if (wifiManager.getConnectionInfo().getBSSID().equals(scanResult.BSSID)) {
            holder.splitLine.setVisibility(View.VISIBLE);
//            holder.tvConnectInfo.setVisibility(View.VISIBLE);
            holder.tvConnectInfo.setVisibility(View.GONE);
            holder.tvConnectInfo.setText("已连接");
            holder.tvWifiDesc.setText("已连接");
            holder.btnOperation.setText("断开");
            holder.btnOperation.setVisibility(View.VISIBLE);
        } else {


            holder.splitLine.setVisibility(View.GONE);
            holder.tvConnectInfo.setVisibility(View.GONE);
            holder.tvWifiDesc.setVisibility(View.VISIBLE);

            if (null != config) {
                holder.btnOperation.setVisibility(View.VISIBLE);
                holder.tvWifiDesc.setText("历史记录");
                holder.btnOperation.setText("忘记");
            } else {
                holder.btnOperation.setVisibility(View.GONE);
            }

        }
        holder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickWLANItemListener) {
                    clickWLANItemListener.onClickWLANItem(scanResult);
                }
            }
        });
        holder.btnOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.getConnectionInfo().getBSSID().equals(scanResult.BSSID)) {//断开

                    wifiManager.disableNetwork(config.networkId);
                } else {//忘记
                    if (null != config) {
                        wifiManager.removeNetwork(config.networkId);
                    }
                    wifiManager.saveConfiguration();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

    public void setResultList(List<ScanResult> resultList) {
        this.resultList = resultList;
    }

    public void setClickWLANItemListener(OnClickWLANItemListener clickWLANItemListener) {
        this.clickWLANItemListener = clickWLANItemListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView btnOperation;
        TextView tvWifiDesc;
        LinearLayout llItem;
        TextView tvConnectInfo;
        View splitLine;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_wifi_name);
            btnOperation = itemView.findViewById(R.id.tv_wifi_connected);
            tvWifiDesc = itemView.findViewById(R.id.tvWifiDesc);
            llItem = itemView.findViewById(R.id.ll_item);
            splitLine = itemView.findViewById(R.id.splitLine);
            tvConnectInfo = itemView.findViewById(R.id.tvConnectInfo);
        }
    }
}
