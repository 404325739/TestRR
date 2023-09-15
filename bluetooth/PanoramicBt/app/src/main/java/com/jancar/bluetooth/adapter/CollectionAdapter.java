package com.jancar.bluetooth.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.utils.NumberFormatUtil;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.sdk.utils.Logcat;

import java.util.List;

/**
 * @anthor Tzq
 * @describe 收藏列表适配器
 */
public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    private List<StPhoneBook> mPhoneBookList;

    public CollectionAdapter(List<StPhoneBook> bookList) {
        this.mPhoneBookList = bookList;
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_collection_recycle, viewGroup, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        final StPhoneBook stPhoneBook = mPhoneBookList.get(position);
        viewHolder.mTvName.setText(stPhoneBook.name);
        viewHolder.mTvNumber.setText(NumberFormatUtil.getNumber(stPhoneBook.phoneNumber));
        viewHolder.mImCollect.setSelected(stPhoneBook.isFavorite);
        viewHolder.mRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
        viewHolder.mImCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消收藏
                stPhoneBook.isFavorite = !stPhoneBook.isFavorite;
                Logcat.d("onBindViewHolder mImCollect " + stPhoneBook.isFavorite);
                mPhoneBookList.remove(position);
                notifyDataSetChanged();
                // 更新数据库的数据，并通知其他页面，刷新
                BluetoothModelUtil.getInstance().updatePhoneBooks(stPhoneBook);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPhoneBookList == null ? 0 : mPhoneBookList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvName;
        private TextView mTvNumber;
        private ImageView mImCollect;
        private RelativeLayout mRelative;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_collection_name);
            mTvNumber = itemView.findViewById(R.id.tv_collection_number);
            mImCollect = itemView.findViewById(R.id.iv_collection);
            mRelative = itemView.findViewById(R.id.collection_rel);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
