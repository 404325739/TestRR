package com.jancar.bluetooth.model;

public class SettingsRepository implements SettingsModel {
    private Callback mCallback;

    public SettingsRepository(Callback mCallback) {
        this.mCallback = mCallback;
    }

}
