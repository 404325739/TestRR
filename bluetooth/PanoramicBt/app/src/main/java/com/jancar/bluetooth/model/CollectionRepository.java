package com.jancar.bluetooth.model;


import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.sdk.utils.Logcat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:10:01
 */
public class CollectionRepository implements CollectionModel {

    private Callback mCallback;
    private List<StPhoneBook> mCollectList = new ArrayList<>();

    public CollectionRepository(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void loadCollectListFirst() {
        List<StPhoneBook> allPhoneBooks = BluetoothModelUtil.getInstance().getAllPhoneBooks();
        if (allPhoneBooks != null && allPhoneBooks.size() > 0) {
            if (mCollectList != null) {
                mCollectList.clear();
                for (StPhoneBook phoneBook : allPhoneBooks) {
                    if (phoneBook.isFavorite) {
                        mCollectList.add(phoneBook);
                    }
                }
            }
        } else {
            Logcat.d("++loadCollectListFirst++ null");
            if (mCollectList != null) {
                mCollectList.clear();
            }
        }
        mCallback.onSuccess(mCollectList);
        mCallback.onFinish(0, "success", mCollectList.size());
    }
}
