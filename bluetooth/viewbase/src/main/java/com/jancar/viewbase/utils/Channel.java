package com.jancar.viewbase.utils;

import android.content.Context;

import com.jancar.sdk.utils.Logcat;
import com.jancar.utils.SystemUtil;

public enum Channel {
    gray, blue, chr, black, metal, vertical, BlueGray, BlueDark, mx8_9853, none;

    public static Channel getChannel(Context context) {
        if (context == null) {
            Logcat.w("context is null");
            return gray;
        }
        String flavor = SystemUtil.getChannel(context).toLowerCase();
        Logcat.d(flavor);
        if ("chr".equals(flavor)) {
            return chr;
        } else if ("blue".equals(flavor)) {
            return blue;
        } else if ("black".equals(flavor)) {
            return black;
        } else if ("gray".equals(flavor)) {
            return gray;
        } else if ("metal".equals(flavor)) {
            return metal;
        } else if ("vertical".equals(flavor)) {
            return vertical;
        } else if ("bluegray".equals(flavor)) {
            return BlueGray;
        } else if ("bluedark".equals(flavor)) {
            return BlueDark;
        } else if ("mx8_9853".equals(flavor)) {
            return mx8_9853;
        } else {
            return none;
        }
    }

    public static boolean isChannelCHR(Context context){
        return getChannel(context).equals(chr) || getChannel(context).equals(mx8_9853);
    }

    public static boolean isChannelBlack(Context context){
        return getChannel(context).equals(black);
    }

    public static boolean isChannelGray(Context context){
        return getChannel(context).equals(gray);
    }

    public static boolean isChannelBlue(Context context){
        return getChannel(context).equals(blue);
    }

    public static boolean isChannelMetal(Context context){
        return getChannel(context).equals(metal);
    }

    public static boolean isChannelVertical(Context context){
        return getChannel(context).equals(vertical);
    }

    public static boolean isChannelBlueGray(Context context) {
        return getChannel(context).equals(BlueGray);
    }

    public static boolean isChannelBlueDark(Context context) {
        return getChannel(context).equals(BlueDark);
    }
}
