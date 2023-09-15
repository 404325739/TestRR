package com.jancar.bluetooth.contract;

import android.content.Context;

import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

/**
 * @author Tzq
 * @date 2019-12-26 20:07:30
 */
public interface DialerContract {

    interface View extends Ui {
        /**
         * 显示拨打提示语
         */
        void showCallTip(int strId);

        /**
         * 获取上下文
         *
         * @return
         */
        Context getUIContext();

    }

    interface Presenter extends IPresenter {
        /**
         * 获取最后一个拨打的电话
         *
         * @return
         */
        String getLastCallNum();

        /**
         * 拨打号码
         *
         * @param number
         */
        void callPhone(String number);

        /**
         * 跳转到设置界面
         *
         * @param mContext
         */
        void go2Setting(Context mContext);

        /**
         * 获取蓝牙状态
         */
        boolean isBtConnected();

    }
}
