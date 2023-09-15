package com.jancar.ui.utils;

import android.content.Context;
import android.text.TextUtils;

import com.jancar.uibase.R;
import com.jancar.sdk.utils.EnvironmentUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储设备显示工具类
 */

public class StorageUtils {

    private Context mContext = null;// 上下文对象

    private EnvironmentUtils mEnvironmentUtils = null;// 存储设备对象

    private Map<String, Integer> mStorageUI = new HashMap<>();// 存储设备显示集合

    /**
     * 无参私有构造
     */
    private StorageUtils() {

    }

    /**
     * 带参公有构造
     */
    public StorageUtils(Context context) {
        mContext = context;
        mEnvironmentUtils = new EnvironmentUtils(context);

        mStorageUI.put(EnvironmentUtils.INAND, R.string.inand);
        mStorageUI.put(EnvironmentUtils.SDCARD, R.string.sdcard);
        mStorageUI.put(EnvironmentUtils.SDCARD1, R.string.sdcard1);
        mStorageUI.put(EnvironmentUtils.USB, R.string.usb);
        mStorageUI.put(EnvironmentUtils.USB1, R.string.usb1);
        mStorageUI.put(EnvironmentUtils.USB2, R.string.usb2);
        mStorageUI.put(EnvironmentUtils.USB3, R.string.usb3);
        mStorageUI.put(EnvironmentUtils.USB4, R.string.usb4);
        mStorageUI.put(EnvironmentUtils.USB5, R.string.usb5);
        mStorageUI.put(EnvironmentUtils.USB6, R.string.usb6);
        mStorageUI.put(EnvironmentUtils.USB7, R.string.usb7);
    }

    /**
     * 获取存储设备显示名字
     *
     * @param name 存储设备名，见{@link EnvironmentUtils#INAND}...
     * @return 存储设备显示名字
     */
    public String getStorageUIByName(String name) {
        String ret = null;

        if (null != mContext && !TextUtils.isEmpty(name)) {
            if (null != mStorageUI) {
                for (Map.Entry<String, Integer> entry : mStorageUI.entrySet()) {
                    if (name.equals(entry.getKey())) {
                        ret = mContext.getResources().getString(entry.getValue());
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 获取存储设备显示名字
     *
     * @param path 存储设备路径
     * @return 存储设备显示名字
     */
    public String getStorageUIByPath(String path) {
        String ret = null;

        if (null != mEnvironmentUtils) {
            final String name = mEnvironmentUtils.getStorageName(path);

            ret = getStorageUIByName(name);
        }
        return ret;
    }
}
