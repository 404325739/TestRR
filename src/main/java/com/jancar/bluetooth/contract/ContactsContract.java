package com.jancar.bluetooth.contract;

import android.content.Context;

import com.jancar.bluetooth.adapter.ContactsAdapter;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.viewbase.widget.LetterSlideBar;
import com.ui.mvp.presenter.IPresenter;
import com.ui.mvp.view.Ui;

import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:06:24
 */
public interface ContactsContract {

    interface View extends Ui {

        /**
         * 获取UI上下文
         *
         * @return 上下文
         */
        Context getUIContext();

        /**
         * 在UI线程中执行线程任务
         *
         * @param runnable 线程任务
         */
        void runOnUIThread(Runnable runnable);

        /**
         * 同步结束
         *
         * @param code
         * @param msg
         * @param listSize 联系人数量
         */
        void synchronousFinish(int code, String msg, int listSize);

        /**
         * 当前下载的联系人信息
         *
         * @param stPhoneBook
         */
        void onProgress(List<StPhoneBook> stPhoneBook);

        LetterSlideBar getLetterSlideBar();
    }

    interface Presenter extends IPresenter {

        /**
         * 获取联系人列表适配器
         *
         * @return
         */
        ContactsAdapter getAdapter();


        /**
         * 首次加载列表数据
         */
        void loadPhoneListFirst();


        /**
         * 搜索联系人列表
         *
         * @param inputKey
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
         * 清除联系人列表
         */
        void clearContactList();

        /**
         * 获取蓝牙连接状态
         *
         * @return
         */
        int getBluConnStatus();

        boolean isBtConnected();

        boolean isDownloading();

        //获取下载状态
        int getDownloadState();
        /**
         * 停止下载任务
         */
        void stopContactOrHistoryLoad();

        /**
         * 拨打选中的联系人电话
         */
        void callSelectedContact();

        /**
         * 删除选中的联系人
         */
        void delSelectedContact();

        /**
         * 更新联系人列表
         *
         * @param bookList 数据列表
         * @param isAll 是否全部数据
         */
        void updatePhoneBook(List<StPhoneBook> bookList, boolean isAll);
    }
}
