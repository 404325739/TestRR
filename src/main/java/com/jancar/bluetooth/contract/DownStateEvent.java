package com.jancar.bluetooth.contract;

/**
 * Created by Administrator on 2020/10/22.
 */

import org.greenrobot.eventbus.EventBus;

/**
 * 联系人下载状态
 */
public class DownStateEvent {
    public final static int STATE_START = 0;
    public final static int STATE_DOWNNING = 1;
    public final static int STATE_FINISH = 2;
    public final static int STATE_ERROR = 3;

    public final static int TYPE_DOWNPHONEBOOK = 0;
    public final static int TYPE_DOWNPHISTORY = 1;
    public int mState;
    public int mType;
    public String mMessage;//提示消息

    public DownStateEvent(int mState, int mType, String mMessage) {
        this.mState = mState;
        this.mType = mType;
        this.mMessage = mMessage;
    }

    public static void postDownStateEvent(DownStateEvent downStateEvent){
        EventBus.getDefault().post(downStateEvent);
    }
}
