package com.jancar.bluetooth.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 系统时间工具类
 */

public class TimeUtil {

    /**
     * 获取当前的系统时间，输出格式  2017/01/09 21:05
     * @return
     */
    public static String getCurSystemTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date curDate = new Date(System.currentTimeMillis()); //获取当前时间
        return formatter.format(curDate);
    }
}
