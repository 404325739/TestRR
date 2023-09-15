package com.jancar.bluetooth.model;

public class BtMusicRepository implements BtMusicModel {

    private Callback mCallback;

    public BtMusicRepository(Callback callback) {
        this.mCallback = callback;
    }

}
