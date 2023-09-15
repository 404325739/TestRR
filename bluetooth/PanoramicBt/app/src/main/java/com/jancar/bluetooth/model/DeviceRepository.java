package com.jancar.bluetooth.model;


import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.Constants;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.btservice.bluetooth.IDeviceCallback;
import com.jancar.btservice.bluetooth.ISearchDeviceCallback;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.ListUtils;
import com.jancar.sdk.utils.Logcat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:10:01
 */
public class DeviceRepository implements DeviceModel {

    private Context mContext;
    private Callback mCallback;
    private ArrayList<BluetoothDevice> mListSearch = new ArrayList<>();
    private ArrayList<BluetoothDevice> mListPair = new ArrayList<>();

    public DeviceRepository(Callback callback) {
        this.mCallback = callback;
        mContext = mCallback.getUiContext();
    }

    @Override
    public boolean isBtConnected() {
        return BluetoothCacheUtil.getInstance().getBluzConnectedStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTED;
    }

    @Override
    public boolean isPowerOn() {
        return BluetoothModelUtil.getInstance().isPowerOn();
    }

    @Override
    public void linkStatusChanged(String addr, int status) {
        for (int i = 0; i < mListSearch.size(); i ++) {
            if (mListSearch.get(i).addr.equals(addr)) {
                mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_ITEM_CHANGED, i, 1);
                break;
            }
        }
    }

    @Override
    public void loadPairlist() {
        BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
        if (btManager == null) return;
        btManager.getPairedDevice(0, new IDeviceCallback.Stub() {
            @Override
            public void onSuccess(List<BluetoothDevice> bluetoothDevices, BluetoothDevice curBluetoothDevices) throws RemoteException {
                Logcat.d("getPairedDevice=onSuccess size: " + bluetoothDevices.size() + " name: " + curBluetoothDevices.name);
                mListPair.clear();
                mListPair.addAll(bluetoothDevices);
                BluetoothCacheUtil.getInstance().setPairedDeviceList(bluetoothDevices);
                mCallback.onPairlistResult(mListPair);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("getPairedDevice=onFailure errorCode: " + errorCode);
                mCallback.onPairlistResult(mListPair);
            }
        });
    }

    @Override
    public void loadSearchlist() {
        int size = mListSearch.size();
        mListSearch.clear();
        mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_DATA_SET_CHANGED, 0, size);
        if (!isPowerOn()) {
            Logcat.d("!isPowerOn, return; mListPair: " + mListPair.size());
            return;
        }
        BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
        if (btManager == null) return;
        btManager.searchNewDevice(0, new ISearchDeviceCallback.Stub() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevices) throws RemoteException {
                Logcat.d("searchNewDevice=onSuccess addr: " + bluetoothDevices.addr + " name: " + bluetoothDevices.name);
                addListResult(bluetoothDevices);
                mCallback.onSearchEnd();
            }

            @Override
            public void onProgress(BluetoothDevice bluetoothDevices) throws RemoteException {
                Logcat.d("searchNewDevice=onProgress addr: " + bluetoothDevices.addr + " name: " + bluetoothDevices.name);
                addListResult(bluetoothDevices);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("searchNewDevice=onFailure errorCode: " + errorCode);
                int size = mListSearch.size();
                mListSearch.clear();
                mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_DATA_SET_CHANGED, 0, size);
                mCallback.onSearchEnd();
            }

            @Override
            public void onProgressList(List<BluetoothDevice> bluetoothDevices) throws RemoteException {
                Logcat.d("searchNewDevice=onProgressList");
                addListResult(bluetoothDevices);
            }
        });
    }

    private void addListResult(BluetoothDevice bluetoothDevice) {
        int lastsize = mListSearch.size();
        if (!TextUtils.isEmpty(bluetoothDevice.addr)) {
            int size = mListSearch.size();
            mListSearch.add(bluetoothDevice);
            mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_ITEM_RANGE_INSERTED, size, 1);
            BluetoothCacheUtil.getInstance().setSearchNewDeviceList(mListSearch);
        } else if (!TextUtils.isEmpty(bluetoothDevice.name)) {
            // addr为空、name不为空，表示移除，此时name存的addr
            for (int n = 0; n < mListSearch.size(); n ++) {
                BluetoothDevice bd = mListSearch.get(n);
                if (TextUtils.equals(bd.addr, bluetoothDevice.name)) {
                    mListSearch.remove(n);
                    removeSearchData(n, 1);
                    BluetoothCacheUtil.getInstance().setSearchNewDeviceList(mListSearch);
                    break;
                }
            }
        }
        Logcat.d(lastsize + " to size: " + mListSearch.size());
    }
    private void addListResult(List<BluetoothDevice> bluetoothDevice) {
        if (!ListUtils.isEmpty(bluetoothDevice)) {
            int size = mListSearch.size();
            mListSearch.addAll(bluetoothDevice);
            mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_ITEM_RANGE_INSERTED, size, bluetoothDevice.size());
            Logcat.d("add: " + bluetoothDevice.size() + " size: " + mListSearch.size());
            BluetoothCacheUtil.getInstance().setSearchNewDeviceList(mListSearch);
        }
    }

    @Override
    public ArrayList<BluetoothDevice> getPairList() {
        return mListPair;
    }

    @Override
    public ArrayList<BluetoothDevice> getSearchList() {
        return mListSearch;
    }

    private void removeSearchData(int start, int size) {
        mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_ITEM_RANGE_REMOVED, start, size);
        // 删除后，须更新后面的item
        if (start < mListSearch.size()) {
            mCallback.onSearchlistResult(mListSearch, Constants.NOTIFY_ITEM_RANGE_CHANGED, start, mListSearch.size() - start);
        }
    }
}
