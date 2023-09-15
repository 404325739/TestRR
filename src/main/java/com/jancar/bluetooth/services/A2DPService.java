package com.jancar.bluetooth.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jancar.bluetooth.MyApplication;
import com.jancar.bluetooth.utils.MediaManagerUtil;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.btservice.bluetooth.IBluetoothStatusCallback;
import com.jancar.sdk.BaseManager;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.media.IVIMedia;
import com.jancar.sdk.system.IVISystem;
import com.jancar.sdk.utils.Logcat;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙音乐服务
 * 20210906 lyy 从lib-music2中复制来
 */

public class A2DPService extends Service {
    public static final float VOLUME_PERCENT_STANDARD = 1.0f;
    private BluetoothManager mBluetoothManager = null;
    private int mA2dpStatus = -1; // 蓝牙音乐状态
    private IVIBluetooth.EventMp3Id3Info mEventMp3Id3Info; // 蓝牙音乐的id3信息
    private float mCurVolumePercent = 1.0f; // 蓝牙音乐音频

    private int mUserSetStatus = -1;

    // 蓝牙连接后是否发播放指令
    private boolean mWillPlayWhenConnect = false;
    private int mRecoverCount = 0; // 用于需要恢复播放时，尝试播放的次数
    public static final int RECOVER_COUNT = 25; //恢复播放时可以尝试的总次数

    MediaManagerUtil mMediaManagerUtil;
    // 用于标记播放暂停命令发送的回馈
    private boolean mIsResponsed = true;
    private static final int WAIT_RESPONSED_COUNT = 10;
    private static int mWaitResponsedIndex = 0;
    private static final int TIMER_A2DP_CMD_DELAY = 200; // 蓝牙音乐指令间隔时间

    @Override
    public void onCreate() {
        super.onCreate();
        Logcat.d();
    }

    @Override
    public void onDestroy() {
        Logcat.d();
        mMediaManagerUtil.destroy();
        unregisterA2DPCallback(mMediaManagerUtil.getA2DPCallback());
        super.onDestroy();
    }

    public class A2DPBinder extends Binder {
        public A2DPService getService() {
            return A2DPService.this;
        }
    }

    A2DPBinder mA2DPBinder = new A2DPBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mA2DPBinder;
    }

    private static final int MSG_PLAY_PAUSE = 1;
    private static final int MSG_CHECK_PLAYSTATE = 2;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PLAY_PAUSE:
                    if (mIsResponsed) {
                        Logcat.d("mIsResponsed >>" + msg.arg1);
                        setMessageToService(msg.arg1);
                    } else {
                        mWaitResponsedIndex++;
                        if (mWaitResponsedIndex >= WAIT_RESPONSED_COUNT) {
                            Logcat.d("The service did not respond to the previous data for a long time, sent directly!!");
                            setMessageToService(msg.arg1);
                            mWaitResponsedIndex = 0;
                            mUserSetStatus = -1; // 强制发送的不做校正
                            return;
                        }
                        Logcat.d("The last sent command has not responded yet and needs to wait for sending.");
                        Message msg1 = obtainMessage();
                        msg1.what = msg.what;
                        msg1.arg1 = msg.arg1;
                        msg1.arg2 = msg.arg2;
                        sendMessageDelayed(msg1, 300);
                    }
                    break;
                case MSG_CHECK_PLAYSTATE:
                    //                    Logcat.d("isPlaying:" + isPlaying() + ", mUserSetState:" + mUserSetState);
                    if (mA2dpStatus != mUserSetStatus) {
                        if (mUserSetStatus == IVIBluetooth.BluetoothA2DPStatus.STREAMING) {
                            play();
                        } else if (mUserSetStatus == IVIBluetooth.BluetoothA2DPStatus.CONNECTED) {
                            pause();
                        }
                    }
                    break;
            }
        }
    };

    private void setMessageToService(int cmd) {
        if (cmd == IVIBluetooth.BluetoothA2DPStatus.STREAMING) {
            if (mMediaManagerUtil != null && !mMediaManagerUtil.isActiveMedia(IVIMedia.Type.A2DP) || !mMediaManagerUtil.canPlayMedia()) {
                // 退出的时候需要发送暂停命令  所以这个判断只能放到播放的地方来处理
                Logcat.d("The status is incorrect, do not send commands！！");
                return;
            }
            Logcat.d("-------------mMediaControl play playState: " + isPlaying());
            if (!isPlaying()) {
                mIsResponsed = false;
                Logcat.d("send play cmd:" + cmd);
                mBluetoothManager.playBtMusic(mIBluetoothExecCallback);
                checkPlayState();
            }

        } else if (cmd == IVIBluetooth.BluetoothA2DPStatus.CONNECTED) {
            Logcat.d("-------------------mMediaControl pause playState:" + isPlaying());
            if (isPlaying()) {
                mIsResponsed = false;
                Logcat.d("send play cmd:" + cmd);
                mBluetoothManager.pauseBtMusic(mIBluetoothExecCallback);
                checkPlayState();
            }
        }
    }

    private void setMessageToService(int cmd, boolean force) {
        if (cmd == IVIBluetooth.BluetoothA2DPStatus.STREAMING) {
            if (mMediaManagerUtil != null && !mMediaManagerUtil.isActiveMedia(IVIMedia.Type.A2DP) || !mMediaManagerUtil.canPlayMedia()) {
                // 退出的时候需要发送暂停命令  所以这个判断只能放到播放的地方来处理
                Logcat.d("The status is incorrect, do not send commands！！");
                return;
            }
            Logcat.d("-------------mMediaControl play playState: " + isPlaying());
            if (!isPlaying() || false) {
                mIsResponsed = false;
                Logcat.d("send play cmd:" + cmd);
                mBluetoothManager.playBtMusic(mIBluetoothExecCallback);
                checkPlayState();
            }

        } else if (cmd == IVIBluetooth.BluetoothA2DPStatus.CONNECTED) {
            Logcat.d("-------------------mMediaControl pause playState:" + isPlaying());
            if (isPlaying() || force) {
                mIsResponsed = false;
                Logcat.d("send play cmd:" + cmd);
                mBluetoothManager.pauseBtMusic(mIBluetoothExecCallback);
                checkPlayState();
            }
        }
    }

    /**
     * 发送检测命令是否发送成功的消息
     */
    private void checkPlayState() {
        mHandler.removeMessages(MSG_CHECK_PLAYSTATE);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_PLAYSTATE, 1000);
    }

    /**
     * 初始化
     */
    public void init(MediaManagerUtil mediaManagerUtil) {
        this.mMediaManagerUtil = mediaManagerUtil;
        registerA2DPCallback(mMediaManagerUtil.getA2DPCallback());
        MediaManagerUtil.registerEventBus(this);

        if (mBluetoothManager == null) {
            mBluetoothManager = new BluetoothManager(MyApplication.getApplication(), new BaseManager.ConnectListener() {
                @Override
                public void onServiceConnected() {
                    Logcat.d("onServiceConnected >>" + mBluetoothManager);
                    if (mBluetoothManager != null) {
                        mBluetoothManager.openBluetoothModule(new IBluetoothExecCallback.Stub() {
                            @Override
                            public void onSuccess(String msg) throws RemoteException {

                            }

                            @Override
                            public void onFailure(int errorCode) throws RemoteException {

                            }
                        });
                        mBluetoothManager.getBluetoothModuleStatus(new IBluetoothStatusCallback.Stub() {
                            @Override
                            public void onSuccess(int status, int hfpStatus, int a2dpStatus) throws RemoteException {
                                // 获取一次a2dp的状态
                                Logcat.d("getBluetoothModuleStatus------" + a2dpStatus);
                                updateA2dpStatus(a2dpStatus);
                            }

                            @Override
                            public void onFailure(int errorCode) throws RemoteException {

                            }
                        });
                    }
                }

                @Override
                public void onServiceDisconnected() {

                }
            });
        }

    }

    /**
     * a2dp的回调
     */
    public interface A2DPCallback {
        /**
         * id3信息改变
         *
         * @param name     名字
         * @param artist   歌手
         * @param album    专辑
         * @param duration 总曲目
         */
        void onId3InfoChanged(String name, String artist, String album, long duration);

        /**
         * 蓝牙状态发生改变
         *
         * @param status {@link IVIBluetooth.BluetoothA2DPStatus}
         */
        void onA2dpStatusChanged(int status);

    }

    private List<A2DPCallback> mA2DPCallbacks = new ArrayList<>();

    /**
     * 监听A2DP回调
     *
     * @param callback
     */
    public void registerA2DPCallback(A2DPCallback callback) {
        if (callback != null && !mA2DPCallbacks.contains(callback)) {
            mA2DPCallbacks.add(callback);
        }
    }

    /**
     * 注销监听
     *
     * @param callback
     */
    public void unregisterA2DPCallback(A2DPCallback callback) {
        if (callback != null && mA2DPCallbacks.contains(callback)) {
            mA2DPCallbacks.remove(callback);
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        mA2DPCallbacks.clear();
        MediaManagerUtil.unregisterEventBus(this);
        mBluetoothManager = null;
    }

    /**
     * 蓝牙音乐id3信息更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventA2DPId3Info(IVIBluetooth.EventMp3Id3Info event) {
        mEventMp3Id3Info = event;
        if (event != null) {
            Logcat.d("event:" + event.toString());
            for (A2DPCallback callback : mA2DPCallbacks) {
                callback.onId3InfoChanged(event.name, event.artist, event.album, event.duration);
            }
        } else {
            for (A2DPCallback callback : mA2DPCallbacks) { // 清空id3信息
                callback.onId3InfoChanged("", "", "", 0);
            }
        }
    }

    /**
     * 蓝牙音乐的连接状态
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventModuleConnectStatus(IVIBluetooth.EventModuleConnectStatus event) {
        if (event != null) {
            Logcat.d("----");
            updateA2dpStatus(event.a2dpStatus);
            mIsResponsed = true;

            //以前这边断开不会发，只会发成功,蓝工修改后各个状态都会发。
            //连接成功
            if (isPlaying()) {
                mWillPlayWhenConnect = false;
                Logcat.d("reset mWillPlayWhenConnect state!!");
            }


        }
    }

    /**
     * 蓝牙音频状态是否需要改变（主要针对carplay/auto等与蓝牙音频状态互斥的应用）
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMediaAppChanged(IVISystem.EventMediaAppChanged event) {
        if (event != null) {
            Logcat.d(" package name = " + event.mPackageName + " isOpen " + event.mIsOpen);
            boolean isShowBTMusic = needShowBtMusic(event);
            Logcat.d("isShowBTMusic:" + isShowBTMusic);
            if (isA2dpConnected() && !isShowBTMusic) {
                if (isPlaying()) {
                    pause();
                }
            }

            // 将消息通知到Activity
            MediaManagerUtil.EventA2DPPermissionChange.sendEvent(isShowBTMusic);
        }
    }

    /**
     * 判定是否显示蓝牙音乐
     *
     * @param event
     * @return
     */
    private boolean needShowBtMusic(IVISystem.EventMediaAppChanged event) {
        if (event != null && TextUtils.equals(IVISystem.PACKAGE_CAR_PLAY, event.mPackageName)
                || TextUtils.equals(IVISystem.PACKAGE_ANDROID_AUTO, event.mPackageName)) {
            return !event.mIsOpen;
        }
        return true;
    }

    /**
     * 下一曲
     */
    public void next() {
        if (mBluetoothManager != null) {
            mBluetoothManager.nextBtMusic(mIBluetoothExecCallback);
        }
    }

    /**
     * 上一曲
     */
    public void prev() {
        if (mBluetoothManager != null) {
            mBluetoothManager.prevBtMusic(mIBluetoothExecCallback);
        }
    }

    /**
     * 播放
     */
    public void play() {
        Logcat.d();
        mUserSetStatus = IVIBluetooth.BluetoothA2DPStatus.STREAMING;
        Message msg = mHandler.obtainMessage(MSG_PLAY_PAUSE, IVIBluetooth.BluetoothA2DPStatus.STREAMING, -1);
        mHandler.removeMessages(MSG_PLAY_PAUSE);
        mHandler.sendMessageDelayed(msg, TIMER_A2DP_CMD_DELAY);

    }

    /**
     * 播放暂停
     */
    public void playAndPause() {
        Logcat.d();
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
        //        if (mBluetoothManager != null) {
        //            mBluetoothManager.playAndPause(mIBluetoothExecCallback);
        //        }
    }

    /**
     * 停止,杰发平台退出后应停止。这样手机端播放蓝牙音乐也不让播
     * 避免混音问题。
     */
    public void stop() {
        if (mBluetoothManager != null) {
            Logcat.d();
            mBluetoothManager.stopBtMusic(mIBluetoothExecCallback);
        }
    }


    /**
     * 暂停播放
     */
    public void pause() {
        Logcat.d();
        mUserSetStatus = IVIBluetooth.BluetoothA2DPStatus.CONNECTED;
        Message msg = mHandler.obtainMessage(MSG_PLAY_PAUSE, IVIBluetooth.BluetoothA2DPStatus.CONNECTED, -1);
        mHandler.removeMessages(MSG_PLAY_PAUSE);
        mHandler.sendMessageDelayed(msg, TIMER_A2DP_CMD_DELAY);
    }

    /**
     * 马上暂停播放，用于systemui划掉的情况
     */
    public void pausenow() {
        Logcat.d();
        //        mHandler.removeMessages(MSG_PLAY_PAUSE);
        //        mUserSetStatus = IVIBluetooth.BluetoothA2DPStatus.CONNECTED;
        ////        Message msg = new Message();
        ////        msg.what = MSG_PLAY_PAUSE;
        ////        msg.arg1 = IVIBluetooth.BluetoothA2DPStatus.CONNECTED;
        ////        msg.arg2 = -1;
        //        Message msg = mHandler.obtainMessage(MSG_PLAY_PAUSE, IVIBluetooth.BluetoothA2DPStatus.CONNECTED , -1);
        //
        //        mHandler.sendMessageDelayed(msg, 0);
        //        mHandler.sendMessage(msg);
        setMessageToService(IVIBluetooth.BluetoothA2DPStatus.CONNECTED, true);
    }

    public void suspend() {
        Logcat.d("isPlaying:" + isPlaying());
        if (isPlaying()) {
            mWillPlayWhenConnect = true;
            pause();
        }
    }

    public void resume() {
        Logcat.d("mWillPlayWhenConnect " + mWillPlayWhenConnect);
        //if (mWillPlayWhenConnect) {
        Logcat.d("is playing before suspend");
        if (!isA2dpConnected()) {
            // 未连接的时候 需要做延迟确认播放的操作
            if (mMediaManagerUtil != null && mMediaManagerUtil.isActiveMedia(IVIMedia.Type.A2DP)) {
                mRecoverCount = 0;
                mHandler.removeCallbacks(recoverRunnable);
                mHandler.postDelayed(recoverRunnable, 100);
            }
        } else {
            // 已连接，则开始播放
            Logcat.d("mWillPlayWhenConnect：" + mWillPlayWhenConnect);
            //if (mWillPlayWhenConnect) {
            mIsResponsed = true;
            play();
            //}
        }
        //}

    }

    Runnable recoverRunnable = new Runnable() {
        @Override
        public void run() { // stop 之后 不应该再尝试
            Logcat.d("playing state:" + isPlaying());
            if (!isPlaying() && mMediaManagerUtil != null && mMediaManagerUtil.isActiveMedia(IVIMedia.Type.A2DP)) {
                play();
                mRecoverCount++;
                if (mRecoverCount < RECOVER_COUNT) {
                    Logcat.d(" the number of attempts to play：" + mRecoverCount + ", mWillPlayWhenConnect:" + mWillPlayWhenConnect);
                    mHandler.postDelayed(this, 1000);
                }
            } else {
                Logcat.d(" isPlaying():" + isPlaying());
                if (isPlaying() || (mMediaManagerUtil != null && !mMediaManagerUtil.isActiveMedia(IVIMedia.Type.A2DP))) {
                    Logcat.d("mWillPlayWhenConnect = false;");
                    mWillPlayWhenConnect = false;
                }
            }
        }
    };

    /**
     * 蓝牙音乐拖动进度，暂不支持
     *
     * @param mesc
     */
    public void seekTo(int mesc) {
        Logcat.w("no support seekTo!");
    }

    /**
     * 设置蓝牙音乐音量，暂不支持，该操作由服务做
     *
     * @param volume
     */
    public void setVolume(float volume) {
        Logcat.w("no support setVolume!");
        if (volume >= 0 && volume <= 1.0f) {
            setBtMusicVolumePercent(volume);
        }
    }

    /**
     * 设置蓝牙音频的音量
     *
     * @param volume
     */
    public void setBtMusicVolumePercent(float volume) {
        if (mBluetoothManager != null) {
            Logcat.d("volume:" + volume);
            mCurVolumePercent = volume;
            mBluetoothManager.setBtMusicVolumePercent(volume, null);
        }
    }

    /**
     * 当前是否是播放状态
     *
     * @return
     */
    public boolean isPlaying() {
        return IVIBluetooth.BluetoothA2DPStatus.STREAMING <= mA2dpStatus;
    }

    /**
     * 获取蓝牙音乐的进度
     *
     * @return
     */
    public int getCurProgress() {
        return 0;
    }

    /**
     * 蓝牙音乐是否连接上
     *
     * @return
     */
    public boolean isA2dpConnected() {
        return IVIBluetooth.BluetoothA2DPStatus.CONNECTED <= mA2dpStatus;
    }

    // 蓝牙监听
    private IBluetoothExecCallback.Stub mIBluetoothExecCallback = new IBluetoothExecCallback.Stub() {
        @Override
        public void onSuccess(String msg) throws RemoteException {

        }

        @Override
        public void onFailure(int errorCode) throws RemoteException {
            Logcat.w("errorCode:" + IVIBluetooth.BluetoothExecErrorMsg.getName(errorCode));
        }
    };

    /**
     * 刷新A2DP的状态
     *
     * @param a2dpStatus {@link IVIBluetooth.BluetoothA2DPStatus}
     */
    private void updateA2dpStatus(int a2dpStatus) {
        mA2dpStatus = a2dpStatus;
        Logcat.d("mA2dpStatus:" + IVIBluetooth.BluetoothA2DPStatus.getName(mA2dpStatus) + " mA2DPCallbacks:" + mA2DPCallbacks.size());
        if (mA2dpStatus == mUserSetStatus) {
            mUserSetStatus = -1;
        }
        for (A2DPCallback callback : mA2DPCallbacks) {
            callback.onA2dpStatusChanged(mA2dpStatus);
        }

        if (a2dpStatus < IVIBluetooth.BluetoothA2DPStatus.CONNECTED) { // 蓝牙音乐断开连接
            onEventA2DPId3Info(null);
        }
    }

    /**
     * 请求播放信息
     */
    public void requestPlayMusicInfo() {
        Logcat.d("------");
        updateA2dpStatus(mA2dpStatus); // 通知APP播放状态
        onEventA2DPId3Info(mEventMp3Id3Info); // 通知APP id3信息
        if (mEventMp3Id3Info == null) {
            mBluetoothManager.getBtMusicId3Info(mIBluetoothExecCallback);
        }
    }

}
