package com.jancar.bluetooth.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.bluetooth.event.EventOnDelayTask;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.btservice.bluetooth.IBluetoothLinkDeviceCallback;
import com.jancar.btservice.bluetooth.IBluetoothStatusCallback;
import com.jancar.btservice.bluetooth.IDeviceCallback;
import com.jancar.sdk.BaseManager;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 蓝牙服务
 */
public abstract class BtBaseService extends Service implements BaseManager.ConnectListener {

    private BluetoothManager mIvIBtManager = null; // 蓝牙服务管理类
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_OPEN_BLUZ_MODULE: // 蓝牙模块打开成功
                    onBluzOpenSuccess();
                    break;
                case MSG_DELAY_LINK_DEVICE: // 连接最后一次设备
                    linkLastDevice();
                    break;
            }
        }
    };

    public class BtBinder extends Binder {
        public BtBaseService getService() {
            return BtBaseService.this;
        }
    }

    private BtBinder mBinder = new BtBinder();

    private static final int MSG_OPEN_BLUZ_MODULE = 0; // 打开蓝牙模块成功
    private static final int MSG_DELAY_LINK_DEVICE = 1; // 延时连接设备

    private static final int DELAY_LINK_DEVICE = 20 * 1000; // 20s一次，连接设备

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logcat.d();
        // 蓝牙管理工具类
        mIvIBtManager = new BluetoothManager(this, this);

        createPhoneCallWindowManager(); // 电话悬浮窗

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        removePhoneCallWindowManager(); // 销毁悬浮窗

        mIvIBtManager.closeBluetoothModule(null);
        mIvIBtManager.disconnect();
        BtApplication.getInstance().setBluetoothManager(null);

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onServiceConnected() {
        Logcat.d();
        BtApplication.getInstance().setBluetoothManager(mIvIBtManager);

        mIvIBtManager.openBluetoothModule(new IBluetoothExecCallback.Stub() {
            @Override
            public void onSuccess(String msg) throws RemoteException {
                Logcat.d("msg:" + msg);
                mHandler.sendEmptyMessage(MSG_OPEN_BLUZ_MODULE);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);
            }
        });
    }

    @Override
    public void onServiceDisconnected() {
        BtApplication.getInstance().setBluetoothManager(null);
    }

    /**
     * 设备打开成功
     */
    private void onBluzOpenSuccess() {
        Logcat.d("onBluzOpenSuccess!");
        mIvIBtManager.getBluetoothModuleStatus(new IBluetoothStatusCallback.Stub() {
            @Override
            public void onSuccess(int status, int hfpStatus, int a2dpStatus) throws RemoteException {
                Logcat.d("status:" + status);
                EventBus.getDefault().post(new EventClassDefine.EventBtStageChange(status == BluetoothAdapter.STATE_ON));
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {

            }
        });
        // 获取已配对设备
        mIvIBtManager.getPairedDevice(0, new IDeviceCallback.Stub() {
            @Override
            public void onSuccess(List<BluetoothDevice> BluetoothDevices, BluetoothDevice curBluetoothDevice) throws RemoteException {
                // 查找到已配对设备，传递给界面
                EventBus.getDefault().post(new EventClassDefine.EventBluetoothDevices(BluetoothDevices, curBluetoothDevice));

                // 缓存列表
                /*if (null != curBluetoothDevice) {
                    BluetoothCacheUtil.getInstance().setCurConnectDevice(curBluetoothDevice.name, curBluetoothDevice.addr);
                } else {
                    BluetoothCacheUtil.getInstance().setCurConnectDevice("", "");
                }*/
                Logcat.d("BluetoothDevices.size:" + BluetoothDevices.size());
                if (BluetoothDevices.size() != 0) { // 模块有时候会出错，会发0个配对设备上来，这时候以应用的为准
                    BluetoothCacheUtil.getInstance().setPairedDeviceList(BluetoothDevices);
                }
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);
            }
        });

        // 获取蓝牙设备名
        mIvIBtManager.getBluetoothName(new IBluetoothExecCallback.Stub() {
            @Override
            public void onSuccess(String msg) throws RemoteException {
                BluetoothCacheUtil.getInstance().setBluzDeviceName(msg);
                EventBus.getDefault().post(new EventClassDefine.EventModifyBluzName(msg));

                Logcat.d("msg:" + msg);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);
            }
        });

        // 获取蓝牙设备的pin码
        mIvIBtManager.getBluetoothPin(new IBluetoothExecCallback.Stub() {
            @Override
            public void onSuccess(String msg) throws RemoteException {
                BluetoothCacheUtil.getInstance().setBluzDevicePin(msg);
                EventBus.getDefault().post(new EventClassDefine.EventModifyBluzPin(msg));

                Logcat.d("msg:" + msg);
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);
            }
        });

        // 蓝牙服务开启的时候获取一次状态，防止主服务起的比应用早，应用起来不知道当前蓝牙状态的问题
        mIvIBtManager.getBluetoothState(new IBluetoothLinkDeviceCallback.Stub() {
            @Override
            public void onSuccess(int status, String addr, String name) throws RemoteException {
                Logcat.d("status:" + status + " addr:" + addr + " name:" + name);
                BluetoothCacheUtil.getInstance().setCurConnectDevice(status, name, addr);
                //20200907
                /*// 通知所有页面
                EventBus.getDefault().post(new IVIBluetooth.EventLinkDevice(status, addr, name));*/
            }

            @Override
            public void onFailure(int errorCode) throws RemoteException {
                Logcat.d("errorCode:" + errorCode);

                // 获取状态失败， 全部清空
                BluetoothCacheUtil.getInstance().setCurConnectDevice(IVIBluetooth.BluetoothConnectStatus.DISCONNECTED, "", "");
            }
        });
        // 主动获取一次电量
        mIvIBtManager.getDeviceBattery(null);

//        if (BluetoothCacheUtil.getInstance().getBluzAutoLink()) {
//            // 用户开启了自动连接，开机起来之后，自动连接蓝牙
//            linkLastDevice();
//        }
    }

    /**
     * 连接最后一次设备
     */
    private void linkLastDevice() {
        String lastAddr = BluetoothCacheUtil.getInstance().getCurConnectDevice().addr;
        if (null != mIvIBtManager && !TextUtils.isEmpty(lastAddr)) { // 连接最后一次设备
            mIvIBtManager.linkDevice(lastAddr, new IBluetoothExecCallback.Stub() {
                @Override
                public void onSuccess(String msg) throws RemoteException {
                    Logcat.d("msg:" + msg); // 连接成功，不管
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    Logcat.d("errorCode:" + errorCode);

                    if (errorCode != IVIBluetooth.BluetoothExecErrorMsg.ERROR_CONNECTED) {
                        // 连接失败，等待20S之后重新连接
                        mHandler.sendEmptyMessageDelayed(MSG_DELAY_LINK_DEVICE, DELAY_LINK_DEVICE);
                    }
                }
            });
        }
    }

    /**
     * 接收event断开信号
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventLinkDevice(IVIBluetooth.EventLinkDevice event) {
        if (null != event) {
            Logcat.d("event =" + event);
            BluetoothCacheUtil.getInstance().setCurConnectDevice(event.status, event.name, event.addr);
            if (event.status != IVIBluetooth.BluetoothConnectStatus.CONNECTED) {
            } else {
                BluetoothModelUtil.getInstance().getPhoneBook();    // 同步数据库联系人信息
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventDelayTask(EventOnDelayTask event) {
        if (null != event) {
            event.onTask(mHandler);
        }
    }

    /**
     * 创建悬浮窗
     */
    protected void createPhoneCallWindowManager() {
//        PhoneCallWindowManager.getInstance().init(this);
    }

    /**
     * 销毁悬浮窗
     */
    protected void removePhoneCallWindowManager() {
//        PhoneCallWindowManager.removeInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // 杀掉重启
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}
