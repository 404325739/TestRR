package com.jancar.bluetooth.ui.callback;

public interface PhoneWindowCallback {
    /**
     * 全屏，半屏切换
     * @param isFullScreen
     */
    void switchScreenTaking(boolean isFullScreen);

    /**
     * mute
     */
    void setMute(boolean isMute);

    /**
     * mute mic
     * @param isMuteMic
     */
    void setMuteMic(boolean isMuteMic);

    /**
     * 挂断第三方来电
     */
    void hangupThreeIncoming();
}
