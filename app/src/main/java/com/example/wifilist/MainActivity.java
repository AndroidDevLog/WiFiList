package com.example.wifilist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 1001;
    private WifiScanner wifiScanner;
    private List<ScanResult> wifiList;
    RecyclerView recyclerView;
    Button scanButton;
    WifiListAdapter wifiListAdapter;

    TextView infoTextView;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiStateReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listenWiFiState();

        // 注册 BroadcastReceiver 监听 Wi-Fi 状态变化
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);

        recyclerView = findViewById(R.id.recycle_view);
        infoTextView = findViewById(R.id.info_textview);
        wifiScanner = new WifiScanner(MainActivity.this);
        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wifiManager.isWifiEnabled()) {
                    Toast.makeText(MainActivity.this, "Wi-Fi is disabled", Toast.LENGTH_SHORT).show();
                    requirePermission();
                    return;
                }
                wifiList = wifiScanner.getWifiList();
                wifiListAdapter.setWifiList(wifiList);
                wifiListAdapter.notifyDataSetChanged();
            }
        });

        requirePermission();
    }

    private void listenWiFiState() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);        // 创建一个 BroadcastReceiver 来监听 Wi-Fi 状态变化
        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifiState = wifiManager.getWifiState();
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        // Wi-Fi 已经打开
                        Toast.makeText(context, "Wi-Fi is enabled", Toast.LENGTH_SHORT).show();
                        checkWiFiState();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        // Wi-Fi 已经关闭
                        Toast.makeText(context, "Wi-Fi is disabled", Toast.LENGTH_SHORT).show();
                        enableWifi();
                        break;
                }
            }
        };
    }
    public void enableWifi() {
        if (!wifiManager.isWifiEnabled()) {
            if (wifiManager.setWifiEnabled(true)) {
                Toast.makeText(this, "Wi-Fi is enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to enable Wi-F", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Wi-Fi is already enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkWiFiState() {
        // 确保 Wi-Fi 已经打开
        if (wifiManager.isWifiEnabled()) {
            // 获取当前连接的 Wi-Fi 信息
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            String ssid = wifiInfo.getSSID(); // 获取 Wi-Fi 的 SSID
            int rssi = wifiInfo.getRssi(); // 获取信号强度
            String bssid = wifiInfo.getBSSID(); // 获取MAC地址

            // 这里可以处理您获取的 Wi-Fi 信息，例如显示在界面上或进行其他操作
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoTextView.setText(ssid + " " + rssi + " " + bssid);
                }
            });
        } else {
            // 如果 Wi-Fi 没有打开，您可以请求用户打开 Wi-Fi
            // 这里添加打开 Wi-Fi 的代码
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoTextView.setText("Not Connecdt Wi-Fi");

                }
            });
            enableWifi();
        }
    }

    private void saveWiFi(String SSID, String passowrd) {
        SharedPreferences sharedPreferences = getSharedPreferences("WiFi", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SSID, passowrd);
        editor.apply();
    }

    private String loadWiFi(String SSID) {
        SharedPreferences sharedPreferences = getSharedPreferences("WiFi", Context.MODE_PRIVATE);
        return sharedPreferences.getString(SSID, "");
    }

    private void showPassword(String SSID) {
        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(SSID);

        // 创建一个 EditText
        final EditText editText = new EditText(MainActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT); // 设置输入类型

        // 将 EditText 添加到 AlertDialog 中
        builder.setView(editText);

        // 添加按钮
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = editText.getText().toString();
                connectWifi(MainActivity.this, SSID, userInput);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // 创建并显示 AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void connectWifi(MainActivity context, String ssid, String password) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                System.out.println("Device is above Android 10....");
                WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(password)
                        .build();

                NetworkRequest networkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(wifiNetworkSpecifier)
                        .build();

                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            Log.d(TAG, "Network is available: " + network);
                            connectivityManager.bindProcessToNetwork(network);
                            future.complete("connected");
                            Toast.makeText(context, ssid, Toast.LENGTH_SHORT).show();
                            checkWiFiState();
                            saveWiFi(ssid, password);
                        }

                        @Override
                        public void onUnavailable() {
                            Log.d(TAG, "Network is unavailable");
                            future.complete("notConnected");
                            Toast.makeText(context, "notConnected", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLosing(@NonNull Network network, int maxMsToLive) {
                            Log.d(TAG, "Network is losing with maxMsToLive: " + maxMsToLive);
                            future.complete("networkLoosing");
                            Toast.makeText(context, "networkLoosing", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            Log.d(TAG, "Network is lost: " + network);
                            System.out.println("fix working....");
                            connectivityManager.bindProcessToNetwork(null);
                            future.complete("networkLost");
                            Toast.makeText(context, "networkLost", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    future.complete("notConnected");
                }
            } else {
                Log.d(TAG, "Android level is low so Executing legacy method");
                future.complete(connectToWifiLegacy(context, ssid, password));
            }
        } catch (Exception e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }
    }

    private String connectToWifiLegacy(MainActivity context, String ssid, String password) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";
        conf.preSharedKey = "\"" + password + "\"";

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // 连接到网络
        int networkId = wifiManager.addNetwork(conf);

        if (networkId != -1) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(networkId, true);
            wifiManager.reconnect();
            Toast.makeText(context, "Connecting to " + ssid, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to add network configuration", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    private void setupRecyelView() {
        wifiList = wifiScanner.getWifiList();
        wifiListAdapter = new WifiListAdapter(wifiList);
        wifiListAdapter.setListener(new WifiListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 处理 RecyclerView 中项的点击事件
                ScanResult itemClicked = wifiList.get(position);
                Toast.makeText(MainActivity.this, itemClicked.SSID, Toast.LENGTH_SHORT).show();
                String password = loadWiFi(itemClicked.SSID);
                if (password.isEmpty()) {
                    showPassword(itemClicked.SSID);
                } else {
                    connectWifi(MainActivity.this, itemClicked.SSID, password);
                }
            }
        });
        recyclerView.setAdapter(wifiListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        // 添加分隔线
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this, R.drawable.line_drawable);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    public void requirePermission() {
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android. Manifest.permission.ACCESS_WIFI_STATE}, PERMISSIONS_REQUEST_CODE);
        } else {
            // 已经有权限，执行您的操作
            Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            setupRecyelView();
            checkWiFiState();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // 检查权限授予结果
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 权限已被授予，执行您的操作
                Toast.makeText(this, "Permission OK", Toast.LENGTH_SHORT).show();
                setupRecyelView();
                checkWiFiState();
            } else {
                // 权限被拒绝，可能需要解释为什么需要这些权限或采取其他适当的措施
                Toast.makeText(this, "Location Permission NG", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }
}