package com.jancar.bluetooth.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.jancar.base.AppManager;
import com.jancar.bluetooth.MyApplication;
import com.jancar.bluetooth.utils.MediaManagerUtil;
import com.jancar.sdk.media.IVIMedia;
import com.jancar.sdk.utils.LogNameUtil;
import com.jancar.sdk.utils.Logcat;

/**
 * 蓝牙音乐 presenter 类，用于与服务通信
 * 20210906 lyy 从lib-music2中复制来
 */

public class A2dpPresenter {

    private Context mContext = MyApplication.getApplication();
    private A2DPService.A2DPCallback mA2DPCallback;
    private A2DPService mA2DPService;
    private MediaManagerUtil mMediaManagerUtil;
    private int mMediaType = IVIMedia.Type.NONE;
    private boolean mIsStart;
    private boolean mIsNeedResume;

    public A2dpPresenter() {
        mMediaManagerUtil = new MediaManagerUtil(mContext, mMediaControlListener);
        Intent services = new Intent(mContext, A2DPService.class);
        mContext.bindService(services, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 服务连接器
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logcat.d();
            mA2DPService = ((A2DPService.A2DPBinder) service).getService(); // 从服务中获取句柄
            Logcat.d("mA2DPService: " + mA2DPService + " mA2DPCallback: " + mA2DPCallback + " mMediaType: " + mMediaType);
            if (null != mA2DPService) {
                mA2DPService.init(mMediaManagerUtil);
            }
            if (mA2DPCallback != null) { // 如果调用早于服务连接，在连接时候设置回调
                registerA2DPCallback(mA2DPCallback);
            }

            if (mMediaType != IVIMedia.Type.NONE) {
                open(mMediaType);
                if (mIsStart) {
                    start();
                }
                requestPlayMusicInfo();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mA2DPService = null;
        }
    };

    IVIMedia.MediaControlListener mMediaControlListener = new IVIMedia.MediaControlListener() {
        @Override
        public void suspend() {
            Logcat.d("isPlaying:" + isPlaying());
            if (isPlaying()) {
                if (isA2DPOpened()) {
                    mA2DPService.suspend();
                }
                mIsNeedResume = true;
            }
        }

        @Override
        public void stop() {
            A2dpPresenter.this.stop();
        }

        @Override
        public void resume() {
            Logcat.d("mIsResume:" + mIsNeedResume + ", " + mMediaManagerUtil.isActiveMedia(mMediaManagerUtil.getMediaType()));
            Logcat.d("canPlayMedia:" + mMediaManagerUtil.canPlayMedia());
            if (mMediaManagerUtil.canPlayMedia() && mMediaManagerUtil.isActiveMedia(mMediaManagerUtil.getMediaType())) {
                if (isA2DPOpened()) { // 修改蓝牙音乐状态语音唤醒蓝牙电话，挂断电话之后 蓝牙音乐不恢复播放问题
                    if (mIsNeedResume) {
                        mIsNeedResume = false;
                        mA2DPService.resume();
                    }
                } else if (mIsNeedResume) {
                    mIsNeedResume = false;
                }
            } else {
                Logcat.d("connot resume, return!!!");
            }
        }

        @Override
        public void pause() {
            A2dpPresenter.this.pause();
            mIsNeedResume = false;
        }

        @Override
        public void play() {
            start();
        }

        @Override
        public void playPause() {
            if (mMediaManagerUtil.canPlayMedia() && isA2DPOpened()) {
                playAndPause();
            }
        }

        @Override
        public void setVolume(float volume) {
            if (isA2DPOpened() && null != mA2DPService) {
                mA2DPService.setVolume(volume);
            }
        }

        @Override
        public void next() {
            if (mMediaManagerUtil.canPlayMedia()) {
                if (isA2DPOpened() && null != mA2DPService) {
                    mA2DPService.next();
                }
            }
        }

        @Override
        public void prev() {
            if (mMediaManagerUtil.canPlayMedia()) {
                if (isA2DPOpened() && null != mA2DPService) {
                    mA2DPService.prev();
                }
            }
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
        public void quitApp(int source) { // 是否应该交由UI去处理 (音乐和蓝牙音乐，划掉的可能是非正在播放的type)
            Logcat.d("source:" + source /*+ ", mediaType:" + mediaType*/);
            if (source == IVIMedia.QuitMediaSource.VOICE) {
                AppManager.getAppManager().finishAllActivity();
            }

            stop();
            A2dpPresenter.this.pause();
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

    /**
     * 注册蓝牙音乐回调监听
     *
     * @param callback
     */
    public void registerA2DPCallback(A2DPService.A2DPCallback callback) {
        if (mA2DPService != null) {
            mA2DPCallback = null;
            Logcat.d();
            mA2DPService.registerA2DPCallback(callback);
        } else {
            mA2DPCallback = callback;
        }
    }

    /**
     * 注销蓝牙音乐回调监听
     *
     * @param callback
     */
    public void unregisterA2DPCallback(A2DPService.A2DPCallback callback) {
        if (mA2DPService != null) {
            Logcat.d();
            mA2DPService.unregisterA2DPCallback(callback);
        }
        if (mA2DPCallback == callback) {
            mA2DPCallback = null;
        }
    }

    /**
     * 打开蓝牙音乐
     */
    public void open() {
        int type = IVIMedia.Type.A2DP;
        if (mA2DPService != null) {
            mMediaType = IVIMedia.Type.NONE;
            open(type);
        } else {
            mMediaType = type;
        }
    }

    /**
     * 关闭蓝牙音乐
     */
    public void close() {
        if (isActive()) {
            pause();
        }
        mMediaType = IVIMedia.Type.NONE;
        if (mMediaManagerUtil != null) {
            mMediaManagerUtil.close(IVIMedia.Type.A2DP);
        }
    }

    /**
     * 蓝牙音乐是否是连接状态
     *
     * @return
     */
    public boolean isA2dpConnected() {
        if (mA2DPService != null) {
            return mA2DPService.isA2dpConnected();
        }
        return false;
    }

    /**
     * 当前是否在播放蓝牙音乐
     *
     * @return
     */
    public boolean isActive() {
        return mMediaManagerUtil.getMediaType() == IVIMedia.Type.A2DP;
    }

    /**
     * 蓝牙媒体是否打开
     * 注意：播放蓝牙音乐时切到其它音源，MediaService回调mMediaControlListener.stop时这里已经false了
     * @return
     */
    public boolean isA2DPOpened() {
        return mMediaManagerUtil.isMediaOpened(IVIMedia.Type.A2DP);
    }

    private void open(int type) {
        /*if (mMediaManagerUtil.getMediaType() != type) { // 如果当前打开的不是已经打开的，则先关闭之前
            if (mMediaManagerUtil.getMediaType() == IVIMedia.Type.MUSIC ||
                    mMediaManagerUtil.getMediaType() == IVIMedia.Type.A2DP) {
                Logcat.d("------:" + LogNameUtil.getName(type, IVIMedia.Type.class));
                stop(); // 停掉之前的
            }
            mMediaManagerUtil.close(mMediaManagerUtil.getMediaType());
        }*/

        if (mMediaManagerUtil != null) {
            mMediaManagerUtil.open(type);
            if (!mMediaManagerUtil.isMediaOpened(type) || (mMediaManagerUtil.getMediaType() == IVIMedia.Type.NONE)) {
                mIsNeedResume = false;
            }
        }
    }


    /**
     * 下一曲
     */
    public void next() {
        if (mA2DPService != null) {
            mA2DPService.next();
        }
    }

    /**
     * 上一曲
     */
    public void prev() {
        if (mA2DPService != null) {
            mA2DPService.prev();
        }
    }

    /**
     * 开始播放，一般用于 start/pause操作
     */
    public void start() {
        Logcat.d("mA2DPService: " + mA2DPService);
        if (mA2DPService != null) {
            if (mMediaManagerUtil.canPlayMedia()) {
                mA2DPService.play();
            } else {
                mIsNeedResume = true;
            }
            mIsStart = false;
        } else {
            mIsStart = true;
        }
    }

    /**
     * 播放或者暂停
     */
    public void playAndPause() {
        if (mA2DPService != null) {
            mA2DPService.playAndPause();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mA2DPService != null) {
            mA2DPService.pause();
        }
        mIsStart = false;
    }

    /**
     * 停止播放
     */
    public void stop() {
        Logcat.d();
        if (isPlaying()) {
            mA2DPService.pausenow();//8257处理蓝牙音乐杀不掉

            //systemUI杀进程消息不能延迟
            //mA2DPService.setBtMusicVolumePercent(A2DPService.VOLUME_PERCENT_STANDARD);

            //现在用这个接口，防止手机端播放蓝牙音乐混音
            mIsNeedResume = true;
            mA2DPService.stop();

            /*// 清空显示数据，主要是清理通知栏信息
            if (mMediaManagerUtil != null && mMediaManagerUtil.getPlayerCallback() != null) {
                mMediaManagerUtil.getPlayerCallback().onMusicInfoChanged(null);
            }*/
        } else {
            Logcat.d("is not play");
            //防止语音混音
            //mA2DPService.pausenow();

            //导航压制音量恢复。
            mA2DPService.setBtMusicVolumePercent(A2DPService.VOLUME_PERCENT_STANDARD);

            //防止蓝牙音乐播电话，切收音机挂电话混音的问题，（这里没有回调，蓝牙音乐开始播放）
            mA2DPService.stop();
        }
    }

    /**
     * 请求一次播放信息
     */
    public void requestPlayMusicInfo() {
        Logcat.d("isA2DPOpened():" + isA2DPOpened());
        if (null != mA2DPService) {
            mA2DPService.requestPlayMusicInfo();
        }
    }

    /**
     * 销毁presenter
     */
    public void destroy() {
        if (mA2DPService != null) {
            Logcat.d();
            mContext.unbindService(mServiceConnection);
            mA2DPService = null;
        }
    }

    /**
     * 判断当前是否是播放状态
     *
     * @return
     */
    public boolean isPlaying() {
        if (/*isA2DPOpened() && */null != mA2DPService) {
            return mA2DPService.isPlaying();
        }
        return false;
    }
}
