package com.jancar.bluetooth.presenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.contract.DialerContract;
import com.jancar.bluetooth.model.DialerModel;
import com.jancar.bluetooth.model.DialerRepository;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.system.IVISystem;
import com.jancar.sdk.utils.DeviceInfoUtil;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.presenter.BaseModelPresenter;

/**
 * @author Tzq
 * @date 2019-12-26 20:07:30
 */
public class DialerPresenter extends BaseModelPresenter<DialerContract.View, DialerModel> implements DialerContract.Presenter, DialerModel.Callback {
    private static final String TAG = "DialerPresenter";
    private static final int CALL_SHORT_LEN = 3; // 可拨打的最短号码长度
    private Paint mMeasurePaint;

    public float getTextLenght(String text, float size) {
        float result = 0;
        if (mMeasurePaint == null) {
            mMeasurePaint = new Paint();
        }
        mMeasurePaint.setTextSize(size);
        result = mMeasurePaint.measureText(text);
        return result;
    }


    @Override
    public DialerModel createModel() {
        return new DialerRepository(this);
    }

    @Override
    public String getLastCallNum() {
        Log.d(TAG, "get Last Call Number");
        return BluetoothModelUtil.getInstance().getCallNumber();
    }

    @Override
    public void callPhone(String number) {
        Logcat.d("");
        if (!isBtConnected()) {
            if (getUi() != null) {
                getUi().showCallTip(R.string.tv_dial_not_conn);
            }
        } else if (!TextUtils.isEmpty(number) && number.length() >= CALL_SHORT_LEN) {
            Log.d(TAG, "call phone:" + number);
            BluetoothModelUtil.getInstance().callPhone(number);
        } else {
            Log.d(TAG, "call phone failure");
            if (getUi() != null) {
                getUi().showCallTip(R.string.call_number_short_toast);
            }
        }
    }

    @Override
    public void go2Setting(Context mContext) {
        if (mContext != null) {
            try {
                if (DeviceInfoUtil.isDeviceMingShang()){
                    Intent intent = new Intent().setComponent(new ComponentName("com.android.settings","com.android.settings.bluetooth.BluetoothSettings"));
                    mContext.startActivity(intent);
                }else{
                    mContext.startActivity(new Intent(IVISystem.ACTION_BLUETOOTH_SETTINGS).setPackage(IVISystem.PACKAGE_BLUETOOTH));
                }
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public boolean isBtConnected() {
        Log.d(TAG, "++isBtConnected++ " + BluetoothCacheUtil.getInstance().getBluzConnectedStatus());
        return BluetoothCacheUtil.getInstance().getBluzConnectedStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTED;
    }

}