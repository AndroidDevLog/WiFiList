package com.example.wifilist;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {
    private List<ScanResult> wifiList;

    private OnItemClickListener listener; // 点击事件监听器接口

    // 点击事件监听器接口
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public WifiListAdapter(List<ScanResult> wifiList) {
        this.wifiList = wifiList;
    }

    public List<ScanResult> getWifiList() {
        return wifiList;
    }

    public void setWifiList(List<ScanResult> wifiList) {
        this.wifiList = wifiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View wifiView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(wifiView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult wifi = wifiList.get(position);
        TextView textView = holder.itemView.findViewById(android.R.id.text1);
        textView.setText(String.format("%s(%s)", wifi.SSID, wifi.capabilities));
        if (wifi.SSID.isEmpty()) {
            textView.setText("Unkown");
        }
        // 设置点击事件监听器
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查监听器是否为 null，然后触发点击事件
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}