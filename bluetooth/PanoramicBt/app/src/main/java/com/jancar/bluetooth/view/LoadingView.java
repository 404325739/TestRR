package com.jancar.bluetooth.view;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.bluetooth.manager.DialogManager;
import com.jancar.bluetooth.utils.AnimUtils;
import com.jancar.bluetooth.R;
import com.jancar.sdk.utils.Logcat;

/**
 * @anthor Tzq
 * @describe 加载提示框
 */
public class LoadingView {
    private Dialog mLoadingView;
    private ImageView mImLoading;
    private TextView mTvLoading;
    private ObjectAnimator mAnimator;

    public LoadingView(Context mContext) {
        if (mContext != null) {
            mLoadingView = DialogManager.getInstance().initDialog(mContext, R.layout.dialog_loading, Gravity.CENTER);
            mImLoading = mLoadingView.findViewById(R.id.iv_loading);
            mTvLoading = mLoadingView.findViewById(R.id.tv_loading_text);
            mAnimator = AnimUtils.rotation(mImLoading);
        }
    }

    /**
     * 设置加载提示文本
     *
     * @param text
     */
    public void setLoadingText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mTvLoading.setText(text);
        }
    }

    /**
     * 显示提示框
     */
    public void show() {
        Logcat.d("mLoadingView.isShowing():" + mLoadingView.isShowing());
        if (mLoadingView.isShowing()) return;
        mAnimator.start();
        DialogManager.getInstance().show(mLoadingView);
    }

    /**
     * 显示提示框
     *
     * @param txtTip
     */
    public void show(String txtTip) {
        mAnimator.start();
        setLoadingText(txtTip);
        DialogManager.getInstance().show(mLoadingView);
    }

    /**
     * 隐藏提示框
     */
    public void hide() {
        mAnimator.pause();
        DialogManager.getInstance().hide(mLoadingView);
    }

    public void dismiss() {
        mAnimator.pause();
        DialogManager.getInstance().dismiss(mLoadingView);
    }

    public void release(){
        dismiss();
        mAnimator = null;
        mLoadingView = null;
        mImLoading = null;
        mTvLoading = null;
    }
    /**
     * 点击外部是否可以取消
     *
     * @param flag
     */
    public void setCancelable(boolean flag) {
        if (mLoadingView != null) {
            mLoadingView.setCancelable(flag);
        }
    }
}
