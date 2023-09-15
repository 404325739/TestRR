package com.jancar.bluetooth.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.core.FlavorsConfig;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.adapter.RRBaseAdapter;

/**
 * 联系人列表的适配器
 */

public class ContactsAdapter extends RRBaseAdapter<StPhoneBook> implements View.OnClickListener {

    protected boolean mIsShowFavorite = true; // 是否显示收藏图标
    protected boolean mIsShowLightStatus = true;

    public ContactsAdapter(Context context) {
        this(context, true);
    }

    public ContactsAdapter(Context context, boolean isShowFavorite) {
        super(context);
        mIsShowFavorite = isShowFavorite;
    }

    /**
     * 设置是否显示点击高亮状态
     * @param isShowLightStatus
     */
    public void setIsShowLightStatus(boolean isShowLightStatus) {
        mIsShowLightStatus = isShowLightStatus;
    }

    @Override
    public int getConvertViewId(int position) {
        return  mIsShowLightStatus ? FlavorsConfig.getDefault().getLayoutId(FlavorsConfig.R_LAYOUT_ITEM_CONTACT)
                :FlavorsConfig.getDefault().getLayoutId(FlavorsConfig.R_LAYOUT_ITEM_CONTACT_NO_CLICKABLE);
    }

    @Override
    public ViewHolder<StPhoneBook> getNewHolder(int position) {
        return new ViewHolder<StPhoneBook>() {
            TextView mTvName; // 名字
            TextView mTvNumber; // 号码
            ImageView mIvFavorite; // 收藏
            RelativeLayout mRlContactBg; // 点击条

            @Override
            public void initHolder(View view, int position) {
                mTvName = (TextView) view.findViewById(R.id.tv_name);
                mTvNumber = (TextView) view.findViewById(R.id.tv_number);
                mIvFavorite = (ImageView) view.findViewById(R.id.iv_favorite);
                mRlContactBg = (RelativeLayout) view.findViewById(R.id.rl_contact_bg);

                if (mIvFavorite != null) {
                    mIvFavorite.setOnClickListener(ContactsAdapter.this);
                }
            }

            @Override
            public void loadData(StPhoneBook data, int position) {
                mTvName.setText(data.name);
                mTvNumber.setText(data.phoneNumber);
                if (mIvFavorite != null) {
                    mIvFavorite.setSelected(data.isFavorite);
                    mIvFavorite.setTag(position);
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        StPhoneBook data = getItem(position);
        if (data != null) {
            data.isFavorite = !data.isFavorite;
            notifyDataSetChanged();

            BluetoothModelUtil.getInstance().updatePhoneBooks(data); // 更新数据库的数据，并通知其他页面，刷新
        }
    }

    /**
     * 跳转检索位置
     * @param letter
     */
    public int getFilterIndex(String letter) {
        if (!TextUtils.isEmpty(letter)) {
            Logcat.d(letter);
            if (TextUtils.equals(letter, "#")) { // 特殊字符处理
                for (int i = 0, s = getCount(); i < s; ++i) {
                    StPhoneBook data = getItem(i);
                    if (data != null && !TextUtils.isEmpty(data.firstLetter)) {
                        char firstChar = data.firstLetter.charAt(0);
                        if (firstChar < 'a' || firstChar > 'z') { // 跳转到第一个特殊字符的位置
                            return i;
                        }
                    }
                }
            } else { // 字母a-z
                for (int i = 0, s = getCount(); i < s; ++i) {
                    StPhoneBook data = getItem(i);
                    if (data != null && !TextUtils.isEmpty(data.firstLetter)) {
                        if (data.firstLetter.startsWith(letter)) { // 跳转到第一个首字母的位置
                            return i;
                        }
                    }
                }
            }

            if (TextUtils.equals(letter, "a")) {
                return 0;
            }
        }
        return -1;
    }
}
