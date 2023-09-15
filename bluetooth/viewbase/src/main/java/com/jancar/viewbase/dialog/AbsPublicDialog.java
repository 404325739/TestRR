package com.jancar.viewbase.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import static com.jancar.viewbase.dialog.AbsPublicDialog.DialogStyle.DL_STYLE_INPUT;
import static com.jancar.viewbase.dialog.AbsPublicDialog.DialogStyle.DL_STYLE_PROGRESS;

public abstract class AbsPublicDialog extends Dialog implements DialogInterface.OnDismissListener{

    // 对话框UI分三部分区域，标题栏和按钮栏可以选择性显示
    protected View mContentArea = null; //内容区域
    protected View mTitleArea = null;   //标题栏区域
    protected View mButtonArea = null;  //按键区域
    private View mContentView = null;

    protected EditText mEditText = null;		// 输入框
    protected ProgressBar mProgressBar = null;	// 进度条

    protected TextView mTitleTextView = null;		// 标题内容
    protected TextView mContentTextView = null; // 显示提示内容
    protected Button mSureButton = null; // 确定按钮
    protected Button mCancelButton = null; // 取消按钮

    protected Context mContext;
    protected DialogButton mDefaultBtn = DialogButton.DL_BTN_NONE;  // 默认按钮(将高亮显示)
    protected DialogCallBack mCallBack = null; // 回调接口
    protected DialogStyle mDialogStyle = null;			// 对话框类型
    protected InputMethodManager mInputMethodManager;

    protected OnKeyListener onKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (KeyEvent.KEYCODE_BACK == keyCode){
                // Back键隐藏键盘
                if (mDialogStyle == DL_STYLE_INPUT) {
                    if (null != mInputMethodManager && mInputMethodManager.isActive()) {
                        if (mEditText != null) {
                            mInputMethodManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
                        }
                    }
                    dismiss();
                    return true;
                }
            }
            return false;
        }
    };

    protected android.view.View.OnClickListener mOnBtnClickListener = new android.view.View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (mSureButton!= null && mSureButton.getId() == id) { // 用户点击确定
                if (mCallBack != null) {
                    String text = null;
                    if (mEditText != null) {
                        text = mEditText.getText().toString().trim();
                    }
                    mCallBack.onClickSure(text);
                    mCallBack = null;
                }
                cancel();
            } else if (mCancelButton != null && mCancelButton.getId() == id) {// 用户点击取消
                if (mCallBack != null) {
                    mCallBack.onClickCancel();
                    mCallBack = null;
                }
                cancel();
            } else if (mTitleArea != null && mTitleArea.getId() == id
                    || mContentArea != null && mContentArea.getId() == id) {
                if (null != mInputMethodManager && mInputMethodManager.isActive()) {
                    mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
    };

    public AbsPublicDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        findViews();// 获取所有控件
        setOnDismissListener(this);
        setOnKeyListener(onKeyListener);
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void findViews() {
        initContentView();
        mEditText = getEditText(mContentView);
        mContentArea = getContentArea(mContentView);
        mProgressBar = getProgressBar(mContentView);
        mTitleArea = getTitleArea();
        mButtonArea = getButtonArea();
        mTitleTextView = getTitleTextView();
        mContentTextView = getContentTextView();
        mSureButton = getSureButton();
        mCancelButton = getCancelButton();
        if (mSureButton != null) {
            mSureButton.setOnClickListener(mOnBtnClickListener);
        }
        if (mCancelButton != null) {
            mCancelButton.setOnClickListener(mOnBtnClickListener);
        }
        if (mTitleArea != null) {
            mTitleArea.setOnClickListener(mOnBtnClickListener);
        }
        if (mContentArea != null) {
            mContentArea.setOnClickListener(mOnBtnClickListener);
        }
    }

    private void initContentView() {
        // 加载不同对话框内容
        LayoutInflater inflate = LayoutInflater.from(mContext.getApplicationContext());
        View contentView;
        switch (mDialogStyle) {
            case DL_STYLE_INPUT:
                contentView = inflate.inflate(getContentInputLayoutId(), null);
                break;
            case DL_STYLE_PROGRESS:
                contentView = inflate.inflate(getContentProgressLayoutId(), null);
                break;
            case DL_STYLE_NORMAL:
            default:
                contentView = inflate.inflate(getContentNormalLayoutId(), null);
                break;
        }
        mContentView = contentView;
    }

    /**
     * 弹框中间显示普通内容的layoutId
     * @return
     */
    protected abstract int getContentNormalLayoutId();

    /**
     * 弹框中间显示进度条内容的layoutId
     * @return
     */
    protected abstract int getContentProgressLayoutId();

    /**
     * 弹框中间显示用户输入内容的layoutId
     * @return
     */
    protected abstract int getContentInputLayoutId();

    /**
     * 获取取消按钮控件
     * @return
     */
    protected abstract Button getCancelButton();

    /**
     * 获取确认按钮控件
     * @return
     */
    protected abstract Button getSureButton();

    /**
     * 获取内容显示控件
     * @return
     */
    protected abstract TextView getContentTextView();

    /**
     * 获取标题显示控件
     * @return
     */
    protected abstract TextView getTitleTextView();

    /**
     * 获取进度条控件
     * @param contentView
     * @return
     */
    protected abstract ProgressBar getProgressBar(View contentView);

    /**
     * 获取输入框控件
     * @param contentView
     * @return
     */
    protected abstract EditText getEditText(View contentView);

    /**
     * 获取按键区域View
     * @return
     */
    protected abstract View getButtonArea();

    /**
     * 获得标题区域View
     * @return
     */
    protected abstract View getTitleArea();

    /**
     * 获得内容区域View
     * @param contentView
     * @return
     */
    protected abstract View getContentArea(View contentView);

    /**
     * 获得Dialog整个视图的layoutId
     * @return
     */
    protected abstract int getLayoutId();

    public interface DialogCallBack {

        /**
         * 用户点击确定按钮的回调函数
         * @param text 回调的信息，可能为null；输入框Style时为输入的text
         */
        void onClickSure(String text);

        /**
         * 用户点击取消按钮的回调函数
         */
        void onClickCancel();
    }

    // 对话框类型
    public enum DialogStyle {
        DL_STYLE_NORMAL,
        DL_STYLE_INPUT,
        DL_STYLE_PROGRESS
    }

    // 对话框按钮
    public enum DialogButton {
        DL_BTN_NONE,
        DL_BTN_OK,
        DL_BTN_CANCEL
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        // 对话框消失，回调如果不为空，说明非点击确定或者取消消失，通知应用取消
        if (mCallBack != null) {
            mCallBack.onClickCancel();
        }
    }

    public void setCallBack(DialogCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public void setDialogStyle(DialogStyle mDialogStyle) {
        this.mDialogStyle = mDialogStyle;
        initContentView();
    }

    /**
     * 弹出对话框
     * @param  strTitle
     * 			  显示的标题，为空则不显示
     * @param strMessage
     *            显示的消息体
     * @param nButton
     *            按钮数量，如果为1，则只显示确定，0 则都不显示，否则显示确定和取消
     */
    public void popDialog(String strTitle, String strMessage, int nButton){
        show();
        if (TextUtils.isEmpty(strTitle)) {
            mTitleArea.setVisibility(View.GONE);
        } else {
            mTitleArea.setVisibility(View.VISIBLE);
            mTitleTextView.setText(strTitle);
        }
        if (mContentTextView != null) {
            if (strMessage != null) {
                mContentTextView.setText(strMessage);
                mContentTextView.setVisibility(View.VISIBLE);
            } else {
                mContentTextView.setVisibility(View.GONE);
            }
        }
        if (nButton == 1) {
            mCancelButton.setVisibility(View.GONE);
        } else if (nButton == 0) {
            mButtonArea.setVisibility(View.GONE);
        }
    }

    public void popDialog(String strMessage, int nButton) {
        popDialog(null, strMessage, nButton);
    }

    /**
     * 更新进度对话框值
     * @param max	ProgressBar最大值
     * @param progress		当前进度
     * @param contentText	  内容信息（为空则不更新，主要适用于删除进度中显示文件，获显示当前文件进度如"2/50"）
     */
    public void updateProgress(int max, int progress, String contentText) {
        if (mDialogStyle == DL_STYLE_PROGRESS
                && mProgressBar != null) {
            mProgressBar.setMax(max);
            mProgressBar.setProgress(progress);
        }
        if (mContentTextView != null) {
            if (contentText != null) {
                mContentTextView.setText(contentText);
                mContentTextView.setVisibility(View.VISIBLE);
            } else {
                mContentTextView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置每个按钮显示什么
     * @param buttonTexts
     */
    public void setButtonText(String[] buttonTexts) {
        if (null == buttonTexts) {
            return;
        }
        if (buttonTexts.length > 0) {
            mSureButton.setText(buttonTexts[0]);
            if (buttonTexts.length > 1) {
                mCancelButton.setText(buttonTexts[1]);
            }
        }
    }

    /**
     * 设置哪个按钮为默认按钮
     * @param btn {@link DialogButton#DL_BTN_OK} / {@link DialogButton#DL_BTN_CANCEL}
     * 默认{@link DialogButton#DL_BTN_NONE}没有高亮按钮
     */
    public void setDefaultBtn(DialogButton btn) {
        mDefaultBtn = btn;
    }

    /**
     * 设置输入对话框的文字
     */
    public void setInputDialogMsg(String message) {
        if (mEditText != null) {
            mEditText.setText(message);
        }
    }

    /**
     * 设置输入对话框不能换行
     */
    public void setInputNoEnter() {
        if (mEditText != null) {
            mEditText.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }

    /**
     * 限制输入长度
     * @param maxLen
     */
    public void setMaxInputLen(int maxLen) {
        if (mEditText != null) {
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        }
    }
}
