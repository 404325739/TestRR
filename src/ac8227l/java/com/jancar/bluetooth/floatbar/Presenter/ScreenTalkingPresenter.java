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

public class ScreenTalkingPresenter implements View.OnClickListener {

    // 全屏的布局，以下定义都针对全屏的通话界面布局
    private FrameLayout mFlScreen;                                                                  // 全屏的布局
    private TextView mTvScreenCallName, mTvScreenCallPhoneNumber, mTvScreenCallType;
    private LinearLayout mLlScreenTalking, mLlScreenTalkingText, mLlThreeTalkingText;               // 通话中的布局
    private TextView mIbTalkingMute, mIbSwitchVoice;                                                // mute 按键
    private TextView mIbSwitchCall, mIbMergeCall, mIbContacts;                                      // 选择通话，合并通话，通讯录
    private TextView mTvThreeCallType, mTvThreeCallPhoneNumber, mTvThreeKeepPhoneNumber;            // 第三方通话，上半部分显示的textView

    private ImageView mIvCallPhone, mIvCallHangUp;                                                  //通话中挂断和切换按钮

    private KeypadPresenter mKeypadUtil;                                                            // 键盘

//    private ContactPresenter mContactUtil;                                                        // 联系人

    private ScreenIncomingPresenter mScreenIncomingPresenter;                                       //来电页面

    private Context mContext;
    private View mParent;

    private PhoneWindowCallback mPhoneCallback;

    public ScreenTalkingPresenter(Context context, View parent, PhoneWindowCallback callback) {
        mContext = context;
        mParent = parent;
        mPhoneCallback = callback;
        initScreenLayout();                                                                         // 初始化全屏的layout
        initKeypadLayout();                                                                         // 初始化键盘的layout
        initContactsLayout();                                                                       // 初始化联系人的layout
        initIncomingLayout();                                                                       // 初始化来电的layout
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
     * 初始化联系人界面
     */
    private void initContactsLayout() {
//        if (mContactUtil == null && mParent != null) {
//            mContactUtil = new ContactPresenter(mContext, (LinearLayout) mParent.findViewById(R.id.ll_contacts));
//        }
    }

    /**
     * 初始化来电的界面
     */
    private void initIncomingLayout() {
        if (mScreenIncomingPresenter == null && mParent != null) {
            mScreenIncomingPresenter = new ScreenIncomingPresenter(mContext,
                    (LinearLayout) mParent.findViewById(R.id.call_phone_incoming), mPhoneCallback);
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

        mFlScreen = (FrameLayout) mParent.findViewById(R.id.fl_screen_page);

        // 名字，号码，通话类型显示控件  去电 和 通话中
        mLlScreenTalkingText = (LinearLayout) mParent.findViewById(R.id.ll_screen_call_text);
        mTvScreenCallType = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_type);
        mTvScreenCallPhoneNumber = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_phone_number);
        mTvScreenCallName = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_name);
        //点击按钮
        mIvCallPhone = mLlScreenTalkingText.findViewById(R.id.iv_call_phone);
        mIvCallHangUp = mLlScreenTalkingText.findViewById(R.id.iv_call_hangup);
        mIvCallPhone.setOnClickListener(this);
        mIvCallHangUp.setOnClickListener(this);
        mLlScreenTalkingText.setVisibility(View.VISIBLE);

        // 第三方通话时，名字，通话类型显示
        mLlThreeTalkingText = (LinearLayout) mParent.findViewById(R.id.ll_screen_three_call_text);
        mTvThreeCallType = (TextView) mLlThreeTalkingText.findViewById(R.id.tv_call_type);          // 当前正在通话类型，一般显示时间
        mTvThreeCallPhoneNumber = (TextView) mLlThreeTalkingText.findViewById(R.id.tv_call_name);   // 当前第三方通话，正在通话的号码
//      mTvThreeKeepPhoneNumber = (TextView) mLlThreeTalkingText.findViewById(R.id.tv_keep_call_name); // 当前第三方通话，保留的号码
        mLlThreeTalkingText.setVisibility(View.GONE);

        // 全屏状态下通话页面的布局
        mLlScreenTalking = (LinearLayout) mParent.findViewById(R.id.ll_screen_taking);
//        mIbTalkingMute = (TextView) mLlScreenTalking.findViewById(R.id.ib_talking_mute);
//        mIbSwitchVoice = (TextView) mLlScreenTalking.findViewById(R.id.ib_switch_voice);
//        mIbSwitchCall  = (TextView) mLlScreenTalking.findViewById(R.id.ib_switch_talking);
//        mIbMergeCall   = (TextView) mLlScreenTalking.findViewById(R.id.ib_merge);
//        mIbContacts    = (TextView) mLlScreenTalking.findViewById(R.id.ib_contacts);

//        mIbTalkingMute.setOnClickListener(this);
//        mIbSwitchVoice.setOnClickListener(this);
//        mIbSwitchCall.setOnClickListener(this);
//        mIbMergeCall.setOnClickListener(this);
//        mIbContacts.setOnClickListener(this);

//        mLlScreenTalking.findViewById(R.id.ib_keypad).setOnClickListener(this);
//        mLlScreenTalking.findViewById(R.id.ib_hangup).setOnClickListener(this);
//        mLlScreenTalking.findViewById(R.id.ib_navi).setOnClickListener(this);

//        mIbSwitchCall.setVisibility(View.GONE);
//        mIbMergeCall.setVisibility(View.GONE);
//        mIbContacts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_call_hangup:// 挂断电话
                BluetoothModelUtil.getInstance().hangup();
                break;
            case R.id.iv_call_phone: // 语音通道切换
                changeVoice();
                break;

//            case R.id.ib_switch_voice: // 语音通道切换
//                changeVoice();
//                return;
//
//            case R.id.ib_hangup: // 挂断电话
//                BluetoothModelUtil.getInstance().hangup();
//                return;
//
//            case R.id.ib_answer: // 接听电话
//                BluetoothModelUtil.getInstance().listenPhone();
//                return;
//
//            case R.id.ib_keypad: // 键盘
//                if (mKeypadUtil != null) {
//                    mKeypadUtil.setKeyboardVisibility(true);
//                }
//                return;
//
//            case R.id.ib_talking_mute:  // 通话界面的静音
//                if (mPhoneCallback != null && mScreenIncomingPresenter != null) {
//                    mPhoneCallback.setMute(!mScreenIncomingPresenter.isUIMute());
//                }
//                break;
//
//            case R.id.ib_navi: // 导航
//                EventOnNaviStart.onEvent();
//                break;
//
//            case R.id.ib_contacts: // 联系人
//                if (mContactUtil != null) {
//                    mContactUtil.setContactsVisibility(true);
//                }
//                break;
//
//            case R.id.ib_switch_talking: // 三方通话之间，来回切换
//                BluetoothModelUtil.getInstance().threePartyCallCtrl(
//                        IVIBluetooth.ThreePartyCallCtrl.ACTION_SWITCH_CALL);
//                break;
//
//            case R.id.ib_merge: // 合并通话
//                BluetoothModelUtil.getInstance().threePartyCallCtrl(
//                        IVIBluetooth.ThreePartyCallCtrl.ACTION_MERGE_CALL);
//                break;
        }
    }

    /**
     * 刷新通话类型
     *
     * @param type
     */
    public void updateType(String type) {
        if (type != null) {
            if (mTvScreenCallType != null) {
                mTvScreenCallType.setText(type);
            }
            if (mTvThreeCallType != null) {
                mTvThreeCallType.setText(type);
            }
        }
    }

    /**
     * 显示键盘和切换声音按钮
     */
    public void showKeyboardAndPhone() {
        //通话中，显示数字键盘
        if (mKeypadUtil != null) {
            mKeypadUtil.setKeyboardVisibility(true);
        }
        if (mIvCallPhone != null) {
            mIvCallPhone.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏键盘和切换声音按钮
     */
    public void hideKeyboardAndPhone() {
        if (mKeypadUtil != null) {
            mKeypadUtil.setKeyboardVisibility(false);
        }
        if (mIvCallPhone != null) {
            mIvCallPhone.setVisibility(View.GONE);
        }
    }

    /**
     * 刷新通话号码
     *
     * @param number
     */
    public void updateNumber(int status, String number) {
        if (number != null) {
//            Logcat.d("status:" + IVIBluetooth.CallStatus.getName(status) + " number:" + number);
            switch (status) {
                case IVIBluetooth.CallStatus.INCOMING:
                case IVIBluetooth.CallStatus.THREE_INCOMING:
                    if (mScreenIncomingPresenter != null) {
                        mScreenIncomingPresenter.updatePhoneNumber(number);
                    }
                    break;

                case IVIBluetooth.CallStatus.RETAIN:
                    if (mTvThreeKeepPhoneNumber != null) {
                        mTvThreeKeepPhoneNumber.setText(number);
                    }
                    if (!BluetoothModelUtil.getInstance().isThreeTalking()) {
                        if (mTvScreenCallPhoneNumber != null) {
                            mTvScreenCallPhoneNumber.setText(number);
                        }
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_TALKING:
                    Logcat.d("BluetoothModelUtil.getInstance().isThreeOutGoing():" + BluetoothModelUtil.getInstance().isThreeOutGoing());
                    if (BluetoothModelUtil.getInstance().isThreeOutGoing()) {                       // 第三方去电状态， 当前通话状态保存到保持位置
                        if (mTvThreeKeepPhoneNumber != null) {
                            mTvThreeKeepPhoneNumber.setText(number);
                        } else if (mTvThreeCallPhoneNumber != null) {
                            mTvThreeCallPhoneNumber.setText(number);
                        }
                    } else {
                        if (mTvThreeCallPhoneNumber != null) {
                            mTvThreeCallPhoneNumber.setText(number);
                        }
                    }
                    // 继续往下执行
                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.OUTGOING:
                    if (mTvScreenCallPhoneNumber != null) {
                        mTvScreenCallPhoneNumber.setText(number);
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_OUTGOING:                                        // 第三方去电
                    if (mTvThreeCallPhoneNumber != null) {
                        mTvThreeCallPhoneNumber.setText(number);
                    }
                    if (mContext != null) {
                        updateType(mContext.getString(R.string.call_out));
                    }
                    break;
            }
        }
    }

    private String mCurThreeTalkingName = "";                                                       // 缓存当前第三方通话的名字

    /**
     * 刷新通话名字
     *
     * @param name
     */
    public void updateName(int status, String name) {
//        Logcat.d("status:" + IVIBluetooth.CallStatus.getName(status) + " name:" + name);
        if (name != null) {
            switch (status) {
                case IVIBluetooth.CallStatus.INCOMING:
                case IVIBluetooth.CallStatus.THREE_INCOMING:
                    if (mScreenIncomingPresenter != null) {
                        mScreenIncomingPresenter.updateName(name);
                    }
                    break;

                case IVIBluetooth.CallStatus.RETAIN:
                    if (!TextUtils.isEmpty(name)) {
                        if (mTvThreeKeepPhoneNumber != null) {
                            mTvThreeKeepPhoneNumber.setText(name);
                        }

                        if (!BluetoothModelUtil.getInstance().isThreeTalking()) {                   // 不是三方通话时，保持状态，重新当前通话
                            if (mTvScreenCallName != null) {
                                mTvScreenCallName.setText(name);
                            }
                        }
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_TALKING:
                    if (!TextUtils.isEmpty(name)) {
                        Logcat.d("BluetoothModelUtil.getInstance().isThreeOutGoing():" + BluetoothModelUtil.getInstance().isThreeOutGoing());
                        if (BluetoothModelUtil.getInstance().isThreeOutGoing()) {                   // 第三方去电状态， 当前通话状态保存到保持位置
                            if (mTvThreeKeepPhoneNumber != null) {
                                mTvThreeKeepPhoneNumber.setText(name);
                            } else if (mTvThreeCallPhoneNumber != null) {
                                mTvThreeCallPhoneNumber.setText(name);
                            }
                        } else {
                            if (mTvThreeCallPhoneNumber != null) {
                                mTvThreeCallPhoneNumber.setText(name);
                            }
                        }

                        if (mTvScreenCallName != null) {
                            mTvScreenCallName.setText(name);
                        }
                    }

                    if (mTvThreeCallPhoneNumber != null) {
                        mCurThreeTalkingName = mTvThreeCallPhoneNumber.getText().toString();
                    }
                    break;

                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.OUTGOING:
                    if (mTvScreenCallName != null) {
                        mTvScreenCallName.setText(name);
                    }
                    break;

                case IVIBluetooth.CallStatus.THREE_OUTGOING: // 第三方去电
                    if (mTvThreeKeepPhoneNumber != null && mTvThreeKeepPhoneNumber.getText().length() == 0) { // 第三方去电，如果保持状态没有号码，将当前通话状态移到保存状态显示
                        mTvThreeKeepPhoneNumber.setText(mCurThreeTalkingName);
                    }

                    if (!TextUtils.isEmpty(name)) {
                        if (mTvThreeCallPhoneNumber != null) {
                            mTvThreeCallPhoneNumber.setText(name);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 设置全屏界面的显示和隐藏
     *
     * @param visibility
     */
    public void setVisibility(int visibility, int callType) {
        Logcat.d("visibility:" + visibility + " callType:" + callType);
        if (mFlScreen != null) {
            mFlScreen.setVisibility(visibility);
            switch (callType) {
                case IVIBluetooth.CallStatus.TALKING:
                case IVIBluetooth.CallStatus.OUTGOING:
                case IVIBluetooth.CallStatus.RETAIN:
                case IVIBluetooth.CallStatus.THREE_TALKING:
                    mLlScreenTalking.setVisibility(View.VISIBLE);
                    break;

                case IVIBluetooth.CallStatus.THREE_INCOMING:
                case IVIBluetooth.CallStatus.INCOMING:
                    mLlScreenTalking.setVisibility(View.GONE);
                    break;
            }

            updateThreeCallType(BluetoothModelUtil.getInstance().isThreeTalking());

            if (mScreenIncomingPresenter != null) {
                mScreenIncomingPresenter.setCallType(callType);
            }
        }
    }

    public void updateMuteIcon(boolean isMute) {
        if (mScreenIncomingPresenter != null) {
            mScreenIncomingPresenter.updateMuteIcon(isMute);
        }

        if (mIbTalkingMute != null) {
            mIbTalkingMute.setSelected(isMute);
        }
    }

    public void updateVoiceIcon(boolean isPhone) {
        /*if (mIbSwitchVoice != null) {
            mIbSwitchVoice.setSelected(isPhone);
        }*/
        if (mIvCallPhone != null) {
            if (isPhone) {
                mIvCallPhone.setBackgroundResource(R.drawable.iv_call_phone);
            } else {
                mIvCallPhone.setBackgroundResource(R.drawable.iv_call_voice);
            }
        }
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
        return PhoneWindowUtil.getScreenHeight(mContext)/* - AppUtils.getStatusBarHeight(mContext)*/;
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

    /**
     * 刷新第三方通话状态
     *
     * @param isThreeTalking
     */
    private void updateThreeCallType(boolean isThreeTalking) {
        Logcat.d("isThreeTalking:" + isThreeTalking);
        //暂不启用 20200728
        if (isThreeTalking) {
//            mIbSwitchCall.setVisibility(View.VISIBLE);
//            mIbMergeCall.setVisibility(View.VISIBLE);
//            mIbContacts.setVisibility(View.GONE);
            /*mLlThreeTalkingText.setVisibility(View.VISIBLE);
            mLlScreenTalkingText.setVisibility(View.GONE);*/
            mLlThreeTalkingText.setVisibility(View.GONE);
            mLlScreenTalkingText.setVisibility(View.VISIBLE);
        } else {
//            mIbSwitchCall.setVisibility(View.GONE);
//            mIbMergeCall.setVisibility(View.GONE);
//            mIbContacts.setVisibility(View.VISIBLE);
            mLlThreeTalkingText.setVisibility(View.GONE);
            mLlScreenTalkingText.setVisibility(View.VISIBLE);
        }
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
