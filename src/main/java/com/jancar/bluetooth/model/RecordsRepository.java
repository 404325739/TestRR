package com.jancar.bluetooth.model;


import android.text.TextUtils;

import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.PBDownLoadStateUtil;
import com.jancar.bluetooth.utils.SpendTimeUtils;
import com.jancar.sdk.utils.Logcat;

import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:09:21
 */
public class RecordsRepository implements RecordsModel {

    private Callback mCallback;
    private boolean isLoadDataNow = false;//防止进入界面多次刷新

    public RecordsRepository(Callback callback) {
        this.mCallback = callback;
    }


    @Override
    public void loadCallHistoryList(boolean isGetBluzModel) {
        if(isLoadDataNow){
            Logcat.d("isLoadDataNow >>  return");
            return;
        }
        isLoadDataNow = true;
        mCallback.onBeginResetTime(true);
        //请求通话记录
        BluetoothModelUtil.getInstance().requestAllHistory(new BluetoothModelUtil.BluzHistoryCallback() {
            @Override
            public void onShowProgress() {
                //下载进度
                Logcat.d("++loadCallHistoryList onShowProgress++");
                mCallback.onBeginResetTime(true);

            }

            @Override
            public void onFinish(List<StCallHistory> stCallHistories) {
                int size = stCallHistories == null ? 0 : stCallHistories.size();
                Logcat.d("++loadCallHistoryList onFinish++  size =" + size);
                isLoadDataNow = false;
                //下载完成
                mCallback.onBeginResetTime(false);
                mCallback.onSuccess(stCallHistories);
                mCallback.onFinish(0, "success", size);
                //添加默认记录号码，以便重拨  20200513
//                int size = stCallHistories.size();
                if (size > 0) {
                    StCallHistory stCalls = stCallHistories.get(0);
                    if (stCalls != null && !TextUtils.isEmpty(stCalls.phoneNumber)) {
                        BluetoothModelUtil.getInstance().setCallNumber(stCalls.phoneNumber);
                    }
                }
            }

            @Override
            public void onProgress(List<StCallHistory> stCallHistories) {
                Logcat.d("++loadCallHistoryList onProgress++");
//                SpendTimeUtils.getInstance().start("loadCallHistoryList");
                if (stCallHistories.size() > 0) {
                    mCallback.onProgress(stCallHistories);
                }
//                SpendTimeUtils.getInstance().spend("loadCallHistoryList");
//                SpendTimeUtils.getInstance().stop("loadCallHistoryList");
            }

            @Override
            public void onUpdate() {
                //更新
                Logcat.d("++loadCallHistoryList onUpdate++");
                mCallback.onSuccess(BluetoothModelUtil.getInstance().getAllCallHistorys());

            }
        }, isGetBluzModel);

    }

    @Override
    public int getdownPhoneBookState() {
        return PBDownLoadStateUtil.getDownloadState();
    }

    @Override
    public void stopContactOrHistoryLoad() {
        BluetoothModelUtil.getInstance().stopContactOrHistoryLoad();
    }

    @Override
    public void resetDownState() {
        isLoadDataNow = false;
    }
}
