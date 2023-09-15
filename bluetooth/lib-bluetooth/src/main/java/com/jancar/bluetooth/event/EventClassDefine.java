package com.jancar.bluetooth.event;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.btservice.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Event bus 实体类定义
 */

public class EventClassDefine {
    public static class EventQuitApp {
        public int source;

        public EventQuitApp(int source) {
            this.source = source;
        }
    }

    // 修改蓝牙名字
    public static class EventModifyBluzName {
        public String name;

        public EventModifyBluzName(String name) {
            this.name = name;
        }
    }

    // 修改蓝牙pin码
    public static class EventModifyBluzPin {
        public String pin;

        public EventModifyBluzPin(String pin) {
            this.pin = pin;
        }
    }

    // event 蓝牙设备列表
    public static class EventBluetoothDevices {
        public List<BluetoothDevice> BluetoothDevices;
        public BluetoothDevice curBluetoothDevices;

        public EventBluetoothDevices(List<BluetoothDevice> BluetoothDevices, BluetoothDevice curBluetoothDevice) {
            this.BluetoothDevices = BluetoothDevices;
            this.curBluetoothDevices = curBluetoothDevice;
        }
    }

    // 断开连接状态
    public static class EventUnLinkBluetoothDevices {
        public boolean mUnLinkSuccess;

        public EventUnLinkBluetoothDevices(boolean unlinkSuccess) {
            this.mUnLinkSuccess = unlinkSuccess;
        }
    }

    // 蓝牙联系人发生了改变
    public static class EventBtContactChange {
        public List<StPhoneBook> stPhoneBooks;

        public EventBtContactChange(List<StPhoneBook> stPhoneBooks) {
            this.stPhoneBooks = stPhoneBooks;
        }
    }

    // 蓝牙开关
    public static class EventBtStageChange {
        public boolean value;

        public EventBtStageChange(boolean value) {
            this.value = value;
        }
    }

    // 回调错误值
    public static class EventCallBackFail {
        public int errorCode;

        public EventCallBackFail(int errorCode) {
            this.errorCode = errorCode;
        }
    }
}
