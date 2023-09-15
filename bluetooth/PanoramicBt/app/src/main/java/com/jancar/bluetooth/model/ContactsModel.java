package com.jancar.bluetooth.model;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.ui.mvp.model.Model;

import java.util.List;


/**
 * @author Tzq
 * @date 2019-12-26 20:06:24
 */
public interface ContactsModel extends Model {

    interface Callback {
        /**
         * 列表加载成功
         *
         * @param data 会话列表
         */
        void onSuccess(List<StPhoneBook> data);

        /**
         * 列表加载完成
         *
         * @param code
         * @param msg
         * @param listSize
         */
        void onFinish(int code, String msg, int listSize);

        /**
         * 当前下载进度的联系人
         *
         * @param stPhoneBook
         */
        void onProgress(List<StPhoneBook> stPhoneBook);

    }


    /**
     * 首次加载列表数据
     */
    void loadPhoneListFirst();

    /**
     * 搜索联系人列表
     *
     * @param inputKey 关键字
     */
    void searchPhoneList(String inputKey);

    /**
     * 请求所有的联系人
     */
    void requestAllPhoneList();

    /**
     * 同步联系人
     */
    void onSyncContact();

    /**
     * 清空联系人
     */
    void clearContactList();

    /**
     * 获取蓝牙电话本，通话记录下载状态
     * @return
     */
    int getdownPhoneBookState();

    /**
     * 停止联系人和通话记录下载
     */
    void stopContactOrHistoryLoad();

}
