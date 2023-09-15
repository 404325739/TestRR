package com.jancar.bluetooth.floatbar;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.jancar.bluetooth.floatbar.Presenter.HalfScreenTalkingPresenter;
import com.jancar.bluetooth.floatbar.Presenter.ScreenTalkingPresenter;
import com.jancar.bluetooth.floatbar.callback.PhoneWindowCallback;
import com.jancar.bluetooth.utils.NumberFormatUtil;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.TalkingTimeUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.setting.IVISetting;
import com.jancar.sdk.utils.AndroidAutoUtil;
import com.jancar.sdk.utils.Logcat;
import com.jancar.utils.SystemUtil;

public class FloatWindowView extends LinearLayout implements PhoneWindowCallback {

    public static final int MAX_NUMBER_LEN = 25;                                                    // 最大号码长度

    private Context mContext;
    private TalkingTimeUtil mTalkingTimerUtil;                                                      // 记录通话时间的工具类
    private String mCurThreeTalkingPhoneNumber;                                                     // 第三方通话的号码

    public interface FloatWindowCallback {
        void setMute(boolean isMute);

        boolean isMute();

        void resizeFloatWindow(int x, int y, int width, int height);

        AndroidAutoUtil getAndroidAutoUtil();
    }
    public interface PresenterCallback {
        void changeFullscreen();
    }

    private FloatWindowCallback mFloatWindowCallback;
    private ScreenTalkingPresenter mScreenTakingPresenter;                                          // 全屏的布局
    private HalfScreenTalkingPresenter mHalfScreenTalking;                                          // 半屏的布局

    private int mCurCallStatus = -1;                                                                // 当前的通话类型
//    private boolean mIsCurFullScreen = false;                                                       // 当前是否是全屏状态
    // 1：全屏；0：半屏；-1：不显示（Auto）
    private int mIsCurFullScreen = 0;
    final int SCREEN_VISIBLE_FULL = 1;
    final int SCREEN_VISIBLE_HALF = 0;
    final int SCREEN_GONE = -1;
    private String[] mNaviPackages = null;

    /**
     * 当前是不是倒车状态
     */
    public boolean sIsCcdOn = false;

    // 获取当前通话悬浮窗是否为全屏
    public int isFullScreen() {
        return mIsCurFullScreen;
    }

    public void setCurFullScreen(int isFull) {
        this.mIsCurFullScreen = isFull;
    }

    public boolean isCcdStatus() {
        return sIsCcdOn;
    }

    public void setCcdStatus(boolean status) {
        sIsCcdOn = status;
    }

    public void setCallStatus(int calltype) {
        this.mCurCallStatus = calltype;
    }

    public int getCallStatus() {
        return mCurCallStatus;
    }

    /**
     * case1倒车，导航是半屏
     * case2.除蓝牙外，都是半屏
     */
    public int isNeedFullScreen(String topPackageName) {
        //by 20200509 lp
        //case1
        //isCcdStatus()||isNaviTop();
        //case2
        if (isCcdStatus()) {
            return SCREEN_VISIBLE_HALF;
        } else if (null != mFloatWindowCallback && mFloatWindowCallback.getAndroidAutoUtil().isAndroidAutoStatus()) {
            return SCREEN_GONE;
        } else {
            return mContext.getPackageName().equals(topPackageName) ? SCREEN_VISIBLE_FULL : SCREEN_VISIBLE_HALF;
        }
    }

    public boolean isNaviTop(String pkg) {
        Logcat.d("isNaviTop :" + pkg);
        if (TextUtils.isEmpty(pkg)) return false;
        String defNavPkgName = IVISetting.getDefNaviPackage(mContext);
        if (pkg.startsWith(defNavPkgName)) return true;
        if (mNaviPackages != null) {
            for (int i = 0; i < mNaviPackages.length; ++i) {
                if (pkg.startsWith(mNaviPackages[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public FloatWindowView(Context context, String number, int callType, FloatWindowCallback
            callback) {
        super(context);
        mContext = context;
        mFloatWindowCallback = callback;

        if (mContext == null) {
            Logcat.e("context null error return");
            return;
        }

        mTalkingTimerUtil = new TalkingTimeUtil();

        mNaviPackages = mContext.getResources().getStringArray(R.array.naviWithFilterList);

        LayoutInflater.from(context).inflate(R.layout.layout_phone_call, this, true);

        mScreenTakingPresenter = new ScreenTalkingPresenter(mContext, this, this); // 全屏逻辑功能类
        mHalfScreenTalking = new HalfScreenTalkingPresenter(mContext, this, this, new PresenterCallback() {

            @Override
            public void changeFullscreen() {
                setFullScreen(mIsCurFullScreen == SCREEN_VISIBLE_FULL ? SCREEN_VISIBLE_HALF : SCREEN_VISIBLE_FULL);
            }
        }); // 半屏的逻辑功能类
        switchScreenTaking(callType);

        if (callback != null) {
            updateMuteIcon(callback.isMute());
        }
    }

    /**
     * 选择是否全屏通话
     *
     * @param callType {@link IVIBluetooth.CallStatus }
     */
    public void switchScreenTaking(int callType) {
        int isFullScreen = isNeedFullScreen(SystemUtil.getTopPackageName(mContext));
        Logcat.w("isFullScreen:" + isFullScreen + " mIsCurFullScreen:" + mIsCurFullScreen +
                " ,mCurCallStatus " + mCurCallStatus + " callType " + callType);
        if (mIsCurFullScreen == isFullScreen && mCurCallStatus == callType) {
            return;
        }
        //挂断直接remove 不必resize 20200525 lp
        /*if ((mCurCallStatus != IVIBluetooth.CallStatus.THREE_INCOMING && mCurCallStatus != IVIBluetooth.CallStatus.THREE_OUTGOING)
                && callType == IVIBluetooth.CallStatus.HANGUP)
            return;*/
        setCallStatus(callType);
        setFullScreen(callType, isFullScreen);
    }

    private void setFullScreen(int isFullScreen) {
        setFullScreen(mCurCallStatus, isFullScreen);
    }

    private void setFullScreen(int callType, int isFullScreen) {
        setCurFullScreen(isFullScreen);

        if (isFullScreen == SCREEN_VISIBLE_FULL) {
            if (mScreenTakingPresenter != null) {
                mScreenTakingPresenter.setVisibility(View.VISIBLE, callType);
            }
            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.setVisibility(View.GONE, callType);
            }
            if (mFloatWindowCallback != null) {
                mFloatWindowCallback.resizeFloatWindow(0, 0, mScreenTakingPresenter.getWidth(), mScreenTakingPresenter.getHeight());
            }
        } else if (isFullScreen == SCREEN_VISIBLE_HALF) {
            if (mScreenTakingPresenter != null) {
                mScreenTakingPresenter.setVisibility(View.GONE, callType);
            }
            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.setVisibility(View.VISIBLE, callType);
            }
            if (mFloatWindowCallback != null) {
                mFloatWindowCallback.resizeFloatWindow(0, 0, mHalfScreenTalking.getWidth(), mHalfScreenTalking.getHeight());
            }
        } else if (isFullScreen == SCREEN_GONE)  {
            if (mScreenTakingPresenter != null) {
                mScreenTakingPresenter.setVisibility(View.GONE, callType);
            }
            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.setVisibility(View.GONE, callType);
            }
            if (mFloatWindowCallback != null) {
                mFloatWindowCallback.resizeFloatWindow(0, 0, 0, 0);
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
        switchScreenTaking(callType);
        //更新键盘显示以及切换声音按钮
        updateKeyboardAndPhoneView(callType);

//        Logcat.d("callType:" + IVIBluetooth.CallStatus.getName(callType) + " phoneNumber:" + phoneNumber);
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
                case IVIBluetooth.CallStatus.THREE_TALKING:                                         // 第三方接通，重新开始计算接通的时间
                    mCurThreeTalkingPhoneNumber = phoneNumber;                                      // 缓存当前通话的电话号码
                    startUpdateTalkingTime(phoneNumber);
                    break;
                case IVIBluetooth.CallStatus.HANGUP:                                                // 挂断第三方通话
                    Logcat.d("mCurThreeTalkingPhoneNumber:" + mCurThreeTalkingPhoneNumber);
                    if (TextUtils.isEmpty(mCurThreeTalkingPhoneNumber)) {
                    } else {
                        stopUpdateTalkingTime(mCurThreeTalkingPhoneNumber);
                        mCurThreeTalkingPhoneNumber = "";
                    }
                    break;
            }
        }
    }

    /**
     * 根据通话状态显示隐藏键盘和声音切换按钮
     *
     * @param type
     */
    public void updateKeyboardAndPhoneView(int type) {
        if (mScreenTakingPresenter != null) {
            switch (type) {
                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.THREE_TALKING:
                    mScreenTakingPresenter.showKeyboardAndPhone();
                    break;
                default:
                    mScreenTakingPresenter.hideKeyboardAndPhone();
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
                    updateTime(text);
                }
            }
        });
    }

    /**
     * 刷新通话类型
     *
     * @param type
     */
    public void updateType(String type) {
        if (type != null) {
            if (mScreenTakingPresenter != null) {
                mScreenTakingPresenter.updateType(type);
            }

            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.updateCallTime(type);
            }
        }
    }
    /**
     * 刷新通话时间
     * 8257类型与时间有区分
     *
     * @param time
     */
    public void updateTime(String time) {
        if (time != null) {
            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.updateTime(time);
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
            if (mScreenTakingPresenter != null) {
                mScreenTakingPresenter.updateNumber(status, number.trim());
            }

            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.updatePhoneNumber(status, number.trim());
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
            if (mScreenTakingPresenter != null) {
                //如果名字是电话号码，全屏下，进行格式话显示
                mScreenTakingPresenter.updateName(status, NumberFormatUtil.getNumber(name.trim()));
            }

            if (mHalfScreenTalking != null) {
                mHalfScreenTalking.updateName(status, name.trim());
            }
        }
    }

    @Override
    public void switchScreenTaking() {
        switchScreenTaking(mCurCallStatus);
    }

    @Override
    public void setMute(boolean isMute) {
        if (mFloatWindowCallback != null) {
            mFloatWindowCallback.setMute(isMute);
        }

        updateMuteIcon(isMute);
    }

    @Override
    public void hangupThreeIncoming() {
        // 该挂断方法只是为了刷新UI，第三方来电拒接时，先刷UI，否则会闪现第三方通话的效果
        BluetoothModelUtil.getInstance().setThreeTalking(false);
        setCallType(IVIBluetooth.CallStatus.HANGUP, "");
    }

    /**
     * 语音通道改变
     *
     * @param isPhone 当前通道是否在手机端
     */
    public void setVoiceChanged(boolean isPhone) {
        if (mScreenTakingPresenter != null) {
            mScreenTakingPresenter.updateVoiceIcon(isPhone);
        }
        if (mHalfScreenTalking != null) {
            mHalfScreenTalking.updateVoiceIcon(isPhone);
        }
    }

    /**
     * 统一刷新全屏，半屏的 icon
     *
     * @param isMute
     */
    public void updateMuteIcon(boolean isMute) {
        if (mScreenTakingPresenter != null) {
            mScreenTakingPresenter.updateMuteIcon(isMute);
        }
        if (mHalfScreenTalking != null) {
            mHalfScreenTalking.updateMuteIcon(isMute);
        }
    }

}
