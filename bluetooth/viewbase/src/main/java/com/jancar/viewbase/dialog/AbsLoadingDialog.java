package com.jancar.viewbase.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AbsLoadingDialog extends Dialog {

    protected TextView mContentTextView = null; // 显示提示内容
    protected View mLoadingBg = null; // 背景
    protected ProgressBar mProgressBar = null; // 进度条
    private Drawable mLoadingBgDrawable = null;

    public AbsLoadingDialog(Context context) {
        super(context);
    }

    public AbsLoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AbsLoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mContentTextView = getContentTextView();
        mLoadingBg = getLoadingBg();
        mProgressBar = getProgressBar();
        if (mLoadingBg != null) {
            mLoadingBgDrawable = mLoadingBg.getBackground();
        }
    }

    /**
     * 获得进度条控件
     * @return
     */
    protected abstract ProgressBar getProgressBar();

    /**
     * 获得整个视图根布局的控件
     * @return
     */
    protected abstract View getLoadingBg();

    /**
     * 获得内容显示控件
     * @return
     */
    protected abstract TextView getContentTextView();

    /**
     * 获得Dialog整个视图的layoutId
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 设置背景隐藏
     * @param isVisibility
     */
    public void setBgVisibility(boolean isVisibility) {
        if (!isVisibility) {
            mLoadingBg.setBackgroundColor(Color.TRANSPARENT);
        } else {
            mLoadingBg.setBackground(mLoadingBgDrawable);
        }
    }

    /**
     * 显示对话框
     * @param message 消息
     * @param isRobFocus 是否抢焦点
     */
    public void showDialog(CharSequence message, boolean isRobFocus){
        show();
        setCanceledOnTouchOutside(isRobFocus);
        mContentTextView.setText(message);
    }

    /**
     * 显示加载中
     * @param isRobFocus
     */
    public void showDialog(boolean isRobFocus) {
        setCanceledOnTouchOutside(isRobFocus);
        show();
    }

    /**
     * 隐藏
     */
    public void hideDialog() {
        dismiss();
    }
}
