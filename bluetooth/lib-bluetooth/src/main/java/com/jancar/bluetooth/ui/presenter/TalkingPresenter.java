package com.jancar.bluetooth.ui.presenter;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.core.FlavorsConfig;
import com.jancar.bluetooth.event.EventOnNaviStart;
import com.jancar.bluetooth.ui.agent.ContactViewAgent;
import com.jancar.bluetooth.ui.agent.KeypadViewAgent;
import com.jancar.bluetooth.ui.agent.TalkingViewAgent;
import com.jancar.bluetooth.ui.callback.KeypadCallback;
import com.jancar.bluetooth.ui.callback.PhoneWindowCallback;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 全屏通话界面控制的工具类
 */

public class TalkingPresenter extends BaseViewPresenter<TalkingViewAgent> {

    public static TalkingPresenter newInstance(@NonNull ViewGroup root, @NonNull TalkingViewAgent viewAgent) {
        TalkingPresenter talkingPresenter = new TalkingPresenter();
        talkingPresenter.init(root, viewAgent);
        return talkingPresenter;
    }

    private KeypadPresenter mKeypadPresenter; // 键盘
    private ContactPresenter mContactPresenter;  // 联系人
    private PhoneWindowCallback mPhoneCallback;
    private TextView mTalkingMute;
    private View mTalkingMuteMic;

    public void setPhoneWindowCallback(PhoneWindowCallback callback) {
        mPhoneCallback = callback;
        if (mViewAgent != null) {
            IncomingPresenter incomingPresenter = mViewAgent.getIncomingPresenter();
            if (incomingPresenter != null) incomingPresenter.setPhoneWindowCallback(callback);
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        View view = root.findViewById(R.id.bt_talking_switch_voice);
        if (view != null) view.setOnClickListener(mSwitchVoiceClickListener);

        view = root.findViewById(R.id.bt_talking_switch_voice_three);
        if (view != null) view.setOnClickListener(mSwitchVoiceClickListener);

        view = root.findViewById(R.id.bt_talking_switch_talking);
        if (view != null) view.setOnClickListener(mSwitchTalkingClickListener);

        view = root.findViewById(R.id.bt_talking_merge);
        if (view != null) view.setOnClickListener(mMergeClickListener);

        view = root.findViewById(R.id.bt_talking_hangup);
        if (view != null) {
            view.setOnClickListener(mHangupClickListener);
        }

        view = root.findViewById(R.id.bt_talking_navi);
        if (view != null) {
            view.setOnClickListener(mNaviClickListener);
        }

        view = root.findViewById(R.id.bt_talking_contacts);
        if (view != null) {
            view.setOnClickListener(mContactClickListener);
        }

        view = root.findViewById(R.id.bt_talking_keypad);
        if (view != null) {
            view.setOnClickListener(mKeypadClickListener);
        }

        mTalkingMute = (TextView) root.findViewById(R.id.bt_talking_mute);
        if (mTalkingMute != null) {
//            mTalkingMute.setCompoundDrawablesWithIntrinsicBounds(0, FlavorsConfigUtil.getBtMuteDrawableId(), 0, 0);
            mTalkingMute.setOnClickListener(mMuteClickListener);
        }

        mTalkingMuteMic = root.findViewById(R.id.bt_talking_mute_mic);
        if (mTalkingMuteMic != null) {
            mTalkingMuteMic.setOnClickListener(mMuteMicClickListener);
        }

        initKeypadPresenter(root); // 初始化键盘的Presenter
        initContactPresenter(root); // 初始化联系人的Presenter
    }

    @Override
    protected void onDestroy() {
        mPhoneCallback = null;

        if (mKeypadPresenter != null) {
            mKeypadPresenter.release();
        }

        if (mContactPresenter != null) {
            mContactPresenter.release();
        }
    }

    /**
     * 初始化键盘界面
     */
    private void initKeypadPresenter(ViewGroup root) {
        if (mKeypadPresenter == null && mViewAgent != null) {
            KeypadViewAgent keypadViewAgent = FlavorsConfig.getDefault().getViewAgent(FlavorsConfig.CLASS_KEYPAD_VIEW_AGENT, mViewAgent.getContext(), root);
            mKeypadPresenter = KeypadPresenter.newInstance(root, keypadViewAgent);
            mKeypadPresenter.setKeypadCallback(new KeypadCallback() {
                @Override
                public void hangup() {
                    BluetoothModelUtil.getInstance().hangup();
                }
            });
        }
    }

    /**
     * 初始化联系人界面
     */
    private void initContactPresenter(ViewGroup root) {
        if (mContactPresenter == null && mViewAgent != null) {
            ContactViewAgent contactViewAgent = FlavorsConfig.getDefault().getViewAgent(FlavorsConfig.CLASS_CONTACT_VIEW_AGENT, mViewAgent.getContext(), root);
            mContactPresenter = ContactPresenter.newInstance(root, contactViewAgent);
        }
    }

    /**
     * 切换语音通道
     */
    private void changeVoice() {
        BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
        if (btManager == null) return;
        btManager.transferCall(new IBluetoothExecCallback.Stub() {
            @Override
            public void onSuccess(String msg) throws RemoteException {
                // 切换成功，UI不在这里刷新，在PhoneCallWindowManager onEventVoiceChange 方法刷新
                Logcat.d("msg:" + msg);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);
            }
        });
    }

    private View.OnClickListener mSwitchVoiceClickListener = new View.OnClickListener() { // 语音通道切换

        @Override
        public void onClick(View v) {
            changeVoice();
        }
    };

    private View.OnClickListener mHangupClickListener = new View.OnClickListener() { // 挂断电话
        @Override
        public void onClick(View v) {
            Logcat.d("hangup click");
            BluetoothModelUtil.getInstance().hangup();
        }
    };

    private View.OnClickListener mNaviClickListener = new View.OnClickListener() { // 导航

        @Override
        public void onClick(View v) {
            Logcat.d("navi click");
            EventOnNaviStart.onEvent();
        }
    };

    private View.OnClickListener mSwitchTalkingClickListener = new View.OnClickListener() { // 三方通话之间，来回切换

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_SWITCH_CALL);
        }
    };

    private View.OnClickListener mMergeClickListener = new View.OnClickListener() { // 合并通话

        @Override
        public void onClick(View v) {
            BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_MERGE_CALL);
        }
    };

    private View.OnClickListener mKeypadClickListener = new View.OnClickListener() { // 键盘

        @Override
        public void onClick(View v) {
            Logcat.d();
            if (mKeypadPresenter != null) {
                Logcat.d("keypad click");
                mKeypadPresenter.getViewAgent().setVisibility(true);
            }
        }
    };

    private View.OnClickListener mMuteClickListener = new View.OnClickListener() { // 通话界面的静音
        @Override
        public void onClick(View v) {
            if (mPhoneCallback != null && mTalkingMute != null) {
                mPhoneCallback.setMute(!mTalkingMute.isSelected());
            }
        }
    };

    private View.OnClickListener mContactClickListener = new View.OnClickListener() { // 联系人

        @Override
        public void onClick(View v) {
            if (mContactPresenter != null) {
                mContactPresenter.getViewAgent().setVisibility(true);
            }
        }
    };

    private View.OnClickListener mMuteMicClickListener = new View.OnClickListener() { // 通话界面静音mic图标
        @Override
        public void onClick(View v) {
            if (mPhoneCallback != null && mTalkingMuteMic != null) {
                mPhoneCallback.setMuteMic(!mTalkingMuteMic.isSelected());
            }
        }
    };
}
