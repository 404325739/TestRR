package com.jancar.bluetooth.utils;

import android.content.Context;
import android.view.View;

public class AnimationUtil {

    /**
     * 开启一个上下滑动的动画
     *
     * @param isDownToUp 是否是从下到上
     * @param view       做动画的控件
     */
    public static void startDownAnimation(boolean isDownToUp, final View view, Context context) {
        if (view != null && context != null) {
            if (isDownToUp) {
                view.setVisibility(View.VISIBLE);
                //20200505 取消特效
//                Animation animation = AnimationUtils.loadAnimation(context, R.anim.push_down_in);
//                view.startAnimation(animation);
            } else {
                view.setVisibility(View.GONE);
                //20200505 取消特效
//                Animation animation = AnimationUtils.loadAnimation(context, R.anim.push_down_out);
//                view.startAnimation(animation);
//                animation.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) { // 动画结束之后消失页面
//                        view.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
            }
        }
    }
}
