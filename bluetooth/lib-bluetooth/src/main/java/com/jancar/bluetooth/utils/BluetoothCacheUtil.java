package com.jancar.bluetooth.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.jancar.bluetooth.BtApplication;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.system.IVIConfig;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.sdk.utils.Logcat;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙数据缓存工具类
 * 简单数据，通过 SharedPreferences 缓存，复杂数据，通过数据库缓存，类似通话记录等
 */

public class BluetoothCacheUtil {

    private static BluetoothCacheUtil pThis = null;

    private static final String CONNECT_DEVICE_FILE_NAME = "connect_device"; // 已经连接的蓝牙设备
    private static final String PAIRED_DEVICE_FILE_NAME  = "paired_device";  // 蓝牙配对设备缓存文件
    private static final String PAIRED_DEVICE_COUNT      = "count";          // 缓存设备数量
    private static final String PAIRED_DEVICE_NAME       = "name";           // 设备名字
    private static final String PAIRED_DEVICE_ADDR       = "addr";           // 设备地址
    private static final String PAIRED_PREV_DEVICE_ADDR  = "prev_addr";      // 上一个配对设备地址
    private static final String BLUZ_DEVICE_NAME         = "bluz_name";      // 蓝牙模组的设备名
    private static final String BLUZ_DEVICE_PIN          = "bluz_pin";       // 蓝牙模组的pin码
    private static final String BLUZ_AUTO_LISTEN         = "auto_listen";    // 自动应答
    private static final String BLUZ_AUTO_LINK           = "auto_link";      // 自动连接
    private static final String NEW_DEVICE_COUNT         = "new_count";      // 新设备数量
    private static final String NEW_DEVICE_NAME          = "new_name";       // 新设备名字
    private static final String NEW_DEVICE_ADDR          = "new_addr";       // 新设备地址
    private static final String PHONE_NUMBER_NAME        = "phone_number_name";  // 通话号码及姓名
    private static final String PHONE_NUMBER             = "phone_number";    // 通话号码
    private static final String PHONE_NAME               = "phone_name";       // 通话名称

    private int mBluzConnectedStatus = IVIBluetooth.BluetoothConnectStatus.DISCONNECTED; // 当前蓝牙模块的连接状态，不缓存到文件里面，断电重启就是断开状态
    private String mBluzConnectedAddr = "";
    private String mBluzConnectedName = ""; // 蓝牙模块连接的设备名字，地址
    //开机自动连接的设备都是上一次记忆的设备，应默认为原来设备；
    private boolean mIsNewConnectedDevice = false; // 是否新连接设备

    // addr、status，包括bound状态
    private String mLastConnectAddr = "";
    private int mLastConnectStatus = IVIBluetooth.BluetoothConnectStatus.DISCONNECTED;

    public static BluetoothCacheUtil getInstance() {
        if (null == pThis) {
            pThis = new BluetoothCacheUtil();
        }
        return pThis;
    }

    private BluetoothCacheUtil() {
    }

    /**
     * 缓存了蓝牙配对列表
     * @param BluetoothDevices 蓝牙设备列表
     */
    public void setPairedDeviceList(List<BluetoothDevice> BluetoothDevices) {
        saveDeviceList(BluetoothDevices, PAIRED_DEVICE_NAME, PAIRED_DEVICE_ADDR, PAIRED_DEVICE_COUNT);
    }

    /**
     * 获取已配对列表
     * @return
     */
    public List<BluetoothDevice> getPairedDeviceList() {
        return getDeviceList(PAIRED_DEVICE_NAME, PAIRED_DEVICE_ADDR, PAIRED_DEVICE_COUNT);
    }

    /**
     * 设置搜索新设备列表
     * @param BluetoothDevices
     */
    public void setSearchNewDeviceList(List<BluetoothDevice> BluetoothDevices) {
        saveDeviceList(BluetoothDevices, NEW_DEVICE_NAME, NEW_DEVICE_ADDR, NEW_DEVICE_COUNT);
    }

    /**
     * 获取新设备列表
     * @return
     */
    public List<BluetoothDevice> getSearchNewDeviceList() {
        return getDeviceList(NEW_DEVICE_NAME, NEW_DEVICE_ADDR, NEW_DEVICE_COUNT);
    }

    /**
     * 清空搜索列表
     */
    public void clearSearchNewDeviceList() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(PAIRED_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        settings.edit().putInt(NEW_DEVICE_COUNT, 0).commit();
    }

    /**
     * 缓存设备列表
     * @param BluetoothDevices
     * @param keyName 存到文件中的键值名字
     * @param keyAddr 存到文件中的键值地址
     * @param keyCount 存到文件中的键值数量
     */
    private void saveDeviceList(List<BluetoothDevice> BluetoothDevices, String keyName, String keyAddr, String keyCount) {
        if (null != BluetoothDevices) {
            SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(PAIRED_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(keyCount, BluetoothDevices.size());

            for (int i = 0; i < BluetoothDevices.size(); ++i) {
                editor.putString(keyName + i, BluetoothDevices.get(i).name);
                editor.putString(keyAddr + i, BluetoothDevices.get(i).addr);
            }

            editor.commit();
        }
    }

    /**
     * 从缓存中获取设备的列表
     * @param keyName
     * @param keyAddr
     * @param keyCount
     * @return
     */
    private List<BluetoothDevice> getDeviceList(String keyName, String keyAddr, String keyCount) {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(PAIRED_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        int nLen = settings.getInt(keyCount, 0);
        List<BluetoothDevice> BluetoothDevices = new ArrayList<>();

        for (int i = 0; i < nLen; ++i) {
            String name = settings.getString(keyName + i, "");
            String addr = settings.getString(keyAddr + i, "");
            BluetoothDevices.add(BluetoothDevice.createDevice(addr, name));
        }

        return BluetoothDevices;
    }

    /**
     * 从缓存中删除已配对设备
     * @param device {@link BluetoothDevice}
     */
    public static void removePairedDevice(BluetoothDevice device) {
        removeDeviceFromList(device, PAIRED_DEVICE_NAME, PAIRED_DEVICE_ADDR, PAIRED_DEVICE_COUNT);
    }

    /**
     * 从缓存中删除搜索到的设备
     * @param device {@link BluetoothDevice}
     */
    public static void removeSearchNewDevice(BluetoothDevice device) {
        removeDeviceFromList(device, NEW_DEVICE_NAME, NEW_DEVICE_ADDR, NEW_DEVICE_COUNT);
    }

    /**
     * 从配对记录SharedPreference中删除缓存项
     * @param device
     * @param keyName  {@link #PAIRED_DEVICE_NAME} or {@link #NEW_DEVICE_NAME}
     * @param keyAddr  {@link #PAIRED_DEVICE_ADDR} or {@link #NEW_DEVICE_ADDR}
     * @param keyCount {@link #PAIRED_DEVICE_COUNT} or {@link #NEW_DEVICE_COUNT}
     */
    private static void removeDeviceFromList(BluetoothDevice device, String keyName, String keyAddr, String keyCount){
        if (device == null || TextUtils.isEmpty(keyAddr) || TextUtils.isEmpty(keyName)
                || TextUtils.isEmpty(keyCount)) {
            Logcat.w("device = " + device + " , keyName = " + keyName + " , keyAddr = "
                    + keyAddr + ", keyCount = " + keyCount);
            return;
        }
        String address = device.addr;
        SharedPreferences devicesPreference = BtApplication.getInstance().getSharedPreferences(PAIRED_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        int nLen = devicesPreference.getInt(keyCount, 0);
        for (int i = 0; i < nLen; ++i) {
            String addrKey = keyAddr + i;
            String addrValue = devicesPreference.getString(addrKey, "");
            if (TextUtils.equals(addrValue, address)) {
                SharedPreferences.Editor editor = devicesPreference.edit();
                editor.remove(keyName + i).remove(addrKey).putInt(keyCount, --nLen);
                editor.apply();
                break;
            }
        }
    }

    /**
     * 缓存当前已经连接的设备，状态、名字、地址，这3个是一起的
     * @param status 状态
     * @param name 名字
     * @param addr 地址
     */
    public void setCurConnectDevice(int status, String name, String addr) {
        mLastConnectStatus = status;
        mLastConnectAddr = addr;
        if (isConnected() &&
            (IVIBluetooth.BluetoothConnectStatus.BOND_BONDING == status
                    || IVIBluetooth.BluetoothConnectStatus.BOND_BONDED == status
                    || IVIBluetooth.BluetoothConnectStatus.BOND_BONDNONE == status
            )) {
            Logcat.w("has connected, not handle bond state: " + status);
            return;
        }
        if (null != name && null != addr) {
            // 持久化缓存设备连接信息
            SharedPreferences devicePrefs = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            devicePrefs.edit().putString(PAIRED_DEVICE_NAME, name).putString(PAIRED_DEVICE_ADDR, addr).commit();
            // 计算是否新连接设备
//            if (!TextUtils.isEmpty(addr)) {
//                String prevConnectedDevice = devicePrefs.getString(PAIRED_PREV_DEVICE_ADDR, "");
//                mIsNewConnectedDevice = TextUtils.equals(addr, prevConnectedDevice) ? false : true;
//                Logcat.w("addr:" + addr + " prevConnectedDevice:" + prevConnectedDevice + " >>>>> mIsNewConnectedDevice " + mIsNewConnectedDevice);
//            }
        }
        mBluzConnectedStatus = status;
        mBluzConnectedAddr = addr;
        mBluzConnectedName = name;
        Logcat.d("(name: " + name + " addr:" + addr + " status: " + status + " ) isNewConntectDevice:" + mIsNewConnectedDevice);
    }

    /**
     * 是否新设备被连接（相较于上一次连接的设备，本次连接的设备地址不同与上一次）
     * @return
     */
    public boolean isNewConntectedDevice() {
        return mIsNewConnectedDevice;
    }

    /**
     * 重置新设备值
     */
    public void resetNewDeviceValue() {
        mIsNewConnectedDevice = false;
    }

    /**
     *  新设备更新前一设备地址信息为当前地址
     */
    public void updatePrevDeviceAddr() {
        if (mIsNewConnectedDevice && !TextUtils.isEmpty(mBluzConnectedAddr)) {
            Logcat.d("update prev device addr: " + mBluzConnectedAddr);
            SharedPreferences devicePrefs = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            devicePrefs.edit().putString(PAIRED_PREV_DEVICE_ADDR, mBluzConnectedAddr).commit();
        }
    }

    /**
     * 获取已连接的设备
     * @return
     */
    public BluetoothDevice getCurConnectedDevice() {
        if (isConnected() && !TextUtils.isEmpty(mBluzConnectedAddr)) {
            return BluetoothDevice.createDevice(mBluzConnectedAddr, mBluzConnectedName);
        } else {
            SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            return BluetoothDevice.createDevice("", "");
        }
    }
    /**
     * 获取正在连接的设备
     * @return
     */
    public BluetoothDevice getCurConnectDevice() {
        if (TextUtils.isEmpty(mBluzConnectedAddr)) {
            SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            return BluetoothDevice.createDevice(settings.getString(PAIRED_DEVICE_ADDR, ""),
                    settings.getString(PAIRED_DEVICE_NAME, ""));
        } else {
            return BluetoothDevice.createDevice(mBluzConnectedAddr, mBluzConnectedName);
        }
    }

    /**
     * 设置蓝牙模组的设备名
     * @param name
     */
    public void setBluzDeviceName(String name) {
        if (!TextUtils.isEmpty(name)) {
            SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            settings.edit().putString(BLUZ_DEVICE_NAME, name).commit();
        }
    }

    /**
     * 设置蓝牙模组的pin
     * @param pin
     */
    public void setBluzDevicePin(String pin) {
        if (!TextUtils.isEmpty(pin)) {
            SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
            settings.edit().putString(BLUZ_DEVICE_PIN, pin).commit();
        }
    }

    /**
     * 获取蓝牙模组的设备名
     * @return
     */
    public String getBluzDeviceName() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        return settings.getString(BLUZ_DEVICE_NAME, "");
    }

    /**
     * 获取蓝牙模组的pin码
     * @return
     */
    public String getBluzDevicePin() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        return settings.getString(BLUZ_DEVICE_PIN, "");
    }

    /**
     * 获取蓝牙自动连接
     * @param isAutoListen
     */
    public void setBluzAutoListen(boolean isAutoListen) {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        settings.edit().putBoolean(BLUZ_AUTO_LISTEN, isAutoListen).commit();
    }

    /**
     * 获取蓝牙模组的pin码
     * @return
     */
    public boolean getBluzAutoListen() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(BLUZ_AUTO_LISTEN, IVIConfig.getBluetoothDefAutoListen());
    }

    /**
     * 获取蓝牙自动连接
     * @param isAutoLink
     */
    public void setBluzAutoLink(boolean isAutoLink) {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        settings.edit().putBoolean(BLUZ_AUTO_LINK, isAutoLink).commit();
    }

    /**
     * 获取蓝牙模组的pin码
     * @return
     */
    public boolean getBluzAutoLink() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(CONNECT_DEVICE_FILE_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(BLUZ_AUTO_LINK, IVIConfig.getBluetoothDefAutoLink());
    }

    /**
     * 设置蓝牙连接状态
     * @param status
     */
    /*public synchronized void setBluzConnectedStatus(int status) {
        Logcat.d("status:" + status + " oldStatus:" + mBluzConnectedStatus);
        mBluzConnectedStatus = status;
    }*/

    public synchronized int getBluzConnectedStatus() {
        Logcat.d("mBluzConnectedStatus:" + mBluzConnectedStatus);
        return mBluzConnectedStatus;
    }

    public synchronized String getBluzConnectedAddr() {
        Logcat.d("mBluzConnectedAddr:" + mBluzConnectedAddr);
        return mBluzConnectedAddr;
    }

    public synchronized int getLastConnectStatus() {
        Logcat.d("mLastConnectStatus:" + mLastConnectStatus);
        return mLastConnectStatus;
    }

    public synchronized String getLastConnectAddr() {
        Logcat.d("mLastConnectAddr:" + mLastConnectAddr);
        return mLastConnectAddr;
    }

    public boolean isConnected() {
        Logcat.d("mBluzConnectedStatus:" + mBluzConnectedStatus);
        return IVIBluetooth.BluetoothConnectStatus.CONNECTED == mBluzConnectedStatus;
    }

	/**
     * 存储通话号码及姓名
     * @param number  通话号码
     * @param name    通话姓名
     */
    public void savePhoneNumberAndName(String number,String name) {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(PHONE_NUMBER_NAME, Context.MODE_PRIVATE);
        settings.edit().putString(PHONE_NUMBER, number).commit();
        settings.edit().putString(PHONE_NAME, name).commit();
    }

    /**
     * 获取存储的通话号码
     * @return
     */
    public String getPhoneNumber() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(PHONE_NUMBER_NAME, Context.MODE_PRIVATE);
        return settings.getString(PHONE_NUMBER, "");
    }

	/**
     * 获取存储的联系人名称
     * @return
     */
    public String getPhoneName() {
        SharedPreferences settings = BtApplication.getInstance().getSharedPreferences(PHONE_NUMBER_NAME, Context.MODE_PRIVATE);
        return settings.getString(PHONE_NAME, "");
    }
}
