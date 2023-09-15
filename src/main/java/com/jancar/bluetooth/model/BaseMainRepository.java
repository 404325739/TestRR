package com.jancar.bluetooth.model;


/**
 * @author Tzq
 * @date 2019-12-24 19:46:00
 */
public class BaseMainRepository implements BaseMainModel {

    private Callback mCallback;

    public BaseMainRepository(Callback callback) {
        this.mCallback = callback;
    }

}
