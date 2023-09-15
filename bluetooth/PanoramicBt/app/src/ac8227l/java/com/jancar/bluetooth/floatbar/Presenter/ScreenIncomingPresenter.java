package com.jancar.bluetooth.floatbar.Presenter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jancar.bluetooth.utils.AnimationUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.floatbar.callback.PhoneWindowCallback;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 全屏下来电的逻辑类.
 */

public class ScreenIncomingPresenter implements View.OnClickListener {

    private LinearLayout mLlScreenIncoming, mLlIncomingLayouts, mLlThreeIncomingLayouts, mLlScreenTalkingText;  // 来电的布局
    private TextView mTvScreenCallType, mTvScreenCallPhoneNumber, mTvScreenCallName;                            // 通话类型，电话号码，名字
    private TextView mIbScreenMute;
    private Context mContext;
    private PhoneWindowCallback mPhoneWindowCallback;

    public ScreenIncomingPresenter(Context context, LinearLayout parent, PhoneWindowCallback callback) {
        if (context == null || parent == null || callback == null) {
            Logcat.w("context :" + context + " parent:" + parent + " callback:" + callback);
            return;
        }
        mContext = context;
        mPhoneWindowCallback = callback;

        // 全屏状态下来电页面布局
        mLlScreenIncoming = (LinearLayout) parent.findViewById(R.id.call_phone_incoming);
        //来电按钮
        mLlIncomingLayouts = (LinearLayout) mLlScreenIncoming.findViewById(R.id.ll_screen_incoming_layouts);
        mLlIncomingLayouts.findViewById(R.id.iv_incoming_answer).setOnClickListener(this);
        mLlIncomingLayouts.findViewById(R.id.iv_incoming_hangup).setOnClickListener(this);

        // 来电页面，上半部分显示
        mLlScreenTalkingText = (LinearLayout) parent.findViewById(R.id.ll_screen_incoming_text);
        mTvScreenCallType = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_type);
        mTvScreenCallPhoneNumber = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_phone_number);
        mTvScreenCallName = (TextView) mLlScreenTalkingText.findViewById(R.id.tv_call_name);
        mTvScreenCallType.setText(R.string.call_in);

        // 第三方通话部分
        mLlThreeIncomingLayouts = (LinearLayout) mLlScreenIncoming.findViewById(R.id.ll_three_screen_incoming_layouts);
        mLlScreenIncoming.findViewById(R.id.iv_three_incoming_hangup).setOnClickListener(this);
        mLlScreenIncoming.findViewById(R.id.iv_three_incoming_answer).setOnClickListener(this);

        mLlScreenIncoming.setOnClickListener(this); // 截获掉整个页面的点击事件，防止透传
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_three_incoming_hangup: // 挂断第三方
                Logcat.d();
                if (mPhoneWindowCallback != null) { // 通知UI，挂断第三方
                    mPhoneWindowCallback.hangupThreeIncoming();
                }
                BluetoothModelUtil.getInstance().threePartyCallCtrl(
                        IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_THREE);
                setVisibility(false);
                break;

            case R.id.iv_three_incoming_answer: // 挂断当前通话，接听第三方
                BluetoothModelUtil.getInstance().threePartyCallCtrl(
                        IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_CUR);
                break;

//            case R.id.ib_three_keep_answer: // 保持当前通话，接听第三方
//                BluetoothModelUtil.getInstance().threePartyCallCtrl(
//                        IVIBluetooth.ThreePartyCallCtrl.ACTION_ANSWER_THREE);
//                break;

            case R.id.iv_incoming_answer: // 接听
                if(BluetoothModelUtil.getInstance().isThreeIncoming()){
                    BluetoothModelUtil.getInstance().threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_ANSWER_THREE);
                }else {
                    BluetoothModelUtil.getInstance().listenPhone();
                }
                break;

            case R.id.iv_incoming_hangup: // 挂断
                BluetoothModelUtil.getInstance().hangup();
                setVisibility(false);
                break;
//
//            case R.id.ib_navi: // 导航
//                ScreenTalkingPresenter.EventOnNaviStart.onEvent();
//                break;
//
//            case R.id.ib_mute: // 静音
//                if (mPhoneWindowCallback != null) {
//                    mPhoneWindowCallback.setMute(!isUIMute());
//                }
//                break;
        }
    }

    /**
     * 当前UI上是否是mute装啊提
     *
     * @return
     */
    public boolean isUIMute() {
        if (mIbScreenMute != null) {
            return mIbScreenMute.isSelected();
        }
        return false;
    }

    /**
     * 刷新mute icon
     *
     * @param isMute
     */
    public void updateMuteIcon(boolean isMute) {
        if (mIbScreenMute != null) {
            mIbScreenMute.setSelected(isMute);
        }
    }

    /**
     * 来电名字
     *
     * @param name
     */
    public void updateName(String name) {
        if (mTvScreenCallName != null) {
            mTvScreenCallName.setText(name);
        }
    }

    /**
     * 来电号码
     *
     * @param phoneNumber
     */
    public void updatePhoneNumber(String phoneNumber) {
        if (mTvScreenCallPhoneNumber != null) {
            mTvScreenCallPhoneNumber.setText(phoneNumber);
        }
    }

    /**
     * 设置通话状态
     *
     * @param callType
     */
    public void setCallType(int callType) {
        Logcat.d("callType:" + callType);
        switch (callType) {
            case IVIBluetooth.CallStatus.INCOMING:
                mLlIncomingLayouts.setVisibility(View.VISIBLE);
                mLlThreeIncomingLayouts.setVisibility(View.GONE);
                setVisibility(true);
                break;

            case IVIBluetooth.CallStatus.THREE_INCOMING:
                mLlIncomingLayouts.setVisibility(View.GONE);
                mLlThreeIncomingLayouts.setVisibility(View.VISIBLE);
                setVisibility(true);
                break;

            case IVIBluetooth.CallStatus.TALKING:
            case IVIBluetooth.CallStatus.OUTGOING:
            case IVIBluetooth.CallStatus.RETAIN:
                setVisibility(false);
                break;
        }
    }

    /**
     * 设置显示和隐藏
     *
     * @param isVisibility
     */
    private void setVisibility(boolean isVisibility) {
        Logcat.d("isVisibility:" + isVisibility + " mLlScreenIncoming.getVisibility():" + mLlScreenIncoming.getVisibility());
        if ((isVisibility && mLlScreenIncoming.getVisibility() != View.VISIBLE) || // 从隐藏到显示
                (!isVisibility && mLlScreenIncoming.getVisibility() == View.VISIBLE)) { // 从显示到隐藏
            AnimationUtil.startDownAnimation(isVisibility, mLlScreenIncoming, mContext);
        }
    }
}
