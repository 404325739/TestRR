package com.jancar.bluetooth.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.NumberFormatUtil;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.ListUtils;
import com.jancar.sdk.utils.Logcat;

import java.util.List;
import java.util.Locale;

import me.jessyan.autosize.AutoSize;

/**
 * @anthor Tzq
 * @describe 通话记录适配器
 */
public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    private List<StCallHistory> mStCallHistoryList;
    private Context mContext;
    public static final int HISTORY_COUNT = 1;
    private boolean isAc8257_YQQD_DY801;
    private int mSelectedPosition = 0;// 号码的通话数量
    private boolean isZh = true; //是否为中文

    public RecordsAdapter(Context context, List<StCallHistory> historyList) {
        this.mContext = context;
        isZh = isZh();
        this.mStCallHistoryList = historyList;
        isAc8257_YQQD_DY801 = AppUtils.isAc8257_YQQD_DY801Platform(mContext);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        AutoSize.autoConvertDensityOfGlobal(AppUtils.getActivityFromContext(mContext));
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_records_recycle, viewGroup, false);
        ViewHolder holder = new ViewHolder(mView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        AutoSize.autoConvertDensityOfGlobal(AppUtils.getActivityFromContext(mContext));
        final int position = viewHolder.getAdapterPosition();
        StCallHistory stCallHistory = getItem(position);
		int color = mContext.getResources().getColor(R.color.white, null);
        switch (stCallHistory.status) {
            case IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS:                               // 未接
                viewHolder.mImCallType.setImageResource(R.drawable.iv_records_miss);

				color = mContext.getResources().getColor(R.color.tv_record_miss, null);
//                viewHolder.mTvAddress.setTextColor(mContext.getResources().getColor(R.color.tv_record_miss));
                break;
            case IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS:                             // 已拨
                viewHolder.mImCallType.setImageResource(R.drawable.iv_records_out);
                break;
            case IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS:                             // 已接
				color = mContext.getResources().getColor(R.color.tv_record_incoming, null);
                viewHolder.mImCallType.setImageResource(R.drawable.iv_records_in);
                break;
        }

        if (isAc8257_YQQD_DY801) {
            viewHolder.mLinearLayout.setSelected(i == mSelectedPosition);
        } else {
            viewHolder.mImCallType.setImageTintList(ColorStateList.valueOf(color));
            viewHolder.mTvName.setTextColor(color);
            viewHolder.mTvNumber.setTextColor(color);
        }

		viewHolder.mTvName.setText(TextUtils.isEmpty(stCallHistory.name) ? mContext.getString(R.string.call_number_unknown) : stCallHistory.name);

        if (isZh){
            viewHolder.mTvNumber.setText(NumberFormatUtil.getNumber(stCallHistory.phoneNumber));
        }else{
            viewHolder.mTvNumber.setText(stCallHistory.phoneNumber);
        }


        if (stCallHistory.mCount > HISTORY_COUNT) {
            String format = String.format(Locale.US,mContext.getString(R.string.call_record_count), stCallHistory.mCount);
            viewHolder.mTvCallTime.setText(format + stCallHistory.time);
        } else {
            viewHolder.mTvCallTime.setText(stCallHistory.time);
        }
//        // TODO: 2020/1/8  后续修改
//        viewHolder.mTvAddress.setText("广东省深圳市");
        viewHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPosition = position;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStCallHistoryList == null ? 0 : mStCallHistoryList.size();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public StCallHistory getItem(int position) {
        if (position >= 0 && !ListUtils.isEmpty(mStCallHistoryList) && mStCallHistoryList.size() > position) {
            return mStCallHistoryList.get(position);
        }
        return null;
    }

    public List<StCallHistory> getDataList() {
        return mStCallHistoryList;
    }

    public void setDataList(List<StCallHistory> stCallHistories) {
        mStCallHistoryList = stCallHistories;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvName;
//        private TextView mTvAddress;
        private TextView mTvNumber;
        private ImageView mImCallType;
        private TextView mTvCallTime;
        private LinearLayout mLinearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvNumber = itemView.findViewById(R.id.tv_records_number);
            mTvName = itemView.findViewById(R.id.tv_records_name);
//            mTvAddress = itemView.findViewById(R.id.tv_records_address);
            mImCallType = itemView.findViewById(R.id.record_type);
            mTvCallTime = itemView.findViewById(R.id.tv_records_time);
            mLinearLayout = itemView.findViewById(R.id.records_rel);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private boolean isZh() {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
