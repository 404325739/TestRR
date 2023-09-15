package com.jancar.bluetooth.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.R;
import com.jancar.btservice.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;


/**
 * 已配对设备列表
 */

public class NewDeviceAdapter extends RecyclerView.Adapter<NewDeviceAdapter.ViewHolder> implements View.OnClickListener {

    NewDeviceAdapter.OnItemClickListener itemClickListener;
    private List<BluetoothDevice> mDiscoveryDevices = new ArrayList<>(); // discovery列表

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setItemClickListener(NewDeviceAdapter.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public NewDeviceAdapter(List<BluetoothDevice> mDevices) {
        this.mDiscoveryDevices = mDevices;
    }

    @NonNull
    @Override
    public NewDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_device_discovery, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final NewDeviceAdapter.ViewHolder viewHolder, int i) {
        final int position = viewHolder.getAdapterPosition();
        viewHolder.mTvName.setText(mDiscoveryDevices.get(i).name);
        BluetoothDevice bdevice = mDiscoveryDevices.get(i);
        BluetoothDevice curConnectDevice = BluetoothCacheUtil.getInstance().getCurConnectedDevice();
        if (!TextUtils.isEmpty(curConnectDevice.addr) && bdevice.addr.equals(curConnectDevice.addr)) {
            viewHolder.mIvState.setImageResource(R.drawable.ic_bt_link_h);
        } else {
            viewHolder.mIvState.setImageResource(R.drawable.ic_bt_link_n);
        }
        viewHolder.mLLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Logcat.d(" onItemClick item : " + i);
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(viewHolder.mLLayout, position);
                }
            }
        });

//        viewHolder.mIvState.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Logcat.d(" onItemClick link item : " + i);
//                if (itemClickListener != null) {
//                    itemClickListener.onItemClick(viewHolder.mLLayout, i);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mDiscoveryDevices.size();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_discovery_state:
                break;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvName;
        private ImageView mIvState;
        private LinearLayout mLLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_discovery_name);
            mIvState = itemView.findViewById(R.id.iv_discovery_state);
            mLLayout = itemView.findViewById(R.id.llayout_discovery_device);
        }
    }

}
