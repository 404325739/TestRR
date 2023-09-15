package com.jancar.uibase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jancar.viewbase.dialog.AbsLoadingDialog;

/**
 * 转圈对话框，以后项目中统一使用该对话框进行转圈
 * @author bin.xie
 * @date 2016/11/14
 */
@SuppressLint("NewApi")
public class LoadingDialog extends AbsLoadingDialog {

	public LoadingDialog(Context context) {
		super(context, R.style.dialog);
	}

	@Override
	protected ProgressBar getProgressBar() {
		return (ProgressBar) findViewById(R.id.loading_progressBar);
	}

	@Override
	protected View getLoadingBg() {
		return findViewById(R.id.loading_dialog);
	}

	@Override
	protected TextView getContentTextView() {
		return (TextView) findViewById(R.id.loading_prompt_textview);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_loading;
	}

	/**
	 * 设置背景隐藏
	 * @param isVisibility
	 */
	public void setBgVisibility(boolean isVisibility) {
		super.setBgVisibility(isVisibility);
		if (!isVisibility) {
			mProgressBar.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.loading_progress));
			LayoutParams params = mProgressBar.getLayoutParams();
			params.width = 38;
			params.height = 38;
			mProgressBar.setLayoutParams(params);
		}
	}
}
