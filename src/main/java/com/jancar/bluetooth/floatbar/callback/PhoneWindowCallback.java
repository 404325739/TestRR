package com.jancar.bluetooth.floatbar.callback;

public interface PhoneWindowCallback {
    /**
     * 全屏，半屏切换
     * // @param isFullScreen
     */
    void switchScreenTaking();

    /**
     * mute
     */
    void setMute(boolean isMute);

    /**
     * 挂断第三方来电
     */
    void hangupThreeIncoming();
}
