package com.jancar.bluetooth.model;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.ui.mvp.model.Model;

import java.util.List;


/**
 * @author Tzq
 * @date 2019-12-26 20:10:01
 */
public interface CollectionModel extends Model {

    interface Callback {
        /**
         * 列表加载成功
         *
         * @param data 会话列表
         */
        void onSuccess(List<StPhoneBook> data);

        /**
         * 下载完成
         *
         * @param code
         * @param msg
         * @param listSize
         */
        void onFinish(int code, String msg, int listSize);

    }

    /**
     * 首次加载列表数据
     */
    void loadCollectListFirst();
}
