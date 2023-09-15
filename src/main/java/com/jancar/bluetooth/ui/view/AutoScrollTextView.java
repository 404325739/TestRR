package com.jancar.bluetooth.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.jancar.sdk.utils.Logcat;

/**
 * Created by LBH on 2020/10/20.
 * 横向自动滚动到编辑位置的textview
 * 此控件必须使用android:layout_width="match_parent"或者固定宽度。
 * 而且layout:grary失效，改为自己scrollto来居中
 * 需要android:ellipsize="start"
 */

@SuppressLint("AppCompatCustomView")
public class AutoScrollTextView extends TextView {
    private Paint mMeasurePaint;
    private boolean isRtl = false;//是否处于界面翻转状态


    private boolean isRtl(Resources res) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public AutoScrollTextView(Context context) {
        super(context);
    }

    public AutoScrollTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoScrollTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoScrollTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //防止初始化时获取到的宽高为0显示错误
        post(new Runnable() {
            @Override
            public void run() {
                String str = getText() == null ? "" : getText().toString();
                autoSrcroll(str);
            }
        });

    }

    private void autoSrcroll(String str) {
        if (TextUtils.isEmpty(str)) {
            str = "";
        }
        mMeasurePaint.setTextSize(getTextSize());
        float lenght = mMeasurePaint.measureText(str);
        int width = getWidth();
        int paddingStart = getPaddingStart();
        int paddingEnd = getPaddingEnd();
        int realwidth = width - paddingEnd - paddingStart;
        Logcat.d("width =" + width + ", lenght =" + lenght + ", str =" + str);
        if (realwidth > 0 && lenght > realwidth) {
            scrollTo((int) (lenght - realwidth), 0);
        } else {
            if (realwidth > 0) {
                scrollTo((int) ((lenght - realwidth) / 2), 0);//居中显示
            }
        }
    }

    private void init() {
        mMeasurePaint = new Paint();
        isRtl = isRtl(getResources());
        Logcat.d("isRtl =" + isRtl);
        setHorizontalScrollBarEnabled(true);
        setScrollIndicators(0);
        setMovementMethod(ScrollingMovementMethod.getInstance());
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int offset = 0;
                if (s != null) {
                    String str = s.toString();
                    autoSrcroll(str);
                }

            }
        });

    }
}
