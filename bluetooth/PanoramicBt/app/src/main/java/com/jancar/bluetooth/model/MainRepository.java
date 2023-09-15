package com.jancar.bluetooth.model;


/**
 * @author Tzq
 * @date 2019-12-24 19:44:11
 */
public class MainRepository implements MainModel {

    private Callback mCallback;

    public MainRepository(Callback callback) {
        this.mCallback = callback;
    }

}
