//package com.jancar.bluetooth.ui;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.PixelFormat;
//import android.os.Build;
//import android.os.RemoteException;
//import android.text.TextUtils;
//import android.view.Gravity;
//import android.view.View;
//import android.view.WindowManager;
//import android.view.WindowManager.LayoutParams;
//
//import com.jancar.base.IntentAction;
//import com.jancar.bluetooth.BtApplication;
//import com.jancar.bluetooth.bean.StCallHistory;
//import com.jancar.bluetooth.core.BtConfig;
//import com.jancar.bluetooth.event.EventClassDefine;
//import com.jancar.bluetooth.event.EventNoiseCallStatusCheck;
//import com.jancar.bluetooth.event.EventOnNaviStart;
//import com.jancar.bluetooth.ui.widget.FloatWindowView;
//import com.jancar.bluetooth.utils.BluetoothCacheUtil;
//import com.jancar.bluetooth.utils.BluetoothModelUtil;
//import com.jancar.bluetooth.utils.PhoneWindowUtil;
//import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
//import com.jancar.sdk.audio.AudioParam;
//import com.jancar.sdk.audio.IVIAudio;
//import com.jancar.sdk.audio.IVIAudioManager;
//import com.jancar.sdk.bluetooth.BluetoothManager;
//import com.jancar.sdk.bluetooth.IVIBluetooth;
//import com.jancar.sdk.car.IVICar;
//import com.jancar.sdk.utils.AndroidAutoUtil;
//import com.jancar.sdk.utils.Logcat;
//import com.jancar.sdk.utils.TimerUtil;
//import com.jancar.utils.SystemUtil;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//
///**
// * 电话悬浮窗管理工具类，整个应用只存在一个，使用单例管理
// * 在服务中进行初始化，通过EventBus进行控制
// */
//public class PhoneCallWindowManager extends BroadcastReceiver {
//    private static final String PKG_NAME_ANDROID_AUTO = "com.google.android.projection.sink";
//
//    private FloatWindowView mSmallWindow = null; // 小悬浮窗View的实例
//    private LayoutParams mSmallWindowParams; // 小悬浮窗View的参数
//    private WindowManager mWindowManager; // 用于控制在屏幕上添加或移除悬浮窗
//    private Context mContext;
//    private StCallHistory mStCallHistory = null; // 缓存当前一条通话记录
//    private StCallHistory mStThreeCallHistory = null; // 缓存一条三方通话的记录
//    private IVIAudioManager mIVIAudioManager;
//
//    private int mTalkingStatus = IVIBluetooth.CallStatus.NORMAL; // 记录当前通话状态
//    private String mTalkingNumber = ""; // 记录当前通话的号码，不记录三方通话号码，由于模块在通话的过程中不会给出号码，所以做缓存
//    private TimerUtil mHangupTimerUtil = null;
//    private static final int HANGUP_HIDE_WINDOW_MS = 500; // 挂断之后，不马上消失页面，因为三方通话时，有多种情况，模块会先发挂断，再发接听，如果马上销毁页面，会闪界面
//
//    private TimerUtil mCallingTimerUtil = null; // 车机端拨打电话，刷出UI，但是存在小概率情况，手机没有拨打出去，通过定时器检测，如果手机没有拨打出去，则过几秒消失UI
//    private static final int CALLING_TEST_MS = 10 * 1000; // 10秒后开始检测手机是否没有拨打出去
//
//    private boolean mIsInPhoneStatus = false; // 是否通话状态，用于解决通话状态连接android auto导致状态错乱界面交互有误问题
//    private AndroidAutoUtil mAndroidAutoUtil; // Android Auto 功能辅助类
//    private TimerUtil mCheckNoiceCallStatusTimer; // 检测通话状态是否混乱导致出现蓝牙界面变成空界面屏幕触摸无作用问题
//    private static final int DELAY_CHECK_NOISE_CALL_STATUS_MS = 1000; // 1000ms后开始检测是否蓝牙状态混乱
//    private boolean mIsAndroidAutoInFrontend = false;
//
//    private static PhoneCallWindowManager pThis = null;
//
//    private PhoneCallWindowManager() {
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent != null) {
//            String action = intent.getAction();
//            Logcat.d("onReceive action:" + action);
//            if (TextUtils.equals(action, IntentAction.ACTION_ACTIVITY_CHANGED)) { // 接收到Activity切换广播
//                String pkgName = intent.getStringExtra("packageName");
//                Logcat.d("activity changed pkgName:" + pkgName);
//                if (TextUtils.equals(pkgName, PKG_NAME_ANDROID_AUTO)) {
//                    mIsAndroidAutoInFrontend = true;
//                } else {
//                    mIsAndroidAutoInFrontend = false;
//                }
//                switchScreenTaking(false);
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onNaviStart(EventOnNaviStart event) {
//        Logcat.d();
//        switchScreenTaking(true);
//    }
//
//    /**
//     * 传递服务的
//     *
//     * @param context
//     */
//    public void init(Context context) {
//        mContext = context;
//
//        mAndroidAutoUtil = new AndroidAutoUtil(context);
//        mIVIAudioManager = new IVIAudioManager(context, null, null); // 调音量接口
//
//        if (mContext != null) { // 监听Activity切换广播
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(IntentAction.ACTION_ACTIVITY_CHANGED);
//            mContext.registerReceiver(this, filter);
//        }
//    }
//
//    /**
//     * 创建PhoneCallWindowManager
//     *
//     * @return
//     */
//    public synchronized static PhoneCallWindowManager getInstance() {
//        if (null == pThis) {
//            pThis = new PhoneCallWindowManager();
//        }
//        return pThis;
//    }
//
//    public synchronized static void removeInstance() {
//        if (null != pThis) {
//            pThis.release();
//            pThis = null;
//        }
//    }
//
//    /**
//     * 电话状态发生改变
//     * 调用位置：BluzModelUtil callphone onSuccess
//     * BluetoothManager onCallStatus
//     * FloatWindowView hangup
//     *
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventPhoneStatus(IVIBluetooth.CallStatus event) {
//        boolean isAndroidAutoUx = (mAndroidAutoUtil != null && mAndroidAutoUtil.isAndroidAutoStatus() && mIsAndroidAutoInFrontend);
//        if (null != event && (mIsInPhoneStatus || !isAndroidAutoUx)) {  // 非 Android Auto连接状态 或者 已经在通话状态 才处理event消息
//            mIsInPhoneStatus = true; // 只要进入这个代码段，说明进入了或即将进入通话状态
//            mTalkingStatus = event.mStatus;
//            if ((event.mStatus == IVIBluetooth.CallStatus.TALKING
//                    || event.mStatus == IVIBluetooth.CallStatus.OUTGOING) // 去电状态，有时候模块会发送两次去电状态，一次没号码，导致会闪UI
//                    && TextUtils.isEmpty(event.mPhoneNumber)) { // 通话中，有些模块不会给出通话号码，做一下缓存
//                event.mPhoneNumber = mTalkingNumber;
//            }
//            stopCallingUITestTimer(); // 停止检测拨打电话UI是否需要消失的定时器
//            Logcat.d("status " + event.getName() + " phone:" + event.mPhoneNumber);
//            String contactName = BluetoothModelUtil.getInstance().getContactName(event.mPhoneNumber); // 获取联系人名字
//            Logcat.d("contactName:" + contactName);
//            if (event.mStatus == IVIBluetooth.CallStatus.NORMAL ||
//                    event.mStatus == IVIBluetooth.CallStatus.HANGUP) {
//
//            } else {
//                if (mHangupTimerUtil != null) {
//                    mHangupTimerUtil.stop();
//                }
//                createSmallWindow(event.mPhoneNumber, event.mStatus);
//                updateCallStatusText(event.mStatus, event.mPhoneNumber, contactName); // 刷新UI
//            }
//
//            if (event.mStatus == IVIBluetooth.CallStatus.THREE_TALKING ||
//                    event.mStatus == IVIBluetooth.CallStatus.TALKING ||
//                    event.mStatus == IVIBluetooth.CallStatus.THREE_OUTGOING) { // 当前通话的号码
//                BluetoothModelUtil.getInstance().setTalkingNumber(event.mPhoneNumber);
//            }
//
//            switch (event.mStatus) {
//                case IVIBluetooth.CallStatus.INCOMING: // 来电
//                    if (BluetoothCacheUtil.getInstance().getBluzAutoListen()) {
//                        // 自动应答
//                        BluetoothModelUtil.getInstance().listenPhone();
//                    }
//                    // 来电，未接状态
//                    mStCallHistory = BluetoothModelUtil.getInstance().getCallHistory(
//                            IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS, event.mPhoneNumber, contactName);
//
//                    mTalkingNumber = event.mPhoneNumber;
//                    break;
//
//                case IVIBluetooth.CallStatus.OUTGOING: // 去电
//                    BluetoothModelUtil.getInstance().setCallNumber(event.mPhoneNumber); // 记录拨出的号码
//                    if (!event.mInsertSql) { // 车机端拨打电话，有时候存在一种情况，手机没有卡，UI已经刷出来，检测2S，如果此时还没有UI过来，则隐藏
//                        startCallingUITestTimer();
//                    } else if (!TextUtils.isEmpty(event.mPhoneNumber) && mStThreeCallHistory == null) { // 有时候第三方通话结束后会发 outgoing ，但是此时，实际是上一次的通话，不需要记录数据库
//                        insertPhoneHistoryToDb(IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName);
//                    }
//                    mTalkingNumber = event.mPhoneNumber;
//                    break;
//
//                case IVIBluetooth.CallStatus.TALKING: // 通话中
//                    BluetoothModelUtil.getInstance().setThreeOutGoing(false); // 变成单方通话
//                    if (mStCallHistory != null && mStCallHistory.status == IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS) { // 通话了，未接状态变成已接状态
//                        mStCallHistory.status = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS; // 已接状态
//                    }
//                    break;
//
//                case IVIBluetooth.CallStatus.THREE_OUTGOING: // 三方去电
//                    BluetoothModelUtil.getInstance().setThreeOutGoing(true);
//                    BluetoothModelUtil.getInstance().setThreeTalking(true);
//                    insertPhoneHistoryToDb(IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName); // 第三方去电，也缓存到数据库中
//                    mStThreeCallHistory = BluetoothModelUtil.getInstance().getCallHistory(
//                            IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName); // 记录当前状态是三方去电状态
//                    break;
//
//                case IVIBluetooth.CallStatus.THREE_INCOMING: // 三方来电
//                    mStThreeCallHistory = BluetoothModelUtil.getInstance().getCallHistory(
//                            IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS, event.mPhoneNumber, contactName);
//                    BluetoothModelUtil.getInstance().setThreeTalking(true);
//                    BluetoothModelUtil.getInstance().setThreeOutGoing(false);
//                    break;
//
//                case IVIBluetooth.CallStatus.THREE_TALKING: // 三方通话
//                    if (mStThreeCallHistory != null &&
//                            mStThreeCallHistory.status != IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS) { // 去电状态不需要改变成接听状态
//                        mStThreeCallHistory.status = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS; // 已接状态
//                    }
//                    BluetoothModelUtil.getInstance().setThreeTalking(true);
//                    break;
//
//                case IVIBluetooth.CallStatus.RETAIN: // 保持状态
//                    // 该处标记不一定是真的不是第三方去电状态，因为获取第三方去电的位置是 THREE_TALKING 位置，如果 保持状态和三方通话状态同时存在，则认为已经非去电状态
//                    BluetoothModelUtil.getInstance().setThreeOutGoing(false);
//                    break;
//
//                case IVIBluetooth.CallStatus.NORMAL: // 常态
//                case IVIBluetooth.CallStatus.HANGUP: // 挂断
//                    if (BluetoothModelUtil.getInstance().isThreeTalking()) {
//                        BluetoothModelUtil.getInstance().setThreeTalking(false);
//                    } else {
//                        if (mHangupTimerUtil == null) {
//                            mHangupTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {
//                                @Override
//                                public void timeout() {
//                                    mHangupTimerUtil.stop();
//                                    onPostHangup(); // 静mic场景下手动恢复mic为默认状态
//                                    removeSmallWindow();
//                                    if (mStCallHistory != null && // 挂断之后，如果是未接或者已接将通话记录插入数据库中
//                                            mStCallHistory.status != IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS) { // 去电状态在打电话时就已经记录
//                                        insertPhoneHistoryToDb(mStCallHistory);
//                                    }
//                                    if (mStThreeCallHistory != null &&
//                                            mStThreeCallHistory.status != IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS) { // 第三方通话在第一次通话之后，加载到后面
//                                        insertPhoneHistoryToDb(mStThreeCallHistory);
//                                    }
//                                    mStCallHistory = null;
//                                    mStThreeCallHistory = null;
//                                }
//                            });
//                        }
//                        mHangupTimerUtil.start(HANGUP_HIDE_WINDOW_MS);
//                        mIsInPhoneStatus = false; // 挂断电话将不在通话状态
//                    }
//                    break;
//            }
//
//            if (mSmallWindow != null) { // 刷新通话状态
//                mSmallWindow.setCallType(event.mStatus, event.mPhoneNumber);
//            }
//        } else {
//            Logcat.d("android auto connect status:" + isAndroidAutoUx);
//        }
//    }
//
//    /**
//     * 语音切换
//     * 调用位置：BluetoothManager onVoiceChange
//     *
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventVoiceChange(IVIBluetooth.EventVoiceChange event) {
//        if (event != null && mSmallWindow != null) {
//            boolean isVoiceOnPhone = (event.type == IVIBluetooth.BluetoothAudioTransferStatus.PHONE_STATUS);
//            Logcat.d("updateCallStatusAudio , isOnPhone = " + isVoiceOnPhone);
//            mSmallWindow.setVoiceChanged(isVoiceOnPhone);
//        } else {
//            Logcat.w("event is null or mSmallWindown: " + mSmallWindow);
//        }
//    }
//
//    /**
//     * 监听打电话过程中，是否有断开连接
//     *
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventLinkDevice(IVIBluetooth.EventLinkDevice event) {
//        if (event != null) {
//            if (!event.isConnected()) { // 打电话过程中断开连接
//                removeSmallWindow();
//            }
//        }
//    }
//
//    // 检测是否所有界面都隐藏了，如果是的话，那么就出现蓝牙状态混乱了，需要强制移除悬浮窗避免导致屏幕触摸无反应问题
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventNoiseCallStatusCheck(EventNoiseCallStatusCheck event) {
//        if (null == mCheckNoiceCallStatusTimer) {
//            mCheckNoiceCallStatusTimer = new TimerUtil(new TimerUtil.TimerCallback() {
//                @Override
//                public void timeout() {
//                    stopCheckNoiceCallStatusTimer(); // 先停止定时器
//                    if (null != mSmallWindow && mSmallWindow.isNoiseCallStatus()) { // 检测悬浮窗界面是否为空界面
//                        Logcat.d("empty float view and remove window");
//                        onEventPhoneStatus(new IVIBluetooth.CallStatus(IVIBluetooth.CallStatus.HANGUP, "", false)); // 模拟一个伪挂断消息出去
//                    }
//                }
//            }, true);
//        }
//        mCheckNoiceCallStatusTimer.start(DELAY_CHECK_NOISE_CALL_STATUS_MS);
//    }
//
//    /**
//     * 刷新通话状态的信息
//     *
//     * @param status      状态
//     * @param phoneNumber 电话号码
//     * @param contactName 联系人名字
//     */
//    public void updateCallStatusText(int status, String phoneNumber, String contactName) {
//        if (mSmallWindow != null) {
////			mSmallWindow.setCallType(status, phoneNumber);
//            Logcat.d("status:" + IVIBluetooth.CallStatus.getName(status) + " phoneNumber:" + phoneNumber + " contactName:" + contactName);
//
//            // 注：必须先刷新号码，再刷新名字，第三方通话，号码和名字是用同一个控件
//            if (phoneNumber != null) {
//                mSmallWindow.updateNumber(status, phoneNumber);
//            }
//            if (contactName != null) {
//                mSmallWindow.updateName(status, contactName);
//            }
//        }
//    }
//
//    /**
//     * 刚刚开机的时候，联系人还没有获取到，马上就有电话过来，此时UI上没有刷新联系人名字
//     * 当联系人获取到后，此时通过接受联系人改变的event，刷新UI上的名字
//     *
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventBtContactChange(EventClassDefine.EventBtContactChange event) {
//        if (mSmallWindow != null && !TextUtils.isEmpty(mTalkingNumber)) {
//            String contactName = BluetoothModelUtil.getInstance().getContactName(mTalkingNumber); // 获取联系人名字
//            Logcat.d("contactName:" + contactName + " mTalkingNumber:" + mTalkingNumber);
//            if (!TextUtils.isEmpty(contactName)) {
//                mSmallWindow.updateName(mTalkingStatus, contactName); // 刷新联系人名字
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onCcdChanged(IVICar.Ccd ccd) {
//        Logcat.d("ccd:" + ccd.mStatus);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//            if (mSmallWindow != null) {
//                mSmallWindow.setCcdStatus(ccd.isOn());
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventMuteChanged(IVIAudio.EventMuteChanged event) {
//        if (mSmallWindow != null && event != null) {
//            mSmallWindow.updateMuteIcon(event.mMute);
//        }
//    }
//
//    private void switchScreenTaking(final boolean isNeedNaviStart) {
//        String pkgName = SystemUtil.getTopPackageName(mContext);
//        boolean isStartNavi = isNeedNaviStart;
//        if (!TextUtils.isEmpty(pkgName)) {
//            if (mSmallWindow != null) {
//                boolean isNeedFullScreen = mSmallWindow.isNeedFullScreen(pkgName);
//                Logcat.d("isNeedFullScreen:" + isNeedFullScreen + " packageName:" + pkgName);
//                mSmallWindow.switchScreenTaking(isNeedFullScreen); // 页面之间切换
//                isStartNavi = (isNeedFullScreen && isNeedNaviStart); // 非地图界面且需要启动地图app
//            }
//        }
//        Logcat.w("pkgName: " + pkgName + " isStartNavi:" + isStartNavi);
//        if (isStartNavi) {
//            BtApplication.getInstance().startNavigationApp();
//        }
//    }
//
//    private void release() {
//        EventBus.getDefault().unregister(this);
//        if (mIVIAudioManager != null) {
//            mIVIAudioManager.disconnect();
//        }
//        if (mContext != null) {
//            mContext.unregisterReceiver(this);
//            mContext = null;
//        }
//        mAndroidAutoUtil = null;
//    }
//
//    private void stopCheckNoiceCallStatusTimer() {
//        if (mCheckNoiceCallStatusTimer != null && mCheckNoiceCallStatusTimer.isActive()) {
//            mCheckNoiceCallStatusTimer.stop();
//        }
//    }
//
//    /**
//     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
//     *
//     * @param number   电话号码
//     * @param callType 通话类型
//     */
//    private void createSmallWindow(String number, int callType) {
//        if (mContext != null) {
//            Logcat.d("mSmallWindow:" + mSmallWindow + " callType:" + callType);
//
//            if (mSmallWindow == null) {
//                WindowManager windowManager = getWindowManager(mContext);
//                if (mSmallWindowParams == null) {
//                    mSmallWindowParams = new LayoutParams();
//                    mSmallWindowParams.type = /*LayoutParams.TYPE_SYSTEM_ERROR*/LayoutParams.TYPE_SYSTEM_DIALOG;
//                    mSmallWindowParams.format = PixelFormat.RGBA_8888;
//                    mSmallWindowParams.flags = /*LayoutParams.FLAG_FULLSCREEN
//                            | */LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
//                    mSmallWindowParams.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
////					mSmallWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
////							| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
//                    mSmallWindowParams.windowAnimations = android.R.anim.fade_in;
//                    mSmallWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
//                    mSmallWindowParams.width = PhoneWindowUtil.getScreenWidth(mContext);
//                    mSmallWindowParams.height = PhoneWindowUtil.getScreenHeight(mContext) - 70;
//                    mSmallWindowParams.x = 0;
//                    mSmallWindowParams.y = 70;// smallWindowParams.height;
//                    Logcat.d("mSmallWindowParams.width:" + mSmallWindowParams.width + " mSmallWindowParams.height:" + mSmallWindowParams.height);
//                }
//
//                mSmallWindow = new FloatWindowView(mContext, number, callType, new FloatWindowView.FloatWindowCallback() {
//
//                    private boolean mIsMicMuted = false; // 用于缓存mic mute status
//
//                    @Override
//                    public void setMute(boolean isMute) {
//                        muteAudio(isMute);
//                    }
//
//                    @Override
//                    public boolean isMute() {
//                        return isAudioMuted();
//                    }
//
//                    @Override
//                    public void setMuteMic(boolean isMuteMic) {
//                        muteMic(isMuteMic);
//                    }
//
//                    @Override
//                    public boolean isMuteMic() {
//                        return isMicMuted();
//                    }
//
//                    @Override
//                    public void resizeFloatWindow(int x, int y, int width, int height) {
//                        Logcat.d("x:" + x + " y:" + y + " width:" + width + " height:" + height);
//                        if (mSmallWindowParams != null) {
//                            mSmallWindowParams.x = x;
//                            mSmallWindowParams.y = y;
//                            mSmallWindowParams.height = height;
//                            mSmallWindowParams.windowAnimations = android.R.anim.fade_in;
////                            mSmallWindowParams.flags = /*LayoutParams.FLAG_FULLSCREEN
////                            | */LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
////                            mSmallWindowParams.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
////
////                            mSmallWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
////                            mSmallWindowParams.width = PhoneWindowUtil.getScreenWidth(mContext);
////                            mSmallWindowParams.height = PhoneWindowUtil.getScreenHeight(mContext) - 70;
////                            mSmallWindowParams.x = 0;
////                            mSmallWindowParams.y = 70;// smallWindowParams.height;
//                            if (mSmallWindow != null) {
//                                // 刷新窗口
//                                WindowManager windowManager = getWindowManager(mContext);
//                                windowManager.updateViewLayout(mSmallWindow, mSmallWindowParams);
//                            }
//                        }
//                    }
//
//                    private void muteAudio(boolean isMute) {
//                        Logcat.d("isMute:" + isMute);
//                        if (mIVIAudioManager != null) {
//                            mIVIAudioManager.setParam(AudioParam.Id.MUTE, isMute ? 1 : 0);
//                        }
//                    }
//
//                    private void muteMic(final boolean isMute) {
//                        BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
//                        if (btManager == null) return;
//                        btManager.muteMic(isMute, new IBluetoothExecCallback.Stub() {
//
//                            @Override
//                            public void onSuccess(String msg) throws RemoteException {
//                                mIsMicMuted = isMute;
//                                Logcat.d("mic mute status: " + mIsMicMuted);
//                            }
//
//                            @Override
//                            public void onFailure(int errorCode) throws RemoteException {
//                                Logcat.e("mute mic error: " + errorCode);
//                            }
//                        });
//                    }
//
//                    private boolean isAudioMuted() {
//                        if (mIVIAudioManager != null) {
//                            return mIVIAudioManager.getParamValue(AudioParam.Id.MUTE) == 1;
//                        }
//                        return false;
//                    }
//
//                    // 暂时用缓存的mic mute状态
//                    private boolean isMicMuted() {
//                        return mIsMicMuted;
//                    }
//                });
//
//                windowManager.addView(mSmallWindow, mSmallWindowParams);
//            }
//        }
//    }
//
//    /**
//     * 将小悬浮窗从屏幕上移除。
//     */
//    private void removeSmallWindow() {
//        Logcat.d("mSmallWindow:" + mSmallWindow);
//        if (null != mSmallWindow && null != mContext) {
//            mSmallWindow.stopUpdateTalkingTime(); // 停止刷新通话时间
//            WindowManager windowManager = getWindowManager(mContext);
//            windowManager.removeView(mSmallWindow);
//            mSmallWindow = null;
//            mSmallWindowParams = null;
//
//            BluetoothModelUtil.getInstance().setTalkingNumber(""); // 清空当前通话号码
//            BluetoothModelUtil.getInstance().setThreeTalking(false); // 清空当前状态
//            BluetoothModelUtil.getInstance().setThreeOutGoing(false); // 清空第三方去电状态
//        }
//    }
//
//    /**
//     * 将通话记录插入到数据库中
//     *
//     * @param status      拨号状态
//     * @param phoneNumber
//     * @param contactName
//     */
//    private void insertPhoneHistoryToDb(int status, String phoneNumber, String contactName) {
//        insertPhoneHistoryToDb(BluetoothModelUtil.getInstance().getCallHistory(status, phoneNumber, contactName));
//    }
//
//    /**
//     * 将一条通话记录插入数据库中
//     *
//     * @param stCallHistory
//     */
//    private void insertPhoneHistoryToDb(StCallHistory stCallHistory) {
//        BluetoothModelUtil.getInstance().addCallHistory(stCallHistory);
//    }
//
//    /**
//     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
//     *
//     * @param context 必须为应用程序的Context.
//     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
//     */
//    private WindowManager getWindowManager(Context context) {
//        if (mWindowManager == null) {
//            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        }
//        return mWindowManager;
//    }
//
//    /**
//     * 开启拨号时UI检测定时器
//     * 添加原因：为了增加体验效果，当用户点击拨打的时候，马上就会弹出通话界面
//     * 存在问题：存在小概率，拨打出去，但是手机并未拨打出去，一直卡在拨通界面的问题
//     */
//    private void startCallingUITestTimer() {
//        if (mCallingTimerUtil == null) {
//            mCallingTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {
//                @Override
//                public void timeout() {
//                    stopCallingUITestTimer();
//                    EventBus.getDefault().post(new IVIBluetooth.CallStatus(
//                            IVIBluetooth.CallStatus.HANGUP, ""));
//                }
//            });
//        }
//        mCallingTimerUtil.start(CALLING_TEST_MS);
//    }
//
//    private void stopCallingUITestTimer() {
//        if (mCallingTimerUtil != null && mCallingTimerUtil.isActive()) {
//            mCallingTimerUtil.stop();
//        }
//    }
//
//    /**
//     * 挂断电话后的动作
//     */
//    private void onPostHangup() {
//        // 静mic场景下手动恢复mic为默认状态
//        BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
//        if (BtConfig.FEATURE_IS_MUTE_MIC && btManager != null) {
//            btManager.muteMic(false, new IBluetoothExecCallback.Stub() {
//
//                @Override
//                public void onSuccess(String msg) throws RemoteException {
//                    Logcat.d("on hangup restore mic success");
//                }
//
//                @Override
//                public void onFailure(int errorCode) throws RemoteException {
//                    Logcat.d("on hangup restore mic failure:" + errorCode);
//                }
//            });
//        }
//    }
//
//}
