package com.jancar.bluetooth.event;

import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

/**
 * 模拟延时处理任务，任务可以是在单独的一个线程执行，也可以延时后再主线程执行
 */

public class EventOnDelayTask {

    /**
     * 发送一个延时在主线程执行的任务，
     *
     * @param delayMillis 延时毫秒数
     * @param task 延时执行的任务
     */
    public static void onEvent(final long delayMillis, final Runnable task) {
        EventBus.getDefault().post(new EventOnDelayTask(delayMillis, true, task));
    }

    /**
     * 发送一个延时在执行的任务，
     *
     * @param delayMillis 延时毫秒数
     * @param task 延时执行的任务
     * @param isTaskRunOnMaster 是否在主线程执行任务
     */
    public static void onEvent(final long delayMillis, final boolean isTaskRunOnMaster, final Runnable task) {
        EventBus.getDefault().post(new EventOnDelayTask(delayMillis, isTaskRunOnMaster, task));
    }

    public final long mDelayMillis;
    public final Runnable mTask;
    public final boolean mIsTaskRunOnMaster;

    private EventOnDelayTask(long delayMillis, boolean isTaskRunOnMaster, Runnable task) {
        mDelayMillis = delayMillis;
        mTask = task;
        mIsTaskRunOnMaster = isTaskRunOnMaster;
    }

    /**
     * 延时后主线程执行
     * @param handler 主线程handler
     */
    public void onTask(Handler handler) {
        if (mDelayMillis >= 0 && mTask != null) {
            if (mIsTaskRunOnMaster) {
                if (handler != null) {
                    handler.postDelayed(mTask, mDelayMillis);
                }
            } else {
                try {
                    Thread.sleep(mDelayMillis);

                    mTask.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
