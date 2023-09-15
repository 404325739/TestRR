package com.jancar.bluetooth.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2020/10/12.
 */

public class ToastUtil {
    private static Toast mToast;
    private ToastUtil(){};
    static class LazyClass{
         static ToastUtil mToastUtil = new ToastUtil();
    }
    public static ToastUtil getInstance(){
        return LazyClass.mToastUtil;
    }

    /**
     * 直接在application中初始化
     * @param context
     */
    public void initToastUtil(Context context){
        mToast = Toast.makeText(context,"",Toast.LENGTH_SHORT);
    }

    /**
     * 注意传入appcation的context，否则有可能引发内存泄露
     * @param
     * @param resid
     * @param duration
     */
    public void showToast(int resid,int duration){
        if(mToast == null){
//            mToast = Toast.makeText(context,resid,duration);
            return;
        }else{
            mToast.setText(resid);
            mToast.setDuration(duration);
        }
        mToast.show();
    }

    /**
     * 注意传入appcation的context，否则有可能引发内存泄露
     * @param
     * @param str
     * @param duration
     */
    public void showToast(CharSequence str,int duration){
        if(mToast == null){
//            mToast = Toast.makeText(context,str,duration);
            return;
        }else{
            mToast.setText(str);
            mToast.setDuration(duration);
        }
        mToast.show();
    }
}
