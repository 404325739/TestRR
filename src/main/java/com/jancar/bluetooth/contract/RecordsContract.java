package com.jancar.bluetooth.contract;

import android.content.Context;

import com.jancar.bluetooth.adapter.RecordsAdapter;
import com.jancar.bluetooth.bean.StCallHistory;
import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:09:21
 */
public interface RecordsContract {

    interface View extends Ui {

        /**
         * 同步结束
         *
         * @param code
         * @param msg
         * @param listSize
         */
        void synchronousFinish(int code, String msg, int listSize);

        /**
         * 获取上下文
         *
         * @return
         */
        Context getUIContext();

        /**
         * 在UI线程中执行线程任务
         *
         * @param runnable 线程任务
         */
        void runOnUIThread(Runnable runnable);

        /**
         * 正在下载的联系人
         *
         * @param callHistory
         */
        void onProgress(List<StCallHistory> callHistory);

    }

    interface Presenter extends IPresenter {
        /**
         * 获取通话记录适配器
         *
         * @return
         */
        RecordsAdapter getAdapter();


        /**
         * 获取通话记录列表
         *
         * @param isGetBluzModel
         */
        void loadCallHistoryList(boolean isGetBluzModel);

        /**
         * 获取蓝牙连接状态
         *
         * @return
         */
        boolean isBtConnected();

        /**
         * 清除通话记录列表
         */
        void clearHistoryList();

        /**
         * 停止下载任务
         */
        void stopContactOrHistoryLoad();


        void callSelectedRecord();

        void delSelectedRecord();

        void setRecordType(int mCurrType);
    }
}
