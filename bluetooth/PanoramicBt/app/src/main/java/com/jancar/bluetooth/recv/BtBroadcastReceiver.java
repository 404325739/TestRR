package com.jancar.bluetooth.recv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.jancar.bluetooth.services.BtAppService;
import com.jancar.sdk.BaseManager;

/**
 * 接收广播，启动服务
 */

public class BtBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != intent) {
            String action = intent.getAction();

            if (TextUtils.equals(action, Intent.ACTION_BOOT_COMPLETED) ||
                    TextUtils.equals(action, BaseManager.JANCAR_BT_SERVICE_READY)) {
                // 开机启动服务
                Intent service = new Intent(context, BtAppService.class);
                context.startService(service);
            }
        }
    }
}
