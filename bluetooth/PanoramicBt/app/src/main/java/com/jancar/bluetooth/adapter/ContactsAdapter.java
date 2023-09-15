package com.jancar.bluetooth.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.NumberFormatUtil;
import com.jancar.bluetooth.view.ThumbUpView;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.bean.StPhoneBook;

import java.util.List;
import java.util.Locale;

/**
 * @anthor Tzq
 * @describe 联系人适配器
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private List<StPhoneBook> mPhoneBookList;
    private Context mContext;
    private boolean isAc8257_YQQD_DY801;
    private int mSelectedPosition = 0;
    private boolean isZh = true; //是否为中文

    public ContactsAdapter(Context mContext,List<StPhoneBook> bookList, boolean isAc8257_YQQD_DY801) {
        this.mPhoneBookList = bookList;
        this.isAc8257_YQQD_DY801 = isAc8257_YQQD_DY801;
        this.mContext = mContext;
        isZh = isZh();
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public StPhoneBook getItem(int position) {
        if (position >= 0 && null != mPhoneBookList && mPhoneBookList.size() > position) {
            return mPhoneBookList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_contact_recycle, viewGroup, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final int position = viewHolder.getAdapterPosition();
        final StPhoneBook stPhoneBook = getItem(position);
        viewHolder.mTvName.setText(stPhoneBook.name);

        if (isZh){
            viewHolder.mTvNumber.setText(NumberFormatUtil.getNumber(stPhoneBook.phoneNumber));
        }else{
            viewHolder.mTvNumber.setText(stPhoneBook.phoneNumber);
        }

        viewHolder.mImCollect.setSelected(stPhoneBook.isFavorite);
//        updateCollectStatus(stPhoneBook.isFavorite, viewHolder.mThumbUpView);
        if (isAc8257_YQQD_DY801) {
            viewHolder.mRelative.setSelected(i == mSelectedPosition);
        }
        viewHolder.mRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPosition = position;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
//        viewHolder.mImCollect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stPhoneBook.isFavorite = !stPhoneBook.isFavorite;
//                notifyDataSetChanged();
//                // 更新数据库的数据，并通知其他页面，刷新
//                BluetoothModelUtil.getInstance().updatePhoneBooks(stPhoneBook);
//            }
//        });

//        viewHolder.mThumbUpView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stPhoneBook.isFavorite = !stPhoneBook.isFavorite;
//                notifyDataSetChanged();
//                // 更新数据库的数据，并通知其他页面，刷新
//                BluetoothModelUtil.getInstance().updatePhoneBooks(stPhoneBook);
//            }
//        });
    }

    /**
     * 设置收藏状态
     */
    private void updateCollectStatus(boolean isFavorite, ThumbUpView mThumbUpView) {
        if (isFavorite) {
            mThumbUpView.Like();
        } else {
            mThumbUpView.UnLike();
        }
    }

    @Override
    public int getItemCount() {
        return mPhoneBookList == null ? 0 : mPhoneBookList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvName;
        private TextView mTvNumber;
        private ImageView mImCollect;
        private View mRelative;
//        private RelativeLayout mRelative;
//        private ThumbUpView mThumbUpView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_contact_name);
            mTvNumber = itemView.findViewById(R.id.tv_contact_number);
            mImCollect = itemView.findViewById(R.id.iv_contact_collect);
//            mThumbUpView = itemView.findViewById(R.id.iv_contact_thumb);
            mRelative = itemView.findViewById(R.id.re_contact);

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
