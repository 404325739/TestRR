package com.jancar.bluetooth.ui.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.bluetooth.ui.agent.BannerViewAgent;
import com.jancar.bluetooth.ui.callback.PhoneWindowCallback;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.system.IVISystem;
import com.jancar.utils.SystemUtil;

/**
 * 半屏通话界面的逻辑功能类
 */

public class BannerPresenter extends BaseViewPresenter<BannerViewAgent> implements View.OnClickListener {

    public static BannerPresenter newInstance(@NonNull ViewGroup root, @NonNull BannerViewAgent viewAgent) {
        BannerPresenter bannerPresenter = new BannerPresenter();
        bannerPresenter.init(root, viewAgent);
        return bannerPresenter;
    }

    private PhoneWindowCallback mPhoneWindowCallback;

    public void setPhoneWindowCallback(PhoneWindowCallback callback) {
        mPhoneWindowCallback = callback;
    }

    @Override
    public void onClick(View view) { // 静音
        int id = view.getId();
        if (id == R.id.bt_banner_incoming_mute_mic) {
            if (mPhoneWindowCallback != null) {
                mPhoneWindowCallback.setMuteMic(!mViewAgent.isUIMuteMic());
            }
        } else if (id == R.id.bt_banner_incoming_mute) {
            if (mPhoneWindowCallback != null) {
                mPhoneWindowCallback.setMute(!mViewAgent.isUIMute());
            }
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        // 初始化来电的 layout
        View view = root.findViewById(R.id.bt_phone_banner_incoming_panel);
        if (view != null) view.setOnClickListener(mMainPanelClickListener);

        view = root.findViewById(R.id.bt_banner_incoming_mute);
        if (view != null) view.setOnClickListener(this);

        view = root.findViewById(R.id.bt_banner_incoming_mute_mic);
        if (view != null) view.setOnClickListener(this);

        view = root.findViewById(R.id.bt_banner_incoming_answer);
        if (view != null) view.setOnClickListener(mAnswerClickListener);

        view = root.findViewById(R.id.bt_banner_incoming_hangup);
        if (view != null) view.setOnClickListener(mHangupClickListener);

        // 第三方通话
        view = root.findViewById(R.id.bt_banner_three_incoming_hangup);
        if (view != null) view.setOnClickListener(mThreeHangupClickListenter);

        view = root.findViewById(R.id.bt_banner_three_incoming_hangup_answer);
        if (view != null) view.setOnClickListener(mThreeHangupAnswerClickListener);

        view = root.findViewById(R.id.bt_bannel_three_incoming_keep_answer);
        if (view != null) view.setOnClickListener(mThreeKeepAnswerClickListener);

        //  初始化通话界面的 layout
        view  = root.findViewById(R.id.bt_phone_banner_talking_panel);
        if (view != null) view.setOnClickListener(mMainPanelClickListener);

        view = root.findViewById(R.id.bt_banner_talking_hangup);
        if (view != null) view.setOnClickListener(mHangupClickListener);
    }

    @Override
    protected void onDestroy() {
        mPhoneWindowCallback = null;
    }

    private View.OnClickListener mAnswerClickListener = new View.OnClickListener() { // 接听

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().listenPhone();
        }
    };

    private View.OnClickListener mHangupClickListener = new View.OnClickListener() { // 通话界面的挂断

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().hangup();
        }
    };

    private View.OnClickListener mMainPanelClickListener = new View.OnClickListener() { // 切全屏

        @Override
        public void onClick(View v) {
            if (!TextUtils.equals(IVISystem.PACKAGE_CCD, SystemUtil.getTopPackageName(mViewAgent.getContext()))) { // 顶层是倒车，不允许切成全屏
                if (mPhoneWindowCallback != null) {
                    mPhoneWindowCallback.switchScreenTaking(true);
                }
            }
        }
    };

    private View.OnClickListener mThreeHangupClickListenter = new View.OnClickListener() { // 挂断第三方

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_THREE);
        }
    };

    private View.OnClickListener mThreeHangupAnswerClickListener = new View.OnClickListener() { // 挂断当前通话，接听第三方

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_CUR);
        }
    };

    private View.OnClickListener mThreeKeepAnswerClickListener = new View.OnClickListener() { // 保持当前通话，接听第三方

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_ANSWER_THREE);
        }
    };

}
