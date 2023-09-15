package com.jancar.bluetooth.ui.presenter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.bluetooth.event.EventOnNaviStart;
import com.jancar.bluetooth.ui.agent.IncomingViewAgent;
import com.jancar.bluetooth.ui.callback.PhoneWindowCallback;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.system.IVIConfig;

/**
 * 全屏下来电的逻辑类.
 */

public class IncomingPresenter extends BaseViewPresenter<IncomingViewAgent> {

    public static IncomingPresenter newInstance(@NonNull ViewGroup root, @NonNull IncomingViewAgent viewAgent) {
        IncomingPresenter incomingPresenter = new IncomingPresenter();
        incomingPresenter.init(root, viewAgent);
        return incomingPresenter;
    }

    private PhoneWindowCallback mPhoneWindowCallback;

    public void setPhoneWindowCallback(PhoneWindowCallback callback) {
        mPhoneWindowCallback = callback;
    }

    @Override
    protected void onCreate(ViewGroup root) {
        View view = root.findViewById(R.id.bt_incoming_answer);
        if (view != null) {
            view.setOnClickListener(mAnswerClickListener);
        }

        view = root.findViewById(R.id.bt_incoming_hangup);
        if (view != null) {
            view.setOnClickListener(mHangupClickListener);
        }

        view = root.findViewById(R.id.bt_incoming_navi);
        if (view != null) {
            if (IVIConfig.getBluetoothNaviBtnVisibility()) { // 蓝牙导航按钮是否显示
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.INVISIBLE);
            }
            view.setOnClickListener(mNaviClickListener);
        }

        view = root.findViewById(R.id.bt_incoming_mute);
        if (view != null) {
            view.setOnClickListener(mMuteClickListener);
        }

        view = root.findViewById(R.id.bt_incoming_mute_mic);
        if (view != null) {
            view.setOnClickListener(mMuteMicClickListener);
        }

        // 第三方通话部分
        view = root.findViewById(R.id.bt_incoming_three_hangup);
        if (view != null) {
            view.setOnClickListener(mThreeHangupClickListener);
        }

        view = root.findViewById(R.id.bt_incoming_three_keep_answer);
        if (view != null) {
            view.setOnClickListener(mThreeKeepAnswerClickListener);
        }

        view = root.findViewById(R.id.bt_incoming_three_hangup_answer);
        if (view != null) {
            view.setOnClickListener(mThreeHangupAnswerClickListener);
        }
    }

    @Override
    protected void onDestroy() {
        mPhoneWindowCallback = null;
    }

    private View.OnClickListener mThreeHangupClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPhoneWindowCallback != null) { // 通知UI，挂断第三方
                mPhoneWindowCallback.hangupThreeIncoming();
            }
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_THREE);
            if (mViewAgent != null) mViewAgent.setVisibility(false);
        }
    };

    private View.OnClickListener mThreeHangupAnswerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_CUR);
        }
    };

    private View.OnClickListener mThreeKeepAnswerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_ANSWER_THREE);
        }
    };

    private View.OnClickListener mAnswerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().listenPhone();
        }
    };

    private View.OnClickListener mHangupClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().hangup();
            if (mViewAgent != null) mViewAgent.setVisibility(false);
        }
    };

    private View.OnClickListener mNaviClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            EventOnNaviStart.onEvent();
        }
    };

    private View.OnClickListener mMuteClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPhoneWindowCallback != null && mViewAgent != null) {
                mPhoneWindowCallback.setMute(!mViewAgent.isUIMute());
            }
        }
    };

    private View.OnClickListener mMuteMicClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPhoneWindowCallback != null && mViewAgent != null) {
                mPhoneWindowCallback.setMuteMic(!mViewAgent.isUIMuteMic());
            }
        }
    };
}
