package com.jancar.bluetooth.utils;

import com.jancar.bluetooth.core.BtConfig;
import com.jancar.bluetooth.core.FlavorsConfig;

public final class FlavorsConfigUtil {

    /**
     * 根据配置获取静音图标资源id
     *
     * @return
     */
    public static int getBtMuteDrawableId() {
        return BtConfig.FEATURE_IS_MUTE_MIC
                ? FlavorsConfig.getDefault().getDrawableId(FlavorsConfig.R_DRAWABLE_BT_MUTE_MIC_SELECTOR)
                : FlavorsConfig.getDefault().getDrawableId(FlavorsConfig.R_DRAWABLE_BT_MUTE_SELECTOR);
    }

    /**
     * 根据配置获取半屏静音图标资源id
     *
     * @return
     */
    public static int getBtMuteHalfDrawableId() {
        return BtConfig.FEATURE_IS_MUTE_MIC
                ? FlavorsConfig.getDefault().getDrawableId(FlavorsConfig.R_DRAWABLE_BT_MUTE_MIC_HALF_SELECTOR)
                : FlavorsConfig.getDefault().getDrawableId(FlavorsConfig.R_DRAWABLE_BT_MUTE_HALF_SELECTOR);
    }

}
