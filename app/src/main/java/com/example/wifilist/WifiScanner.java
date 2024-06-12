package com.example.wifilist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class WifiScanner {

    private final WifiManager wifiManager;

    public WifiScanner(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public List<ScanResult> getWifiList() {
        // 开始Wi-Fi扫描
        wifiManager.startScan();

        return wifiManager.getScanResults();
    }
}