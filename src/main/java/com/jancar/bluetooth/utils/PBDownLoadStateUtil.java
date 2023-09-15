package com.jancar.bluetooth.utils;

import com.jancar.utils.SystemPropertiesUtil;

/**
 * Created by Administrator on 2020/10/22.
 * 电话下载工具类
 */

public class PBDownLoadStateUtil {
    public static final String PER_DOWNPB_STATE = "persist.sys.btpb.downstate";//电话本下载状态，1表示正在下载电话簿，2表示下载通话记录，0表示空闲,此属性在服务启动时重置一次

    public static boolean isDownLoading() {
        return getDownloadState() != 0;
    }

    public static int getDownloadState() {
        return Integer.valueOf(SystemPropertiesUtil.get(PER_DOWNPB_STATE, "0"));
    }

}
