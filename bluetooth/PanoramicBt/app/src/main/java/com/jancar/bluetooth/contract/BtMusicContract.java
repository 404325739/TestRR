package com.jancar.bluetooth.contract;

import android.content.Context;

import com.jancar.bluetooth.services.A2DPService;
import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

public interface BtMusicContract {

    interface View extends Ui {

        /**
         * 获取UI上下文
         *
         * @return 上下文
         */
        Context getUIContext();

        /**
         * 在UI线程中执行线程任务
         *
         * @param runnable 线程任务
         */
        void runOnUIThread(Runnable runnable);

    }

    interface Presenter extends IPresenter {

        /**
         * 获取蓝牙连接状态
         *
         * @return
         */
        int getBluConnStatus();

        boolean isBtConnected();

        /**
         * 上一曲
         */
        void prev();

        /**
         * 播放/暂停
         */
        void playAndPause();

        /**
         * 下一曲
         */
        void next();

        /**
         * 设置蓝牙音乐为当前播放音源
         *
         * @param on true：进入；false：退出；
         */
        void setBtMusicOn(boolean on);

        /**
         * 退出
         */
        void release();

        /**
         * a2dp的回调
         */
        void setA2DPCallback(A2DPService.A2DPCallback mA2DPCallback);
    }
}
