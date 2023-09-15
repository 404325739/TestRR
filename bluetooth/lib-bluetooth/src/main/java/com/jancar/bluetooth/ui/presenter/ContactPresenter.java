package com.jancar.bluetooth.ui.presenter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.bluetooth.ui.agent.ContactViewAgent;
import com.jancar.lib_bluetooth.R;

/**
 * 悬浮窗联系人控制工具类.
 */

public class ContactPresenter extends BaseViewPresenter<ContactViewAgent> implements View.OnClickListener {

    public static ContactPresenter newInstance(@NonNull ViewGroup root, @NonNull ContactViewAgent viewAgent) {
        ContactPresenter contactPresenter = new ContactPresenter();
        contactPresenter.init(root, viewAgent);
        return contactPresenter;
    }

    @Override
    public void onClick(View view) {
        if (mViewAgent != null) {
            mViewAgent.setVisibility(false);
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        View view = root.findViewById(R.id.bt_contacts_down);
        if (null != view) {
            view.setOnClickListener(this);
        }
    }
}
