package com.jancar.bluetooth.bean;

import android.text.TextUtils;

import com.jancar.sdk.bluetooth.BluetoothModel;

/**
 * 通话记录
 */

public class StCallHistory implements Cloneable{
    public static final int ADDER_SQLITE = 0;
    public static final int ADDER_HANGUP = 1;
    public int status = 0; // 状态
    public String sort = ""; // 排序时间
    public String name = ""; // 名字
    public String phoneNumber = ""; // 电话号码
    public String time = ""; // 显示时间
    public int mCount;       // 显示次数
    public String addr;       // 设备地址
    public int adder = ADDER_SQLITE;//由谁添加，0表示数据源自杰发数据库查询，1表示本地挂断自己添加

    public String getDisplay() {
        return TextUtils.isEmpty(name) ? phoneNumber : name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof StCallHistory) {
            StCallHistory other = (StCallHistory) o;
            if (other.status == status &&
                    BluetoothModel.isPhoneNumberEquals(phoneNumber, other.phoneNumber) &&
                    TextUtils.equals(time, other.time)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StCallHistory clone() throws CloneNotSupportedException {
        return (StCallHistory)super.clone();
    }
}
