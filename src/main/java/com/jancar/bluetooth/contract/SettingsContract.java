package com.jancar.bluetooth.contract;

import android.content.Context;

import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

public interface SettingsContract {

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
         * 蓝牙开关状态
         *
         * @return
         */
        boolean isPowerOn();

        boolean setPower(boolean state);

        /**
         * 自动连接
         *
         * @return
         */
        boolean isAutoLinkOn();

        boolean setAutoLink(boolean state);

        /**
         * 自动应答
         *
         * @return
         */
        boolean isAutoListen();

        boolean setAutoListen(boolean state);

        /**
         * 获取蓝牙的名字，同步获取
         */
        String getBluetoothDeviceName();

        /**
         * 获取蓝牙pin码，同 getBluetoothPin 方法
         */
        String getBluetoothDevicePin();

        /**
         * 修改模块名字
         *
         * @param name
         */
        void modifyModuleName(String name);

        /**
         * 修改模块 pin 码（不支持？）
         *
         * @param pin
         */
        void modifyModulePIN(String pin);

    }
}
