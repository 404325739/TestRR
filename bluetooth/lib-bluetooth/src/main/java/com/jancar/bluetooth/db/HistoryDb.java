package com.jancar.bluetooth.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.db.DBBaseHelper;
import com.jancar.sdk.bluetooth.BluetoothModel;
import com.jancar.sdk.utils.ListUtils;
import com.jancar.sdk.utils.Logcat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 通话记录的数据库操作对象
 */

public class HistoryDb extends DBBaseHelper<StCallHistory> {

    private static final String HISTORY_DB_NAME = "history.db"; // 数据库名
    private static final String HISTORY_XML_NAME = "history";
    private static final String ADDR = "addr";  // 通话记录的地址要和通讯录单独保存
    private Object mInserLock = new Object();//插入锁

    private ExecutorService mSignalExecutorService = Executors.newSingleThreadExecutor(); // 批量数据库操作，放到线程中执行

    public HistoryDb() {
        this(BtApplication.getInstance(), HISTORY_DB_NAME, StCallHistory.class);
    }

    private HistoryDb(Context context, String name, Class<?>... clasz) {
        super(context, name, clasz);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHistoryListener(EventOnHistoryListener event) {
        if (event != null) {
            Logcat.d("history changed");
            event.evaluate();
        } else {
            Logcat.w("history changed bug failure update");
        }
    }

    /**
     * 将通话记录插入数据库
     *
     * @param stCallHistories
     */
    public synchronized void insert(final List<StCallHistory> stCallHistories) {
        setAddr(BluetoothCacheUtil.getInstance().getCurConnectDevice().addr); // 插入的时候，缓存当前连接的设备
        mSignalExecutorService.execute(new Runnable() { // 批量插入操作，放到线程中执行
            @Override
            public void run() {
                delete(StCallHistory.class, "", null);


                if (ListUtils.isEmpty(stCallHistories)) {
                    Logcat.d("insert null to delect all history");
                    return;
                }
                List<StCallHistory> copylist = new ArrayList<>();
                try{
                    for(int i = 0; i < stCallHistories.size(); i++){
                        copylist.add(((StCallHistory)stCallHistories.get(i)).clone());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                synchronized (mInserLock){
                    insertObjectIntoTable(copylist); // 插入对象
                    Logcat.d("HistoryDb insert size =" + copylist.size());
                }

                getContext().getContentResolver().notifyChange(BluetoothModel.Provider.URI_CALL_HISTORY, null);
            }
        });
    }

    /**
     * 添加一条通话记录
     *
     * @param stCallHistory
     */
    public synchronized void addCallHistory(final StCallHistory stCallHistory) {
        mSignalExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                List<StCallHistory> stCallHistories = new ArrayList<>();
                stCallHistories.add(stCallHistory);
                synchronized (mInserLock) {
                    insertObjectIntoTable(stCallHistories);
                }
                Logcat.d("addCallHistory = " + stCallHistory.phoneNumber + " :" + stCallHistory.name);
				getContext().getContentResolver().notifyChange(BluetoothModel.Provider.URI_CALL_HISTORY, null);
            }
        });
    }

    public interface HistoryCallback {
        void onResult(List<StCallHistory> StCallHistory); // 查询到数据

        void onFailure(); // 换设备，返回失败
    }
    public void query(final HistoryCallback callback) {

    }


    /**
     * 查询通话记录
     *
     * @param callback
     */
    public synchronized void query(final HistoryCallback callback,String addr) {
//        if (!BluetoothCacheUtil.getInstance().isNewConntectedDevice()) {
        if (TextUtils.equals(addr,getAddr())) {
            mSignalExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    List<StCallHistory> stCallHistorys = new ArrayList<>();
                    stCallHistorys.addAll(getObjectFromTable("", null, "", "", StCallHistory.class));

                    // 对查询结果进行排序
                    Collections.sort(stCallHistorys, new Comparator<StCallHistory>() {
                        @Override
                        public int compare(StCallHistory t1, StCallHistory t2) {
                            if (null != t1 && null != t2) {
                                return t2.sort.compareTo(t1.sort);
                            }
                            return 0;
                        }
                    });
                    Logcat.d("stCallHistorys.size:" + stCallHistorys.size());
                    if (callback != null) {
                        EventOnHistoryListener.onEvent(callback, stCallHistorys);
                    }
                }
            });
        } else {
            if (callback != null) callback.onFailure();
        }
    }

    /**
     * 缓存地址
     *
     * @param addr
     */
    private void setAddr(String addr) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(HISTORY_XML_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ADDR, addr).commit();
    }

    /**
     * 获取地址
     *
     * @return
     */
    private String getAddr() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(HISTORY_XML_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ADDR, "");
    }

    public static class EventOnHistoryListener {
        private final List<StCallHistory> mCallHistories;
        private final HistoryCallback mHistoryCallback;

        public static void onEvent(final HistoryCallback callback, final List<StCallHistory> callHistories) {
            EventBus.getDefault().post(new EventOnHistoryListener(callback, callHistories));
        }
        private EventOnHistoryListener(HistoryCallback callback, List<StCallHistory> callHistories) {
            mCallHistories = callHistories;
            mHistoryCallback = callback;
        }

        public boolean isValid() {
            return (mCallHistories != null && mHistoryCallback != null);
        }

        public void evaluate() {
            if (mHistoryCallback != null) {
                if (mCallHistories != null) {
                    mHistoryCallback.onResult(mCallHistories);
                } else {
                    mHistoryCallback.onFailure();
                }
            }
        }
    }
}
