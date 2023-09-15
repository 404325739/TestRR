package com.jancar.bluetooth.ui.presenter;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.jancar.bluetooth.ui.agent.ViewAgent;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseViewPresenter<T  extends ViewAgent> implements ViewPresenter<T> {

    protected T mViewAgent;
    private AtomicBoolean mIsInitDone = new AtomicBoolean(false);

    @Override
    public void init(@NonNull ViewGroup root, @NonNull T viewAgent) {
        if (!mIsInitDone.get() && root != null) {
            mViewAgent = viewAgent;
            onCreate(root);
            mIsInitDone.set(true);
        }
    }

    @Override
    public void release() {
        if (mIsInitDone.get()) {
            mIsInitDone.set(false);
            onDestroy();
            if (mViewAgent != null) {
                mViewAgent.release();
                mViewAgent = null;
            }
        }
    }

    @Override
    public T getViewAgent() {
        return mViewAgent;
    }

    protected void onDestroy() {
        // Empty
    }

    protected abstract void onCreate(ViewGroup root);
}
