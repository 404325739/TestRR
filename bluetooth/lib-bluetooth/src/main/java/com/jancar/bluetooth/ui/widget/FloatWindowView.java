package com.jancar.bluetooth.ui.widget;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.jancar.bluetooth.core.FlavorsConfig;
import com.jancar.bluetooth.event.EventNoiseCallStatusCheck;
import com.jancar.bluetooth.ui.agent.BannerViewAgent;
import com.jancar.bluetooth.ui.agent.TalkingViewAgent;
import com.jancar.bluetooth.ui.callback.PhoneWindowCallback;
import com.jancar.bluetooth.ui.presenter.BannerPresenter;
import com.jancar.bluetooth.ui.presenter.TalkingPresenter;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.TalkingTimeUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.setting.IVISetting;
import com.jancar.sdk.utils.Logcat;
import com.jancar.utils.SystemUtil;

import static com.jancar.bluetooth.core.FlavorsConfig.R_LAYOUT_PHONE_CALL;
@Deprecated
public class FloatWindowView extends LinearLayout implements PhoneWindowCallback {
    private Context mContext;
    private TalkingTimeUtil mTalkingTimerUtil; // 记录通话时间的工具类
    private String mCurThreeTalkingPhoneNumber; // 第三方通话的号码

    private boolean mIsCurFullScreen = false; // 当前是否是全屏状态
    private int mCurCallStatus = -1; // 当前的通话类型
    private TalkingPresenter mTakingPresenter; // 全屏的布局
    private BannerPresenter mBannerPresenter; // 半屏的布局
    private String[] mNaviPackages = null;

    private FloatWindowCallback mFloatWindowCallback;
    /**
     * 当前是不是倒车状态
     */
    public boolean sIsCcdOn = false;

    // 获取当前通话悬浮窗是否为全屏
    public boolean isFullScreen() {
        return mIsCurFullScreen;
    }

    public boolean isCcdStatus() {
        return sIsCcdOn;
    }

    public void setCcdStatus(boolean status) {
        sIsCcdOn = status;
    }

    /**
     * 倒车，导航是半屏
     */
    public boolean isNeedFullScreen(String topPackageName) {
        if (isCcdStatus()) return false;
        if (!TextUtils.isEmpty(topPackageName)) {
            Logcat.d("topPackageName:" + topPackageName);
            String defNavPkgName = IVISetting.getDefNaviPackage(mContext);
            if (!TextUtils.isEmpty(defNavPkgName) && topPackageName.startsWith(defNavPkgName)) { // 判断默认导航再前台, 非全屏
                return false;
            } else if (mNaviPackages != null) {
                for (int i = 0; i < mNaviPackages.length; ++i) {
                    if (topPackageName.startsWith(mNaviPackages[i])) { // 导航应用在顶层，非全屏
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onDetachedFromWindow() {
        Logcat.d();
        if (mTakingPresenter != null) {
            mTakingPresenter.release();
        }
        if (mBannerPresenter != null) {
            mBannerPresenter.release();
        }
        super.onDetachedFromWindow();
    }

    public FloatWindowView(Context context, String number, int callType, FloatWindowCallback callback) {
        super(context);

        mContext = context;
        mFloatWindowCallback = callback;

        if (mContext == null) {
            Logcat.e("context null error return");
            return;
        }

        mTalkingTimerUtil = new TalkingTimeUtil();

        mNaviPackages = mContext.getResources().getStringArray(R.array.naviWithFilterList);
        LayoutInflater.from(context).inflate(FlavorsConfig.getDefault().getLayoutId(R_LAYOUT_PHONE_CALL), this); // Maybe InflateException but do not handle
        TalkingViewAgent talkingViewAgent = FlavorsConfig.getDefault().getViewAgent(FlavorsConfig.CLASS_TALKING_VIEW_AGENT, context, this);
        mTakingPresenter = TalkingPresenter.newInstance(this, talkingViewAgent); // 全屏逻辑功能类
        mTakingPresenter.setPhoneWindowCallback(this);

        BannerViewAgent bannerViewAgent = FlavorsConfig.getDefault().getViewAgent(FlavorsConfig.CLASS_BANNER_VIEW_AGENT, context, this);
        mBannerPresenter = BannerPresenter.newInstance(this, bannerViewAgent); // 半屏的逻辑功能类
        mBannerPresenter.setPhoneWindowCallback(this);

        mIsCurFullScreen = !isNeedFullScreen(SystemUtil.getTopPackageName(mContext)); // 第一次初始化
        switchScreenTaking(!mIsCurFullScreen, callType);
        if (callback != null) {
            updateMuteIcon(callback.isMute());
            updateMuteMicIcon(callback.isMuteMic());
        }
    }

    /**
     * 选择是否全屏通话
     *
     * @param isFullScreen
     * @param callType     {@link IVIBluetooth.CallStatus }
     */
    public void switchScreenTaking(boolean isFullScreen, int callType) {
        if (mIsCurFullScreen == isFullScreen && mCurCallStatus == callType) {
            Logcat.w("isFullScreen:" + isFullScreen + " mCurCallStatus:" + IVIBluetooth.CallStatus.getName(mCurCallStatus));
            return;
        }
        Logcat.d("isFullScreen:" + isFullScreen + " callType:" + IVIBluetooth.CallStatus.getName(callType));
        mIsCurFullScreen = isFullScreen;
        mCurCallStatus = callType;
        if (isFullScreen) {
            if (mBannerPresenter != null) {
                mBannerPresenter.getViewAgent().setVisibility(false, callType);
            }
            if (mTakingPresenter != null) {
                mTakingPresenter.getViewAgent().setVisibility(true, callType);
            }
            if (mFloatWindowCallback != null) {
                mFloatWindowCallback.resizeFloatWindow(0, 0, mTakingPresenter.getViewAgent().getWidth(), mTakingPresenter.getViewAgent().getHeight());
            }
        } else {
            if (mTakingPresenter != null) {
                mTakingPresenter.getViewAgent().setVisibility(false, callType);
            }
            if (mBannerPresenter != null) {
                mBannerPresenter.getViewAgent().setVisibility(true, callType);
            }
            if (mFloatWindowCallback != null) {
                mFloatWindowCallback.resizeFloatWindow(0, 0, mBannerPresenter.getViewAgent().getWidth(), mBannerPresenter.getViewAgent().getHeight());
            }
        }
    }

    /**
     * 设置通话类型
     *
     * @param callType
     * @param phoneNumber 当前操作的电话号码
     */
    public void setCallType(int callType, String phoneNumber) {
        switchScreenTaking(mIsCurFullScreen, callType);

        Logcat.d("callType:" + IVIBluetooth.CallStatus.getName(callType) + " phoneNumber:" + phoneNumber);
        if (null != mContext) {
            switch (callType) {
                case IVIBluetooth.CallStatus.TALKING:
                    startUpdateTalkingTime(phoneNumber);
                    return;

                case IVIBluetooth.CallStatus.INCOMING:
                case IVIBluetooth.CallStatus.THREE_INCOMING:
                    if (TextUtils.isEmpty(BluetoothModelUtil.getInstance().getTalkingNumber())) {
                        updateType(mContext.getString(R.string.call_in));
                    }
                    break;

                case IVIBluetooth.CallStatus.OUTGOING:
                    if (TextUtils.isEmpty(BluetoothModelUtil.getInstance().getTalkingNumber())) {
                        updateType(mContext.getString(R.string.call_out));
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_OUTGOING:
                    break;

                case IVIBluetooth.CallStatus.THREE_TALKING: // 第三方接通，重新开始计算接通的时间
                    mCurThreeTalkingPhoneNumber = phoneNumber; // 缓存当前通话的电话号码
                    startUpdateTalkingTime(phoneNumber);
                    break;

                case IVIBluetooth.CallStatus.HANGUP: // 挂断第三方通话
                    Logcat.d("mCurThreeTalkingPhoneNumber:" + mCurThreeTalkingPhoneNumber);
                    if (TextUtils.isEmpty(mCurThreeTalkingPhoneNumber)) {
                    } else {
                        // stopUpdateTalkingTime(mCurThreeTalkingPhoneNumber);
                        mCurThreeTalkingPhoneNumber = "";
                    }
                    break;
            }
        }
    }

    /**
     * 停掉当前这个号码的定时器
     *
     * @param phoneNumber
     */
    public void stopUpdateTalkingTime(String phoneNumber) {
        Logcat.d("phoneNumber:" + phoneNumber);
        mTalkingTimerUtil.stopTalkingTimer(phoneNumber);
    }

    /**
     * 停止所有通话中的定时器
     *
     * @注 只需要在页面销毁的时候停止，手机在切换保持状态时候会发送挂断消息，此时页面做了过滤，如果这个地方停止，会导致每次保持之后，时间清零
     */
    public void stopUpdateTalkingTime() {
        Logcat.d("stop all time!");
        mTalkingTimerUtil.stopAllTalkingTimer();
    }

    /**
     * 刷新通话类型
     *
     * @param type
     */
    public void updateType(String type) {
        if (type != null) {
            if (mTakingPresenter != null) {
                mTakingPresenter.getViewAgent().updateType(type);
            }

            if (mBannerPresenter != null) {
                mBannerPresenter.getViewAgent().updateCallTime(type);
            }
        }
    }

    /**
     * 刷新通话号码
     *
     * @param number
     */
    public void updateNumber(int status, String number) {
        if (!TextUtils.isEmpty(number)) {
            if (mTakingPresenter != null) {
                mTakingPresenter.getViewAgent().updateNumber(status, number.trim());
            }

            if (mBannerPresenter != null) {
                mBannerPresenter.getViewAgent().updatePhoneNumber(status, number.trim());
            }
        }
    }

    /**
     * 刷新通话名字
     *
     * @param name
     */
    public void updateName(int status, String name) {
        if (name != null) {
            if (mTakingPresenter != null) {
                mTakingPresenter.getViewAgent().updateName(status, name.trim());
            }

            if (mBannerPresenter != null) {
                mBannerPresenter.getViewAgent().updateName(status, name.trim());
            }
        }
    }

    @Override
    public void switchScreenTaking(boolean isFullScreen) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (sIsCcdOn && isFullScreen) {
                Logcat.i("if current is ccd on, can not switch full screen");
                return;
            }
        }
        switchScreenTaking(isFullScreen, mCurCallStatus);
    }

    @Override
    public void setMute(boolean isMute) {
        if (mFloatWindowCallback != null) {
            mFloatWindowCallback.setMute(isMute);
        }

        updateMuteIcon(isMute);
    }

    @Override
    public void setMuteMic(boolean isMuteMic) {
        if (mFloatWindowCallback != null) {
            mFloatWindowCallback.setMuteMic(isMuteMic);
        }

        updateMuteMicIcon(isMuteMic);
    }

    @Override
    public void hangupThreeIncoming() {
        // 该挂断方法只是为了刷新UI，第三方来电拒接时，先刷UI，否则会闪现第三方通话的效果
        BluetoothModelUtil.getInstance().setThreeTalking(false);
        setCallType(IVIBluetooth.CallStatus.HANGUP, "");
        EventNoiseCallStatusCheck.onEvent();
    }

    /**
     * 语音通道改变
     *
     * @param isPhone 当前通道是否在手机端
     */
    public void setVoiceChanged(boolean isPhone) {
        if (mTakingPresenter != null) {
            mTakingPresenter.getViewAgent().updateVoiceIcon(isPhone);
        }
    }

    /**
     * 统一刷新全屏，半屏的 icon
     *
     * @param isMute
     */
    public void updateMuteIcon(boolean isMute) {
        if (mTakingPresenter != null) {
            mTakingPresenter.getViewAgent().updateMuteIcon(isMute);
        }
        if (mBannerPresenter != null) {
            mBannerPresenter.getViewAgent().updateMuteIcon(isMute);
        }
    }

    /**
     * 刷新mute mic的状态
     *
     * @param isMuteMic 当前是否是mute mic状态
     */
    public void updateMuteMicIcon(boolean isMuteMic) {
        if (mTakingPresenter != null) {
            mTakingPresenter.getViewAgent().updateMuteMicIcon(isMuteMic);
        }
        if (mBannerPresenter != null) {
            mBannerPresenter.getViewAgent().updateMuteMicIcon(isMuteMic);
        }
    }

    /**
     * 是否已经通话状态混乱了(检测悬浮窗界面是否为空界面)
     *
     * @return
     */
    public boolean isNoiseCallStatus() {
        return mTakingPresenter != null ? mTakingPresenter.getViewAgent().checkNoiseCallStatus() : true;
    }

    /**
     * 开始刷新通话，该方法只是用来刷新通话时间
     */
    private void startUpdateTalkingTime(String phoneNumber) {
        Logcat.d("phoneNumber:" + phoneNumber);
        mTalkingTimerUtil.startTalkingTimer(phoneNumber, new TalkingTimeUtil.TalkingTimeCallback() {
            @Override
            public void updateTalkingText(String text, String phoneNumber) {
                if (TextUtils.equals(phoneNumber, BluetoothModelUtil.getInstance().getTalkingNumber()) &&
                        !BluetoothModelUtil.getInstance().isThreeOutGoing()) {
                    updateType(text);
                }
            }
        });
    }

    public interface FloatWindowCallback {
        /**
         * 设置mute，设置mute 喇叭状态
         *
         * @param isMute
         */
        void setMute(boolean isMute);

        /**
         * 当前是否是mute状态，仅仅指喇叭
         *
         * @return
         */
        boolean isMute();

        /**
         * 设置mute mic状态
         *
         * @param isMuteMic
         */
        void setMuteMic(boolean isMuteMic);

        /**
         * 当前是否是mute mic状态，仅仅指mic
         *
         * @return
         */
        boolean isMuteMic();

        void resizeFloatWindow(int x, int y, int width, int height);
    }
}
