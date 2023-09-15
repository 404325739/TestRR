package com.jancar.bluetooth.utils;

import android.util.Log;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.sdk.system.IVIConfig;
import com.jancar.sdk.utils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 通讯录处理工具类
 */

public class VBookUtil {

    /**
     * 通过号码过滤电话
     *
     * @param totalPhoneBooks 总列表
     * @param phoneNumber     电话号码
     * @return
     */
    public static List<StPhoneBook> filterPhoneBookNumber(final List<StPhoneBook> totalPhoneBooks, String phoneNumber) {
        if (ListUtils.isEmpty(totalPhoneBooks)) {
            return new ArrayList<>();
        }

        List<StPhoneBook> totals = new ArrayList<>();
        totals.addAll(totalPhoneBooks); // 不修改原值

        List<StPhoneBook> results = new ArrayList<>();

        for (int i = 0; i < totals.size(); ++i) { // 过滤电话号码
            if (totals.get(i).phoneNumber.contains(phoneNumber)) {
                results.add(totals.remove(i--));
            }
        }
        return results;
    }

    /**
     * 通过关键字，包括号码，汉字，拼音等过滤电话本
     *
     * @param totalPhoneBooks 总电话本
     * @param filter          过滤条件
     * @return
     */
    public static List<StPhoneBook> filterPhoneBook(final List<StPhoneBook> totalPhoneBooks, String filter) {
        if (ListUtils.isEmpty(totalPhoneBooks)) {
            return new ArrayList<>();
        }
        List<StPhoneBook> totals = new ArrayList<>();
        totals.addAll(totalPhoneBooks); // 不修改原值
        Log.d("VBookUtil", "filter" + filter);
        List<StPhoneBook> results = new ArrayList<>();
        //四合一
        //中文环境下搜索
        for (StPhoneBook book : totals) {
            if (book.phoneNumber.contains(filter)) {
                results.add(book);
            } else if (book.name.toLowerCase().contains(filter.toLowerCase())) { // 过滤全名时，全部转成小写
                results.add(book);
            } else if (book.firstLetter.toLowerCase().contains(filter.toLowerCase())) {
                results.add(book);
            } else if (book.pinyin.replace(" ", "").toLowerCase().contains(filter.toLowerCase())) {
                results.add(book);
            }
        }
        //todo 外文，姓名颠倒，+91号码，正反匹配等
        return results;
    }

    /**
     * 针对电话本名字进行排序
     *
     * @param stPhoneBooks 电话本列表
     * @return
     */
    public static List<StPhoneBook> sortPhoneBook(Set<StPhoneBook> stPhoneBooks) {
        List<StPhoneBook> results = new ArrayList<>();
        if (stPhoneBooks != null) {
            results.addAll(stPhoneBooks);
            Collections.sort(results, new Comparator<StPhoneBook>() {
                @Override
                public int compare(StPhoneBook stPhoneBook1, StPhoneBook stPhoneBook2) {
                    // Logcat.d("stPhoneBook1.pinyin:" + stPhoneBook1.pinyin + " stPhoneBook2.pinyin:" + stPhoneBook2.pinyin);
                    // 将特殊字符全部丢到最后
                    boolean phoneBook1IsSpecial = false;
                    if (!stPhoneBook1.pinyin.isEmpty()) {
                        char src = stPhoneBook1.pinyin.charAt(0);
                        if (src < 'a' || src > 'z') {
                            phoneBook1IsSpecial = true;
                        }
                    } else {
                        phoneBook1IsSpecial = true;
                    }

                    boolean phoneBook2IsSpecial = false; // 电话本是否是特殊字符开头
                    if (!stPhoneBook2.pinyin.isEmpty()) {
                        char dest = stPhoneBook2.pinyin.charAt(0);
                        if (dest < 'a' || dest > 'z') {
                            phoneBook2IsSpecial = true;
                        }
                    } else {
                        phoneBook2IsSpecial = true;
                    }

                    if (phoneBook1IsSpecial == phoneBook2IsSpecial) { // 按照拼音排序
                        return stPhoneBook1.pinyin.compareTo(stPhoneBook2.pinyin);
                    } else if (phoneBook1IsSpecial) {
                        return IVIConfig.isBluetoothContactSpecialFirst() ? -1 : 1;
                    } else {
                        return IVIConfig.isBluetoothContactSpecialFirst() ? 1 : -1;
                    }
                }
            });
        }
        return results;
    }
}
