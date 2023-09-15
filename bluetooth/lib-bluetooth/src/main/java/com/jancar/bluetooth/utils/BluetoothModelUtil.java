package com.jancar.bluetooth.utils;

import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.db.HistoryDb;
import com.jancar.bluetooth.db.PhoneBookDb;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.bluetooth.event.EventOnDelayTask;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.btservice.bluetooth.BluetoothVCardBook;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.btservice.bluetooth.IBluetoothStatusCallback;
import com.jancar.btservice.bluetooth.IBluetoothVCardCallback;
import com.jancar.hanzi.HanziToPinyinUtil;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.BluetoothModel;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.jancar.utils.SystemPropertiesUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 蓝牙模块工具类
 * 封装一些很多页面用到的公共方法
 */
public class BluetoothModelUtil {
    private static final int TIME_DELAY_CHECK_CONTACT_SYNC_FAILURED = 500; // 延时500ms检测当前是否蓝牙断开才导致通话本同步失败
    private static final int TIME_MAX_LEN = 15; // 时间的长度
    private static final int FILTER_MAX_COUNT = 5000; // 超过5000条数据，需要加转圈

    private static final String DIAL_TYPE = "DIAL"; // 已拨
    private static final String RECE_TYPE = "RECE"; // 已接
    private static final String MISS_TYPE = "MISS"; // 未接

    private String mCallNumber = ""; // 拨出的号码
    private String mTalkingNumber = ""; // 当前通话的号码
    private boolean mIsThreeTalking = false; // 是否是第三方通话
    private boolean mIsThreeIncoming = false; // 是否是第三方来电通话
    private boolean mIsThreeOutGoing = false; // 第三方去电状态
    private boolean mIsHangupByCar = false;  // 是否是车机端点的挂断键
    private boolean mIsContactDownloading = false; //电话本是否下载数据中
    private boolean mIsHistoryDownloading = false; // 通话记录时否下载中

    private static BluetoothModelUtil pThis = null;
    private Set<StPhoneBook> mStPhoneBooks = new HashSet<>();//缓存当前下载成功的数据

    private final String defaultDeviceName = "Car BT";
    private final String defaultDevicePin = "1234";

    private HistoryDb mHistoryDb = new HistoryDb();

    private BluzHistoryCallback mBluzHistoryCallback = null;
    private List<StCallHistory> mStAllCallHistorys = new ArrayList<>(); // 缓存所有的通话记录
    private ExecutorService mSignalExecutorService = Executors.newSingleThreadExecutor(); // 创建单列的线程池，用来过滤列表
    private Handler mHandler = new Handler();

    private GetPhoneContactsCallback mGetPhoneContactsCallback;
    private List<StPhoneBook> mStAllPhoneBooks = new ArrayList<>(); // 缓存所有的电话本

    public static boolean isAc8257_YQQD_DY801;

    public static BluetoothModelUtil getInstance() {
        if (null == pThis) {
            pThis = new BluetoothModelUtil();
        }
        return pThis;
    }

    // 蓝牙模块的历史记录回调
    public interface BluzHistoryCallback {
        void onShowProgress(); // 是否需要显示进度条，页面在该回调里面显示进度条

        void onFinish(List<StCallHistory> stCallHistories); // 获取完成

        void onProgress(List<StCallHistory> stCallHistories); // 进度

        void onUpdate(); // 刷新UI
    }

    private BluetoothModelUtil() {
        // Empty
    }

    public static void setIsAc8257_YQQD_DY801(boolean f) {
        isAc8257_YQQD_DY801 = f;
        Logcat.d(" " + isAc8257_YQQD_DY801);
    }

    /**
     * 设置挂断方式
     *
     * @param isCar 挂断类型
     */
    public synchronized void setHangupByCar(boolean isCar) {
        mIsHangupByCar = isCar;
    }

    public synchronized boolean getHangupByCar() {
        return mIsHangupByCar;
    }

    /**
     * 设置拨打号码
     *
     * @param number 电话号码
     */
    public synchronized void setCallNumber(String number) {
        mCallNumber = number;
    }

    public synchronized String getCallNumber() {
        return mCallNumber;
    }

    public boolean getDownloadStatus() {
        Logcat.d("mIsContactDownloading: " + mIsContactDownloading + ", mIsHistoryDownLoading: " + mIsHistoryDownloading);
        return (mIsContactDownloading || mIsHistoryDownloading);
    }

    /**
     * 拨打电话, 拨打的记录在蓝牙模块的时候才记录
     *
     * @param phoneNumber 电话号码
     */
    public void callPhone(final String phoneNumber) {
        Logcat.d("phoneNumber:" + phoneNumber);
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            if (TextUtils.isEmpty(phoneNumber)) {
                return;
            }
            BtApplication.getInstance().getBluetoothManager().callPhone(phoneNumber.trim(), new IBluetoothExecCallback.Stub() {
                @Override
                public void onSuccess(String msg) throws RemoteException {
                    // 拨打成功
                    Logcat.d("callPhone msg:" + msg);

                    // post 通知，通知 PhoneCallWindowManager
                    EventBus.getDefault().post(new IVIBluetooth.CallStatus(
                            IVIBluetooth.CallStatus.OUTGOING,
                            phoneNumber,
                            false  // 不插入数据库，等待蓝牙模块返回通话状态时再插入
                    ));
                    setCallNumber(phoneNumber);
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.d("callPhone errorCode:" + errorCode);
                    EventBus.getDefault().post(new EventClassDefine.EventCallBackFail(errorCode));
                }
            });
        }
    }

    /**
     * 接听电话
     */
    public void listenPhone() {
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            BtApplication.getInstance().getBluetoothManager().listenPhone(new IBluetoothExecCallback.Stub() {
                @Override
                public void onSuccess(String msg) throws RemoteException {
                    Logcat.d("listenPhone msg:" + msg);
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.d("listenPhone errorCode:" + errorCode);
                }
            });
        }
    }

    /**
     * 挂断电话
     */
    public void hangup() {
        //暂不管3方通话，直接全部挂断 @LBH 20201026
//        if (isThreeTalking()) { // 第三方通话
//            threePartyCallCtrl(IVIBluetooth.ThreePartyCallCtrl.ACTION_HANGUP_CUR);
//        } else
            {
            if (null != BtApplication.getInstance().getBluetoothManager()) {
                BtApplication.getInstance().getBluetoothManager().hangPhone(new IBluetoothExecCallback.Stub() {
                    @Override
                    public void onSuccess(String msg) throws RemoteException {
                        Logcat.d("hangup onSuccess msg:" + msg); // 挂断成功，该位置最快，如果发现挂断窗口消失慢，可直接在该位置消失窗口
                    }

                    @Override
                    public void onFailure(int errorCode) throws RemoteException {
                        Logcat.d("hangup onSuccess errorCode:" + errorCode);
                    }
                });
            }
            Logcat.d("EventBus post HANGUP");
            //需要传递特殊号码标记是手动挂断
//            EventBus.getDefault().post(new IVIBluetooth.CallStatus(IVIBluetooth.CallStatus.HANGUP, "HANGUP"));
            //注意，这里是ui消失后又出现再消失的元凶， 因为真正的回调没有回来，这里直接hanup，真正回调可能还有别的状态，会导致ui又显示出来,应该延迟发送？？？（如果这时候又打进电话或者呼出？？） @LBH
//            EventBus.getDefault().post(new IVIBluetooth.CallStatus(IVIBluetooth.CallStatus.HANGUP, "")); // 挂断电话，先隐藏UI，免得蓝牙模块异常，UI卡死
        }
    }

//    /**
//     * 需手动下载通讯录时，取消后主动设置回调
//     *
//     * @param callback
//     */
//    public void setHistoryCallback(BluzHistoryCallback callback) {
//        mBluzHistoryCallback = callback;
//    }

    /**
     * 进入电话本页面，主动设置回调
     *
     * @param callback
     */
    public void setPhoneContactsCallback(GetPhoneContactsCallback callback) {
        mGetPhoneContactsCallback = callback;
    }

    /**
     * 清空缓存通话记录数据
     */
    public void clearHistoryDb() {
        mStAllCallHistorys.clear();
        mHistoryDb.insert(mStAllCallHistorys);
    }

    /**
     * 清空联系人数据库
     */
    public void clearPhoneBookDb() {
        mStAllPhoneBooks.clear();
        PhoneBookDb.getInstance().insert(mStAllPhoneBooks, TimeUtil.getCurSystemTime());
    }

    /**
     * 请求所有历史记录
     *
     * @param callback
     * @param isBluzModel 是否强制从蓝牙模块获取
     */
    public void requestAllHistory(BluzHistoryCallback callback, boolean isBluzModel) {
        mBluzHistoryCallback = callback;
        mStAllCallHistorys.clear();
        mIsHistoryDownloading = true; // 通话记录下载中
        String downstate = SystemPropertiesUtil.get("persist.sys.btpb.downstate","0");
        Logcat.d("requestAllHistory, isBluzModel:" + isBluzModel + ", downstate =" + downstate);
        //如果正在下载，不管本地数据库有没有数据，都向服务请求数据
        if (isBluzModel||!"0".equals(downstate)) {
            delayRequestBluzModeAllHistory();
        } else {
            BluetoothDevice device = BluetoothCacheUtil.getInstance().getCurConnectedDevice();
            String addr = device.addr;
            String name = device.name;
            Logcat.d(" loadPhoneListFirst name =" + name +  ", addr =" + addr);
            //如果当前无设备连接（可能状态没更新到），直接返回，等待下次刷新
            if(TextUtils.isEmpty(addr)){
                return;
            }
            mHistoryDb.query(new HistoryDb.HistoryCallback() {
                @Override
                public void onResult(List<StCallHistory> stCallHistory) { // 数据库中有数据，直接缓存
                    Logcat.d(stCallHistory.size() + "");
                    if (stCallHistory.size() != 0) {
                        mIsHistoryDownloading = false; // 从数据库拉取数据完成，相当于下载完成
                        Logcat.d("history downloading from sqlite finish");
                        appendCallHistory(stCallHistory);
                        if (null != mBluzHistoryCallback) {
                            mBluzHistoryCallback.onFinish(mStAllCallHistorys); // 从数据库，直接完成
                        }
                    } else {
                        onFailure(); // 没有通话记录，重新获取
                    }
                }

                @Override
                public void onFailure() { // 数据库中无相关记录，重新获取
                    delayRequestBluzModeAllHistory();
                }

                // 添加到缓存数据(去重)
                private void appendCallHistory(List<StCallHistory> stCallHistories) {
                    for (StCallHistory stCallHistory : stCallHistories) {
                        if (!mStAllCallHistorys.contains(stCallHistory)) {
                            mStAllCallHistorys.add(stCallHistory);
                        }
                    }
                }
            },addr);
        }
    }

    /**
     * 延时获取通话记录，避免通话记录比通讯录初始化更早，导致先下载通话记录，名称都显示为未知现象。
     */
    private void delayRequestBluzModeAllHistory() {
        Logcat.d();
        // 使用Timer延时有问题， 改用下面这种方式
        EventOnDelayTask.onEvent(100, new Runnable() {
            @Override
            public void run() {
                requestBluzModelAllHistory();
            }
        });
    }

//    /**
//     * 请求所有历史记录
//     *
//     * @param callback
//     */
//    public void requestAllHistory(BluzHistoryCallback callback) {
//        requestAllHistory(callback, false);
//    }

    /**
     * 从总列表里面过滤指定类型的列表
     *
     * @param type
     * @return
     */
    public List<StCallHistory> filterTypeHistory(final int type) {
        final List<StCallHistory> stCallHistories = new ArrayList<>();
        for (StCallHistory history : mStAllCallHistorys) {
            if (type == history.status) {
                stCallHistories.add(history);
            }
        }
        return stCallHistories;
    }

    /**
     * 获取所有通话记录
     *
     * @return
     */
    public List<StCallHistory> getAllCallHistorys() {
        return mStAllCallHistorys;
    }

    // 获取蓝牙电话本回调
    public interface GetPhoneContactsCallback {
        void onProgress(List<StPhoneBook> stPhoneBooks); // 进度

        void onFailue(int errorCode); // 错误码

        void onSuccess(List<StPhoneBook> stPhoneBooks); // 获取成功，最后结果一次返回
    }

    /**
     * 设置所有的电话号码
     *
     * @param stPhoneBooks
     */
    public void setAllPhoneBooks(List<StPhoneBook> stPhoneBooks) {
        mStAllPhoneBooks.clear();
        if (null != stPhoneBooks) {
            mStAllPhoneBooks.addAll(stPhoneBooks);
        }
        EventBus.getDefault().post(new EventClassDefine.EventBtContactChange(new ArrayList<StPhoneBook>(mStAllPhoneBooks))); // 通知关心的UI页面，刷新UI
    }

    /**
     * 更新数据库
     *
     * @param stPhoneBook
     */
    public void updatePhoneBooks(StPhoneBook stPhoneBook) {
        for (int i = 0; i < mStAllPhoneBooks.size(); ++i) {
            if (mStAllPhoneBooks.get(i).equals(stPhoneBook)) {
                mStAllPhoneBooks.set(i, stPhoneBook);
                break;
            }
        }
        PhoneBookDb.getInstance().update(stPhoneBook); // 更新数据库中的数据
//      不用反复刷新
        EventBus.getDefault().post(new EventClassDefine.EventBtContactChange(new ArrayList<>(mStAllPhoneBooks))); // 通知关心的UI页面，刷新UI
    }

    public List<StPhoneBook> getAllPhoneBooks() {
        List<StPhoneBook> stPhoneBooks = new ArrayList<>();
        stPhoneBooks.addAll(mStAllPhoneBooks);
        return stPhoneBooks;
    }

    /**
     * 同步数据库联系人信息
     */
    public void getPhoneBook() {
        setAllPhoneBooks(null); // 去获取联系人之前，先清空列表
        BluetoothDevice device = BluetoothCacheUtil.getInstance().getCurConnectedDevice();
        String addr = device.addr;
        String name = device.name;
        Logcat.d(" loadPhoneListFirst name =" + name +  ", addr =" + addr);
        //如果当前无设备连接（可能状态没更新到），直接返回，等待下次刷新
        if(TextUtils.isEmpty(addr)){
            return;
        }
        PhoneBookDb.getInstance().query(new PhoneBookDb.PhoneBookDbCallback() { // 从数据库获取数据
            @Override
            public void onResult(List<StPhoneBook> stPhoneBooks) {
                if (stPhoneBooks.size() == 0) {
                    onFailure();
                } else {
                    setAllPhoneBooks(stPhoneBooks); // 缓存电话本
                }
            }

            @Override
            public void onFailure() { // 失败了，直接去联系人获取
            }
        },addr);
    }

    private boolean mIsContactSyncFailured = false; // 电话本是否同步失败

    /**
     * 电话本是否之前蓝牙连接后同步失败过， 默认是false
     *
     * @return boolean
     */
    public boolean getIsContactSyncFailured() {
        Logcat.d("mIsContactSyncFailured:" + mIsContactSyncFailured);
        return mIsContactSyncFailured;
    }

    private IBluetoothVCardCallback.Stub mPhoneContastsBluetoothVCardCallback = new IBluetoothVCardCallback.Stub() {


        @Override
        public void onProgress(List<BluetoothVCardBook> books) throws RemoteException {
            Logcat.d("contacts onProgress books size : " + books.size() + " lastsize: " + mStPhoneBooks.size());
            List<StPhoneBook> stPhoneBooks = new ArrayList<>();
            for (BluetoothVCardBook book : books) {
                stPhoneBooks.add(BluetoothVCardBookToStPhoneBook(book));
            }
            if (!isAc8257_YQQD_DY801) { // 20210830 lyy 8257暂时分段回调的
                mStPhoneBooks.clear();
            }
            mStPhoneBooks.addAll(stPhoneBooks);
            if (null != mGetPhoneContactsCallback) {
                mGetPhoneContactsCallback.onProgress(stPhoneBooks);
            }
        }

        @Override
        public void onFailure(int errorCode) throws RemoteException {
            // 延时500毫秒检测是否同步过程中蓝牙断开导致下载电话本出错，该场景出错将会在下次蓝牙连接时强制重新下载电话本
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothCacheUtil.getInstance().getBluzConnectedStatus() == IVIBluetooth.BluetoothConnectStatus.DISCONNECTED) {
                        mIsContactSyncFailured = true;
                        Logcat.w("update contact but failured by bluetooth disconnected");
                    }
                }
            }, TIME_DELAY_CHECK_CONTACT_SYNC_FAILURED);
            if (null != mGetPhoneContactsCallback) {
                mGetPhoneContactsCallback.onFailue(errorCode);
            }
            // 下载联系人失败也要通过接口更新已下载的电话本,避免联系人列表没有缓存导致的界面交互问题（收藏、搜索、排序）
            Logcat.d("update data onFailure and ErrorCode: " + IVIBluetooth.BluetoothExecErrorMsg.getName(errorCode));
            handleResult();
//                    mBtManager.onEventVCard(new IVIBluetooth.EventVCard(IVIBluetooth.EventVCard.ON_FAILURE, errorCode, "", null));
        }

        @Override
        public void onSuccess(String msg) throws RemoteException {
            Logcat.d("Success msg: " + msg);
            handleResult();
//                    mBtManager.onEventVCard(new IVIBluetooth.EventVCard(IVIBluetooth.EventVCard.ON_FAILURE, -1, "", null));
        }

        private void handleResult() {
            // 对结果进行排序
            mSignalExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    List<StPhoneBook> stPhoneBooks = VBookUtil.sortPhoneBook(mStPhoneBooks);
                    Logcat.d("mStPhoneBooks.size:" + mStPhoneBooks.size());

//                    // 回复之前收藏状态
//                    for (StPhoneBook stPhoneBook : stPhoneBooks) {
//                        for (StPhoneBook lastPhoneBook : mStAllPhoneBooks) {
//                            if (stPhoneBook.equals(lastPhoneBook)) {
//                                stPhoneBook.isFavorite = lastPhoneBook.isFavorite;
//                                break;
//                            }
//                        }
//                    }
                    mStAllPhoneBooks.clear();
                    mStAllPhoneBooks.addAll(stPhoneBooks);

                    mHandler.post(new Runnable() { // 结果通过post到主线程回调
                        @Override
                        public void run() {
                            if (null != mGetPhoneContactsCallback) {
                                mGetPhoneContactsCallback.onSuccess(mStAllPhoneBooks);
                            }
                            mIsContactDownloading = false; // 电话本开始下载完成
                            Logcat.d("contact downloading finished");
                        }
                    });
                }
            });
        }
    };

    /**
     * 获取电话本，结果通过callback返回，返回的结果是通过拼音排序之后的结果
     *
     * @param callback
     */
    public void getPhoneContacts(GetPhoneContactsCallback callback) {
        mIsContactDownloading = true; // 电话本开始下载中
        mIsContactSyncFailured = false;  // 电话本先默认置为同步成功
        mGetPhoneContactsCallback = callback;
        Logcat.d("contacts downloading start");
        final BluetoothManager mBtManager = BtApplication.getInstance().getBluetoothManager();
        if (null != mBtManager) {
            mStPhoneBooks.clear();
            mBtManager.getPhoneContacts(mPhoneContastsBluetoothVCardCallback);
        }
    }

    /**
     * 添加一条历史通话记录
     *
     * @param stCallHistory
     */
    public void addCallHistory(final StCallHistory stCallHistory) {
//        if (mStAllCallHistorys.contains(stCallHistory)) { // 通话记录已经包含
//            return;
//        }

        for (StCallHistory call : mStAllCallHistorys){
        	if (stCallHistory.status == call.status
			 && TextUtils.equals(stCallHistory.name, call.name)
			&& TextUtils.equals(stCallHistory.phoneNumber, call.phoneNumber)
			&& TextUtils.equals(stCallHistory.time,call.time)){
        		return;
			}
		}
        Logcat.i("status =" + stCallHistory.status + ", name =" + stCallHistory.name + ", phoneNumber= " + stCallHistory.phoneNumber
		 + ", time=" + stCallHistory.time);

        mHistoryDb.addCallHistory(stCallHistory);
        mStAllCallHistorys.add(0, stCallHistory); // 将当前通话记录添加到第一条

		Logcat.i("mBluzHistoryCallback =" + mBluzHistoryCallback + ", size=" + mStAllCallHistorys.size() +
				 ", status=" + stCallHistory.status);
        if (null != mBluzHistoryCallback) { // callHistores列表，有可能出现现在在未接来电，刷新全部，会错乱的问题
            mBluzHistoryCallback.onUpdate();
        }
    }

    /**
     * 通过电话号码获取名字
     *
     * @param phoneNumber
     * @return
     */
    public String getContactName(String phoneNumber) {
        List<StPhoneBook> stPhoneBooks = getAllPhoneBooks();
        if (stPhoneBooks != null && !TextUtils.isEmpty(phoneNumber)) {
            for (StPhoneBook stPhoneBook : stPhoneBooks) {
                if (BluetoothModel.isPhoneNumberEquals(stPhoneBook.phoneNumber, phoneNumber)) {
                    return stPhoneBook.name;
                }
            }
        }
        return "";
    }

    private IBluetoothVCardCallback.Stub mCallRecordBluetoothVCardCallback = new IBluetoothVCardCallback.Stub() {
        @Override
        public void onProgress(List<BluetoothVCardBook> books) throws RemoteException {
            Logcat.d( "requestBluzModelAllHistory getAllCallRecord booksize "+books.size());
            int index = 0;
            long start = System.currentTimeMillis();
            for(BluetoothVCardBook book:books){
                Logcat.d("requestBluzModelAllHistory index =" + index + ", book =" + book.toString());
                index ++;
            }
            Logcat.d("logcat spend =" + (System.currentTimeMillis() - start));
            if (null != mBluzHistoryCallback) {
                List<StCallHistory> stCallHistories = BluetoothVCardBooksToStCallHistoryS(books);
                //fixme 这里8227l为一次性回调所有（不知道8257是否分段回调） @LBH 20201015
//                if (!isAc8257_YQQD_DY801) { // 20210827 lyy Ac8257_YQQD_DY801 暂时分段
                // 20210927 lyy Ac8257_YQQD_DY801一次回调全部
                    mStAllCallHistorys.clear();
//                }
                mStAllCallHistorys.addAll(stCallHistories);
                if (!isAc8257_YQQD_DY801) { // 20210827 lyy Ac8257_YQQD_DY801从数据库查询时已排序
                    //过程增加排序
                    Collections.sort(mStAllCallHistorys, new Comparator<StCallHistory>() {
                        @Override
                        public int compare(StCallHistory t1, StCallHistory t2) {
                            if (null != t1 && null != t2) {
                                return t2.sort.compareTo(t1.sort);
                            }
                            return 0;
                        }
                    });
                }
                mBluzHistoryCallback.onProgress(mStAllCallHistorys);
            }
        }

        @Override
        public void onFailure(int errorCode) throws RemoteException {
            Logcat.d( "requestBluzModelAllHistory errorCode "+errorCode);
            queryAllHistoryFinish();
        }

        @Override
        public void onSuccess(String msg) throws RemoteException {
            Logcat.d( "requestBluzModelAllHistory onSuccess "+msg);
            queryAllHistoryFinish();
        }
    };
    /**
     * 获取蓝牙模块的历史记录
     */
    private void requestBluzModelAllHistory() {
        Logcat.d();
        if (null != mBluzHistoryCallback) {
            mBluzHistoryCallback.onShowProgress();
        }
        //test
//        requestReceivedPhonebook(); // 请求已接
//        requestDialedPhonebook();   // 请求已拨
//        requestMissedPhonebook();   // 请求未接

        if (null != BtApplication.getInstance().getBluetoothManager()) {
            BtApplication.getInstance().getBluetoothManager().getAllCallRecord(mCallRecordBluetoothVCardCallback);
        }
    }

    /**
     * 查找已接通话记录
     */
    private void requestReceivedPhonebook() {
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            BtApplication.getInstance().getBluetoothManager().getReceivedCallRecord(new IBluetoothVCardCallback.Stub() {
                @Override
                public void onProgress(List<BluetoothVCardBook> books) throws RemoteException {

                    if (null != mBluzHistoryCallback) {
                        List<StCallHistory> stCallHistories = BluetoothVCardBooksToStCallHistoryS(books,
                                IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS);
                        mStAllCallHistorys.addAll(stCallHistories);
                        mBluzHistoryCallback.onProgress(stCallHistories);
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.d("errorCode:" + errorCode);
                }

                @Override
                public void onSuccess(String msg) throws RemoteException {
                    Logcat.d("msg:" + msg);
                }
            });
        }
    }

    /**
     * 请求已拨电话本
     */
    private void requestDialedPhonebook() {
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            BtApplication.getInstance().getBluetoothManager().getDialedCallRecord(new IBluetoothVCardCallback.Stub() {
                @Override
                public void onProgress(List<BluetoothVCardBook> books) throws RemoteException {
                    if (null != mBluzHistoryCallback) {
                        List<StCallHistory> stCallHistories = BluetoothVCardBooksToStCallHistoryS(books,
                                IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS);
                        mStAllCallHistorys.addAll(stCallHistories);
                        mBluzHistoryCallback.onProgress(stCallHistories);
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.d("errorCode:" + errorCode);
                }

                @Override
                public void onSuccess(String msg) throws RemoteException {
                    Logcat.d("msg:" + msg);
                }
            });
        }
    }

    /**
     * 请求未接
     */
    private void requestMissedPhonebook() {
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            BtApplication.getInstance().getBluetoothManager().getMissedCallRecord(new IBluetoothVCardCallback.Stub() {
                @Override
                public void onProgress(List<BluetoothVCardBook> books) throws RemoteException {
                    if (null != mBluzHistoryCallback) {
                        List<StCallHistory> stCallHistories = BluetoothVCardBooksToStCallHistoryS(books,
                                IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS);
                        mStAllCallHistorys.addAll(stCallHistories);
                        mBluzHistoryCallback.onProgress(stCallHistories);
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.d("errorCode:" + errorCode);
                    queryAllHistoryFinish();
                }

                @Override
                public void onSuccess(String msg) throws RemoteException {
                    Logcat.d("msg:" + msg);
                    queryAllHistoryFinish();
                }
            });
        }
    }

    /**
     * 查询完成
     */
    private void queryAllHistoryFinish() {
        Collections.sort(mStAllCallHistorys, new Comparator<StCallHistory>() {
            @Override
            public int compare(StCallHistory t1, StCallHistory t2) {
                if (null != t1 && null != t2) {
                    return t2.sort.compareTo(t1.sort);
                }
                return 0;
            }
        });
        if (null != mBluzHistoryCallback) {
            mBluzHistoryCallback.onFinish(mStAllCallHistorys);
        }
        mIsHistoryDownloading = false; // 通话记录下载完成
        Logcat.d("history downloading finish" + (mStAllCallHistorys == null ?"0":mStAllCallHistorys.size()));
        mHistoryDb.insert(mStAllCallHistorys); // 将所有通话记录缓存入数据库
    }

    /**
     * 获取一条通话记录，通过状态，电话号码，联系人名字
     *
     * @param status
     * @param phoneNumber
     * @param contactName
     * @return
     */
    public StCallHistory getCallHistory(int status, String phoneNumber, String contactName) {
        StCallHistory stCallHistory = new StCallHistory();
        stCallHistory.name = contactName; // 名字为空，显示未知
        stCallHistory.phoneNumber = phoneNumber.trim();
        stCallHistory.time = getSystemTime();
        Logcat.d("time "+stCallHistory.time);
        stCallHistory.sort = stCallHistory.time.replace("/", "").replace(" ", "");
        stCallHistory.status = status;
        stCallHistory.addr = BluetoothCacheUtil.getInstance().getCurConnectDevice().addr;
        return stCallHistory;
    }

    /**
     * 获取系统时间，以天为单位
     *
     * @return
     */
    private String getSystemTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss", Locale.US);//设置日期格式
        return df.format(new Date());
    }

    /**
     * 将 BluetoothVCardBook 类型转换成 StCallHistory
     *
     * @param book
     * @param type 是呼出还是呼入还是未接
     * @return
     */
    private StCallHistory BluetoothVCardBookToStCallHistory(BluetoothVCardBook book, int type) {
        StCallHistory stCallHistory = new StCallHistory();
        if (null != book) {
            stCallHistory.phoneNumber = book.phoneNumber.trim();
            stCallHistory.name = getContactName(stCallHistory.phoneNumber); // 模块有时候有问题，模块返回的号码和名字是匹配的，所以全部通过号码查询数据库
//            Logcat.d("stCallHistory.phoneNumber:" + stCallHistory.phoneNumber + " name:" + stCallHistory.name + " calltype： " + book.type + " curtype: " + type);
            stCallHistory.status = type;
            if (!TextUtils.isEmpty(book.callTime)) {
                if (book.callTime.length() >= TIME_MAX_LEN) {
                    stCallHistory.time = book.callTime.substring(0, 4) + "/" +
                            book.callTime.substring(4, 6) + "/" +
                            book.callTime.substring(6, 8) + "  " +
                            book.callTime.substring(9, 11) + ":" +
                            book.callTime.substring(11, 13) + ":" +
                            book.callTime.substring(13, 15);
                    stCallHistory.sort = book.callTime.replace("T", "");
                }
            }
            stCallHistory.addr = BluetoothCacheUtil.getInstance().getCurConnectDevice().addr;
        }
        return stCallHistory;
    }

    /**
     * 将 BluetoothVCardBook 类型转换成 StCallHistory
     *
     * @param book
     * @return
     */
    private StCallHistory BluetoothVCardBookToStCallHistory(BluetoothVCardBook book) {
        if (book != null) {
            int type;
            if (TextUtils.equals(book.type, MISS_TYPE)) {
                type = IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS;
            } else if (TextUtils.equals(book.type, RECE_TYPE)) {
                type = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS;
            } else if (TextUtils.equals(book.type, DIAL_TYPE)) {
                type = IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS;
            } else {
                type = IVIBluetooth.BluetoothCallHistoryStatus.UNKNOWN_STATUS;
            }
            return BluetoothVCardBookToStCallHistory(book, type);
        } else {
            return BluetoothVCardBookToStCallHistory(book, IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS);
        }
    }

    /**
     * 批量将 List<BluetoothVCardBook> 转换成 List<StCallHistory>
     *
     * @param vCardBooks
     * @param type
     * @return
     */
    private List<StCallHistory> BluetoothVCardBooksToStCallHistoryS(List<BluetoothVCardBook> vCardBooks, int type) {
        List<StCallHistory> stCallHistories = new ArrayList<>();
        for (BluetoothVCardBook book : vCardBooks) {
            stCallHistories.add(BluetoothVCardBookToStCallHistory(book, type));
        }
        return stCallHistories;
    }

    /**
     * 批量将 List<BluetoothVCardBook> 转换成 List<StCallHistory>，内部自动识别type
     *
     * @param vCardBooks
     * @return
     */
    private List<StCallHistory> BluetoothVCardBooksToStCallHistoryS(List<BluetoothVCardBook> vCardBooks) {
        List<StCallHistory> stCallHistories = new ArrayList<>();
        for (BluetoothVCardBook book : vCardBooks) {
            stCallHistories.add(BluetoothVCardBookToStCallHistory(book));
        }
        return stCallHistories;
    }

    /**
     * BluetoothVCardBook 转换成 StPhoneBook
     *
     * @param book
     * @return
     */
    private StPhoneBook BluetoothVCardBookToStPhoneBook(BluetoothVCardBook book) {
        StPhoneBook stPhoneBook = new StPhoneBook();
        stPhoneBook.name = book.name;
        stPhoneBook.phoneNumber = book.phoneNumber.replace("-", "").trim(); // "-" 去掉
        List<String> pinyins = HanziToPinyinUtil.getPinYinList(stPhoneBook.name);
        for (String p : pinyins) {
            if (!TextUtils.isEmpty(p)) {
                stPhoneBook.pinyin += (p + " "); // 拼音
                stPhoneBook.firstLetter += ("" + p.charAt(0)); // 首字母
            }
        }
        return stPhoneBook;
    }

    /**
     * 第三方通话时的操作接口
     *
     * @param action {@link com.jancar.sdk.bluetooth.IVIBluetooth.ThreePartyCallCtrl}
     */
    public void threePartyCallCtrl(int action) {
        Logcat.d(" action : " + action);
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            BtApplication.getInstance().getBluetoothManager().threePartyCallCtrl(action, new IBluetoothExecCallback.Stub() {
                @Override
                public void onSuccess(String msg) throws RemoteException {
                    Logcat.d("msg:" + msg);
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.w("errorCode:" + errorCode);
                }
            });
        }
    }

    /**
     * 设置当前是否是第三方通话
     *
     * @param isThreeTalking
     */
    public synchronized void setThreeTalking(boolean isThreeTalking) {
        mIsThreeTalking = isThreeTalking;
    }

    public synchronized boolean isThreeTalking() {
        return mIsThreeTalking;
    }

    public synchronized void setThreeInComing(boolean isThreeIncoming) {
        mIsThreeIncoming = isThreeIncoming;
    }

    public synchronized boolean isThreeIncoming() {
        return mIsThreeIncoming;
    }

    /**
     * 设置是否第三方去电状态
     *
     * @param isThreeOutGoing
     */
    public synchronized void setThreeOutGoing(boolean isThreeOutGoing) {
        mIsThreeOutGoing = isThreeOutGoing;
    }

    public synchronized boolean isThreeOutGoing() {
        return mIsThreeOutGoing;
    }

    /**
     * 设置当前通话的号码
     *
     * @param phoneNumber
     */
    public synchronized void setTalkingNumber(String phoneNumber) {
        mTalkingNumber = phoneNumber;
    }

    public synchronized String getTalkingNumber() {
        return mTalkingNumber;
    }

    /**
     * 停止所有下载任务
     */
    public void stopContactOrHistoryLoad(){
        Logcat.d();
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return;
        }
        BtApplication.getInstance().getBluetoothManager().stopContactOrHistoryLoad(new IBluetoothExecCallback.Stub() {
            @Override
            public void onSuccess(String msg) throws RemoteException {
                Logcat.d("stopContactOrHistoryLoad msg:" + msg);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.w("stopContactOrHistoryLoad errorCode:" + errorCode);
            }
        });
    }

    public boolean isPowerOn() {
        boolean state = false;
        if (null != BtApplication.getInstance().getBluetoothManager()) {
            state = BtApplication.getInstance().getBluetoothManager().isPowerOn();
        } else {
            Logcat.d();
        }
        return state;
    }

    public boolean setPower(boolean state) {
        Logcat.d("state: " + state);
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return false;
        }
        if (state) {
            BtApplication.getInstance().getBluetoothManager().powerOn();
        } else {
            BtApplication.getInstance().getBluetoothManager().powerOff();
        }
        return true;
    }

    public boolean setAutoLink(boolean state) {
        Logcat.d("state: " + state);
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return false;
        }
        BtApplication.getInstance().getBluetoothManager().setAutoLink(state, null);
        return true;
    }

    public boolean setAutoListen(boolean state) {
        Logcat.d("state: " + state);
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return state;
        }
//        BtApplication.getInstance().getBluetoothManager().setAutoListen(state, null);
        return state;
    }

    public void modifyModuleName(String name) {
        Logcat.d("name: " + name);
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return;
        }
        BtApplication.getInstance().getBluetoothManager().modifyModuleName(name, null);
    }

    public void modifyModulePIN(String pin) {
        Logcat.d("pin: " + pin);
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return;
        }
        BtApplication.getInstance().getBluetoothManager().modifyModulePIN(pin, null);
    }

    public boolean isAutoLinkOn() {
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return false;
        }
        return BtApplication.getInstance().getBluetoothManager().isAutoLinkOn();
    }

    public String getBluetoothName() {
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return "";
        }
        return BtApplication.getInstance().getBluetoothManager().getBluetoothName();
    }

    public String getBluetoothPin() {
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            return "";
        }
        return BtApplication.getInstance().getBluetoothManager().getBluetoothPin();
    }

    /**
     * 从数据库删除一条通话记录
     */
    public void delete(StCallHistory stCallHistory) {
        mStAllCallHistorys.remove(stCallHistory);
//        if (null != mBluzHistoryCallback) {
//            mBluzHistoryCallback.onFinish(mStAllCallHistorys);
//        }
        Logcat.d("history delete finish" + (mStAllCallHistorys == null ? "0" : mStAllCallHistorys.size()));
        String clause = getFieldName("phoneNumber=? and ") + getFieldName("time=?");
        int affects = mHistoryDb.delete(StCallHistory.class, clause, new String[]{stCallHistory.phoneNumber, stCallHistory.time}); // 从数据库删除
        Logcat.d("affects: " + affects);
    }

    /**
     * 从数据库删除一个联系人电话
     */
    public void delete(StPhoneBook phoneBook) {
        mStAllPhoneBooks.remove(phoneBook);
        Logcat.d("contact delete finish" + (mStAllPhoneBooks == null ? "0" : mStAllPhoneBooks.size()));
        String clause = getFieldName("phoneNumber=? and ") + getFieldName("name=?");
        int affects = PhoneBookDb.getInstance().delete(StPhoneBook.class, clause, new String[]{phoneBook.phoneNumber, phoneBook.name}); // 从数据库删除
        Logcat.d("affects: " + affects);
    }
    /**
     * 获取数据库字段名
     *
     * @param name 数据库字段名
     * @return
     */
    protected String getFieldName(String name) {
        if (TextUtils.equals(name, "order")) {
            return "_" + name;
        }
        return name;
    }

    public void getBluetoothModuleStatus(IBluetoothStatusCallback.Stub callback) {
        if(BtApplication.getInstance().getBluetoothManager() == null){
            Logcat.d("getBluetoothManager is null");
            if (null != callback) {
                try {
                    callback.onFailure(0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        BtApplication.getInstance().getBluetoothManager().getBluetoothModuleStatus(callback);
    }
}
