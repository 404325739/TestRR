package com.jancar.activity;

import com.jancar.ui.utils.ToastUtil;
import com.jancar.uibase.LoadingDialog;
import com.jancar.uibase.PublicDialog;
import com.jancar.uibase.R;
import com.jancar.viewbase.activity.ViewBaseActivity;
import com.jancar.viewbase.dialog.AbsLoadingDialog;
import com.jancar.viewbase.dialog.AbsPublicDialog;
import com.jancar.viewbase.utils.AbsToast;

public abstract class BaseActivity extends ViewBaseActivity {

    @Override
    protected AbsToast getToast() {
        return ToastUtil.getInstance();
    }

    @Override
    protected AbsLoadingDialog getLoadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    protected AbsPublicDialog getPublicDialog() {
        return new PublicDialog(this);
    }

    @Override
    protected void startActivityWithTransition() {
        overridePendingTransition(R.anim.push_right_out, R.anim.none);
    }

    @Override
    protected void closeActivityWithTransition() {
        overridePendingTransition(R.anim.none, R.anim.push_right_out);
    }

    @Override
    protected String getLoadingText() {
        return getString(R.string.loading);
    }
}
