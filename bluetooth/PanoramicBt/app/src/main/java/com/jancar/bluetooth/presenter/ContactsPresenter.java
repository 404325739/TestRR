package com.jancar.bluetooth.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.adapter.ContactsAdapter;
import com.jancar.bluetooth.contract.ContactsContract;
import com.jancar.bluetooth.model.ContactsModel;
import com.jancar.bluetooth.model.ContactsRepository;
import com.jancar.bluetooth.utils.Constants;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.widget.LetterSlideBar;
import com.ui.mvp.presenter.BaseModelPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:06:24
 */
public class ContactsPresenter extends BaseModelPresenter<ContactsContract.View, ContactsModel>
        implements ContactsContract.Presenter, ContactsModel.Callback {

    private static final String TAG = "ContactsPresenter";

    private ContactsAdapter mAdapter;
    private volatile List<StPhoneBook> mList;
    private boolean isDownLoading;
    private boolean isAc8257_YQQD_DY801;

    @Override
    public ContactsModel createModel() {
        return new ContactsRepository(this);
    }

    @Override
    public void onModelReady(ContactsModel model) {
        super.onModelReady(model);
        Logcat.d();
    }

    @Override
    public ContactsAdapter getAdapter() {
        Logcat.d("getUi():" + getUi());
        if (getUi() != null) {
            Context uiContext = getUi().getUIContext();
            if (mAdapter == null && uiContext != null) {
                if (mList == null) {
                    mList = new ArrayList<>();
                }
                isAc8257_YQQD_DY801 = AppUtils.isAc8257_YQQD_DY801Platform(uiContext);
                mAdapter = new ContactsAdapter(uiContext,mList, isAc8257_YQQD_DY801);
                mAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Logcat.d("ContactsAdapter ItemPos:" + position + "   ListSize:" + mList.size());
                        //拨打号码
                        if (mList != null && mList.size() > 0) {
                            if (isAc8257_YQQD_DY801) {
                                // 选中
                                mAdapter.notifyDataSetChanged();
                            } else {
                                callPhone(position);
                            }
                        }
                    }
                });
                initLetterSlideBar();
            }
        }
        return mAdapter;
    }

    @Override
    public void loadPhoneListFirst() {
        if (null != getModel()) {
            isDownLoading = true;
            getModel().loadPhoneListFirst();
        }
    }

    @Override
    public void searchPhoneList(String inputKey) {
        if (null != getModel()) {
            getModel().searchPhoneList(inputKey);
        }
    }

    @Override
    public void requestAllPhoneList() {
        if (null != getModel()) {
            getModel().requestAllPhoneList();
        }
    }

    @Override
    public void onSyncContact() {
        if (null != getModel()) {
            isDownLoading = true;
            clearContactList();
            getModel().onSyncContact();
        }
    }

    @Override
    public void clearContactList() {
        if (mAdapter != null && (mList != null && mList.size() > 0) && getUi() != null && getUi().getUIContext() != null) {
            getUi().runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mList.clear();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
        getModel().clearContactList();
    }

    @Override
    public int getBluConnStatus() {
        return BluetoothCacheUtil.getInstance().getBluzConnectedStatus();
    }

    @Override
    public boolean isBtConnected() {
        return getBluConnStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTED;
    }

    @Override
    public boolean isDownloading() {
        int state = getModel().getdownPhoneBookState();
        return state != 0;
    }

    @Override
    public int getDownloadState() {
        return getModel().getdownPhoneBookState();
    }

    @Override
    public void stopContactOrHistoryLoad() {
        getModel().stopContactOrHistoryLoad();
    }

    @Override
    public void callSelectedContact() {
        if (null != mAdapter) {
            callPhone(mAdapter.getSelectedPosition());
        }
    }

    @Override
    public void delSelectedContact() {
        if (null == mAdapter) {
            Logcat.d("mAdapter is null, return;");
            return;
        }
        StPhoneBook phoneBook = mAdapter.getItem(mAdapter.getSelectedPosition());
        if (null != phoneBook && null != mList && mList.size() > mAdapter.getSelectedPosition()) {
            Logcat.d("remove name:" + phoneBook.name);
            mList.remove(phoneBook);
            mAdapter.notifyDataSetChanged();
            BluetoothModelUtil.getInstance().delete(phoneBook);
        }
    }

    private void callPhone(int position) {
        if (null == mAdapter) {
            Logcat.d("mAdapter is null, return;");
            return;
        }
        StPhoneBook phoneBook = mAdapter.getItem(position);
        if (phoneBook != null) {
            Logcat.d("call Num:" + phoneBook.phoneNumber);
            String number = phoneBook.phoneNumber;
            if (!TextUtils.isEmpty(number) && number.length() >= Constants.ALL_SHORT_LEN) {
                BluetoothModelUtil.getInstance().callPhone(number);
            } else {
                Logcat.d("Not call");
            }
        }

    }

    @Override
    public void onSuccess(List<StPhoneBook> data) {
        Logcat.d("contact size :" + (data == null ? 0 : data.size()));
        isDownLoading = false;
        updatePhoneBook(data, true);
    }

    @Override
    public void onFinish(int code, String msg, int listSize) {
        isDownLoading = false;
        if (null != getUi()) {
            getUi().synchronousFinish(code, msg, listSize);
        }
    }


    @Override
    public void onProgress(List<StPhoneBook> stPhoneBookList) {
        Logcat.d("contact size :" + (stPhoneBookList == null ? 0 : stPhoneBookList.size()));
        isDownLoading = true;
        updatePhoneBook(stPhoneBookList, !isAc8257_YQQD_DY801); // 20210830 lyy 8257暂时分段回调的
    }

    /**
     * 更新联系人列表
     *
     * @param bookList 数据列表
     * @param isAll 是否全部数据
     */
    @Override
    public synchronized void updatePhoneBook(final List<StPhoneBook> bookList, boolean isAll) {
        boolean isConnect = isBtConnected();
        Logcat.d("size: " + bookList.size() + ", isConnect =" + isConnect);
        if(isConnect){
            if (getUi() != null && getUi().getUIContext() != null) {
                if (bookList != null && bookList.size() > 0) {
                    getUi().runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null == mList) {
                                mList = new ArrayList<>();
                            } else if (isAll) {
                                mList.clear();
                            }
                            mList.addAll(bookList);
                            if (mAdapter != null) {
                                Logcat.d("updatePhoneBook notifyDataSetChanged size =" + mList.size());
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        }else{
            Logcat.d("updatePhoneBook not connect , clear");
            clearContactList();
        }

    }

    @Override
    public void onUiDestroy(ContactsContract.View ui) {
        super.onUiDestroy(ui);
        mAdapter = null;
        mList = null;
    }

    private void initLetterSlideBar() {
        getUi().getLetterSlideBar().setOnLetterChangeLister(new LetterSlideBar.OnLetterChangeLister() {
            @Override
            public void onLetterChange(String letter, int position) {

            }

            @Override
            public String getPositionLetter(int position) {
                Logcat.d("mList.size(): "+mList.size() + ", position: " +position);
                if (mList.size() > position && position >= 0) {
                    String letter = mList.get(position).firstLetter.toUpperCase();
                    Logcat.d("letter: "+letter);
                    return letter;
                }
                return null;
            }

            @Override
            public int getLetterPosition(String letter) {
                int position = -1;
                for (int i = 0; i < mList.size(); i ++) {
                    if (mList.get(i).pinyin.toLowerCase().startsWith(letter.toLowerCase())) {
                        position = i;
                        break;
                    }
                }
                Logcat.d("letter: "+letter + ", position: " +position);
                return position;
            }
        });
    }
}