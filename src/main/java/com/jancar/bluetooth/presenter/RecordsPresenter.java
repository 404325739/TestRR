package com.jancar.bluetooth.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.jancar.bluetooth.adapter.RecordsAdapter;
import com.jancar.bluetooth.contract.RecordsContract;
import com.jancar.bluetooth.model.RecordsModel;
import com.jancar.bluetooth.model.RecordsRepository;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.Constants;
import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.ListUtils;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.presenter.BaseModelPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tzq
 * @date 2019-12-26 20:09:21
 */
public class RecordsPresenter extends BaseModelPresenter<RecordsContract.View, RecordsModel> implements RecordsContract.Presenter, RecordsModel.Callback {

    private List<StCallHistory> mHistoryList;
    private RecordsAdapter mAdapter;
    private Object mListLock = new Object();
    private final static int MSG_RESET = 0;
    private boolean beginGetHistory = false; //是否由界面触发的获取数据库，此处还需判断如果是界面触发的，而且数据库中只有本地插入的数据库，向服务查询

    private int mCurrType = IVIBluetooth.BluetoothCallHistoryStatus.ALL_STATUS;
    Map<Integer, List<StCallHistory>> mListsMap = new HashMap<>(); // key: 通话记录类型；value：列表
    private boolean isAc8257_YQQD_DY801;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(MSG_RESET == msg.what){
                if(getModel() != null){
                    getModel().resetDownState();
                }
            }
        }
    };

    @Override
    public RecordsModel createModel() {
        return new RecordsRepository(this);
    }

    @Override
    public RecordsAdapter getAdapter() {
        Logcat.d("getUi() " + getUi());
        if (getUi() != null) {
            Context uiContext = getUi().getUIContext();
            if (mAdapter == null && uiContext != null) {
                isAc8257_YQQD_DY801 = AppUtils.isAc8257_YQQD_DY801Platform(uiContext);
                if (mHistoryList == null) {
                    mHistoryList = new ArrayList<>();
                    mListsMap.put(IVIBluetooth.BluetoothCallHistoryStatus.ALL_STATUS, mHistoryList);
                    mListsMap.put(IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, new ArrayList<>());
                    mListsMap.put(IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS, new ArrayList<>());
                    mListsMap.put(IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS, new ArrayList<>());
                    mListsMap.put(IVIBluetooth.BluetoothCallHistoryStatus.UNKNOWN_STATUS, new ArrayList<>());
                }
                mAdapter = new RecordsAdapter(uiContext, mListsMap.get(mCurrType));
                mAdapter.setOnItemClickListener(new RecordsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //item 点击事件
                        if (!ListUtils.isEmpty(mAdapter.getDataList())) {
                            if (isAc8257_YQQD_DY801) {
                                mAdapter.notifyDataSetChanged();
                            } else {
                                callHistoryPhone(position);
                            }
                        }
                    }
                });
            }
        }
        return mAdapter;
    }

    /**
     * 拨打号码
     *
     * @param position 在列表中的位置
     */
    private void callHistoryPhone(int position) {
        if (null == mAdapter) {
            Logcat.d("mAdapter is null, return;");
            return;
        }
        StCallHistory stCallHistory;
        synchronized (mListLock) {
            stCallHistory = mAdapter.getItem(position);
        }
        if (stCallHistory != null) {
            String number = stCallHistory.phoneNumber;

            Logcat.d("callHistoryPhone:" + number);
            if (!TextUtils.isEmpty(number) && number.length() >= Constants.ALL_SHORT_LEN) {
                BluetoothModelUtil.getInstance().callPhone(number);
            } else {
                Logcat.d("Not call");
            }
        }
    }

    @Override
    public void loadCallHistoryList(boolean isGetBluzModel) {
        if(!isGetBluzModel){
            beginGetHistory = true;
        }
        getModel().loadCallHistoryList(isGetBluzModel);
    }

    @Override
    public boolean isBtConnected() {
        return BluetoothCacheUtil.getInstance().getBluzConnectedStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTED;
    }

    @Override
    public void clearHistoryList() {
        if (mAdapter != null && getUi().getUIContext() != null) {
            getUi().runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    boolean datachange = false;
                    if (!ListUtils.isEmpty(mListsMap.get(mCurrType))) {
                        datachange = true;
                    }
                    synchronized (mListLock) {
                        for (List<StCallHistory> list : mListsMap.values()) {
                            list.clear();
                        }
                    }
                    if (datachange) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void stopContactOrHistoryLoad() {
        getModel().stopContactOrHistoryLoad();
    }

    @Override
    public void callSelectedRecord() {
        if (null != mAdapter) {
            callHistoryPhone(mAdapter.getSelectedPosition());
        }
    }

    @Override
    public void delSelectedRecord() {
        if (null == mAdapter) {
            Logcat.d("mAdapter is null, return;");
            return;
        }
        StCallHistory stCallHistory = mAdapter.getItem(mAdapter.getSelectedPosition());
        if (null != stCallHistory) {
            int lastSize = mListsMap.get(mCurrType).size();
            Logcat.d("remove name:" + stCallHistory.name);
            for (List<StCallHistory> list : mListsMap.values()) {
                list.remove(stCallHistory);
            }
            int currSize = mListsMap.get(mCurrType).size();
            // 判断当前显示的类型是否删除了
            if (currSize != lastSize) {
                mAdapter.notifyDataSetChanged();
            }
            BluetoothModelUtil.getInstance().delete(stCallHistory);
        }
    }

    @Override
    public void setRecordType(int type) {
        if (mCurrType != type) {
            mCurrType = type;
            mAdapter.setDataList(mListsMap.get(type));
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onSuccess(List<StCallHistory> data) {
        Logcat.d("onSuccess =" + (data == null ? "null" : data.size()));
        updatePhoneBook(data, true);
        getHistoryAgainIfNeed(data);
    }

    @Override
    public void onFinish(int code, String msg, int listSize) {
        if (null != getUi()) {
            getUi().synchronousFinish(code, msg, listSize);
        }
    }

    /**
     * 是否需要再次（从服务获取）历史记录
     * 当列表中只有本地插入的数据时需要再次向服务请求获取数据
     * 应对场景：当点击下载电话本，清空了本地历史记录，此时打电话，会插入一条数据到数据库，不要在下载的时候切换到通话记录界面，等下载完成后再进入，会导致界面只有一条数据
     * @param data
     */
    private void getHistoryAgainIfNeed(List<StCallHistory> data){
        if(beginGetHistory){
            beginGetHistory =false;
            if(ListUtils.isEmpty(data)){
                return;
            }
            boolean isNeed = true;
            for(StCallHistory history:data){
                if(history.adder == StCallHistory.ADDER_SQLITE){
                    isNeed = false;
                    break;
                }
            }
            Logcat.d("isNeed =" + isNeed);
            if(isNeed){
                loadCallHistoryList(true);
            }
        }

    }


    @Override
    public void onProgress(List<StCallHistory> history) {
        if (null != getUi()) {
            Logcat.d("onProgress =" + (history == null ? "null" : history.size()));
//            updatePhoneBook(history, !isAc8257_YQQD_DY801); // 20210830 lyy 8257暂时分段回调的
            updatePhoneBook(history, true);// 20210927 lyy Ac8257_YQQD_DY801不分段
            getUi().onProgress(history);
        }
    }

    @Override
    public void onBeginResetTime(boolean begin) {
        Logcat.d("begin =" + begin);
        if(begin){
            mHandler.removeMessages(MSG_RESET);
            mHandler.sendEmptyMessageDelayed(MSG_RESET,30*1000);//如果超过30s没有任何回调，则重置该标志位，使界面能更新
        }else{
            mHandler.removeMessages(MSG_RESET);
        }
    }


    /**
     * 更新通话记录列表
     *
     * @param historyList
     */
    private synchronized void updatePhoneBook(final List<StCallHistory> historyList, boolean isAll) {
        if (getUi() != null && getUi().getUIContext() != null) {
            if (historyList != null && historyList.size() > 0) {
                getUi().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mHistoryList != null) {
                            int lastSize = mListsMap.get(mCurrType).size();
                            synchronized (mListLock) {
                                if (isAll) {
                                    for (List<StCallHistory> list : mListsMap.values()) {
                                        list.clear();
                                    }
                                }
                                mHistoryList.addAll(historyList);
                                for (StCallHistory stCallHistory : historyList) {
                                    mListsMap.get(stCallHistory.status).add(stCallHistory);
                                }
                            }
                            int currSize = mListsMap.get(mCurrType).size();
                            // 判断当前显示的类型是否删除了
                            if (currSize != lastSize && mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onUiDestroy(RecordsContract.View ui) {
        super.onUiDestroy(ui);
        mAdapter = null;
        mHandler.removeCallbacksAndMessages(null);
        synchronized (mListLock) {
            for (List<StCallHistory> list : mListsMap.values()) {
                list = null;
            }
            mListsMap = null;
        }
    }
}