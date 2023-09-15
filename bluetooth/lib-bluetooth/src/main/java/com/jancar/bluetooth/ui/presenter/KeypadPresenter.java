package com.jancar.bluetooth.ui.presenter;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.ui.agent.KeypadViewAgent;
import com.jancar.bluetooth.ui.callback.KeypadCallback;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 该类主要是对悬浮窗键盘类的控制
 */

public class KeypadPresenter extends BaseViewPresenter<KeypadViewAgent> implements View.OnClickListener {

    public static KeypadPresenter newInstance(@NonNull ViewGroup root, @NonNull KeypadViewAgent viewAgent) {
        KeypadPresenter keypadPresenter = new KeypadPresenter();
        keypadPresenter.init(root, viewAgent);
        return keypadPresenter;
    }

    private KeypadCallback mKeypadCallback;

    public void setKeypadCallback(KeypadCallback callback) {
        mKeypadCallback = callback;
    }

    @Override
    public void onClick(View view) {
        mViewAgent.setVisibility(false);
    }

    void onKeyClick(final int position) {
        if (position >= 0 && position < IVIBluetooth.BluetoothDTMFCode.DTMF_CODE.length) {
            if (mViewAgent != null) {
                String keyText = mViewAgent.getKeyText() + IVIBluetooth.BluetoothDTMFCode.DTMF_CODE[position]; // 添加一个字符，该位置不能用 append
                mViewAgent.setKeyText(keyText);
            }
            requestDTMF(IVIBluetooth.BluetoothDTMFCode.DTMF_CODE[position].charAt(0)); // 发送一个DTMF按键
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        View view = root.findViewById(R.id.bt_keypad_one);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.ONE_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_two);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.TWO_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_three);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.THREE_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_four);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.FOUR_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_five);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.FIVE_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_six);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.SIX_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_seven);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.SEVEN_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_eight);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.EIGHT_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_nine);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.NINE_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_zero);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.ZERO_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_asterisk);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.ASTERISK_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_pound);
        if (view != null) {
            view.setOnClickListener(new KeyClickListener(IVIBluetooth.KeyPositionDefined.POUND_POSITION));
        }

        view = root.findViewById(R.id.bt_keypad_hangup);
        if (view != null) {
            view.setOnClickListener(mHangupClickListener);
        }

        view = root.findViewById(R.id.bt_keypad_down);
        if (view != null) {
            view.setOnClickListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        mKeypadCallback = null;
    }


    /**
     * 发送一个dtmf按键值
     *
     * @param code
     */
    private void requestDTMF(final int code) {
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
            }
        });
    }

    private class KeyClickListener implements View.OnClickListener {
        private final int mIndex;

        public KeyClickListener(int index) {
            mIndex = index;
        }

        @Override
        public void onClick(View v) {
            onKeyClick(mIndex);
        }
    }

    private View.OnClickListener mHangupClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mKeypadCallback != null) {
                mKeypadCallback.hangup();
            }
        }
    };
}
