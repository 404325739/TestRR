package com.jancar.bluetooth.ui.presenter;

import android.view.ViewGroup;

import com.jancar.bluetooth.ui.agent.ViewAgent;

public interface ViewPresenter<T extends ViewAgent> {

    /**
     * 初始化
     * @param root
     * @param viewAgent
     */
    void init(ViewGroup root, T viewAgent);

    void release();

    T getViewAgent();
}
