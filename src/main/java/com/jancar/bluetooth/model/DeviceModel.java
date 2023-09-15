package com.jancar.bluetooth.model;

import android.content.Context;

import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.ui.mvp.model.Model;

import java.util.ArrayList;


/**
 * @author Tzq
 * @date 2019-12-24 19:44:11
 */
public interface DeviceModel extends Model {

    boolean isBtConnected();

    boolean isPowerOn();

    void linkStatusChanged(String addr, int status);

    interface Callback {
        void onPairlistResult(ArrayList<BluetoothDevice> data);

        /**
         *
         * @param data
         * @param type -1：remove；0：edit；1：add；
         * @param start
         * @param count
         */
        void onSearchlistResult(ArrayList<BluetoothDevice> data, int type, int start, int count);
        void onSearchEnd();
        Context getUiContext();
    }

    void loadPairlist();

    void loadSearchlist();

    ArrayList<BluetoothDevice> getPairList();
    ArrayList<BluetoothDevice> getSearchList();
}
