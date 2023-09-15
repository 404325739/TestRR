package com.jancar.bluetooth.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * FileName: AnimUtils
 * Profile: 动画工具类
 */
public class AnimUtils {

    /**
     * 旋转动画
     * @param view
     * @return
     */
    public static ObjectAnimator rotation(View view) {
        ObjectAnimator mAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        mAnim.setDuration(1200);
        mAnim.setRepeatMode(ValueAnimator.RESTART);
        mAnim.setRepeatCount(ValueAnimator.INFINITE);
        mAnim.setInterpolator(new LinearInterpolator());
        return mAnim;
    }

}
