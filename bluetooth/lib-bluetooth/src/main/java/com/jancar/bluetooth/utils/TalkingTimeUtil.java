package com.jancar.bluetooth.utils;

import com.jancar.sdk.utils.TimerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通话时间计算工具类.
 */

public class TalkingTimeUtil {

    private static final int TALKING_TIMER_OUT = 1 * 1000; // 1s刷一次通话时间

    public interface TalkingTimeCallback {
        /**
         * 更新通话时间
         * @param text
         */
        void updateTalkingText(String text, String phoneNumber);
    }

    private Map<String, TimerUtil> mTalkingTimerMap = new HashMap<>();

    public TalkingTimeUtil() {

    }

    /**
     * 打开通话定时器
     * @param phoneNumber
     * @param callback
     */
    public void startTalkingTimer(final String phoneNumber, final TalkingTimeCallback callback) {
        TimerUtil timer = mTalkingTimerMap.get(phoneNumber);
        if (timer == null) {
            timer = new TimerUtil(new TimerUtil.TimerCallback() {
                int mCallTime = 1;

                @Override
                public void timeout() {
                    if (callback != null) {
                        callback.updateTalkingText(getCallingTime(mCallTime++), phoneNumber);
                    }
                }
            });
            timer.start(TALKING_TIMER_OUT);

            if (callback != null) { // 先刷新第0秒
                callback.updateTalkingText(getCallingTime(0), phoneNumber);
            }

            mTalkingTimerMap.put(phoneNumber, timer);
        }
    }

    /**
     * 停止通话定时器
     * @param phoneNumber
     */
    public void stopTalkingTimer(String phoneNumber) {
        TimerUtil timer = mTalkingTimerMap.get(phoneNumber);
        if (timer != null) {
            timer.stop();

            mTalkingTimerMap.remove(phoneNumber);
        }
    }

    /**
     * 停止所有通话定时器
     */
    public void stopAllTalkingTimer() {
        Set<String> keys = mTalkingTimerMap.keySet();

        for (String key : keys) {
            TimerUtil timer = mTalkingTimerMap.get(key);
            if (timer != null) {
                timer.stop();
            }
        }

        mTalkingTimerMap.clear();
    }

    /**
     * 刷新通话时间
     * @param count
     */
    private String getCallingTime(int count) {
        int min = count / 60 % 60;
        int sec = count % 60;
        int h = count / 60 / 60;
        if (h > 0) { // 打电话超过一个小时
            return (timeToString(h) + ":" + timeToString(min) + ":" + timeToString(sec));
        } else {
            return (timeToString(min) + ":" + timeToString(sec));
        }
    }

    /**
     * 时间转换成String，不足两位，自动补0
     * @param s
     * @return
     */
    private String timeToString(int s) {
        return s >= 10 ? ("" + s) : ("0" + s);
    }
}
