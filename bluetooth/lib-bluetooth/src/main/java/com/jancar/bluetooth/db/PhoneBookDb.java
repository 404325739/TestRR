package com.jancar.bluetooth.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.VBookUtil;
import com.jancar.db.DBBaseHelper;
import com.jancar.sdk.bluetooth.BluetoothModel;
import com.jancar.sdk.utils.ListUtils;
import com.jancar.sdk.utils.Logcat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 联系人的数据库工具类
 */

public class PhoneBookDb extends DBBaseHelper<StPhoneBook> {

    private static final String DATABASE_NAME = "phone_book.db";
    public static final String DATABASE_FILE_NAME = "phone_book"; // 用来缓存当前数据库对应的xml
    public static final String ADDR = "addr"; // 存当前设备的地址
    private static final String INSERT_TIME = "time"; // 插入时间

    private ExecutorService mSignalExecutorService = Executors.newSingleThreadExecutor(); // 批量数据库操作，放到线程中执行
    private Handler mHandler = null;

    private static PhoneBookDb pThis = null;
    public static PhoneBookDb getInstance() {
        if (null == pThis) {
            pThis = new PhoneBookDb(BtApplication.getInstance(), DATABASE_NAME, StPhoneBook.class);
        }
        return pThis;
    }

    private PhoneBookDb(Context context, String name, Class<?>... clasz) {
        super(context, name, clasz);

        if (null != context) {
            mHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (null != mPhoneBookDbCallback) {
                        mPhoneBookDbCallback.onResult((List<StPhoneBook>) msg.obj);
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    /**
     * 执行插入操作
     * @param books 插入列表
     */
    public void insert(final List<StPhoneBook> books, final String time) {
        String addr = BluetoothCacheUtil.getInstance().getCurConnectDevice().addr;
        Logcat.d("addr:" + addr);

        if(!ListUtils.isEmpty(books)){
            // 记录当前的数据库的联系人对应的 addr
            SharedPreferences setting = BtApplication.getInstance().getSharedPreferences(DATABASE_FILE_NAME, Context.MODE_PRIVATE);
            setting.edit().putString(ADDR, addr).commit();
            setting.edit().putString(INSERT_TIME, time).commit();
        }


        mSignalExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                delete(StPhoneBook.class, "", null);

                insertObjectIntoTable(books); // 插入对象

                getContext().getContentResolver().notifyChange(BluetoothModel.Provider.URI, null);
            }
        });
    }

    public interface PhoneBookDbCallback {
        void onResult(List<StPhoneBook> stPhoneBooks); // 查询到数据
        void onFailure(); // 换设备，返回失败
    }
    private PhoneBookDbCallback mPhoneBookDbCallback = null;



//    public void query(PhoneBookDbCallback callback) {
//        query(callback,"");
//    }
    /**
     * 查询电话本
     * @param callback
     */
    public void query(PhoneBookDbCallback callback, final String addr) {
        // 同一连接设备连接设备地址与缓存电话记录中的设备地址相同
//        if (!BluetoothCacheUtil.getInstance().isNewConntectedDevice()) {
        String pbaddr = getAddr();
        Logcat.d("pbaddr =" + pbaddr + ", queryaddr =" + addr);
        if (TextUtils.equals(addr,pbaddr)) {
            mPhoneBookDbCallback = callback;
            mSignalExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    Set<StPhoneBook> stPhoneBooks = new HashSet<>();
                    stPhoneBooks.addAll(getObjectFromTable("", null, "", "", StPhoneBook.class)); // 使用 Set 过滤重复
                    List<StPhoneBook> results = VBookUtil.sortPhoneBook(stPhoneBooks); // 每次从数据库中获取数据都进行一次排序
                    if (null != mHandler) {
                        Logcat.d("local db stPhoneBooks.size:" + stPhoneBooks.size());
                        Message msg = mHandler.obtainMessage();
                        msg.obj = results;
                        msg.sendToTarget();
                    } else {
                        Logcat.d("stPhoneBooks.size:" + stPhoneBooks.size() + " but mHandler is null");
                    }
                }
            });
        } else { // 新设备被连接，清空电话信息
            Logcat.d("new device connectd clear cached info first");
            BluetoothModelUtil.getInstance().setCallNumber(""); // 更换设备后，清空上一个设备的拨号记录。
            BluetoothModelUtil.getInstance().setAllPhoneBooks(null); // 清空缓存的数据
            BluetoothModelUtil.getInstance().clearPhoneBookDb(); //清空前一个设备的电话本
            BluetoothModelUtil.getInstance().clearHistoryDb();   // 清空前一个设备的历史记录
            BluetoothCacheUtil.getInstance().updatePrevDeviceAddr(); // 新设备更新前一设备地址信息为当前地址
            BluetoothCacheUtil.getInstance().resetNewDeviceValue();  // 清空过一次，则认为不再是新设备。
            if (callback != null) callback.onFailure();
        }
    }

    /**
     * 获取当前电话本的的地址
     * @return
     */
    private String getAddr() {
        SharedPreferences setting = BtApplication.getInstance().getSharedPreferences(DATABASE_FILE_NAME, Context.MODE_PRIVATE);
        return setting.getString(ADDR, "");
    }

    /**
     * 获取插入时间
     * @return
     */
    public String getInsertTime() {
        SharedPreferences setting = BtApplication.getInstance().getSharedPreferences(DATABASE_FILE_NAME, Context.MODE_PRIVATE);
        return setting.getString(INSERT_TIME, "");
    }

    /**
     * 更新数据
     * @param stPhoneBook
     */
    public void update(final StPhoneBook stPhoneBook) {
        if (stPhoneBook != null) {
            mSignalExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    ContentValues values = new ContentValues();
                    values.put(getFieldName("name"), stPhoneBook.name);
                    values.put(getFieldName("phoneNumber"), stPhoneBook.phoneNumber);
                    values.put(getFieldName("firstLetter"), stPhoneBook.firstLetter);
                    values.put(getFieldName("isFavorite"), stPhoneBook.isFavorite);
                    values.put(getFieldName("pinyin"), stPhoneBook.pinyin);
                    update(StPhoneBook.class, values, getFieldName("name") + "=? and " + getFieldName("phoneNumber") + "=?",
                            new String[]{stPhoneBook.name, stPhoneBook.phoneNumber});
                }
            });
        }
    }

}
