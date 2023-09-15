package com.jancar.bluetooth.contract;

import android.content.Context;

import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

/**
 * @author Tzq
 * @date 2019-12-24 19:46:00
 */
public interface BaseMainContract {

    interface View extends Ui {
        /**
         * 获取上下文
         *
         * @return
         */
        Context getUIContext();

    }

    interface Presenter extends IPresenter {

        /**
         * 开启蓝牙服务
         */
        void runService();

    }
}
