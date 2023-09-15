package com.jancar.bluetooth.bean;

import android.text.TextUtils;

/**
 * 电话本
 */

public class StPhoneBook {
    public boolean isFavorite = false; // 是否是收藏
    public String name        = ""; // 名字
    public String phoneNumber = ""; // 电话号码
    public String firstLetter = ""; // 首字母
    public String pinyin      = ""; // 拼音

    public String getDisplay() {
        return TextUtils.isEmpty(name) ? phoneNumber : name;
    }

    public String toString() {
        return "name:" + name + " phoneNumber:" + phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o instanceof StPhoneBook) {
            StPhoneBook other = (StPhoneBook) o;
            return TextUtils.equals(other.name, name) &&
                    TextUtils.equals(other.phoneNumber, phoneNumber);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        }
        return 0;
    }
}
