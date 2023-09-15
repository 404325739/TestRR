package com.jancar.bluetooth.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.jancar.bluetooth.services.A2DPService;
import com.jancar.sdk.BaseManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.media.IVIMedia;
import com.jancar.sdk.media.MediaManager;
import com.jancar.sdk.utils.LogNameUtil;
import com.jancar.sdk.utils.Logcat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 和MediaManager交互的工具类，用于和 Services 进行通讯 </br>
 * 例：将播放信息发送给launcher，原车等等
 * 20210906 lyy 从lib-music2中复制来
 */

public class MediaManagerUtil {

    private MediaManager mMediaManager = null; // 获取媒体数据
    private Context mContext;
    private int mMediaType = IVIMedia.Type.NONE; // 音乐内部记忆的状态，不一定是当前系统的媒体状态

    private BaseManager.ConnectListener mMediaConnectListener = new BaseManager.ConnectListener() {
        @Override
        public void onServiceConnected() {
            Logcat.d();
        }

        @Override
        public void onServiceDisconnected() {
            Logcat.d();
            if (mMediaManager != null) {
                mMediaManager.connect();
            }
        }
    };

    private MediaManagerUtil() {

    }

    public MediaManagerUtil(Context context, IVIMedia.MediaControlListener listener) {
        if (context != null) {
            mContext = context.getApplicationContext();
            mMediaManager = new MediaManager(mContext, mMediaConnectListener, listener, false); // MediaManager

        }

        registerEventBus(this);
    }

    public MediaManager getMediaManager() {
        return mMediaManager;
    }

    /**
     * 销毁
     */
    public void destroy() {
        unregisterEventBus(this);
        if (null != mMediaManager) {
            mMediaManager.unregisterEventBus(this);
        }
    }

    /**
     * 蓝牙音乐回调监听
     */
    private A2DPService.A2DPCallback mA2DPCallback = new A2DPService.A2DPCallback() {
        @Override
        public void onId3InfoChanged(String name, String artist, String album, long duration) {
            setMediaInfo(IVIMedia.Type.A2DP, name, artist, null, 0, 0);
        }

        @Override
        public void onA2dpStatusChanged(int status) {
            setMediaState(IVIMedia.Type.A2DP,
                    status == IVIBluetooth.BluetoothA2DPStatus.STREAMING ? IVIMedia.MediaState.PLAYING : IVIMedia.MediaState.PAUSED,
                    0, 0);
        }

    };

    /**
     * 返回蓝牙音乐监听
     *
     * @return
     */
    public A2DPService.A2DPCallback getA2DPCallback() {
        return mA2DPCallback;
    }

    /**
     * 打开音乐
     *
     * @param type {@link IVIMedia.Type}
     */
    public void open(int type) {
        mMediaType = type;
        mMediaManager.open(type);
        mMediaManager.setCurrentShownMediaType(type);
        Logcat.d("type:" + IVIMedia.Type.getName(type));
    }

    /**
     * 退出app
     *
     * @param type {@link IVIMedia.Type}
     */
    public void close(int type) {
        if (mMediaType == type) {
            mMediaType = IVIMedia.Type.NONE;
        }

        mMediaManager.close(type);
        Logcat.d("type:" + IVIMedia.Type.getName(type));

        // 清空媒体状态和状态栏
        setMediaInfo();
    }

    /**
     * 返回是否允许播放媒体
     *
     * @return
     */
    public boolean canPlayMedia() {
        return mMediaManager.canPlayMedia();
    }

    /**
     * 判断media type 是否是活动的
     *
     * @param type
     * @return
     */
    public boolean isActiveMedia(int type) {
//        Logcat.d("current media type:" + LogNameUtil.getName(mMediaManager.getActiveMedia(), IVIMedia.Type.class));
        return mMediaManager.getActiveMedia() == type;
    }

    /**
     * 是否是蓝牙音乐状态
     *
     * @return
     */
    public boolean isMusicMediaActive() {
        return mMediaManager.getActiveMedia() == IVIMedia.Type.A2DP;
    }

    /**
     * 设置播放信息
     */
    private void setMediaInfo() {// 没有播放歌曲，清空桌面控件
        setMediaInfo(IVIMedia.Type.A2DP, "", "", null, 0, 0); // 清空媒体信息
        setMediaState(IVIMedia.Type.A2DP, IVIMedia.MediaState.STOPPED, 0, 0);
    }

    /**
     * 设置播放状态
     *
     * @param isPlaying 当前是否是播放状态
     * @param position  进度
     * @param duration  总时间
     */
    private void setMediaState(boolean isPlaying, int position, int duration) {
        setMediaState(IVIMedia.Type.A2DP, isPlaying ? IVIMedia.MediaState.PLAYING : IVIMedia.MediaState.PAUSED,
                position, duration);
    }

    /**
     * 设置媒体信息给其他控件
     *
     * @param type      {@link IVIMedia.Type}
     * @param name      媒体名字
     * @param artist    歌手
     * @param artBitmap 歌手图片
     * @param index     第几首歌曲
     * @param totalSize 总歌曲数
     */
    private void setMediaInfo(int type, String name, String artist, Bitmap artBitmap, int index, int totalSize) {
        Logcat.d("type:" + LogNameUtil.getName(type, IVIMedia.Type.class) + ", name:" + name + ", artist:" + artist + ", index:" + index + ", total:" + totalSize);
        mMediaManager.setMediaInfo(type, name, artist, artBitmap, index, totalSize, false);
        if (isMusicMediaActive() || TextUtils.isEmpty(name)) {
            NotificationUtil.getInstance().setMediaInfo(name, artist, artBitmap);
        }
    }

    /**
     * 设置媒体的播放状态
     *
     * @param type      {@link IVIMedia.Type}
     * @param playState 当前是否播放 {@link IVIMedia.MediaState}
     * @param position  当前进度
     * @param duration  总时间
     */
    private void setMediaState(int type, int playState, int position, int duration) {
//        mWorkHandler.post(new WorkRunnable(new MediaInfo(type,playState,position,duration)));
//        mWorkHandler.post(new Runnable() {
//            @Override
//            public void run() {
////                Logcat.d("type:" + LogNameUtil.getName(type, IVIMedia.Type.class) + ", playState:" + LogNameUtil.getName(playState, IVIMedia.MediaState.class) + ",position:" + position + ", duration:" + duration);
        mMediaManager.setMediaState(type,
                playState,
                position / 1000,
                duration / 1000);
        if (isMusicMediaActive()) {
            NotificationUtil.getInstance().setMediaState(playState);
        }
//            }
//        });

    }

    /**
     * 返回当前媒体类型
     *
     * @return {@link IVIMedia.Type}
     */
    public int getMediaType() {
        return mMediaType;
    }

    /**
     * 系统当前媒体打开状态
     *
     * @param mediaType
     * @return
     */
    public boolean isMediaOpened(int mediaType) {
        return mMediaManager == null ? false : mMediaManager.isOpened(mediaType);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventA2DPPermissionChange(EventA2DPPermissionChange event) {
        if (event != null) {
            Logcat.d("mIsShowA2DP:" + event.mIsShowA2DP + ", mMediaType:" + LogNameUtil.getName(mMediaType, IVIMedia.Type.class));
            if (mMediaType == IVIMedia.Type.A2DP && !event.mIsShowA2DP) {
                close(mMediaType);
            }
        }
    }

    /**
     * 注册一个EventBus监听类
     *
     * @param object
     */
    public static void registerEventBus(Object object) {
        if (!EventBus.getDefault().isRegistered(object)) {
            EventBus.getDefault().register(object);
        }
    }

    /**
     * 注销一个EventBus的监听类
     *
     * @param object
     */
    public static void unregisterEventBus(Object object) {
        if (EventBus.getDefault().isRegistered(object)) {
            EventBus.getDefault().unregister(object);
        }
    }

    /**
     * 通过 eventBus post一个消息
     *
     * @param object
     */
    public static void post(Object object) {
        EventBus.getDefault().post(object);
    }

    /**
     * 是否隐藏蓝牙音乐界面的通知
     */
    public static class EventA2DPPermissionChange {
        public boolean mIsShowA2DP;

        private EventA2DPPermissionChange(boolean isShowA2DP) {
            mIsShowA2DP = isShowA2DP;
        }

        /**
         * 发送event消息
         */
        public static void sendEvent(boolean isShowA2DP) {
            post(new EventA2DPPermissionChange(isShowA2DP));
        }

        /**
         * 将类打印成String
         *
         * @return
         */
        @Override
        public String toString() {
            return LogNameUtil.toString(this);
        }
    }

    /**
     * 状态栏通知信息改变的EventBus
     */
    public static class EventNotificationStateChange {

        /**
         * 通知栏需要显示的歌曲名称
         */
        public String mTrack;
        /**
         * 通知栏需要显示的歌手名称
         */
        public String mArtist;
        /**
         * 通知栏需要显示的专辑图片
         */
        public Bitmap mBitmap;
        /**
         * 播放状态
         */
        public int mPlayState;

        private EventNotificationStateChange(String track, String artist, Bitmap bitmap, int playState) {
            mTrack = track;
            mArtist = artist;
            mBitmap = bitmap;
            mPlayState = playState;
        }

        /**
         * 发送event消息
         */
        public static void sendEvent(String track, String artist, Bitmap bitmap, int playState) {
            post(new EventNotificationStateChange(track, artist, bitmap, playState));
        }
    }
}
