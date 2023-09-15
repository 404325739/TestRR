package com.jancar.bluetooth.ui.agent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseViewAgent implements ViewAgent {

    protected Context mContext;
    private AtomicBoolean mIsInitDone = new AtomicBoolean(false);

    @Override
    public void init(@NonNull Context context, @NonNull ViewGroup root) {
        if (!mIsInitDone.get() && root != null) {
            mContext = context;
            onCreate(root);
            mIsInitDone.set(true);
        }
    }

    @Override
    public void release() {
        if (mIsInitDone.get()) {
            mIsInitDone.set(false);
            onDestroy();
            mContext = null;
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    /**
     * 可显示设置
     * @param isVisibility 是否可显示
     */
    public void setVisibility(boolean isVisibility) {
        onVisibilityChanged(isVisibility, CALL_TYPE_NONE);
    }

     /**
     * 可显示设置
     * @param isVisibility 是否可显示
     * @param callType 来电类型 （如果不需要处理这个参数可以传CALL_TYPE_NONE）
     */
    @Override
    public void setVisibility(boolean isVisibility, int callType) {
        onVisibilityChanged(isVisibility, callType);
    }

    protected void onDestroy() {
        // Emptyp
    }

    protected abstract void onVisibilityChanged(boolean isVisibility, int callType);
    protected abstract void onCreate(ViewGroup root);
}
