package com.jancar.bluetooth;

import android.content.Context;
import android.content.Intent;

import com.jancar.bluetooth.services.BtAppService;
import com.jancar.bluetooth.ui.TranslucentActivity;
import com.jancar.bluetooth.utils.SpUtils;
import com.jancar.bluetooth.utils.ToastUtil;
import com.jancar.sdk.utils.Logcat;

import java.util.Locale;

/**
 * @author Tzq
 * @date 2020/1/9 19:10
 */
public class MyApplication extends BtApplication {
    public static int TAG_INDEX = -1;//用于保存语言切换时activity销毁重建的tab

    @Override
    public void onCreate() {
        super.onCreate();
        Logcat.d("++onCreate++");
        ToastUtil.getInstance().initToastUtil(this);
        SpUtils.getInstance().initSp(this);

        Intent intent = new Intent(this, TranslucentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        startService(new Intent(this, BtAppService.class));
    }
}
