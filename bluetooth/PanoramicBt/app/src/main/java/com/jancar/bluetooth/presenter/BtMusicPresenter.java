package com.jancar.bluetooth.presenter;

import com.jancar.bluetooth.contract.BtMusicContract;
import com.jancar.bluetooth.model.BtMusicModel;
import com.jancar.bluetooth.model.BtMusicRepository;
import com.jancar.bluetooth.services.A2DPService;
import com.jancar.bluetooth.services.A2dpPresenter;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.presenter.BaseModelPresenter;

public class BtMusicPresenter extends BaseModelPresenter<BtMusicContract.View, BtMusicModel>
        implements BtMusicContract.Presenter, BtMusicModel.Callback {

    private boolean isBtMusicOn = false;
    private A2dpPresenter mA2dpPresenter = new A2dpPresenter();
    private A2DPService.A2DPCallback mA2DPCallback;

    public BtMusicPresenter() {
    }

    @Override
    public BtMusicModel createModel() {
        return new BtMusicRepository(this);
    }

    @Override
    public void onModelReady(BtMusicModel model) {
        super.onModelReady(model);
        Logcat.d();
    }

    @Override
    public int getBluConnStatus() {
        return BluetoothCacheUtil.getInstance().getBluzConnectedStatus();
    }

    @Override
    public boolean isBtConnected() {
        return getBluConnStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTED;
    }

    @Override
    public void prev() {
        mA2dpPresenter.prev();
    }

    @Override
    public void playAndPause() {
        mA2dpPresenter.playAndPause();
    }

    @Override
    public void next() {
        mA2dpPresenter.next();
    }

    @Override
    public void setBtMusicOn(boolean on) {
        boolean isA2DPOpened = mA2dpPresenter.isA2DPOpened();
        Logcat.d("on: " + on + " isBtMusicOn: " + isBtMusicOn + " isA2DPOpened: " + isA2DPOpened);
        if (on) {
            if (!isBtMusicOn || !isA2DPOpened) {
                mA2dpPresenter.registerA2DPCallback(mA2DPCallback);
                mA2dpPresenter.open();
                mA2dpPresenter.requestPlayMusicInfo();
                mA2dpPresenter.start();
            }
        } else {
            mA2dpPresenter.unregisterA2DPCallback(mA2DPCallback);
        }
        isBtMusicOn = on;
    }

    @Override
    public void release() {
        Logcat.d();
        mA2dpPresenter.close();
        mA2dpPresenter.destroy();
    }

    @Override
    public void setA2DPCallback(A2DPService.A2DPCallback mA2DPCallback) {
        this.mA2DPCallback = mA2DPCallback;
    }
}