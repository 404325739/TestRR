package com.jancar.bluetooth.ui.agent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.bluetooth.core.BtConfig;
import com.jancar.bluetooth.utils.PhoneWindowUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 半屏通话界面的逻辑功能类
 */

public class BannerViewAgent extends BaseViewAgent {

    public static BannerViewAgent newInstance(@NonNull Context context, @NonNull ViewGroup root) {
        BannerViewAgent bannerViewAgent = new BannerViewAgent();
        bannerViewAgent.init(context, root);
        return bannerViewAgent;
    }

    // 以下是来电 layout
    private ViewGroup mPhoneBannerIncomingPanel, mBannerIncomingPanel, mBannerThreeIncomingPanel;
    private TextView mBannerIncomingCallName, mBannerIncomingCallNumber;
    private ImageView mBannerIncomingMute;
    private View mBannerIncomingMuteMic; // 静音mic图标

    // 以下是通话 layout
    private ViewGroup mPhoneBannerTalkingPanel;
    private TextView mBannerTalkingCallType, mBannerTalkingCallTime; // 通话时间

    private static final int INCOMING_HEIGHT = 120; // 来电界面的高度
    private static final int TAKING_HEIGHT = 44; // 通话高度

    public void updateMuteIcon(boolean isMute) {
        if (mBannerIncomingMute != null) {
            mBannerIncomingMute.setSelected(isMute);
        }
    }

    /**
     * 刷新静音mic的图标
     * @param isMuteMic
     */
    public void updateMuteMicIcon(boolean isMuteMic) {
        if (mBannerIncomingMuteMic != null) {
            mBannerIncomingMuteMic.setSelected(isMuteMic);
        }
    }

    public void updateName(int callType, String name) {
        if (mBannerIncomingCallName != null) {
            if (callType == IVIBluetooth.CallStatus.INCOMING ||
                    callType == IVIBluetooth.CallStatus.THREE_INCOMING) {
                mBannerIncomingCallName.setText(name);
            }
        }
    }

    public void updatePhoneNumber(int callType, String phoneNumber) {
        if (mBannerIncomingCallNumber != null) {
            if (callType == IVIBluetooth.CallStatus.INCOMING || callType == IVIBluetooth.CallStatus.THREE_INCOMING) {
                mBannerIncomingCallNumber.setText(phoneNumber);
            }
        }
    }

    public void updateCallTime(String callTime) {
        if (mBannerTalkingCallTime != null) {
            mBannerTalkingCallTime.setText(callTime);
        }
    }

    private int mCurCallType = IVIBluetooth.CallStatus.NORMAL;
    /**
     * 设置当前半屏页面是否显示
     * @param isVisibility
     * @param callType
     */
    @Override
    protected void onVisibilityChanged(boolean isVisibility, int callType) {
        Logcat.d("visibility:" + isVisibility + " callType:" + IVIBluetooth.CallStatus.getName(callType));
        if (mPhoneBannerIncomingPanel != null && mPhoneBannerTalkingPanel != null) {
            if (callType != IVIBluetooth.CallStatus.THREE_TALKING) {
                // 第三方来电时，部分手机会连续发送两次状态，一次是来电，一次是通话，此时应该还标记为之前状态
                mCurCallType = callType;
            }
            if (isVisibility) {
                switch (mCurCallType) {
                    case IVIBluetooth.CallStatus.INCOMING: // 来电
                        if (mBannerIncomingPanel != null) mBannerIncomingPanel.setVisibility(View.VISIBLE);
                        if (mBannerThreeIncomingPanel != null) mBannerThreeIncomingPanel.setVisibility(View.GONE);
                        if (mPhoneBannerIncomingPanel != null) mPhoneBannerIncomingPanel.setVisibility(View.VISIBLE);
                        if (mPhoneBannerTalkingPanel != null) mPhoneBannerTalkingPanel.setVisibility(View.GONE);
                        break;

                    case IVIBluetooth.CallStatus.THREE_INCOMING: // 第三方来电
                        if (mBannerIncomingPanel != null) mBannerIncomingPanel.setVisibility(View.GONE);
                        if (mBannerThreeIncomingPanel != null) mBannerThreeIncomingPanel.setVisibility(View.VISIBLE);
                        if (mPhoneBannerIncomingPanel != null) mPhoneBannerIncomingPanel.setVisibility(View.VISIBLE);
                        if (mPhoneBannerTalkingPanel != null) mPhoneBannerTalkingPanel.setVisibility(View.GONE);
                        break;

                    case IVIBluetooth.CallStatus.TALKING:
                    case IVIBluetooth.CallStatus.RETAIN:
                    case IVIBluetooth.CallStatus.OUTGOING:
                    case IVIBluetooth.CallStatus.THREE_OUTGOING:
                        if (mBannerTalkingCallType != null) mBannerTalkingCallType.setVisibility(callType == IVIBluetooth.CallStatus.OUTGOING ? View.GONE : View.VISIBLE);
                        if (mPhoneBannerIncomingPanel != null) mPhoneBannerIncomingPanel.setVisibility(View.GONE);
                        if (mPhoneBannerTalkingPanel != null) mPhoneBannerTalkingPanel.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                if (mPhoneBannerIncomingPanel != null) mPhoneBannerIncomingPanel.setVisibility(View.GONE);
                if (mPhoneBannerTalkingPanel != null) mPhoneBannerTalkingPanel.setVisibility(View.GONE);
            }
        } else {
            Logcat.w("mPhoneBannerIncomingPanel: " + mPhoneBannerIncomingPanel + " mPhoneBannerTalkingPanel: " + mPhoneBannerTalkingPanel);
        }
    }

    /**
     * 当前UI上是否是mute状态
     *
     * @return
     */
    public boolean isUIMute() {
        if (mBannerIncomingMute != null) {
            return mBannerIncomingMute.isSelected();
        }
        return false;
    }

    /**
     * 当前UI上是否显示mute mic状态
     * @return
     */
    public boolean isUIMuteMic() {
        if (mBannerIncomingMuteMic != null) {
            return mBannerIncomingMuteMic.isSelected();
        }
        return false;
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
        if (mPhoneBannerIncomingPanel != null) {
            int heightTalking = TAKING_HEIGHT;
            int heightIncommming = INCOMING_HEIGHT;
            if (mContext != null) {
                heightIncommming=  mContext.getResources().getInteger(R.integer.half_screen_height_incomming);
                heightTalking = mContext.getResources().getInteger(R.integer.half_screen_height_talking);
            }
            if (mPhoneBannerIncomingPanel.getVisibility() == View.VISIBLE) {
                return heightIncommming;
            } else {
                return heightTalking;
            }

        }
        return 0;
    }

    @Override
    public boolean isViewVisible() {
        return ((mPhoneBannerIncomingPanel != null ? mPhoneBannerIncomingPanel.getVisibility() == View.VISIBLE : false)
                || ((mPhoneBannerTalkingPanel != null) ? mPhoneBannerTalkingPanel.getVisibility() == View.VISIBLE : false));
    }

    @Override
    protected void onCreate(ViewGroup root) {
        // 初始化来电的 layout
        mPhoneBannerIncomingPanel = (ViewGroup) root.findViewById(R.id.bt_phone_banner_incoming_panel);
        mBannerIncomingPanel = (ViewGroup) root.findViewById(R.id.bt_banner_incoming_panel);
        mBannerIncomingCallName = (TextView) root.findViewById(R.id.bt_banner_incoming_call_name);
        mBannerIncomingCallNumber = (TextView) root.findViewById(R.id.bt_banner_incoming_phone_number);

        mBannerIncomingMute = (ImageView) root.findViewById(R.id.bt_banner_incoming_mute);
        mBannerIncomingMuteMic = root.findViewById(R.id.bt_banner_incoming_mute_mic);
        if (BtConfig.FEATURE_IS_MUTE_MIC) { // 选择是mute mic还是mute 喇叭
            mBannerIncomingMuteMic.setVisibility(View.VISIBLE);
            mBannerIncomingMute.setVisibility(View.GONE);
        } else {
            mBannerIncomingMuteMic.setVisibility(View.GONE);
            mBannerIncomingMute.setVisibility(View.VISIBLE);
        }

        // 第三方通话
        mBannerThreeIncomingPanel = (ViewGroup) root.findViewById(R.id.bt_banner_three_incoming_panel);

        // 初始化通话界面的 layout
        mPhoneBannerTalkingPanel = (ViewGroup) root.findViewById(R.id.bt_phone_banner_talking_panel);
        mBannerTalkingCallTime = (TextView) root.findViewById(R.id.bt_banner_talking_call_time);
        mBannerTalkingCallType = (TextView) root.findViewById(R.id.bt_banner_talking_call_prompt);
    }
}
