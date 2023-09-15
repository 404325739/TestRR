package com.jancar.bluetooth.manager;

import android.app.Dialog;
import android.content.Context;

import com.jancar.bluetooth.R;

/**
 * @anthor Tzq
 * @describe DialogManager
 */
public class DialogManager {
    private static volatile DialogManager sManager = null;

    private DialogManager() {

    }

    public static DialogManager getInstance() {
        if (sManager == null) {
            synchronized (DialogManager.class) {
                sManager = new DialogManager();
            }
        }
        return sManager;
    }

    public Dialog initDialog(Context context, int layoutId, int gravity) {
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        dialog.setContentView(layoutId);
        dialog.getWindow().setGravity(gravity);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void show(Dialog view) {
        if (view != null) {
            if (!view.isShowing()) {
                view.show();
            }
        }
    }

    public void hide(Dialog view) {
        if (view != null) {
            if (view.isShowing()) {
                view.hide();
            }
        }
    }

    public void dismiss(Dialog view) {
        if (null != view) {
            if (view.isShowing()) {
                view.dismiss();
            }
        }
    }
}
