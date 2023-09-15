package com.jancar.bluetooth.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.jancar.bluetooth.utils.Constants;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

import java.util.ArrayList;
import java.util.List;

/**
 * 已配对设备列表
 */

public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.ViewHolder> {

    OnItemClickListener itemClickListener;
    private List<BluetoothDevice> mPairDevices = new ArrayList<>(); //
    private Context mContext;
    private boolean isAc8257_YQQD_DY801;
    private boolean ispaired;
    private int mSelectedPosition = -1;
    private String status = "";

    public List<BluetoothDevice> getList() {
        return mPairDevices;
    }

    public BluetoothDevice getItem(int position) {
        if (position >= 0 && null != mPairDevices && mPairDevices.size() > position) {
            return mPairDevices.get(position);
        }
        return null;
    }

    /**
     * @return 选中的item，不包括title(返回null)；
     */
    public BluetoothDevice getSelectedItem() {
        return getItem(mSelectedPosition);
    }

    public void setSelectedItem(int position) {
        if (mSelectedPosition == position) {
            Logcat.d("same item, return");
            return;
        }
        int lastPos = mSelectedPosition;
        mSelectedPosition = position;
        if (getItem(lastPos) != null) {
            notifyItemChanged(lastPos, Constants.NOTIFY_TYPE_SELECT_POS);
        }
        if (getItem(mSelectedPosition) != null) {
            notifyItemChanged(mSelectedPosition, Constants.NOTIFY_TYPE_SELECT_POS);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public PairedDeviceAdapter(Context context, List<BluetoothDevice> mDevices, boolean isAc8257_YQQD_DY801, boolean ispaired) {
        this.mContext = context;
        this.mPairDevices = mDevices;
        this.isAc8257_YQQD_DY801 = isAc8257_YQQD_DY801;
        this.ispaired = ispaired;
        if (isAc8257_YQQD_DY801 && getItemCount() > 0) {
            mSelectedPosition = 0;
        }
        Logcat.d("ispaired: " + ispaired + " size: " + mPairDevices.size() + " isAc8257_YQQD_DY801: "
                + isAc8257_YQQD_DY801 + " mSelectedPosition: " + mSelectedPosition
                + " mContext: " + mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_device_pair, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(viewHolder, position);
        } else if (null != mContext) {
            String payload = payloads.get(0).toString();
            Logcat.d("pos: " + position + " payload: " + payload);
            if (Constants.NOTIFY_TYPE_LINK_STATUS.equals(payload)) {
                getItemStatus(getItem(position));
                viewHolder.mTvStatus.setText(status);
            } else if (Constants.NOTIFY_TYPE_SELECT_POS.equals(payload)) {
                if (isAc8257_YQQD_DY801) {
                    viewHolder.mLLayout.setSelected(position == mSelectedPosition);
                }
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        BluetoothDevice bdevice = getItem(position);
        int color = -1;
        if (null != mContext && !isAc8257_YQQD_DY801) {
            color = mContext.getResources().getColor(R.color.white);
            viewHolder.mTvName.setTextColor(color);
            viewHolder.mTvAddr.setTextColor(color);
            viewHolder.mTvStatus.setTextColor(color);
        }
//        Logcat.d(" listPair : " + bdevice.addr + " >> " + bdevice.name + " $$$ "
//                + " connectAddr : " + connectAddr + " color: " + color);

        viewHolder.mLLayout.setVisibility(View.VISIBLE);
        viewHolder.mTvName.setText(bdevice.name);
        viewHolder.mTvAddr.setText(bdevice.addr);
        if (getItemStatus(bdevice)) {
            viewHolder.mIvState.setImageResource(R.drawable.ic_bt_link_h);
            if (null != mContext && !isAc8257_YQQD_DY801) {
                color = mContext.getResources().getColor(R.color.device_linked_color);
                viewHolder.mTvName.setTextColor(color);
                viewHolder.mTvAddr.setTextColor(color);
                viewHolder.mTvStatus.setTextColor(color);
            }
        } else {
            viewHolder.mIvState.setImageResource(R.drawable.ic_bt_link_n);
        }
        viewHolder.mTvStatus.setText(status);
        if (isAc8257_YQQD_DY801) {
            viewHolder.mLLayout.setSelected(position == mSelectedPosition);
        }
        viewHolder.mLLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPosition = viewHolder.getAdapterPosition();
                Logcat.d(" onItemClick item : " + mSelectedPosition);
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(viewHolder.mLLayout, mSelectedPosition);
                }
            }
        });
        viewHolder.mLLayout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(viewHolder.mLLayout, viewHolder.getAdapterPosition());
                }
                return true;
            }
        });

//        viewHolder.mIvState.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Logcat.d(" onItemClick item : " + position);
//                if (itemClickListener != null) {
//                    itemClickListener.onItemClick(viewHolder.mLLayout, position);
//                }
//            }
//        });
    }

    private boolean getItemStatus(BluetoothDevice bdevice) {
        if (null == mContext || null == bdevice) {
            return false;
        }
        String connectAddr = BluetoothCacheUtil.getInstance().getBluzConnectedAddr();
        String lastAddr = BluetoothCacheUtil.getInstance().getLastConnectAddr();
        if (ispaired) {
            status = mContext.getResources().getString(R.string.status_paired);
            if (BluetoothCacheUtil.getInstance().isConnected()
                    && !TextUtils.isEmpty(connectAddr) && TextUtils.equals(bdevice.addr, connectAddr)) {
                status = mContext.getResources().getString(R.string.status_connected);
                return true;
            } else if (!TextUtils.isEmpty(lastAddr)
                    && TextUtils.equals(bdevice.addr, lastAddr)
                    && BluetoothCacheUtil.getInstance().getBluzConnectedStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTING) {
                status = mContext.getResources().getString(R.string.status_connecting);
            }
        } else {
            status = mContext.getResources().getString(R.string.status_unpaired);
            if (!TextUtils.isEmpty(lastAddr)
                    && TextUtils.equals(bdevice.addr, lastAddr)
                    && BluetoothCacheUtil.getInstance().getLastConnectStatus() == IVIBluetooth.BluetoothConnectStatus.BOND_BONDING) {
                status = mContext.getResources().getString(R.string.status_pairing);
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return mPairDevices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvName, mTvStatus;
        private ImageView mIvState;
        private LinearLayout mLLayout;
        private TextView mTvAddr;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_pair_name);
            mTvStatus = itemView.findViewById(R.id.tv_pair_status);
            mIvState = itemView.findViewById(R.id.iv_pair_state);
            mLLayout = itemView.findViewById(R.id.llayout_pair_device);
            mTvAddr = itemView.findViewById(R.id.tv_pair_addr);
        }
    }

}
