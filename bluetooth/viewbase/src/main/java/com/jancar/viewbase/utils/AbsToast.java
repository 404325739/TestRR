package com.jancar.viewbase.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

public abstract class AbsToast {

    protected Toast mToast;

    /**
     * 删除泡泡
     */
    public final void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    public final void showToast(@StringRes int resId, @NonNull Context context) {
        if (null != context) {
            showToast(context.getText(resId), context);
        }
    }

    /**
     * 弹出泡泡，统一使用的方法
     * @param text
     */
    public abstract void showToast(CharSequence text, Context context);
}
