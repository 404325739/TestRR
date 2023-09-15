package com.jancar.bluetooth.ui.agent;


import android.content.Context;
import android.view.ViewGroup;

public interface ViewAgent {

    int CALL_TYPE_NONE = -1;

    /**
     * 初始化View
     * @param context
     * @param root
     */
    void init(Context context, ViewGroup root);

    Context getContext();

    void release();

    /**
     * 是否显示
     * @return
     */
    boolean isViewVisible();

    /**
     * 可显示设置
     * @param isVisibility 是否可显示
     * @param callType 来电类型 （如果不需要处理这个参数可以传CALL_TYPE_NONE）
     */
    void setVisibility(boolean isVisibility, int callType);
}
