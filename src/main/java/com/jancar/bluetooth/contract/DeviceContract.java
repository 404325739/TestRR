package com.jancar.bluetooth.contract;

import android.content.Context;

import com.jancar.bluetooth.adapter.NewDeviceAdapter;
import com.jancar.bluetooth.adapter.PairedDeviceAdapter;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

/**
 * @author Tzq
 * @date 2020/8/17 19:52
 */
public interface DeviceContract {

    interface Presenter extends IPresenter {
        PairedDeviceAdapter getNewAdapter();

        PairedDeviceAdapter getPairAdapter();

        void searchNewDevice();

        void loadPairDevice();

        void unpairDevice(BluetoothDevice device);

        /**
         *
         * @param link true：连接选中的设备；false：断开与选中设备的连接
         */
        void linkSelectedDevice(boolean link);

        boolean isPowerOn();

        boolean setPower(boolean state);

        void modifyModuleName(String name);
        void modifyModulePIN(String pin);

        String getBluetoothDeviceName();

        String getBluetoothDevicePin();

        void linkStatusChanged(String addr, int status);
    }

    interface View extends Ui {
        void updateProgressState(boolean start);

//        void updateDiscoveryAdapter();
        void updateDiscoveryAdapter(int add, int start, int count);

        void updatePairAdapter();

        void showToast(int resid);

        void showUnPairDialog(BluetoothDevice device);

        Context getUIContext();
    }
}
