package com.jancar.bluetooth.contract;

import android.content.Context;

import com.jancar.bluetooth.adapter.CollectionAdapter;
import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

/**
 * @author Tzq
 * @date 2019-12-26 20:10:01
 */
public interface CollectionContract {

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

        /**
         * 同步结束
         *
         * @param code
         * @param msg
         * @param listSize
         */
        void synchronousFinish(int code, String msg, int listSize);


    }

    interface Presenter extends IPresenter {
        /**
         * 获取联系人列表适配器
         *
         * @return
         */
        CollectionAdapter getAdapter();


        /**
         * 首次加载列表数据
         */
        void loadCollectListFirst();

        /**
         * 获取蓝牙连接状态
         */
        boolean isBtConnected();

        /**
         * 清空收藏列表
         */
        void clearCollectList();

    }
}
