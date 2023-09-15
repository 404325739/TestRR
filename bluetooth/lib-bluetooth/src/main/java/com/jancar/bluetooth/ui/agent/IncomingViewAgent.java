package com.jancar.bluetooth.ui.agent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.bluetooth.core.BtConfig;
import com.jancar.bluetooth.utils.AnimationUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 全屏下来电的逻辑类.
 */

public class IncomingViewAgent extends BaseViewAgent implements View.OnClickListener {

    public static IncomingViewAgent newInstance(@NonNull Context context, @NonNull ViewGroup root) {
        IncomingViewAgent incomingViewAgent = new IncomingViewAgent();
        incomingViewAgent.init(context, root);
        return incomingViewAgent;
    }

    private ViewGroup mIncomingMainPanel, mThreeIncomingPanel; // 来电的布局
    protected TextView mIncomingCallType, mIncomingCallPhoneNumber, mIncomingCallName; // 通话类型，电话号码，名字
    protected TextView mIncomingMute;
    private View mIncomingMuteMic;
    protected ViewGroup mPhoneIncomingPanel;

    @Override
    public void onClick(View view) {
        // Do Nothing
    }

    /**
     * 当前UI上是否是mute
     *
     * @return
     */
    public boolean isUIMute() {
        if (mIncomingMute != null) {
            return mIncomingMute.isSelected();
        }
        return false;
    }

    /**
     * 当前UI上是否 mute mic
     * @return
     */
    public boolean isUIMuteMic() {
        if (mIncomingMuteMic != null) {
            return mIncomingMuteMic.isSelected();
        }
        return false;
    }

    /**
     * 刷新mute icon
     *
     * @param isMute
     */
    public void updateMuteIcon(boolean isMute) {
        if (mIncomingMute != null) {
            mIncomingMute.setSelected(isMute);
        }
    }

    /**
     * 刷新 mute mic icon
     * @param isMuteMic
     */
    public void updateMuteMicIcon(boolean isMuteMic) {
        if (mIncomingMuteMic != null) {
            mIncomingMuteMic.setSelected(isMuteMic);
        }
    }

    /**
     * 来电名字
     *
     * @param name
     */
    public void updateName(String name) {
        if (mIncomingCallName != null) {
            mIncomingCallName.setText(name);
        }
    }

    /**
     * 来电号码
     *
     * @param phoneNumber
     */
    public void updatePhoneNumber(String phoneNumber) {
        if (mIncomingCallPhoneNumber != null) {
            mIncomingCallPhoneNumber.setText(phoneNumber);
        }
    }

    /**
     * 设置通话状态
     *
     * @param callType
     */
    public void setCallType(int callType) {
        switch (callType) {
            case IVIBluetooth.CallStatus.INCOMING:
                updateUIIncomming();
                setVisibility(true);
                break;

            case IVIBluetooth.CallStatus.THREE_INCOMING:
                updateUIThreeInComming();
                setVisibility(true);
                break;

            case IVIBluetooth.CallStatus.TALKING:
            case IVIBluetooth.CallStatus.OUTGOING:
            case IVIBluetooth.CallStatus.RETAIN:
                setVisibility(false);
                break;
        }
    }

    protected void updateUIIncomming() {
        if (mIncomingMainPanel != null) mIncomingMainPanel.setVisibility(View.VISIBLE);
        if (mThreeIncomingPanel != null) mThreeIncomingPanel.setVisibility(View.GONE);
    }

    protected void updateUIThreeInComming() {
        if (mIncomingMainPanel != null) mIncomingMainPanel.setVisibility(View.GONE);
        if (mThreeIncomingPanel != null) mThreeIncomingPanel.setVisibility(View.VISIBLE);
    }

    /**
     * 是否已经显示
     */
    @Override
    public boolean isViewVisible() {
        return (null != mIncomingMainPanel) ? mIncomingMainPanel.getVisibility() == View.VISIBLE : false;
    }

    @Override
    protected void onVisibilityChanged(boolean isVisibility, int callType) {
        Logcat.d("isVisibility:" + isVisibility + " mPhoneIncomingPanel.getVisibility():" + mPhoneIncomingPanel.getVisibility());
        if ((isVisibility && mPhoneIncomingPanel.getVisibility() != View.VISIBLE) || // 从隐藏到显示
                (!isVisibility && mPhoneIncomingPanel.getVisibility() == View.VISIBLE)) { // 从显示到隐藏
            AnimationUtil.startDownAnimation(isVisibility, mPhoneIncomingPanel, mContext);
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        // 全屏状态下来电页面布局
        mPhoneIncomingPanel = (ViewGroup) root.findViewById(R.id.bt_phone_incoming_panel);
        mIncomingMainPanel = (ViewGroup) root.findViewById(R.id.bt_incoming_main_panel);

        mIncomingMute = (TextView) root.findViewById(R.id.bt_incoming_mute);
        mIncomingMuteMic = root.findViewById(R.id.bt_incoming_mute_mic);
        if (BtConfig.FEATURE_IS_MUTE_MIC) {
            mIncomingMuteMic.setVisibility(View.VISIBLE);
            mIncomingMute.setVisibility(View.GONE);
        } else {
            mIncomingMute.setVisibility(View.VISIBLE);
            mIncomingMuteMic.setVisibility(View.GONE);
        }

        // 来电页面，上半部分显示
        mIncomingCallType = (TextView) root.findViewById(R.id.bt_talking_call_type);
        mIncomingCallPhoneNumber = (TextView) root.findViewById(R.id.bt_talking_call_phone_number);
        mIncomingCallName = (TextView) root.findViewById(R.id.bt_talking_call_name);
        mIncomingCallType.setText(R.string.call_in);

        // 第三方通话部分
        mThreeIncomingPanel = (ViewGroup) root.findViewById(R.id.bt_incoming_three_panel);

        if (mThreeIncomingPanel != null) mPhoneIncomingPanel.setOnClickListener(this); // 截获掉整个页面的点击事件，防止透传
    }
}
