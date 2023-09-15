package com.jancar.viewbase.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueenTextView extends TextView {


    public MarqueenTextView(Context context) {
        super(context);
    }

    public MarqueenTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueenTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
