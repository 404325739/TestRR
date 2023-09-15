package com.jancar.viewbase.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.jancar.sdk.system.IVIConfig;
import com.jancar.sdk.utils.ListUtils;
import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.dialog.AbsLoadingDialog;
import com.jancar.viewbase.dialog.AbsPublicDialog;
import com.jancar.viewbase.utils.AbsToast;

import java.lang.reflect.Field;

/**
 * 类描述：activity基类
 * 
 * 修改备注：增加接口，在子类中可以得到当前是否是已经打开activity的状态
 * 增加获取资源文件接口，getResId 通过资源文件名获取资源文件id
 */
public abstract class ViewBaseActivity extends Activity {
    private boolean mIsStartActivity = false;
    protected AbsLoadingDialog mLoadingDialog = null; // 加载对话框
    protected AbsPublicDialog mPublicDialog = null; // 对话框
    protected AbsToast mToast = null; //Toast框
    
    private boolean mIsActivityRunningState = false; // activity是否运行状态
    private boolean mIsVertical = false; // 是否是竖屏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setBarColor();
        requestFullScreenNoTitle();
        super.onCreate(savedInstanceState);
        mIsActivityRunningState = true;
        mToast = getToast();
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            setContentView(layoutId);
        }
        // 获取宽度和高度
 		try {
 			DisplayMetrics dm = new DisplayMetrics();
 		    getWindowManager().getDefaultDisplay().getMetrics(dm);
 		    if (dm.widthPixels < dm.heightPixels) {
 		    	// 宽度小于高度，竖屏
 		    	mIsVertical = true;
 		    }
 		} catch (Exception e) {
 		}
        onViewCreated(savedInstanceState);
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                onInitData();
            }
        });
    }

    protected abstract int getLayoutId();

    protected abstract void onViewCreated(Bundle savedInstanceState);

    /**
     * 获取Toast实例
     * @return
     */
    protected abstract AbsToast getToast();

    /**
     * 自定义加载弹出框
     * @return
     */
    protected abstract AbsLoadingDialog getLoadingDialog();

    /**
     * 自定义公共弹框
     * @return
     */
    protected abstract AbsPublicDialog getPublicDialog();

    /**
     * 初始化数据
     */
    protected abstract void onInitData();

    /**
     * 设置打开activity的过渡动画
     */
    protected abstract void startActivityWithTransition();

    /**
     * 设置关闭activity的过渡动画
     */
    protected abstract void closeActivityWithTransition();

    /**
     * 获取加载框的提示语
     * @return
     */
    protected abstract String getLoadingText();

    protected ViewBaseActivity getActivity() {
        return this;
    }

    @Override
    protected void onResume() {
        mIsStartActivity = false;
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(isFinishAinm());
    }

    @Override
    protected void onDestroy() {
        hideLoadingDialog();
        hideDialog();
        mIsActivityRunningState = false;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // 导航栏在右边的时候，在很多界面按两次面板上的MENU键，右侧的导航栏会遮住当前界面，
            // 这里进行拦截处理
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 默认不适用activity过渡动画效果，子类可以重载该方法返回true，来获取是否完成时需要动画
     *
     * @return
     */
    protected boolean isFinishAinm() {
        return false;
    }

    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivitySafety(intent);
    }

    public void startActivity(Class<?> cls, String key, String value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(key, value);
        startActivitySafety(intent);
    }

    public void startActivity(Class<?> cls, String key, int value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(key, value);
        startActivitySafety(intent);
    }

    public void startActivityInNewTask(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivitySafety(intent);
    }

    public void startActivityInNewTask(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivitySafety(intent);
    }

    /**
     * 打开activity，都使用这个来打开activity
     *
     * @param intent
     */
    public boolean startActivitySafety(Intent intent) {
        if (mIsStartActivity) {
            return false;
        }
        mIsStartActivity = true;
        try {
            startActivity(intent);
            startActivityWithTransition();
            return true;
        } catch (ActivityNotFoundException e) {
            mIsStartActivity = false;
        }
        return false;
    }

    public boolean startActivityForResultSafety(Intent intent, int requestCode) {
        if (mIsStartActivity) {
            return false;
        }
        mIsStartActivity = true;
        try {
            startActivityForResult(intent, requestCode);
            startActivityWithTransition();
            return true;
        } catch (ActivityNotFoundException e) {
            mIsStartActivity = false;
        }
        return false;
    }

    /**
     * see {@link #isFinishAinm()}
     *
     * @param isAnim
     */
    protected void overridePendingTransition(boolean isAnim) {
        if (isAnim) {
            closeActivityWithTransition();
        }
    }

    /**
     * 删除泡泡
     */
    protected final void cancelToast() {
        if (mToast != null) {
            mToast.cancelToast();
        } else {
            Logcat.e("Toast is not initialized, Please overide the method getToast");
        }
    }

    protected final void showToast(@StringRes int resId) {
        if (mToast != null) {
            mToast.showToast(resId, this);
        } else {
            Logcat.e("Toast is not initialized, Please overide the method getToast");
        }
    }

    /**
     * 弹出泡泡，统一使用的方法
     *
     * @param text
     */
    protected final void showToast(CharSequence text) {
        if (mToast != null) {
            mToast.showToast(text, this);
        } else {
            Logcat.e("Toast is not initialized, Please overide the method getToast");
        }
    }

    /**
     * 显示对话框
     *
     * @param resId
     * @param isRobFocus
     * @param isHideBg   是否隐藏背景
     */
    protected void showLoadingDialog(int resId, boolean isRobFocus, boolean isHideBg) {
        showLoadingDialog(resId, isRobFocus);
        if (null != mLoadingDialog) {
            mLoadingDialog.setBgVisibility(isHideBg);
        }
    }

    protected void showLoadingDialog(boolean isRobFocus, boolean isHideBg) {
        showLoadingDialog(isRobFocus);
        if (null != mLoadingDialog) {
            mLoadingDialog.setBgVisibility(isHideBg);
        }
    }

    protected void showLoadingDialog(CharSequence message, boolean isRobFocus, boolean isHideBg) {
        showLoadingDialog(message, isRobFocus);
        if (null != mLoadingDialog) {
            mLoadingDialog.setBgVisibility(isHideBg);
        }
    }

    /**
     * 使用默认的加载中的提示
     *
     * @param isRobFocus
     */
    protected void showLoadingDialog(boolean isRobFocus) {
        showLoadingDialog(getLoadingText(), isRobFocus);
    }

    /**
     * 弹出加载对话框对话框
     *
     * @param resId
     * @param isRobFocus
     */
    protected void showLoadingDialog(int resId, boolean isRobFocus) {
        showLoadingDialog(getText(resId), isRobFocus);
    }

    protected void showLoadingDialog(CharSequence message, boolean isRobFocus) {
        hideLoadingDialog(); // 先隐藏之前的
        if (!mIsActivityRunningState) { // activity不是运行状态
            return;
        }
        mLoadingDialog = getLoadingDialog();
        if (mLoadingDialog == null) {
            Logcat.e("LoadingDialog is null");
            return;
        }
        setDialogPos(mLoadingDialog);
        mLoadingDialog.showDialog(message, isRobFocus);
    }

    /**
     * 隐藏对话框
     */
    protected void hideLoadingDialog() {
        if (null != mLoadingDialog) {
            mLoadingDialog.hideDialog();
            mLoadingDialog = null;
        }
    }

    /**
     * 设置PublicDialog模态属性
     * @param flag true非模态，false模态
     * @return true成功，false失败
     */
    protected boolean setPublicDialogCancelable(boolean flag) {
        boolean ret = false;
        if (null != mPublicDialog) {
            mPublicDialog.setCancelable(flag);
            ret = true;
        }
        return ret;
    }

    /**
     * 设置LoadingDialog模态属性
     * @param flag true非模态，false模态
     * @return true成功，false失败
     */
    protected boolean setLoadingDialogCancelable(boolean flag) {
        boolean ret = false;
        if (null != mLoadingDialog) {
            mLoadingDialog.setCancelable(flag);
            ret = true;
        }
        return ret;
    }

    /**
     * 显示消息对话框(无标题，显示默认两个按钮)
     *
     * @param resId    显示内容
     * @param callback
     */
    protected void showMessageDialog(int resId, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(getText(resId), callback);
    }

    protected void showMessageDialog(CharSequence message, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(message, 2, callback);
    }

    /**
     * 显示消息对话框(显示默认两个按钮)
     *
     * @param title    标题
     * @param message  显示内容
     * @param callback 回调
     */
    protected void showMessageDialog(CharSequence title, CharSequence message, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(title, message, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    /**
     * 显示对话框(无标题，指定按钮显示字串和确定按钮个数)
     *
     * @param message     消息
     * @param buttonTexts 每个按钮显示什么，最多可以两个按钮
     * @param callback    回调
     */
    protected void showMessageDialog(String message, String[] buttonTexts, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(null, message, buttonTexts, callback);
    }

    /**
     * 显示对话框(无标题，指定按钮显示字串和确定按钮个数)
     *
     * @param message     消息
     * @param buttonTexts 每个按钮显示什么，最多可以两个按钮
     * @param callback    回调
     */
    protected void showMessageDialog(String title, String message, String[] buttonTexts, AbsPublicDialog.DialogCallBack callback) {
        int buttonCount = 1;
        if (null != buttonTexts && buttonTexts.length > 1) {
            buttonCount = 2;
        }
        showMessageDialog(title, message, buttonCount,
                buttonTexts, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    /**
     * 显示消息对话框(无标题，指定按钮数量)
     *
     * @param resId
     * @param buttonCount 按钮数量
     * @param callback
     */
    protected void showMessageDialog(int resId, int buttonCount, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(getText(resId), buttonCount, callback);
    }

    protected void showMessageDialog(CharSequence message, int buttonCount, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(null, message.toString(), buttonCount, null, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    /**
     * 显示消息对话框（高亮默认按钮）
     *
     * @param message       显示消息内容
     * @param defaultButton 默认按钮
     * @param callback
     */
    protected void showMessageDialog(CharSequence message, AbsPublicDialog.DialogButton defaultButton, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog("", message, defaultButton, callback);
    }

    protected void showMessageDialog(CharSequence title, CharSequence message, AbsPublicDialog.DialogButton defaultButton, AbsPublicDialog.DialogCallBack callback) {
        showMessageDialog(title.toString(), message.toString(), 2,
                null, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    protected void showMessageDialog(String title, String message, int buttonCount,
                                     String[] btnTexts, AbsPublicDialog.DialogButton defaultBtn, AbsPublicDialog.DialogCallBack callback) {
        showPublicDialog(AbsPublicDialog.DialogStyle.DL_STYLE_NORMAL, title, message, buttonCount, btnTexts, defaultBtn, callback);
    }

    /**
     * 显示输入框
     *
     * @param title       输入框标题
     * @param buttonTexts 每个按钮显示什么，显示两个按钮 （例如显示保存/取消）
     * @param callback    回调
     */
    protected void showInputDialog(String title, String[] buttonTexts, AbsPublicDialog.DialogCallBack callback) {
        showInputDialog(title, buttonTexts, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    /**
     * 显示对话框，两个按钮，没有高亮
     *
     * @param title    标题
     * @param callback
     */
    protected void showInputDialog(String title, AbsPublicDialog.DialogCallBack callback) {
        showInputDialog(title, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    protected void showInputDialog(String title, int buttonCount, AbsPublicDialog.DialogCallBack callback) {
        showInputDialog(title, buttonCount, null, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    protected void showInputDialog(String title, AbsPublicDialog.DialogButton defaultButton, AbsPublicDialog.DialogCallBack callback) {
        showInputDialog(title, null, defaultButton, callback);
    }

    protected void showInputDialog(String title, String[] buttonTexts, AbsPublicDialog.DialogButton defaultButton, AbsPublicDialog.DialogCallBack callback) {
        if (ListUtils.isEmpty(buttonTexts)) {
            showInputDialog(title, 2, null, defaultButton, callback);
        } else {
            showInputDialog(title, buttonTexts.length, buttonTexts, defaultButton, callback);
        }
    }

    protected void showInputDialog(String title, int buttonCount,
                                   String[] btnTexts, AbsPublicDialog.DialogButton defaultBtn, AbsPublicDialog.DialogCallBack callback) {
        showPublicDialog(AbsPublicDialog.DialogStyle.DL_STYLE_INPUT, title, null, buttonCount,
                btnTexts, defaultBtn, callback);
    }

    /**
     * 显示进度框（默认不显示按钮）
     *
     * @param title    进度框标题
     * @param callback 回调
     */
    protected void showProgressDialog(String title, AbsPublicDialog.DialogCallBack callback) {
        showProgressDialog(title, 0, callback);
    }

    protected void showProgressDialog(String title, int buttonCount, AbsPublicDialog.DialogCallBack callback) {
        showProgressDialog(title, buttonCount, null, callback);
    }

    protected void showProgressDialog(String title, String[] btnTexts, AbsPublicDialog.DialogCallBack callback) {
        if (ListUtils.isEmpty(btnTexts)) {
            showProgressDialog(title, 0, callback);
        } else {
            showProgressDialog(title, btnTexts.length, btnTexts, callback);
        }
    }

    protected void showProgressDialog(String title, int buttonCount, String[] btnTexts, AbsPublicDialog.DialogCallBack callback) {
        showProgressDialog(title, buttonCount, btnTexts, AbsPublicDialog.DialogButton.DL_BTN_NONE, callback);
    }

    protected void showProgressDialog(String title, int buttonCount,
                                      String[] btnTexts, AbsPublicDialog.DialogButton defaultBtn, AbsPublicDialog.DialogCallBack callback) {
        showPublicDialog(AbsPublicDialog.DialogStyle.DL_STYLE_PROGRESS, title, null, buttonCount,
                btnTexts, defaultBtn, callback);
    }

    protected void showPublicDialog(AbsPublicDialog.DialogStyle dialogStyle, String title, String message, int buttonCount,
                                    String[] btnTexts, AbsPublicDialog.DialogButton defaultBtn, AbsPublicDialog.DialogCallBack callback) {
        hideDialog();
        if (!mIsActivityRunningState) { // activity不是运行状态
            return;
        }
        mPublicDialog = getPublicDialog();
        if (mPublicDialog == null) {
            Logcat.e("PublicDialog is null");
            return;
        }
        mPublicDialog.setDialogStyle(dialogStyle);
        mPublicDialog.setCallBack(callback);
        mPublicDialog.setDefaultBtn(defaultBtn);
        mPublicDialog.popDialog(title, message, buttonCount);
        mPublicDialog.setCanceledOnTouchOutside(false);
        setDialogPos(mPublicDialog);
        if (null != btnTexts) {
            mPublicDialog.setButtonText(btnTexts);
        }
    }

    /**
     * 更新对话框
     *
     * @param max        进度对话框 进度最大值
     * @param progress   进度对话框 进度值
     * @param strMessage 显示的消息内容 （输入框不支持）
     *                   （进度框可传入当前进度说明，如正在删除的文件名，百分之多少之类）
     */
    protected void updateDialog(int max, int progress, String strMessage) {
        if (mPublicDialog != null) {
            mPublicDialog.updateProgress(max, progress, strMessage);
        }
    }

    /**
     * 更新对话框消息
     *
     * @param strMessage 显示的消息内容 （输入框不支持）
     */
    protected void updateDialog(String strMessage) {
        if (mPublicDialog != null) {
            mPublicDialog.updateProgress(0, 0, strMessage);
        }
    }

    /**
     * 设置对话框是否可以点击外面取消
     *
     * @param cancel
     */
    protected void setDialogCancelOnTouchOutside(boolean cancel) {
        if (mPublicDialog != null) {
            mPublicDialog.setCanceledOnTouchOutside(cancel);
        }
    }

    /**
     * 更新对话框按钮文本
     *
     * @param btnTexts 对话框按钮文本按钮数组
     */
    protected void setDialogButtonText(String[] btnTexts) {
        if (mPublicDialog != null) {
            mPublicDialog.setButtonText(btnTexts);
        }
    }

    /**
     * 隐藏对话框
     */
    protected void hideDialog() {
        if (null != mPublicDialog) {
            mPublicDialog.dismiss();
            mPublicDialog = null;
        }
    }

    /**
     * 对话框是否存在
     *
     * @return
     */
    protected boolean isDialogShow() {
        return mPublicDialog != null;
    }

    /**
     * 是否正在打开activity
     *
     * @return
     */
    protected boolean isStartActivitying() {
        return mIsStartActivity;
    }

    /**
     * 设置对话框位置，竖屏要调整位置
     *
     * @param dialog
     */
    private void setDialogPos(Dialog dialog) {
        if (mIsVertical) { // 竖屏，重新设置对话框位置
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.TOP);
            if (dialog instanceof AbsPublicDialog) {
                lp.y = 100; // 新位置Y坐标
            } else {
                lp.y = 200;
            }
            dialogWindow.setAttributes(lp);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    /**
     * 通过资源的名字获取资源的id，例：manual_1 获取到 R.drawable.manual_1
     *
     * @param name
     * @return 成功找到返回id，否则返回-1
     * @see int id = getResId(R.drawable.class, "manual_1");
     */
    protected int getResId(Class resClz, String name) {
        try {
            Field field = resClz.getField(name);
            return field.getInt(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 设置沉浸式状态栏
     */
    protected void setBarColor() {
        int color = IVIConfig.getStatusBarColor();
        if (color != -1) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    getWindow().setStatusBarColor(color);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否全屏无标题，默认返回false， 字类可以覆盖返回true
     * @return false/true
     */
    protected boolean isFullScreenNoTitle() {
        return false;
    }

    /**
     * 根据需要全屏无标题
     */
    private void requestFullScreenNoTitle() {
        if (isFullScreenNoTitle()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
