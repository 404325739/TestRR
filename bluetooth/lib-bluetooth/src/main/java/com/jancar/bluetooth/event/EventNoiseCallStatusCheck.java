package com.jancar.bluetooth.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 检测当前通话状态是否混乱导致悬浮窗是个空界面屏幕无法触摸
 * 解决JIRA问题#FORDFOCUS-835
 */

public class EventNoiseCallStatusCheck {

    public static void onEvent() {
        EventBus.getDefault().post(new EventNoiseCallStatusCheck());
    }

    private EventNoiseCallStatusCheck() {
        // Empty
    }
}
