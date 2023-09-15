package com.jancar.bluetooth.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.jancar.utils.SystemUtil;

import java.lang.reflect.Method;

/**
 * 悬浮窗管理工具类.
 */

public class PhoneWindowUtil {

    private static final String ANDROID_AUTO_PACKAGE = "com.google.android.projection.sink"; // android auto的包名

    /**
     * 是否允许显示悬浮窗
     * 顶层是Android Auto不能显示悬浮窗
     *
     * @return
     */
    public static boolean isAllowShowPhoneWindow(Context context) {
        if (context != null) {
            return !TextUtils.equals(ANDROID_AUTO_PACKAGE, SystemUtil.getTopPackageName(context));
        }
        return true;
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        int dpi = 0;
        if (context != null) {
            DisplayMetrics dm = getDisplayMetrics(context);
            if (dm == null) {
                dpi = context.getResources().getDisplayMetrics().widthPixels;
            } else {
                dpi = dm.widthPixels;
            }
        }
        return dpi;
    }

    /**
     * 获取屏幕实际高度，加上状态栏的高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        int dpi = 0;
        if (context != null) {
            DisplayMetrics dm = getDisplayMetrics(context);
            if (dm == null) {
                dpi = context.getResources().getDisplayMetrics().heightPixels;
            } else {
                dpi = dm.heightPixels;
            }
        }
        return dpi;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        if (context != null) {
            WindowManager manager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            Class c;
            try {
                c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                return dm;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
