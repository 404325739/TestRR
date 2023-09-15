package com.jancar.uibase;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * 进度条对话框
 * @author bin.xie
 * @date 2016/11/14
 */
public class RRProgressDialog extends ProgressDialog {

	public RRProgressDialog(Context context) {
		super(context);
		
		setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}
}
