package com.jancar.bluetooth.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.jancar.bluetooth.adapter.CollectionAdapter;
import com.jancar.bluetooth.contract.CollectionContract;
import com.jancar.bluetooth.model.CollectionModel;
import com.jancar.bluetooth.model.CollectionRepository;
import com.jancar.bluetooth.utils.Constants;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.presenter.BaseModelPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:10:01
 */
public class CollectionPresenter extends BaseModelPresenter<CollectionContract.View, CollectionModel> implements CollectionContract.Presenter, CollectionModel.Callback {

    private volatile List<StPhoneBook> mCollectList;
    private CollectionAdapter mAdapter;

    @Override
    public CollectionModel createModel() {
        return new CollectionRepository(this);
    }

    @Override
    public CollectionAdapter getAdapter() {
        Logcat.d("getUi():" + getUi());
        if (getUi() != null) {
            Context uiContext = getUi().getUIContext();
            if (mAdapter == null && uiContext != null) {
                if (mCollectList == null) {
                    mCollectList = new ArrayList<>();
                }
                mAdapter = new CollectionAdapter(mCollectList);
                mAdapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //拨打号码
                        if (mCollectList != null && mCollectList.size() > 0) {
                            StPhoneBook phoneBook = mCollectList.get(position);
                            Logcat.d("call Num:" + phoneBook.phoneNumber);
                            if (phoneBook != null) {
                                String number = phoneBook.phoneNumber;
                                callCollectPhone(number);
                            }
                        }
                    }
                });
            }
        }
        return mAdapter;
    }

    /**
     * 收藏界面拨打号码
     *
     * @param number
     */
    private void callCollectPhone(String number) {
        if (!TextUtils.isEmpty(number) && number.length() >= Constants.ALL_SHORT_LEN) {
            BluetoothModelUtil.getInstance().callPhone(number);
        } else {
            Logcat.d("Number Nulll");
        }
    }

    @Override
    public void loadCollectListFirst() {
        getModel().loadCollectListFirst();
    }

    @Override
    public boolean isBtConnected() {
        return BluetoothCacheUtil.getInstance().getBluzConnectedStatus() == IVIBluetooth.BluetoothConnectStatus.CONNECTED;
    }

    @Override
    public void clearCollectList() {
        if (mAdapter != null && (mCollectList != null && mCollectList.size() > 0) && getUi().getUIContext() != null) {
            getUi().runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mCollectList.clear();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onSuccess(List<StPhoneBook> data) {
        updateCollectList(data);

    }

    @Override
    public void onFinish(int code, String msg, int listSize) {
        if (null != getUi()) {
            getUi().synchronousFinish(code, msg, listSize);
        }
    }

    /**
     * 更新List
     *
     * @param bookList
     */
    private synchronized void updateCollectList(final List<StPhoneBook> bookList) {
        if (getUi() != null && getUi().getUIContext() != null) {
            Logcat.d("updateCollectList:" + bookList.size());
            if (bookList != null && bookList.size() >= 0) {
                getUi().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCollectList != null) {
                            mCollectList.clear();
                            for (StPhoneBook stPhoneBook : bookList) {
                                mCollectList.add(stPhoneBook);
                            }
                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Logcat.d("Adapter Null");
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onUiDestroy(CollectionContract.View ui) {
        super.onUiDestroy(ui);
        mAdapter = null;
        mCollectList = null;
    }
}