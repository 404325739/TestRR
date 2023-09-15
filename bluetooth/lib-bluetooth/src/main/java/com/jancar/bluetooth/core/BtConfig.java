package com.jancar.bluetooth.core;

import com.jancar.sdk.system.IVIConfig;

public class BtConfig {
    public static final int PHONE_NUMBER_MAX_LEN = 15; // 最大号码长度
    public static final boolean FEATURE_IS_MUTE_MIC = IVIConfig.getBluetoothMuteMic(); // 蓝牙通话界面静音按钮功能是否为静车机的麦克风输入
}