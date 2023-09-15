package com.jancar.bluetooth.ui.agent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.core.FlavorsConfig;
import com.jancar.bluetooth.ui.adapter.ContactsAdapter;
import com.jancar.bluetooth.utils.AnimationUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.system.IVIConfig;
import com.jancar.viewbase.widget.LetterSlideBar;

/**
 * 悬浮窗联系人控制工具类.
 */

public class ContactViewAgent extends BaseViewAgent implements LetterSlideBar.OnLetterChangeLister {

    public static ContactViewAgent newInstance(@NonNull Context context, @NonNull ViewGroup root) {
        ContactViewAgent contactViewAgent = new ContactViewAgent();
        contactViewAgent.init(context, root);
        return contactViewAgent;
    }

    private ViewGroup mPhoneContactsPanel;
    private ListView mLvContact;
    private LetterSlideBar mLsbLetter;
    private ContactsAdapter mContactsAdapter;

    @Override
    protected void onCreate(ViewGroup root) {
        mPhoneContactsPanel = (ViewGroup) root.findViewById(R.id.bt_phone_contacts_panel);
        mLvContact = (ListView) root.findViewById(R.id.lv_contact);
        if (mLvContact != null) {
            mContactsAdapter = FlavorsConfig.getDefault().getAdapter(FlavorsConfig.CLASS_CONTACTS_ADAPTER, mContext);
            mContactsAdapter.setIsShowLightStatus(false);
            mLvContact.setAdapter(mContactsAdapter);
        }

        mLsbLetter = (LetterSlideBar) root.findViewById(R.id.lsb_letter);
        if (!IVIConfig.getBluetoothLetterSlideBarVisibility()) {
            if (mLsbLetter != null) mLsbLetter.setVisibility(View.GONE);
        } else {
            if (mLsbLetter != null) {
                mLsbLetter.setListView(mLvContact);
                mLsbLetter.setTvBigLetter((TextView) root.findViewById(R.id.tv_letter));
                mLsbLetter.setOnLetterChangeLister(this);
            }
        }
    }

    /**
     * 是否已经显示
     */
    @Override
    public boolean isViewVisible() {
        return (null != mPhoneContactsPanel) ? mPhoneContactsPanel.getVisibility() == View.VISIBLE : false;
    }

    @Override
    public void onLetterChange(String letter, int position) {
        // Empty
    }

    @Override
    public String getPositionLetter(int position) {
        StPhoneBook data = mContactsAdapter.getItem(position);
        if (data != null && !TextUtils.isEmpty(data.firstLetter)) {
            return "" + data.firstLetter.charAt(0);
        }
        return "";
    }

    @Override
    public int getLetterPosition(String letter) {
        if (!TextUtils.isEmpty(letter)) {
            return mContactsAdapter.getFilterIndex(letter.toLowerCase());
        }
        return -1;
    }

    @Override
    public void onVisibilityChanged(boolean isVisibility, int callType) {
        AnimationUtil.startDownAnimation(isVisibility, mPhoneContactsPanel, mContext);
        if (isVisibility) {
            mContactsAdapter.setData(BluetoothModelUtil.getInstance().getAllPhoneBooks());
        }
    }
}
