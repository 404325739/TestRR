package com.jancar.bluetooth.utils;

/**
 * @author Tzq
 * @date 2020/1/3 14:34
 * 常量定义类
 */
public class Constants {
    // 可拨打的最短号码长度
    public static final int ALL_SHORT_LEN = 3;
    public static final String NEW_APP = "establish";
    public static final String ACTION_BLUETOOTH_SETTINGS = "android.settings.BLUETOOTH_SETTINGS";

    // 刷新列表内容变化
    public static String NOTIFY_TYPE_LINK_STATUS = "linkStatus"; // 连接状态
    public static String NOTIFY_TYPE_SELECT_POS = "selectPosition"; // 选中状态

    // 列表数据变化类型
    public static int NOTIFY_ITEM_CHANGED = 0;
    public static int NOTIFY_ITEM_RANGE_INSERTED = 1;
    public static int NOTIFY_ITEM_RANGE_CHANGED = 2;
    public static int NOTIFY_ITEM_RANGE_REMOVED = 3;
    public static int NOTIFY_DATA_SET_CHANGED = 4;
}
