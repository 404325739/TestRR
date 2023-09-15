package com.jancar.bluetooth.presenter;

import com.jancar.bluetooth.contract.SettingsContract;
import com.jancar.bluetooth.model.SettingsModel;
import com.jancar.bluetooth.model.SettingsRepository;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.ui.mvp.presenter.BaseModelPresenter;

/**
 * 应用和服务都有蓝牙数据缓存工具类，自动应答在服务中没有，用应用的；
 * 自动连接、蓝牙名称、蓝牙Pin码以服务为主
 */
public class SettingsPresenter extends BaseModelPresenter<SettingsContract.View, SettingsModel> implements SettingsContract.Presenter, SettingsModel.Callback {
    @Override
    public SettingsModel createModel() {
        return new SettingsRepository(this);
    }

    @Override
    public boolean isPowerOn() {
        return BluetoothModelUtil.getInstance().isPowerOn();
    }

    @Override
    public boolean setPower(boolean state) {
        return BluetoothModelUtil.getInstance().setPower(state);
    }

    @Override
    public boolean isAutoLinkOn() {
        return BluetoothModelUtil.getInstance().isAutoLinkOn();
    }

    @Override
    public boolean setAutoLink(boolean state) {
        return BluetoothModelUtil.getInstance().setAutoLink(state);
    }

    @Override
    public boolean isAutoListen() {
        return BluetoothCacheUtil.getInstance().getBluzAutoListen();
    }

    @Override
    public boolean setAutoListen(boolean state) {
//        return BluetoothModelUtil.getInstance().setAutoListen(state);
        BluetoothCacheUtil.getInstance().setBluzAutoListen(state);
        return true;
    }

    @Override
    public String getBluetoothDeviceName() {
//        return BluetoothCacheUtil.getInstance().getBluzDeviceName();
        return BluetoothModelUtil.getInstance().getBluetoothName();
    }

    @Override
    public String getBluetoothDevicePin() {
//        return BluetoothCacheUtil.getInstance().getBluzDevicePin();
        return BluetoothModelUtil.getInstance().getBluetoothPin();
    }

    @Override
    public void modifyModuleName(String name) {
        BluetoothModelUtil.getInstance().modifyModuleName(name);
    }

    @Override
    public void modifyModulePIN(String pin) {
        BluetoothModelUtil.getInstance().modifyModulePIN(pin);
    }

}
