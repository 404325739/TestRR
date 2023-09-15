package com.jancar.viewbase.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.jancar.sdk.utils.Logcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by roadrover on 2018/6/21.
 * 需要输入异常提示时，可以注册LimitTextCallback回调
 */

public class LimitEditText extends EditText {

    /** 限制表情输入 */
    Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    /** 文件命名的正则表达式*/
    String regex = "(?!((^(con)$)|^(con)/..*|(^(prn)$)|^(prn)/..*|(^(aux)$)|^(aux)/..*|(^(nul)$)|^(nul)/..*|(^(com)[1-9]$)|^(com)[1-9]/..*|(^(lpt)[1-9]$)|^(lpt)[1-9]/..*)|^/s+|.*/s$)(^[^/////:/*/?/\"/</>/|]{1,255}$)";

    public class LimitType {
        /** 不允许输入表情*/
        public static final int LIMIT_EMOJI = 0;

        /** 不允许输入特殊字符*/
        public static final int LIMIT_SPECIAL_CHARACTERS = 1;
    }

    public interface LimitTextCallback {
        /**
         *
         * @param type {@link com.jancar.viewbase.widget.LimitEditText.LimitType}
         */
        void onInputError(int type);
    }

    private LimitTextCallback mLimitTextCallback;

    public void setLimitTextCallback(LimitTextCallback limitTextCallback) {
        this.mLimitTextCallback = limitTextCallback;
    }

    public LimitEditText(Context context) {
        super(context);
    }

    public LimitEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LimitEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new InnerInputConnecttion(super.onCreateInputConnection(outAttrs),
                false);
    }

    class InnerInputConnecttion extends InputConnectionWrapper implements InputConnection {

        public InnerInputConnecttion(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            Matcher emojiMatcher = emoji.matcher(text);
            if (emojiMatcher.find()) {
                if (mLimitTextCallback != null) {
                    mLimitTextCallback.onInputError(LimitType.LIMIT_EMOJI);
                }
                Logcat.d("emoji is unallowed!");
                return false;
            }

            boolean state = Pattern.matches(regex, text.toString());
            if (!state) {
                if (mLimitTextCallback != null) {
                    mLimitTextCallback.onInputError(LimitType.LIMIT_SPECIAL_CHARACTERS);
                }
                Logcat.d("special character is unallowed!");
                return false;
            }

            return super.commitText(text, newCursorPosition);
        }
    }
}
