package com.jancar.uibase;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jancar.viewbase.dialog.AbsPublicDialog;

import static com.jancar.viewbase.dialog.AbsPublicDialog.DialogStyle.DL_STYLE_NORMAL;

/**
 * 弹出公有提示对话框
 * @author bin.xie
 * @date 2016/11/14
 */
public class PublicDialog extends AbsPublicDialog{

	public PublicDialog(Context context) {
		this(null, context);
	}

	public PublicDialog(DialogCallBack callBack, Context context) {
		this(DL_STYLE_NORMAL, callBack, context);
	}

	public PublicDialog(DialogStyle dialogStyle, DialogCallBack callBack, Context context) {
		super(context, R.style.dialog);
		mContext = context;
		mDialogStyle = dialogStyle;
		mCallBack = callBack;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_public;
	}

	@Override
	protected int getContentInputLayoutId() {
		return R.layout.dialog_content_input;
	}

	@Override
	protected int getContentNormalLayoutId() {
		return R.layout.dialog_content_normal;
	}

	@Override
	protected int getContentProgressLayoutId() {
		return R.layout.dialog_content_progress;
	}

	@Override
	protected Button getCancelButton() {
		Button btn = (Button) findViewById(R.id.dialog_myBtnCancel);
		btn.setBackgroundResource(DialogButton.DL_BTN_CANCEL == mDefaultBtn?
				R.drawable.dialog_default_button_selector:
				R.drawable.dialog_button_selector);
		return btn;
	}

	@Override
	protected Button getSureButton() {
		Button btn = (Button) findViewById(R.id.dialog_myBtnSure);
		btn.setBackgroundResource(DialogButton.DL_BTN_OK == mDefaultBtn?
				R.drawable.dialog_default_button_selector:
				R.drawable.dialog_button_selector);
		return btn;
	}

	@Override
	protected TextView getContentTextView() {
		return (TextView) findViewById(R.id.dialog_myContentText);
	}

	@Override
	protected TextView getTitleTextView() {
		return (TextView) findViewById(R.id.dialog_myTitleText);
	}

	@Override
	protected ProgressBar getProgressBar(View contentView) {
		if (contentView == null) {
			return null;
		}
		return (ProgressBar) contentView.findViewById(R.id.dialog_myProgressBar);
	}

	@Override
	protected EditText getEditText(View contentView) {
		if (contentView == null) {
			return null;
		}
		return (EditText) contentView.findViewById(R.id.dialog_myEditText);
	}

	@Override
	protected View getButtonArea() {
		return findViewById(R.id.dialog_myButtonArea);
	}

	@Override
	protected View getTitleArea() {
		View v = findViewById(R.id.dialog_myTitleArea);
		return v;
	}

	@Override
	protected View getContentArea(View contentView) {
		RelativeLayout contentArea = (RelativeLayout) findViewById(R.id.dialog_myContentArea);
		contentArea.addView(contentView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		return contentArea;
	}
}
