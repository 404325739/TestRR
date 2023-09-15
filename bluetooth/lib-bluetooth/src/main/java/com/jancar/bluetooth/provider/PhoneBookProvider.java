package com.jancar.bluetooth.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.db.HistoryDb;
import com.jancar.bluetooth.db.PhoneBookDb;
import com.jancar.sdk.bluetooth.BluetoothModel;
import com.jancar.sdk.utils.Logcat;

/**
 * 提供电话本对外的数据接口，只允许外部查询，不能插入，删除，修改数据
 */

public class PhoneBookProvider extends ContentProvider {
    private final static int CONTACT = 1;
    private final static int CALL_HISTORY = 2;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private HistoryDb mHistoryDb = null;

    static {
        sUriMatcher.addURI(BluetoothModel.Provider.AUTHORITY, BluetoothModel.BluetoothTable.TABLE_CONTACT, CONTACT);
        sUriMatcher.addURI(BluetoothModel.Provider.AUTHORITY, BluetoothModel.BluetoothTable.TABLE_CALL_HISTORY, CALL_HISTORY);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Logcat.d("uriMatcher.match(uri) = " + sUriMatcher.match(uri));
        int matcher = sUriMatcher.match(uri);
        if (matcher == CONTACT) {
            return PhoneBookDb.getInstance().query(PhoneBookDb.getInstance().getTableName(StPhoneBook.class),
                    projection, selection, selectionArgs, sortOrder, "");
        } else if (matcher == CALL_HISTORY) {
            if (mHistoryDb == null) {
                mHistoryDb = new HistoryDb();
            }
            return mHistoryDb.query(mHistoryDb.getTableName(StCallHistory.class),
                    projection, selection, selectionArgs, sortOrder, "");
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // 电话本接口，不支持获取类型
        throw new UnsupportedOperationException("getType not yet implemented!");
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        // 不支持外部插入
        throw new UnsupportedOperationException("insert not yet implemented!");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        // 不支持外部删除
        throw new UnsupportedOperationException("delete not yet implemented!");
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        // 不支持外部更改数据
        throw new UnsupportedOperationException("update not yet implemented!");
    }
}
