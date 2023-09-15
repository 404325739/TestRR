package com.jancar.bluetooth.ui.agent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.bluetooth.utils.AnimationUtil;
import com.jancar.lib_bluetooth.R;
import com.jancar.sdk.utils.Logcat;

/**
 * 该类主要是对悬浮窗键盘类的View控制
 */

public class KeypadViewAgent extends BaseViewAgent implements View.OnClickListener {

    public static KeypadViewAgent newInstance(@NonNull Context context, @NonNull ViewGroup root) {
        KeypadViewAgent keypadViewAgent = new KeypadViewAgent();
        keypadViewAgent.init(context, root);
        return keypadViewAgent;
    }

    private ViewGroup mPhoneKeypadPanel;
    private TextView mKeypadInput;

    @Override
    public void onClick(View view) {
        // Empty
    }

    @Override
    public boolean isViewVisible() {
        return (null != mPhoneKeypadPanel) ? mPhoneKeypadPanel.getVisibility() == View.VISIBLE : false;
    }

    public void setKeyText(String text) {
        if (mKeypadInput != null && !TextUtils.isEmpty(text)) {
            mKeypadInput.setText(text);
        }
    }

    public CharSequence getKeyText() {
        CharSequence result = "";
        if (mKeypadInput != null) {
            result = mKeypadInput.getText();
        }
        return result;
    }

    @Override
    protected void onVisibilityChanged(boolean isVisibility, int callType) {
        if (mPhoneKeypadPanel != null) {
            AnimationUtil.startDownAnimation(isVisibility, mPhoneKeypadPanel, mContext);
        } else {
            Logcat.w("phone keypad panel is null");
        }
    }

    @Override
    protected void onCreate(ViewGroup root) {
        mPhoneKeypadPanel = (ViewGroup) root.findViewById(R.id.bt_phone_keypad_panel);
        mKeypadInput = (TextView) root.findViewById(R.id.bt_keypad_input);
        mPhoneKeypadPanel.setOnClickListener(this); // 截掉后面的按键事件
    }
}
