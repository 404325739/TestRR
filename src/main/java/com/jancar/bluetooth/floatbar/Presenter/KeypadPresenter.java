package com.jancar.bluetooth.floatbar.Presenter;

import android.content.Context;
import android.os.RemoteException;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.utils.AnimationUtil;
import com.jancar.bluetooth.R;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 该类主要是对悬浮窗键盘类的控制
 */

public class KeypadPresenter implements View.OnClickListener {

    private LinearLayout mRlKeyPad;
    private Context mContext;
    private TextView mTvKeyDisplay;

    public interface KeypadCallback {
        void hangup();
    }

    private KeypadCallback mKeypadCallback;

    public KeypadPresenter(Context context, LinearLayout parent, KeypadCallback callback) {
        mRlKeyPad = parent;
        mContext = context;
        mKeypadCallback = callback;
        if (mRlKeyPad != null) {
//            mRlKeyPad.findViewById(R.id.ib_down).setOnClickListener(this);
//            mRlKeyPad.findViewById(R.id.ib_hangup).setOnClickListener(this);
            mTvKeyDisplay = (TextView) mRlKeyPad.findViewById(R.id.tv_call_input);
//            mTvKeyDisplay.setMovementMethod(ScrollingMovementMethod.getInstance());

            mRlKeyPad.findViewById(R.id.call_num_1).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_2).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_3).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_4).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_5).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_6).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_7).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_8).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_9).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_star).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_0).setOnClickListener(this);
            mRlKeyPad.findViewById(R.id.call_num_10).setOnClickListener(this);
            mRlKeyPad.setOnClickListener(this); // 截掉后面的按键事件
        }
    }

    @Override
    public void onClick(View view) {
        int position = -1;
        switch (view.getId()) {
            case R.id.call_num_1:
                position = IVIBluetooth.KeyPositionDefined.ONE_POSITION;
                break;

            case R.id.call_num_2:
                position = IVIBluetooth.KeyPositionDefined.TWO_POSITION;
                break;

            case R.id.call_num_3:
                position = IVIBluetooth.KeyPositionDefined.THREE_POSITION;
                break;

            case R.id.call_num_4:
                position = IVIBluetooth.KeyPositionDefined.FOUR_POSITION;
                break;

            case R.id.call_num_5:
                position = IVIBluetooth.KeyPositionDefined.FIVE_POSITION;
                break;
            case R.id.call_num_6:
                position = IVIBluetooth.KeyPositionDefined.SIX_POSITION;
                break;

            case R.id.call_num_7:
                position = IVIBluetooth.KeyPositionDefined.SEVEN_POSITION;
                break;

            case R.id.call_num_8:
                position = IVIBluetooth.KeyPositionDefined.EIGHT_POSITION;
                break;

            case R.id.call_num_9:
                position = IVIBluetooth.KeyPositionDefined.NINE_POSITION;
                break;

            case R.id.call_num_star:
                position = IVIBluetooth.KeyPositionDefined.ASTERISK_POSITION;
                break;

            case R.id.call_num_0:
                position = IVIBluetooth.KeyPositionDefined.ZERO_POSITION;
                break;

            case R.id.call_num_10:
                position = IVIBluetooth.KeyPositionDefined.POUND_POSITION;
                break;

//            case R.id.ib_down: // 返回
//                setKeyboardVisibility(false);
//                return;
//
//            case R.id.ib_hangup: // 挂断
//                if (mKeypadCallback != null) {
//                    mKeypadCallback.hangup();
//                }
//                return;
        }
        if (position >= 0 && position < IVIBluetooth.BluetoothDTMFCode.DTMF_CODE.length) {
            mTvKeyDisplay.setText(mTvKeyDisplay.getText() + IVIBluetooth.BluetoothDTMFCode.DTMF_CODE[position]); // 添加一个字符，该位置不能用 append
            requestDTMF(IVIBluetooth.BluetoothDTMFCode.DTMF_CODE[position].charAt(0)); // 发送一个DTMF按键
        }
    }

    /**
     * 设置蓝牙通话悬浮窗键盘显示或隐藏
     */
    public void setKeyboardVisibility(boolean isVisibility) {
//        Logcat.d("show:" + isVisibility);
        AnimationUtil.startDownAnimation(isVisibility, mRlKeyPad, mContext);
    }

    /**
     * 发送一个dtmf按键值
     *
     * @param code
     */
    private void requestDTMF(int code) {
        BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
        if (btManager == null) return;
        btManager.requestDTMF(code, new IBluetoothExecCallback.Stub() {
            @Override
            public void onSuccess(String msg) throws RemoteException {
                Logcat.d("msg:" + msg); // 发送成功
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);
//					ToastUtil.getInstance().showToast("requestDTMF errorCode:" + errorCode, getContext());
            }
        });
    }
}
