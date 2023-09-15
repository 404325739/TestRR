package com.jancar.bluetooth.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 通话界面启动导航按钮触发时发送
 */

public class EventOnNaviStart {

    public static void onEvent() {
        EventBus.getDefault().post(new EventOnNaviStart());
    }

    private EventOnNaviStart() {
        // Empty
    }
}
