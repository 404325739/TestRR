package com.jancar.viewbase.utils;

import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.jancar.base.BaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 按钮工具类，用于做长按处理，以及点击间隔处理 </br>
 * 例：如果设置点击间隔时间为1s，那么，1s内的连续点击，不会回调通知应用 </br>
 * 如果设置了长按的响应时间1s，那么，长按每1s回调一次
 */

public class ButtonUtil {

    public interface ButtonListener {
        /**
         * 点击事件
         * @param view
         */
        void onClick(View view);

        /**
         * 长按事件
         * @param view
         */
        void onLongClick(View view);
    }

    /** 长按what */
    private static final int LONG_MSG_WHAT = 0;
    /** 短按what */
    private static final int SHORT_MSG_WHAT = 1;

    /**
     * 按钮监听结构体
     */
    private static class StButtonListener {
        int mRepeatMs; // 短按间隔时间
        int mLongMs;   // 长按间隔时间
        View mView;    // 控件
        ButtonListener mButtonListener; // 监听回调

        // 计时
        private BaseHandler mHandler = new BaseHandler(this) {
            @Override
            public void handleMessage(Object activityOrFragment, Message msg) {
                if (msg != null) {
                    StButtonListener stButtonListener = (StButtonListener) msg.obj;
                    switch (msg.what) {
                        case LONG_MSG_WHAT: // 长按
                            if (stButtonListener != null) { // 长按时间到
                                mHandler.removeMessages(LONG_MSG_WHAT);
                                stButtonListener.mButtonListener.onLongClick(stButtonListener.mView);
                                mHandler.sendMessageDelayed(Message.obtain(msg), stButtonListener.mLongMs);
                            }
                            break;

                        case SHORT_MSG_WHAT: // 短按标记
                            mHandler.removeMessages(SHORT_MSG_WHAT);
                            break;
                    }
                }
            }
        };

        private StButtonListener(int repeatMs, int longMs, View view, ButtonListener listener) {
            mRepeatMs = repeatMs;
            mLongMs   = longMs;
            mView     = view;
            mButtonListener = listener;
        }
    }
    /** 监听列表 */
    private static List<StButtonListener> sStButtonListeners = new ArrayList<>();

    /**
     * 监听onTouch事件
     */
    private static View.OnTouchListener sOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            StButtonListener stButtonListener = getViewByList(v);
            if (stButtonListener != null) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) { // 按下
                    Message msg = stButtonListener.mHandler.obtainMessage(LONG_MSG_WHAT);
                    msg.obj = stButtonListener;
                    stButtonListener.mHandler.sendMessageDelayed(msg, stButtonListener.mLongMs);
                } else if (event.getAction() == MotionEvent.ACTION_UP) { // 抬起
                    stButtonListener.mHandler.removeMessages(LONG_MSG_WHAT);
                    if (!stButtonListener.mHandler.hasMessages(SHORT_MSG_WHAT)) { // 执行短按超过了 mRepeatMs 时间
                        stButtonListener.mHandler.sendEmptyMessageDelayed(SHORT_MSG_WHAT, stButtonListener.mRepeatMs);
                        stButtonListener.mButtonListener.onClick(stButtonListener.mView);
                    }
                }
            }
            return false;
        }
    };

    /**
     * 监听按钮的点击事件 </br>
     * 调用该方法，当用户连续点击按钮时，每隔repeatMs会回调一次 onClick，用于过滤 </br>
     * 当用户长按按钮时，每隔longMs会回调一次 onLongClick
     * @param repeatMs 短按间隔时间
     * @param longMs   长按间隔时间
     * @param view     监听的控件
     * @param listener 回调
     */
    public static void startListenerTouch(int repeatMs, int longMs, View view, ButtonListener listener) {
        if (view != null && listener != null) {
            sStButtonListeners.add(new StButtonListener(repeatMs, longMs, view, listener));
            view.setOnTouchListener(sOnTouchListener);
        }
    }

    /**
     * 停止监听
     * @param view
     */
    public static void stopListenerTouch(View view) {
        StButtonListener stButtonListener = getViewByList(view);
        if (stButtonListener != null) {
            stButtonListener.mView.setOnTouchListener(null);
            sStButtonListeners.remove(stButtonListener);
        }
    }

    /**
     * 清理所有监听
     */
    public static void clearListenerTouch() {
        for (StButtonListener stButtonListener : sStButtonListeners) {
            if (stButtonListener.mView != null) {
                stButtonListener.mView.setOnTouchListener(null);
            }
        }
        sStButtonListeners.clear();
    }

    /**
     * 从列表中获取view对应的监听
     * @param view
     * @return
     */
    private static StButtonListener getViewByList(View view) {
        if (view != null) {
            for (StButtonListener stButtonListener : sStButtonListeners) {
                if (view.equals(stButtonListener.mView)) {
                    return stButtonListener;
                }
            }
        }
        return null;
    }
}
