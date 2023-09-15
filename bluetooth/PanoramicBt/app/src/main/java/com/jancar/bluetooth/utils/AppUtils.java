package com.jancar.bluetooth.utils;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.BuildConfig;
import com.jancar.sdk.utils.Logcat;

public class AppUtils {

    public static void hideIputKeyboard(final Activity activity) {
        if (null != activity) {
            InputMethodManager mInputKeyBoard = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null) {
                mInputKeyBoard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        }
    }

    public static String getGradleResString(Context ctx, int id) {
        return ctx.getResources().getString(id);
    }

    public static boolean isAc8227Platform(Context ctx) {
        Logcat.d("BuildConfig.FLAVOR: " + BuildConfig.FLAVOR);
//        if (BuildConfig.FLAVOR.equals(getGradleResString(ctx, R.string.flavors))) {
        if (BuildConfig.FLAVOR.equals("ac8227l")) {
            return true;
        } else { // ac8257
            return false;
        }
    }
    public static boolean isAc8257_YQQD_DY801Platform(Context ctx) {
        Logcat.d("BuildConfig.FLAVOR: " + BuildConfig.FLAVOR);
        if (BuildConfig.FLAVOR.equals("ac8257_YQQD_DY801")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isLandscape(Context ctx){
    	if (ctx != null){
			int orientation = ctx.getResources().getConfiguration().orientation;
			if (orientation == Configuration.ORIENTATION_LANDSCAPE)
				return true;
		}
    	return false;
	}

	public static int getStatusBarHeight(Context mContext) {
		Resources resources = mContext.getResources();
		int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
		int height = resources.getDimensionPixelSize(resourceId);
		return height;
	}

    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }

        if (context instanceof Application || context instanceof Service) {
            return null;
        }

        Context c = context;
        while (c != null) {
            if (c instanceof ContextWrapper) {
                c = ((ContextWrapper) c).getBaseContext();

                if (c instanceof Activity) {
                    return (Activity) c;
                }
            } else {
                return null;
            }
        }

        return null;
    }
}
