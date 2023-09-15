package com.jancar.bluetooth.floatbar.Presenter;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.floatbar.callback.PhoneWindowCallback;
import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.PhoneWindowUtil;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

import org.greenrobot.eventbus.EventBus;

/**
 * 全屏通话界面控制的工具类
 */

public class ScreenTalkingPresenter {

    private Context mContext;
    private View mParent;
    private PhoneWindowCallback mPhoneCallback;
    private FrameLayout mFlScreen;
    private KeypadPresenter mKeypadUtil;

    public ScreenTalkingPresenter(Context context, View parent, PhoneWindowCallback callback) {
        mContext = context;
        mParent = parent;
        mPhoneCallback = callback;
        initScreenLayout();                                                                         // 初始化全屏的layout
        initKeypadLayout();                                                                            // 初始化来电的layout
    }

    /**
     * 初始化键盘界面
     */
    private void initKeypadLayout() {
        if (mKeypadUtil == null && mParent != null) {
            mKeypadUtil = new KeypadPresenter(mContext, (LinearLayout) mParent.findViewById(R.id.item_line_number),
                    new KeypadPresenter.KeypadCallback() {
                        @Override
                        public void hangup() {
                            BluetoothModelUtil.getInstance().hangup();
                        }
                    });
        }
    }

    /**
     * 初始化全屏时的布局
     */
    private void initScreenLayout() {
        if (mParent == null || mContext == null) {
            Logcat.w("mParent:" + mParent + " mContext:" + mContext);
            return;
        }

        // 大小是全屏，显示键盘的
        mFlScreen = (FrameLayout) mParent.findViewById(R.id.fl_screen_page);

    }

    /**
     * 刷新通话类型
     *
     * @param type
     */
    public void updateType(String type) {
        if (type != null) {
            /*if (mTvScreenCallType != null) {
                mTvScreenCallType.setText(type);
            }
            if (mTvThreeCallType != null) {
                mTvThreeCallType.setText(type);
            }*/
        }
    }

    /**
     * 显示键盘和切换声音按钮
     */
    public void showKeyboardAndPhone() {
        //通话中，显示数字键盘
        /*if (mKeypadUtil != null) {
            mKeypadUtil.setKeyboardVisibility(true);
        }
        if (mIvCallPhone != null) {
            mIvCallPhone.setVisibility(View.VISIBLE);
        }*/
    }

    /**
     * 隐藏键盘和切换声音按钮
     */
    public void hideKeyboardAndPhone() {
        /*if (mKeypadUtil != null) {
            mKeypadUtil.setKeyboardVisibility(false);
        }
        if (mIvCallPhone != null) {
            mIvCallPhone.setVisibility(View.GONE);
        }*/
    }

    /**
     * 刷新通话号码
     *
     * @param number
     */
    public void updateNumber(int status, String number) {}

    /**
     * 刷新通话名字
     *
     * @param name
     */
    public void updateName(int status, String name) {}

    /**
     * 设置全屏界面的显示和隐藏
     *
     * @param visibility
     */
    public void setVisibility(int visibility, int callType) {
        Logcat.d("visibility:" + visibility + " callType:" + callType);
        if (mFlScreen != null) {
            mFlScreen.setVisibility(visibility);
        }
    }

    public void updateMuteIcon(boolean isMute) {
        /*if (mScreenIncomingPresenter != null) {
            mScreenIncomingPresenter.updateMuteIcon(isMute);
        }

        if (mIbTalkingMute != null) {
            mIbTalkingMute.setSelected(isMute);
        }*/
    }

    public void updateVoiceIcon(boolean isPhone) {
        /*--if (mIbSwitchVoice != null) {
            mIbSwitchVoice.setSelected(isPhone);
        }--*/
        /*if (mIvCallPhone != null) {
            if (isPhone) {
                mIvCallPhone.setBackgroundResource(R.drawable.iv_call_phone);
            } else {
                mIvCallPhone.setBackgroundResource(R.drawable.iv_call_voice);
            }
        }*/
    }

    /**
     * 获取当前layout的宽度
     *
     * @return
     */
    public int getWidth() {
        return PhoneWindowUtil.getScreenWidth(mContext);
    }

    /**
     * 获取当前显示的layout的高度
     *
     * @return
     */
    public int getHeight() {
        return PhoneWindowUtil.getScreenHeight(mContext) - AppUtils.getStatusBarHeight(mContext);
    }

    public static class EventOnNaviStart {

        public static void onEvent() {
            EventBus.getDefault().post(new EventOnNaviStart());
        }

        private EventOnNaviStart() {
            // Empty
        }
    }
}