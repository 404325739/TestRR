package com.jancar.bluetooth;

import android.os.RemoteException;

import com.jancar.base.BaseApplication;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.btservice.bluetooth.IBluetoothCallback;
import com.jancar.sdk.BaseManager;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.media.IVIMedia;
import com.jancar.sdk.media.MediaManager;
import com.jancar.sdk.system.SystemManager;
import com.jancar.sdk.utils.Logcat;
import com.jancar.services.system.ISystemCallback;

import org.greenrobot.eventbus.EventBus;

public class BtApplication extends BaseApplication {

    private static BtApplication pThis = null;
    private BluetoothManager mBluetoothManager = null;
    private SystemManager mSystemManager = null;
    private MediaManager mMediaManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        pThis = this;
        mSystemManager = new SystemManager(this, iSystemConnectListen);
        mMediaManager = new MediaManager(this, iMediaConnectListen, iMediaControlListen);
    }

    public static BtApplication getInstance() {
        return pThis;
    }

    /**
     * 蓝牙服务管理工具类
     *
     * @param manager
     */
    public void setBluetoothManager(BluetoothManager manager) {
        Logcat.d("setBluetoothManager -> BluetoothManager = " + manager);
        if (manager == null) {
            mBluetoothManager = null;
        } else {
            mBluetoothManager = manager;
            mBluetoothManager.registerBluetoothCallback(iBluetoothCallback);
        }
    }

    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    /**
     * 打开导航应用
     */
    public void startNavigationApp() {
        if (mSystemManager != null) {
            mSystemManager.openNaviApp();
        }
    }

    IBluetoothCallback.Stub iBluetoothCallback = new IBluetoothCallback.Stub() {
        @Override
        public void onConnectStatus(int nStatus, String addr, String name) throws RemoteException {

        }

        @Override
        public void onCallStatus(int nStatus, String phoneNumber, String contactName) throws RemoteException {

        }

        @Override
        public void onVoiceChange(int type) throws RemoteException {

        }

        @Override
        public void onA2DPConnectStatus(int avstatus, boolean isStoped) throws RemoteException {

        }

        @Override
        public void onBtMusicId3Info(String name, String artist, String album, long duration) throws RemoteException {

        }

        @Override
        public void onBtBatteryValue(int value) throws RemoteException {

        }

        @Override
        public void onBtSignalValue(int value) throws RemoteException {

        }

        @Override
        public void onPowerStatus(boolean value) throws RemoteException {

        }
    };

    ISystemCallback.Stub iSystemCallback = new ISystemCallback.Stub() {
        @Override
        public void onOpenScreen(int from) throws RemoteException {

        }

        @Override
        public void onCloseScreen(int from) throws RemoteException {

        }

        @Override
        public void onScreenBrightnessChange(int id, int brightness) throws RemoteException {

        }

        @Override
        public void onCurrentScreenBrightnessChange(int id, int brightness) throws RemoteException {

        }

        @Override
        public void quitApp() throws RemoteException {
            Logcat.d("=====ISystemCallback quitApp");
            EventBus.getDefault().post(new EventClassDefine.EventQuitApp(IVIMedia.Type.PHONE));
        }

        @Override
        public void startNavigationApp() throws RemoteException {

        }

        @Override
        public void onMediaAppChanged(String packageName, boolean isOpen) throws RemoteException {

        }

        @Override
        public void gotoSleep() throws RemoteException {

        }

        @Override
        public void wakeUp() throws RemoteException {

        }

        @Override
        public void onFloatBarVisibility(int visibility) throws RemoteException {

        }

        @Override
        public void onTboxChange(boolean isOpen) throws RemoteException {

        }

        @Override
        public void onScreenProtection(boolean isEnterScreenProtection) throws RemoteException {

        }

        @Override
        public void onTelPhoneStatusChange(int status, String phoneNumber, String phoneName) throws RemoteException {

        }

        @Override
        public void onTouchEventPos(int x, int y) throws RemoteException {

        }
    };

    IVIMedia.MediaControlListener iMediaControlListen = new IVIMedia.MediaControlListener() {
        @Override
        public void suspend() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void play() {

        }

        @Override
        public void playPause() {

        }

        @Override
        public void setVolume(float volume) {

        }

        @Override
        public void next() {

        }

        @Override
        public void prev() {

        }

        @Override
        public void select(int index) {

        }

        @Override
        public void setFavour(boolean isFavour) {

        }

        @Override
        public void filter(String title, String singer) {

        }

        @Override
        public void playRandom() {

        }

        @Override
        public void setPlayMode(int mode) {

        }

        @Override
        public void quitApp(int quitSource) {
            Logcat.d("===== IVIMedia.MediaControlListener quitApp");
            EventBus.getDefault().post(new EventClassDefine.EventQuitApp(quitSource));
        }

        @Override
        public void onVideoPermitChanged(boolean show) {

        }

        @Override
        public void seekTo(int msec) {

        }

        @Override
        public void setFrequencyDoubling(int operation, int rate) {

        }
    };

    BaseManager.ConnectListener iMediaConnectListen = new BaseManager.ConnectListener() {
        @Override
        public void onServiceConnected() {
        }

        @Override
        public void onServiceDisconnected() {
        }
    };
    BaseManager.ConnectListener iSystemConnectListen = new BaseManager.ConnectListener() {
        @Override
        public void onServiceConnected() {
            mSystemManager.registerSystemCallback(iSystemCallback);
        }

        @Override
        public void onServiceDisconnected() {
            mSystemManager.unRegisterSystemCallback(iSystemCallback);
        }
    };
}
