package com.jancar.bluetooth.model;

import com.jancar.bluetooth.bean.StCallHistory;
import com.ui.mvp.model.Model;

import java.util.List;


/**
 * @author Tzq
 * @date 2019-12-26 20:09:21
 */
public interface RecordsModel extends Model {

    interface Callback {
        /**
         * 列表加载成功
         *
         * @param data 会话列表
         */
        void onSuccess(List<StCallHistory> data);

        /**
         * 加载完成
         *
         * @param code
         * @param msg
         * @param listSize
         */
        void onFinish(int code, String msg, int listSize);

        /**
         * 正在下载的联系人
         *
         * @param history
         */
        void onProgress(List<StCallHistory> history);

        /**
         *是否开始计时
         * @param  begin true 开始重新计时，false 直接移除
         */
        void onBeginResetTime(boolean begin);

    }

    /**
     * 加载列表数据
     *
     * @param isGetBluzModel
     */
    void loadCallHistoryList(boolean isGetBluzModel);


    /**
     * 获取蓝牙电话本，通话记录下载状态
     * @return
     */
    int getdownPhoneBookState();


    /**
     * 停止联系人和通话记录下载
     */
    void stopContactOrHistoryLoad();

    /**
     * 重置下载标志位，防止因为意外情况导致的无法更新界面
     */
    void resetDownState();


}
