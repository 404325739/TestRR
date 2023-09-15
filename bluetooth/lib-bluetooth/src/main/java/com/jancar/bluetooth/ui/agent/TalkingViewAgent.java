package com.jancar.bluetooth.ui.agent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.bluetooth.core.BtConfig;
import com.jancar.bluetooth.core.FlavorsConfig;
import com.jancar.bluetooth.ui.presenter.IncomingPresenter;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.PhoneWindowUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 全屏通话界面控制的工具类
 */

public class TalkingViewAgent extends BaseViewAgent {

    public static TalkingViewAgent newInstance(@NonNull Context context, @NonNull ViewGroup root) {
        TalkingViewAgent talkingViewAgent = new TalkingViewAgent();
        talkingViewAgent.init(context, root);
        return talkingViewAgent;
    }

    // 全屏的布局，以下定义都针对全屏的通话界面布局
    protected ViewGroup mPhoneMainPanel; // 全屏的布局
    protected TextView mTalkingCallType, mTalkingCallNumber, mTalkingCallName;
    private ViewGroup mPhoneTalkingPanel, mTalkingCallInfoPanel, mThreeCallInfoPanel; // 通话中的布局
    protected TextView mTalkingMute; // mute 按键
    private View mTalkingMuteMic; // mute icon 按键
    protected TextView mTalkingSwitchVoice, mTalkingSwitchVoiceThree; // 语音切换按键，手机切车机，车机切手机
    protected TextView mTalkingSwitchCall, mTalkingMergeCall, mTalkingContacts; // 选择通话，合并通话，通讯录
    protected TextView mThreeCallType, mThreeCallName, mThreeKeepCallName; // 第三方通话，上半部分显示的textView

    private String mCurThreeTalkingName = ""; // 缓存当前第三方通话的名字
    private IncomingPresenter mIncomingPresenter; // 来电页面

    /**
     * 检测是否通话状态混乱了， 只有通话界面和来电界面都隐藏了(悬浮窗界面是否为空界面)才是混乱状态，
     * 这个时候屏幕显示其他界面但无法触摸
     *
     * @return true/false
     */
    public boolean checkNoiseCallStatus() {
        return !(isViewVisible() || mIncomingPresenter.getViewAgent().isViewVisible());
    }

    /**
     * 刷新通话名字
     * @param name
     */
    public void updateName(int status, String name) {
        Logcat.d("status:" + IVIBluetooth.CallStatus.getName(status) + " name:" + name);
        if (name != null) {
            switch (status) {
                case IVIBluetooth.CallStatus.INCOMING:
                case IVIBluetooth.CallStatus.THREE_INCOMING:
                    if (mIncomingPresenter != null) {
                        mIncomingPresenter.getViewAgent().updateName(name);
                    }
                    break;

                case IVIBluetooth.CallStatus.RETAIN:
                    if (!TextUtils.isEmpty(name)) {
                        if (mThreeKeepCallName != null) {
                            mThreeKeepCallName.setText(name);
                        }

                        if (!BluetoothModelUtil.getInstance().isThreeTalking()) { // 不是三方通话时，保持状态，重新当前通话
                            if (mTalkingCallName != null) {
                                mTalkingCallName.setText(name);
                            }
                        }
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_TALKING:
                    if (!TextUtils.isEmpty(name)) {
                        Logcat.d("BluzModelUtil.getInstance().isThreeOutGoing():" + BluetoothModelUtil.getInstance().isThreeOutGoing());
                        if (BluetoothModelUtil.getInstance().isThreeOutGoing()) { // 第三方去电状态， 当前通话状态保存到保持位置
                            if (mThreeKeepCallName != null) {
                                mThreeKeepCallName.setText(name);
                            }
                        } else {
                            if (mThreeCallName != null) {
                                mThreeCallName.setText(name);
                            }
                        }

                        if (mTalkingCallName != null) {
                            mTalkingCallName.setText(name);
                        }
                    }

                    if (mThreeCallName != null) {
                        mCurThreeTalkingName = mThreeCallName.getText().toString();
                    }
                    break;

                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.OUTGOING:
                    if (mTalkingCallName != null) {
                        mTalkingCallName.setText(name);
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_OUTGOING: // 第三方去电
                    if (mThreeKeepCallName != null && mThreeKeepCallName.getText().length() == 0) { // 第三方去电，如果保持状态没有号码，将当前通话状态移到保存状态显示
                        mThreeKeepCallName.setText(mCurThreeTalkingName);
                    }

                    if (!TextUtils.isEmpty(name)) {
                        if (mThreeCallName != null) {
                            mThreeCallName.setText(name);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 刷新通话号码
     * @param number
     */
    public void updateNumber(int status, String number) {
        if (number != null) {
            Logcat.d("status:" + IVIBluetooth.CallStatus.getName(status) + " number:" + number);
            switch (status) {
                case IVIBluetooth.CallStatus.INCOMING:
                case IVIBluetooth.CallStatus.THREE_INCOMING:
                    if (mIncomingPresenter != null) {
                        mIncomingPresenter.getViewAgent().updatePhoneNumber(number);
                    }
                    break;

                case IVIBluetooth.CallStatus.RETAIN:
                    if (mThreeKeepCallName != null) {
                        mThreeKeepCallName.setText(number);
                    }
                    if (!BluetoothModelUtil.getInstance().isThreeTalking()) {
                        if (mTalkingCallNumber != null) {
                            mTalkingCallNumber.setText(number);
                        }
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_TALKING:
                    Logcat.d("BluzModelUtil.getInstance().isThreeOutGoing():" + BluetoothModelUtil.getInstance().isThreeOutGoing());
                    if (BluetoothModelUtil.getInstance().isThreeOutGoing()) { // 第三方去电状态， 当前通话状态保存到保持位置
                        if (mThreeKeepCallName != null) {
                            mThreeKeepCallName.setText(number);
                        }
                    } else {
                        if (mThreeCallName != null) {
                            mThreeCallName.setText(number);
                        }
                    }
                    // 继续往下执行

                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.OUTGOING:
                    if (mTalkingCallNumber != null) {
                        mTalkingCallNumber.setText(number);
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_OUTGOING: // 第三方去电
                    if (mThreeCallName != null) {
                        mThreeCallName.setText(number);
                    }
                    if (mContext != null) {
                        updateType(mContext.getString(R.string.call_out));
                    }
                    break;
            }
        }
    }

    /**
     * 刷新通话类型
     * @param type
     */
    public void updateType(String type) {
        if (type != null) {
            if (mTalkingCallType != null) {
                mTalkingCallType.setText(type);
            }
            if (mThreeCallType != null) {
                mThreeCallType.setText(type);
            }
        }
    }

    /**
     * mute 喇叭icon 刷新
     * @param isMute
     */
    public void updateMuteIcon(boolean isMute) {
        if (mIncomingPresenter != null) {
            mIncomingPresenter.getViewAgent().updateMuteIcon(isMute);
        }

        if (mTalkingMute != null) {
            mTalkingMute.setSelected(isMute);
        }
    }

    /**
     * mute mic icon刷新
     * @param isMuteMic
     */
    public void updateMuteMicIcon(boolean isMuteMic) {
        if (mIncomingPresenter != null) {
            mIncomingPresenter.getViewAgent().updateMuteMicIcon(isMuteMic);
        }

        if (mTalkingMuteMic != null) {
            mTalkingMuteMic.setSelected(isMuteMic);
        }
    }

    public void updateVoiceIcon(boolean isPhone) {
        if (mTalkingSwitchVoice != null) {
            mTalkingSwitchVoice.setSelected(isPhone);
        }

        if (mTalkingSwitchVoiceThree != null) {
            mTalkingSwitchVoiceThree.setSelected(isPhone);
        }
    }

    /**
     * 获取当前layout的宽度
     * @return
     */
    public int getWidth() {
        return PhoneWindowUtil.getScreenWidth(mContext);
    }

    /**
     * 获取当前显示的layout的高度
     * @return
     */
    public int getHeight() {
        return PhoneWindowUtil.getScreenHeight(mContext);
    }

    public IncomingPresenter getIncomingPresenter() {
        return mIncomingPresenter;
    }

    @Override
    public boolean isViewVisible() {
        return (null != mPhoneTalkingPanel) ? mPhoneTalkingPanel.getVisibility() == View.VISIBLE : false;
    }

    @Override
    protected void onVisibilityChanged(boolean isVisibility, int callType) {
        Logcat.d("visibility:" + isVisibility + " callType:" + callType);
        if (mPhoneMainPanel != null) {

            switch (callType) {
                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.OUTGOING:
                    if (mPhoneTalkingPanel != null) mPhoneTalkingPanel.setVisibility(View.VISIBLE);
                    break;

                case IVIBluetooth.CallStatus.INCOMING:
                    if (mPhoneTalkingPanel != null) mPhoneTalkingPanel.setVisibility(View.GONE);
                    break;
            }

            mPhoneMainPanel.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
            updateThreeCallType(BluetoothModelUtil.getInstance().isThreeTalking());

            if (mIncomingPresenter != null) {
                mIncomingPresenter.getViewAgent().setCallType(callType);
            }
        } else {
            Logcat.w("mPhoneMainPanel is null");
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        mPhoneMainPanel = (ViewGroup) root.findViewById(R.id.bt_phone_main_panel);

        // 名字，号码，通话类型显示控件
        mTalkingCallType = (TextView) root.findViewById(R.id.bt_talking_call_type);
        mTalkingCallNumber = (TextView) root.findViewById(R.id.bt_talking_call_phone_number);
        mTalkingCallName = (TextView) root.findViewById(R.id.bt_talking_call_name);
        if (mTalkingCallInfoPanel != null) mTalkingCallInfoPanel.setVisibility(View.VISIBLE);

        // 第三方通话时，名字，通话类型显示
        mThreeCallType = (TextView) root.findViewById(R.id.bt_three_call_type); // 当前正在通话类型，一般显示时间
        mThreeCallName = (TextView) root.findViewById(R.id.bt_three_call_name); // 当前第三方通话，正在通话的号码
        mThreeKeepCallName = (TextView) root.findViewById(R.id.bt_three_keep_call_name); // 当前第三方通话，保留的号码

        // 全屏状态下通话页面的布局
        mPhoneTalkingPanel = (ViewGroup) root.findViewById(R.id.bt_phone_taking_panel);
        mTalkingSwitchVoice = (TextView) root.findViewById(R.id.bt_talking_switch_voice);
        mTalkingSwitchVoiceThree = (TextView) root.findViewById(R.id.bt_talking_switch_voice_three);

        mTalkingContacts = (TextView) root.findViewById(R.id.bt_talking_contacts);
        mTalkingSwitchCall = (TextView) root.findViewById(R.id.bt_talking_switch_talking);
        mTalkingMergeCall = (TextView) root.findViewById(R.id.bt_talking_merge);
        mTalkingMute = (TextView) root.findViewById(R.id.bt_talking_mute);
        mTalkingMuteMic = root.findViewById(R.id.bt_talking_mute_mic);

        if (BtConfig.FEATURE_IS_MUTE_MIC) { // 静mic显示
            if (mTalkingMuteMic != null) {
                mTalkingMuteMic.setVisibility(View.VISIBLE);
            }
            mTalkingMute.setVisibility(View.GONE);
        } else {
            if (mTalkingMuteMic != null) {
                mTalkingMuteMic.setVisibility(View.GONE);
            }
            mTalkingMute.setVisibility(View.VISIBLE);
        }

        ViewGroup phoneIncomingPanel = (ViewGroup) root.findViewById(R.id.bt_phone_incoming_panel);
        initIncomingPresenter(phoneIncomingPanel); // 初始化来电的Presenter
        onBindView(root);
    }

    @Override
    protected void onDestroy() {
        if (mIncomingPresenter != null) {
            mIncomingPresenter.release();
        }
    }

    /**
     * 初始化全屏时的布局
     */
    protected void onBindView(ViewGroup root) {
        mTalkingCallInfoPanel = (ViewGroup) root.findViewById(R.id.bt_talking_call_info);
        mThreeCallInfoPanel = (ViewGroup) root.findViewById(R.id.bt_three_call_info);
        if (mThreeCallInfoPanel != null) mThreeCallInfoPanel.setVisibility(View.GONE);
        if (mTalkingContacts != null) mTalkingContacts.setVisibility(View.VISIBLE);
        if (mTalkingSwitchCall != null) mTalkingSwitchCall.setVisibility(View.GONE);
        if (mTalkingMergeCall != null) mTalkingMergeCall.setVisibility(View.GONE);
    }

    /**
     * 刷新第三方通话状态
     * @param isThreeTalking
     */
    protected void updateThreeCallType(boolean isThreeTalking) {
        Logcat.d("isThreeTalking:" + isThreeTalking);
        if (isThreeTalking) {
            if (mTalkingSwitchVoiceThree != null) mTalkingSwitchVoiceThree.setVisibility(View.VISIBLE);
            if (mTalkingSwitchVoice != null) mTalkingSwitchVoice.setVisibility(View.GONE);
            if (mTalkingSwitchCall != null) mTalkingSwitchCall.setVisibility(View.VISIBLE);
            if (mTalkingMergeCall != null) mTalkingMergeCall.setVisibility(View.VISIBLE);
            if (mTalkingContacts != null) mTalkingContacts.setVisibility(View.GONE);
            if (mThreeCallInfoPanel != null) mThreeCallInfoPanel.setVisibility(View.VISIBLE);
            if (mTalkingCallInfoPanel != null) mTalkingCallInfoPanel.setVisibility(View.GONE);
        } else {
            if (mTalkingSwitchVoiceThree != null) mTalkingSwitchVoiceThree.setVisibility(View.GONE);
            if (mTalkingSwitchVoice != null) mTalkingSwitchVoice.setVisibility(View.VISIBLE);
            if (mTalkingSwitchCall != null) mTalkingSwitchCall.setVisibility(View.GONE);
            if (mTalkingMergeCall != null) mTalkingMergeCall.setVisibility(View.GONE);
            if (mTalkingContacts != null) mTalkingContacts.setVisibility(View.VISIBLE);
            if (mThreeCallInfoPanel != null) mThreeCallInfoPanel.setVisibility(View.GONE);
            if (mTalkingCallInfoPanel != null) mTalkingCallInfoPanel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化来电的界面
     */
    private void initIncomingPresenter(ViewGroup root) {
        if (mIncomingPresenter == null) {
            IncomingViewAgent incomingViewAgent = FlavorsConfig.getDefault().getViewAgent(FlavorsConfig.CLASS_INCOMING_VIEW_AGENT, mContext, root);
            mIncomingPresenter = IncomingPresenter.newInstance(root, incomingViewAgent);
        }
    }

}
