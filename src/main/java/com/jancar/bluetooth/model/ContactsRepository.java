package com.jancar.bluetooth.model;


import android.text.TextUtils;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.db.PhoneBookDb;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.TimeUtil;
import com.jancar.bluetooth.utils.VBookUtil;
import com.jancar.bluetooth.contract.DownStateEvent;
import com.jancar.bluetooth.utils.PBDownLoadStateUtil;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:06:25
 */
public class ContactsRepository implements ContactsModel {
    private static final String TAG = "ContactsRepository";
    public static final String PER_DOWNPB_STATE = "persist.sys.btpb.downstate";//电话本下载状态，1表示正在下载电话簿，2表示下载通话记录，0表示空闲,此属性在服务启动时重置一次

    private Callback mCallback;
    private boolean isSyncStarting = false;
    private int iSize = 0;

    public ContactsRepository(Callback callback) {
        this.mCallback = callback;
    }


    @Override
    public void loadPhoneListFirst() {
        BluetoothDevice device = BluetoothCacheUtil.getInstance().getCurConnectedDevice();
        String addr = device.addr;
        String name = device.name;
        Logcat.d(" loadPhoneListFirst name =" + name +  ", addr =" + addr);
        //如果当前无设备连接（可能状态没更新到），直接返回，等待下次刷新
        if(TextUtils.isEmpty(addr)){
            return;
        }
        // 从数据库获取数据
        PhoneBookDb.getInstance().query(new PhoneBookDb.PhoneBookDbCallback() {
            @Override
            public void onResult(List<StPhoneBook> stPhoneBooks) {
                Logcat.d("++loadPhoneListFirst onSuccess++ :" + stPhoneBooks.size());
                if (stPhoneBooks.size() == 0) {
                    onFailure();
                } else {
                    // 缓存电话本
                    BluetoothModelUtil.getInstance().setAllPhoneBooks(stPhoneBooks);
                    mCallback.onSuccess(stPhoneBooks);
                    onFinish(stPhoneBooks.size());
                }
            }

            @Override
            public void onFailure() {
                // 失败了，直接去联系人获取
                requestPhoneBook();
            }
        },addr);
    }

    @Override
    public void searchPhoneList(String inputKey) {
        // 过滤电话本
        List<StPhoneBook> stPhoneBooks = null;
        // 当输入的字符超过三个时去联系人列表匹配号码
        if (!TextUtils.isEmpty(inputKey)/* && inputKey.length() >= SEEK_CONTACT_LEN*/) {
            ///*filterPhoneBookNumber*/
            stPhoneBooks = VBookUtil.filterPhoneBook(BluetoothModelUtil.getInstance().getAllPhoneBooks(), inputKey);
        }
        mCallback.onSuccess(stPhoneBooks);
    }

    @Override
    public void requestAllPhoneList() {
        List<StPhoneBook> allPhoneBooks = BluetoothModelUtil.getInstance().getAllPhoneBooks();
        mCallback.onSuccess(allPhoneBooks);
    }

    @Override
    public void onSyncContact() {
        Logcat.d();
        requestPhoneBook();
    }

    @Override
    public void clearContactList() {
        BluetoothModelUtil.getInstance().clearPhoneBookDb();
    }

    @Override
    public int getdownPhoneBookState() {
        return PBDownLoadStateUtil.getDownloadState();
    }

    @Override
    public void stopContactOrHistoryLoad() {
        BluetoothModelUtil.getInstance().stopContactOrHistoryLoad();
    }


    private BluetoothModelUtil.GetPhoneContactsCallback mGetPhoneContactsCallback = new BluetoothModelUtil.GetPhoneContactsCallback() {
        @Override
        public void onProgress(List<StPhoneBook> stPhoneBooks) {//同步进度
            iSize = stPhoneBooks.size();
            Logcat.d("++requestPhoneBook onProgress ++ :" + iSize);
            if (iSize != 0) {
                mCallback.onProgress(stPhoneBooks);
                DownStateEvent.postDownStateEvent(new DownStateEvent(DownStateEvent.STATE_DOWNNING,DownStateEvent.TYPE_DOWNPHONEBOOK,"(" + iSize + ")"));
            }
        }

        @Override
        public void onFailue(int errorCode) { //同步失败
            Logcat.d("++onFinish++errorCode " + errorCode);
            //超时 ，下载数为0 才视为失败 20200604lp
            boolean isFail = errorCode != IVIBluetooth.BluetoothExecErrorMsg.ERROR_TIMER_OUT
                    || (errorCode == IVIBluetooth.BluetoothExecErrorMsg.ERROR_TIMER_OUT && iSize <= 0);
            if (isFail) {
                mCallback.onFinish(errorCode, "Failure", iSize);
                isSyncStarting = false;
            }
        }

        @Override
        public void onSuccess(List<StPhoneBook> stPhoneBooks) {//同步成功
            Logcat.d("++requestPhoneBook onSuccess++ :" + stPhoneBooks.size());
            String time = TimeUtil.getCurSystemTime();
            DownStateEvent.postDownStateEvent(new DownStateEvent(DownStateEvent.STATE_FINISH,DownStateEvent.TYPE_DOWNPHONEBOOK,"(" + stPhoneBooks.size() + ")"));
            // 将数据缓存到数据库
            PhoneBookDb.getInstance().insert(stPhoneBooks, time);
            mCallback.onSuccess(stPhoneBooks);
            onFinish(stPhoneBooks.size());
            isSyncStarting = false;
        }
    };
    //防止多次下载 20200527
    private void requestPhoneBook() {
        Logcat.d("requestPhoneBook :" + isSyncStarting +", instance = " +this.hashCode());
//        if (isSyncStarting) {
//            return;
//        }
        isSyncStarting = true;
        iSize = 0;
        //下载联系人的时候先把通话记录清空，使下次进入通话记录界面主动向服务查询，同步最新 @LBH
        BluetoothModelUtil.getInstance().clearHistoryDb();
        //不管怎样，只要点击了下载，就要把本地联系人数据库清空,然后通知界面刷新
        BluetoothModelUtil.getInstance().clearPhoneBookDb();
        mCallback.onProgress(new ArrayList<StPhoneBook>());
        DownStateEvent.postDownStateEvent(new DownStateEvent(DownStateEvent.STATE_START,DownStateEvent.TYPE_DOWNPHONEBOOK,""));
        BluetoothModelUtil.getInstance().getPhoneContacts(mGetPhoneContactsCallback);
    }

    private void onFinish(int listSize) {
        Logcat.d("++onFinish++");
        mCallback.onFinish(0, "onSuccess", listSize);
    }
}
