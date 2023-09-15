package com.jancar.bluetooth.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * @author Tzq
 * @date 2020/1/9 11:39
 * Handler 辅助类
 */
public class HandlerUtil extends Handler {
    private WeakReference<Callback> mWeakReference;

    public HandlerUtil(Callback callback) {
        mWeakReference = new WeakReference<>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (null != mWeakReference && null != mWeakReference.get()) {
            Callback callback = mWeakReference.get();
            callback.handleMessage(msg);
        }
    }
}
