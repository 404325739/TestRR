package com.jancar.bluetooth.floatbar.Presenter;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
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
    private FloatWindowView.PresenterCallback mPresenterCallback;

    private LinearLayout mLlScreenTalkingText, mLlTalking;
    private TextView mTvScreenCallName, mTvScreenCallType, mTvCallTime;
    private ImageView mIvMute, mIvAnswer, mIvHangup, mIbSource, mIbKeypad;
    private boolean isMute;
    private int mCurCallType;

    public HalfScreenTalkingPresenter(Context context, View parent, PhoneWindowCallback callback, FloatWindowView.PresenterCallback callback2) {
        mContext = context;
        mParent = parent;
        mPhoneWindowCallback = callback;
        mPresenterCallback = callback2;

        initTalkingLayout();
    }

    public void updateMuteIcon(boolean isMute) {
        this.isMute = isMute;
        if (null != mIvMute) {
            if (isMute) {
                mIvMute.setImageResource(R.drawable.iv_call_mute_selector);
            } else {
                mIvMute.setImageResource(R.drawable.iv_call_unmute_selector);
            }
        }
    }
    /**
     * 初始化通话界面的 layout
     */
    private void initTalkingLayout() {
        if (mContext != null && mParent != null) {

            // 文字部分、通话联系人、类型、时间
            mLlScreenTalkingText = (LinearLayout) mParent.findViewById(R.id.ll_screen_call_text);
            mTvScreenCallName = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_name);
            mTvScreenCallType = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_type);
            mTvCallTime = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_time_type);

            // 按键部分
            mLlTalking = (LinearLayout) mParent.findViewById(R.id.ll_half_screen_talking);
            // 静音、接听（通话中时显示静音键、来去电时显示接听键）、挂断、音源（手机/车机）、数字键盘显示/隐藏
            mIvMute = mLlTalking.findViewById(R.id.ib_mute);
            mIvAnswer = mLlTalking.findViewById(R.id.ib_answer);
            mIvHangup = mLlTalking.findViewById(R.id.ib_hangup);
            mIbSource = mLlTalking.findViewById(R.id.ib_source);
            mIbKeypad = mLlTalking.findViewById(R.id.ib_keypad);
            mIvMute.setOnClickListener(this);
            mIvAnswer.setOnClickListener(this);
            mIvHangup.setOnClickListener(this);
            mIbSource.setOnClickListener(this);
            mIbKeypad.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_mute:
                if (mPhoneWindowCallback != null) {
                    mPhoneWindowCallback.setMute(!isMute);
                }
                break;
            case R.id.ib_answer:
                BluetoothModelUtil.getInstance().listenPhone();
                break;
            case R.id.ib_hangup:
                BluetoothModelUtil.getInstance().hangup();
                break;
            case R.id.ib_source:
                changeVoice();
                break;
            case R.id.ib_keypad:
                if (null != mPresenterCallback) {
                    mPresenterCallback.changeFullscreen();
                }
                break;
        }
    }

    public void updateName(int callType, String name) {
        if (mTvScreenCallName != null) {
            mTvScreenCallName.setText(name);
        }
    }

    public void updatePhoneNumber(int callType, String phoneNumber) {
       /* if (mTvPhoneNumber != null) {
//            if (callType == IVIBluetooth.CallStatus.INCOMING ||
//                    callType == IVIBluetooth.CallStatus.THREE_INCOMING) {
            mTvPhoneNumber.setText(NumberFormatUtil.getNumber(phoneNumber));
//            }
        }*/
    }

    /**
     * 刷新通话
     *
     * @param callTime 这里传来的是string，8257需要区分通话中，这里不处理；
     *             1.在setVisibility中通过callType来显示类型；
     *             2.在updateTime中刷新时间；
     */
    public void updateCallTime(String callTime) {
        /*if (mTvCallTime != null) {
            mTvCallTime.setText(callTime);
        }*/
    }

    /**
     * 刷新通话时间
     *
     * @param time
     */
    public void updateTime(String time) {
        if (time != null) {
            if (mTvCallTime != null) {
                mTvCallTime.setText(time);
            }
        }
    }

    public void updateVoiceIcon(boolean isPhone) {
        if (mIbSource != null) {
            if (isPhone) {
                mIbSource.setImageResource(R.drawable.iv_call_source_phone_selector);
            } else {
                mIbSource.setImageResource(R.drawable.iv_call_source_car_selector);
            }
        }
    }

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
        if (mLlTalking != null) {
            int showTime;
            int showAnswer;
            String calltype = null;
            if (IVIBluetooth.CallStatus.TALKING == mCurCallType) {
                showTime = View.VISIBLE;
                showAnswer = View.GONE;
                calltype = mContext.getString(R.string.call_ing);
            } else {
                showTime = View.GONE;
                showAnswer = View.VISIBLE;
                switch (mCurCallType) {
                    case IVIBluetooth.CallStatus.INCOMING: // 来电
                    case IVIBluetooth.CallStatus.THREE_INCOMING: // 第三方来电
                        calltype = mContext.getString(R.string.call_in);
                        break;
                    case IVIBluetooth.CallStatus.OUTGOING:
                    case IVIBluetooth.CallStatus.THREE_OUTGOING:
                        calltype = mContext.getString(R.string.call_out);
                        break;
                }
            }
            mIvMute.setVisibility(showTime);
            mIvAnswer.setVisibility(showAnswer);
            mTvCallTime.setVisibility(showTime);
            if (!TextUtils.isEmpty(calltype)) {
                mTvScreenCallType.setText(calltype);
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
