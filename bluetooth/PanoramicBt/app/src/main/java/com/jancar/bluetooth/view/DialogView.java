package com.jancar.bluetooth.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

/**
 * @anthor Tzq
 * @time 2020/1/1 9:47
 * @describe DialogView
 */
public class DialogView extends Dialog {
    public DialogView(Context mContext, int LayoutId, int style, int gravity) {
        super(mContext, style);
        setContentView(LayoutId);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = gravity;
        window.setAttributes(layoutParams);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }
}
