package com.jancar.bluetooth.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @anthor Tzq
 * @describe 号码格式化工具类
 */
public class NumberFormatUtil {
    public static boolean isZh = false;



    public static boolean isZh(Context mContext) {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            isZh = true;
            return isZh;
        }else{
            isZh = false;
            return isZh;}
    }

    /**
     * 大陆号码或香港号码均可
     */
    public static boolean isPhoneLegal(String str) throws PatternSyntaxException {
        return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 145,147,149
     * 15+除4的任意数(不要写^4，这样的话字母也会被认为是正确的)
     * 166
     * 17+3,5,6,7,8
     * 18+任意数
     * 198,199
     */
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[3,5,6,7,8])" +
                "|(18[0-9])|(19[8,9]))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     */
    public static boolean isHKPhoneLegal(String str) throws PatternSyntaxException {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static String getNumber(String number) {
        if (!isZh){
            return number;
        }

        if (TextUtils.isEmpty(number)) {
            return "";
        } else {
            boolean isPhoneNum = false;
            isPhoneNum = isPhoneLegal(number);
            int len = number.length();
            if (len >= 8 && !number.contains(" ") && isPhoneNum) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < len; i++) {
                    char c = number.trim().charAt(i);
                    stringBuffer.append(c);
                    //400 888 5656
                    if (len == 10) {
                        if (i == 2 || i == 5) {
                            stringBuffer.append(" ");
                        }
                    } else {
                        if (number.startsWith("+")) {
                            if (i == 2) {
                                stringBuffer.append(" ");
                            }
                        }
                        //+86 138 0000 5555
                        if ((len - 1 - i) % 4 == 0) {
                            if (number.startsWith("+")) {
                                //+91 10000
                                if (len > 8 && i > 4) {
                                    stringBuffer.append(" ");
                                }
                            } else if (i > 1) {
                                stringBuffer.append(" ");
                            }
                        }
                    }
                }
                return stringBuffer.toString();
            } else {
                return number;
            }
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
