package com.jancar.bluetooth.floatbar.Presenter;

import android.content.Context;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.floatbar.FloatWindowView;
import com.jancar.bluetooth.floatbar.callback.PhoneWindowCallback;
import com.jancar.bluetooth.utils.NumberFormatUtil;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.PhoneWindowUtil;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 半屏通话界面的逻辑功能类
 */

public class HalfScreenTalkingPresenter implements View.OnClickListener {

    private Context mContext;
    private View mParent;
    private PhoneWindowCallback mPhoneWindowCallback;

    // 以下是来电 layout
    private LinearLayout mLlTalking, mLlIncomingLayouts, mLlThreeIncomingLayouts;
    private TextView mTvName, mTvPhoneNumber;
    private ImageView mIvAnswer;
    private ImageView mIbSource, mIbThreeSourc;

    // 以下是通话 layout
    private RelativeLayout mRlTalking;
    private TextView mTvCallType, mTvCallTime; // 通话时间


    public HalfScreenTalkingPresenter(Context context, View parent, PhoneWindowCallback callback, FloatWindowView.PresenterCallback callback2) {
        mContext = context;
        mParent = parent;
        mPhoneWindowCallback = callback;

        initIncomingLayout(); // 初始化来电 layout
        initTalkingLayout();
    }

    public void updateMuteIcon(boolean isMute) {
    }

    /**
     * 初始化来电的 layout
     */
    private void initIncomingLayout() {
        if (mContext != null && mParent != null) {
            mLlTalking = (LinearLayout) mParent.findViewById(R.id.ll_half_screen_talking);
            mLlIncomingLayouts = (LinearLayout) mLlTalking.findViewById(R.id.ll_incoming);
            mTvName = (TextView) mLlTalking.findViewById(R.id.tv_call_name);
            mTvPhoneNumber = (TextView) mLlTalking.findViewById(R.id.tv_call_phone_number);
            mTvCallTime = mLlTalking.findViewById(R.id.tv_call_time_type);
            mIvAnswer = mLlTalking.findViewById(R.id.ib_answer);
            mIbSource = mLlTalking.findViewById(R.id.ib_source);
            mIbThreeSourc = mLlTalking.findViewById(R.id.ib_three_source);
            mIbSource.setOnClickListener(this);
            mIbThreeSourc.setOnClickListener(this);
            mLlTalking.setOnClickListener(this);
            mIvAnswer.setOnClickListener(this);
            mLlTalking.findViewById(R.id.ib_hangup).setOnClickListener(this);
            // 第三方通话
            mLlThreeIncomingLayouts = (LinearLayout) mLlTalking.findViewById(R.id.ll_three_incoming);
            mLlTalking.findViewById(R.id.ib_three_hangup).setOnClickListener(this);
            mLlTalking.findViewById(R.id.ib_three_hangup_answer).setOnClickListener(this);
            mLlTalking.findViewById(R.id.ib_three_keep_answer).setOnClickListener(this);
        }
    }

    /**
     * 初始化通话界面的 layout
     */
    private void initTalkingLayout() {
//        if (mContext != null && mParent != null) {
//            mRlTalking  = (RelativeLayout) mParent.findViewById(R.id.rl_half_screen_talking);
//            mTvCallTime = (TextView) mRlTalking.findViewById(R.id.tv_call_time);
//            mTvCallType = (TextView) mRlTalking.findViewById(R.id.tv_call_prompt);
//
//            mRlTalking.findViewById(R.id.ib_talking_hangup).setOnClickListener(this);
//            mRlTalking.setOnClickListener(this);
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_source:
            case R.id.ib_three_source:
                changeVoice();
                break;
            case R.id.ib_answer:
                BluetoothModelUtil.getInstance().listenPhone();
                break;
            case R.id.ib_hangup:
//            case R.id.ib_talking_hangup: // 通话界面的挂断
                BluetoothModelUtil.getInstance().hangup();
                break;

//            case R.id.ll_half_screen_incoming:
//            case R.id.rl_half_screen_talking:
//                if (!TextUtils.equals(IVISystem.PACKAGE_CCD, SystemUtil.getTopPackageName(mContext))) { // 顶层是倒车，不允许切成全屏
//                    if (mPhoneWindowCallback != null) {
//                        mPhoneWindowCallback.switchScreenTaking(true);
//                    }
//                }
//                break;
//
            case R.id.ib_three_hangup: // 挂断第三方
                BluetoothModelUtil.getInstance().threePartyCallCtrl(
                        IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_THREE);
                break;

            case R.id.ib_three_hangup_answer: // 挂断当前通话，接听第三方
                BluetoothModelUtil.getInstance().threePartyCallCtrl(
                        IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_CUR);
                break;
//
//            case R.id.ib_three_keep_answer: // 保持当前通话，接听第三方
//                BluetoothModelUtil.getInstance().threePartyCallCtrl(
//                        IVIBluetooth.ThreePartyCallCtrl.ACTION_ANSWER_THREE);
//                break;
        }
    }

    public void updateName(int callType, String name) {
        if (mTvName != null) {
//            if (callType == IVIBluetooth.CallStatus.INCOMING ||
//                    callType == IVIBluetooth.CallStatus.THREE_INCOMING) {
            mTvName.setText(name);
//            }
        }
    }

    public void updatePhoneNumber(int callType, String phoneNumber) {
        if (mTvPhoneNumber != null) {
//            if (callType == IVIBluetooth.CallStatus.INCOMING ||
//                    callType == IVIBluetooth.CallStatus.THREE_INCOMING) {
            mTvPhoneNumber.setText(NumberFormatUtil.getNumber(phoneNumber));
//            }
        }
    }

    public void updateCallTime(String callTime) {
        if (mTvCallTime != null) {
            mTvCallTime.setText(callTime);
        }
    }

    public void updateTime(String callTime) {
    }

    public void updateVoiceIcon(boolean isPhone) {
        if (mIbSource != null) {
            if (isPhone) {
                mIbSource.setImageResource(R.drawable.iv_call_phone);
            } else {
                mIbSource.setImageResource(R.drawable.iv_call_voice);
            }
        }
    }

    private int mCurCallType = IVIBluetooth.CallStatus.NORMAL;

    /**
     * 设置当前半屏页面是否显示
     *
     * @param visibility
     * @param callType
     */
    public void setVisibility(int visibility, int callType) {
        Logcat.d("visibility:" + visibility + " callType:" + IVIBluetooth.CallStatus.getName(callType));
        if (callType != IVIBluetooth.CallStatus.THREE_TALKING) {
            // 第三方来电时，部分手机会连续发送两次状态，一次是来电，一次是通话，此时应该还标记为之前状态
            mCurCallType = callType;
        }
//        if (mLlTalking != null && mRlTalking != null)
        if (mLlTalking != null) {
            if (visibility == View.VISIBLE) {
                switch (mCurCallType) {
                    case IVIBluetooth.CallStatus.INCOMING: // 来电
                        mLlTalking.setVisibility(View.VISIBLE);
                        mLlIncomingLayouts.setVisibility(View.VISIBLE);
                        mLlThreeIncomingLayouts.setVisibility(View.GONE);
                        if (null != mIvAnswer) {
                            mIvAnswer.setVisibility(View.VISIBLE);
                        }
                        if (mIbSource != null) {
                            mIbSource.setVisibility(View.GONE);
                        }
                        break;

                    case IVIBluetooth.CallStatus.THREE_INCOMING: // 第三方来电
                        mLlIncomingLayouts.setVisibility(View.GONE);
                        mLlThreeIncomingLayouts.setVisibility(View.VISIBLE);
                        mLlTalking.setVisibility(View.VISIBLE);
                        break;

                    case IVIBluetooth.CallStatus.TALKING:
                    case IVIBluetooth.CallStatus.RETAIN:
                    case IVIBluetooth.CallStatus.OUTGOING:
                    case IVIBluetooth.CallStatus.THREE_OUTGOING:
//                        mTvCallType.setVisibility(callType == IVIBluetooth.CallStatus.OUTGOING ? View.GONE : View.VISIBLE);
                        mLlTalking.setVisibility(View.VISIBLE);
                        mLlIncomingLayouts.setVisibility(View.VISIBLE);
                        mLlThreeIncomingLayouts.setVisibility(View.GONE);
                        if (null != mIvAnswer) {
                            mIvAnswer.setVisibility(View.GONE);
                        }
                        if (mIbSource != null) {
                            mIbSource.setVisibility(View.VISIBLE);
                        }
                        if (mIbThreeSourc != null) {
                            mIbThreeSourc.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            } else {
                mLlTalking.setVisibility(View.GONE);
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
        if (mContext != null) {
            Logcat.d("++getHeight Not Null ++");
            return mContext.getResources().getInteger(R.integer.half_screen_height);
        }
        Logcat.d("++getHeight  Null ++");
        return 100;
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
}
