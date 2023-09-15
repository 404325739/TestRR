package com.jancar.bluetooth.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.jancar.bluetooth.MyApplication;
import com.jancar.sdk.setting.IVISetting;
import com.jancar.sdk.setting.SettingModel;
import com.jancar.sdk.utils.Logcat;

/**
 * 20210906 lyy 从lib-music2中复制来
 */
public class NotificationUtil {
    private static NotificationUtil sThis;

    /***是否允许发送通知的标记***/
    private boolean mMediaNotificationEnable = false;
    private Context mContext = MyApplication.getApplication();
    /**当前播放歌曲的歌曲名称**/
    private String mTrack;
    /**当前播放歌曲的歌手名称**/
    private String mArtist;
    /**当前播放歌曲的缩略图**/
    private Bitmap mBitmap;
    /**当前的播放状态**/
    private int mPlayState = -1;

    private NotificationUtil() {
        mMediaNotificationEnable = IVISetting.showMediaNotification(mContext);
        SettingModel.registerModelListener(mContext, IVISetting.Global.NAME, IVISetting.Global.MediaNotification, new SettingModel.ModelListener() {
            @Override
            public void onChange(String oldVal, String newVal) {
                Logcat.d("MediaNotification changed: " + oldVal + "->" + newVal);
                if (TextUtils.equals(newVal, "true")) {
                    mMediaNotificationEnable = true;
                    MediaManagerUtil.EventNotificationStateChange.sendEvent(mTrack, mArtist, mBitmap, mPlayState);
                } else if (TextUtils.equals(newVal, "false")) {
                    mMediaNotificationEnable = false;
                    MediaManagerUtil.EventNotificationStateChange.sendEvent("", "", null, -1);
                }
                Logcat.d("mMediaNotificationEnable:" + mMediaNotificationEnable);
            }
        });
    }

    public static NotificationUtil getInstance() {
        if (sThis == null) {
            sThis = new NotificationUtil();
        }
        return sThis;
    }

    public boolean getMediaNotificationEnable() {
        return mMediaNotificationEnable;
    }

    /**
     * 设置通知栏的MediaInfo
     * @param track
     * @param artist
     * @param bitmap
     */
    public void setMediaInfo(String track, String artist, Bitmap bitmap) {
        mTrack = track;
        mArtist = artist;
        mBitmap = bitmap;
        if (mMediaNotificationEnable) {
            MediaManagerUtil.EventNotificationStateChange.sendEvent(mTrack, mArtist, mBitmap, mPlayState);
        }
    }

    /**
     * 设置通知栏的播放状态
     * @param playState
     */
    public void setMediaState(int playState) {
        mPlayState = playState;
        if (mMediaNotificationEnable) {
            MediaManagerUtil.EventNotificationStateChange.sendEvent(mTrack, mArtist, mBitmap, mPlayState);
        }
    }

}
