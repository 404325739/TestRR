package com.jancar.bluetooth.model;


/**
 * @author Tzq
 * @date 2019-12-26 20:07:30
 */
public class DialerRepository implements DialerModel {

    private Callback mCallback;

    public DialerRepository(Callback callback) {
        this.mCallback = callback;
    }

}
