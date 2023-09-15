package com.jancar.bluetooth.core;

import java.util.HashMap;

/**
 * FlavorsConfig Builder
 */

public class FlavorsConfigBuilder {
    HashMap<String, Integer> mAssetsIds;
    HashMap<String, String> mClassMap;

    FlavorsConfigBuilder() {
        // Empty
    }

    public FlavorsConfigBuilder assetsIds(final HashMap<String, Integer> assetsIds) {
        mAssetsIds = assetsIds;
        return this;
    }

    public FlavorsConfigBuilder classMap(HashMap<String, String> classMap) {
        mClassMap = classMap;
        return this;
    }

    public void installDefault() {
        FlavorsConfig flavorsConfig = build();
        FlavorsConfig.sDefaultInstance = flavorsConfig;
    }

    private FlavorsConfig build() {
        return new FlavorsConfig(this);
    }
}
