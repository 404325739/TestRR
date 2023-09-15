package com.jancar.ui.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jancar.uibase.R;
import com.jancar.viewbase.utils.AbsToast;
import com.jancar.viewbase.utils.Channel;

/**
 * 整个进程只能有一个Toast在弹
 */

public class ToastUtil extends AbsToast{

    private static ToastUtil mInstance = null;

    public static ToastUtil getInstance() {
        if (null == mInstance) {
            mInstance = new ToastUtil();
        }
        return mInstance;
    }

    private ToastUtil() {
    }

    /**
     * 弹出泡泡，统一使用的方法
     * @param text
     */
    public final void showToast(CharSequence text, Context context) {
        if (null == context) {
            return;
        }
        cancelToast();
        //mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        //mToast.show();
        // 使用自定义Toast
        if (null == mToast) {
            LayoutInflater inflate = LayoutInflater.from(context.getApplicationContext());
            View view = inflate.inflate(R.layout.toast, null);
            if (null == view) {
                return;
            }
            mToast = new Toast(context.getApplicationContext());
            mToast.setView(view);
            if (Channel.vertical == Channel.getChannel(context)){
                mToast.setGravity(Gravity.TOP, 0, 150);
            } else {
                mToast.setGravity(Gravity.CENTER, 0, -150);
            }
        }
        mToast.setDuration(Toast.LENGTH_SHORT);
        TextView tv = (TextView) mToast.getView().findViewById(R.id.toast_content);
        tv.setText(text);
        mToast.show();
    }
}
