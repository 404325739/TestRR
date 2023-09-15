package com.jancar.bluetooth.floatbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.floatbar.Presenter.ScreenTalkingPresenter;
import com.jancar.base.IntentAction;
import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.bluetooth.ui.MainActivity;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.PhoneWindowUtil;
import com.jancar.bluetooth.utils.LogUtils;
import com.jancar.sdk.audio.AudioParam;
import com.jancar.sdk.audio.IVIAudio;
import com.jancar.sdk.audio.IVIAudioManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.car.CarManager;
import com.jancar.sdk.car.IVICar;
import com.jancar.sdk.utils.AndroidAutoUtil;
import com.jancar.sdk.utils.Logcat;
import com.jancar.sdk.utils.TimerUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 电话悬浮窗管理工具类，整个应用只存在一个，使用单例管理
 * 在服务中进行初始化，通过EventBus进行控制
 */
public class FloatPhoneCallWindowManager extends BroadcastReceiver {

	private FloatWindowView mSmallWindow = null;                                                    // 小悬浮窗View的实例
	private LayoutParams mSmallWindowParams;                                                        // 小悬浮窗View的参数
	private WindowManager mWindowManager;                                                           // 用于控制在屏幕上添加或移除悬浮窗
	private Context mContext;
	private StCallHistory mStCallHistory = null;                                                    // 缓存当前一条通话记录
	private StCallHistory mStThreeCallHistory = null;                                               // 缓存一条三方通话的记录
	private IVIAudioManager mIVIAudioManager;
	private int mLastPhoneState = -1;//最后的电话状态， 如果最后不是通话中的状态而走了挂断，则认为是miss电话

	private String mTalkingNumber = "";                                                             // 记录当前通话的号码，不记录三方通话号码，由于模块在通话的过程中不会给出号码，所以做缓存
	private TimerUtil mHangupTimerUtil = null;
	private TimerUtil mFilterHangupTimerUtil = null;//过滤挂断后状态的定时器
	private static final int HANGUP_HIDE_WINDOW_MS = 500;                                           // 挂断之后，不马上消失页面，因为三方通话时，有多种情况，模块会先发挂断，再发接听，如果马上销毁页面，会闪界面

	private TimerUtil mCallingTimerUtil = null;                                                     // 车机端拨打电话，刷出UI，但是存在小概率情况，手机没有拨打出去，通过定时器检测，如果手机没有拨打出去，则过几秒消失UI
	private static final int CALLING_TEST_MS = 10 * 1000;                                            // 5秒后检测手机是否没有拨打出去
	int lastY, moveY, winY;//窗口上次的位置Y
	private int mlastX,mLastY;//记录手指落点，以此判断是否是单击
	private static FloatPhoneCallWindowManager pThis = null;
	private CarManager mCarManager;
	private boolean isVoiceOnPhone = false;//记录通话声音在那一边，防止窗口还没创建窗口就收到消息

	private AndroidAutoUtil mAndroidAutoUtil; // Android Auto 功能辅助类

	private int mHalfScreenHeight;
	private boolean isAc8257_YQQD_DY801;

	private final String ACTIVITY_STATE_CHANGED = "android.activity.action.STATE_CHANGED";

	private FloatPhoneCallWindowManager() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			Logcat.d("onReceive action:" + action);
			if (TextUtils.equals(action, ACTIVITY_STATE_CHANGED)) {// 接收到Activity切换广播
				switchScreenTaking();
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNaviStart(ScreenTalkingPresenter.EventOnNaviStart event) {
		Logcat.d();
		switchScreenTaking();
		BtApplication.getInstance().startNavigationApp();
	}

	private void switchScreenTaking() {
		if (mSmallWindow != null) {
			mSmallWindow.switchScreenTaking();
		}
	}

	private void release() {
		EventBus.getDefault().unregister(this);
		if (mIVIAudioManager != null) {
			mIVIAudioManager.disconnect();
		}
		if (mContext != null) {
			mContext.unregisterReceiver(this);
			mContext = null;
		}
		if(mCarManager != null){
			mCarManager.disconnect();
			mCarManager = null;
		}
		mAndroidAutoUtil = null;
	}

	/**
	 * 传递服务的
	 *
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mAndroidAutoUtil = new AndroidAutoUtil(context);

		mIVIAudioManager = new IVIAudioManager(context, null, null); // 调音量接口

		if (mContext != null) {
			mHalfScreenHeight = mContext.getResources().getInteger(R.integer.half_screen_height);
			isAc8257_YQQD_DY801 = AppUtils.isAc8257_YQQD_DY801Platform(context);
			// 监听Activity切换广播
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTIVITY_STATE_CHANGED);
			mContext.registerReceiver(this, filter);
			mCarManager = new CarManager(context, null, null, true);
		}
	}

	/**
	 * 创建PhoneCallWindowManager
	 *
	 * @return
	 */
	public synchronized static FloatPhoneCallWindowManager getInstance() {
		if (null == pThis) {
			pThis = new FloatPhoneCallWindowManager();
		}
		return pThis;
	}

	public synchronized static void removeInstance() {
		if (null != pThis) {
			pThis.release();
			pThis = null;
		}
	}

	private int lastCallStatus = -1;
	private String lastCallPhoneNumber = "";


	/**
	 * 此方法用于拦截手动挂断后手机发送的多余的状态，
	 * 为了快速隐藏界面，挂断后不受理多余的状态
	 * 收到真正的挂断电话或者超时后重置mHandupNumper以处理真正的状态
	 * @anthor LBH
	 * @return
	 */
	public static final String HANGUP_NUMPER = "HANGUP";//自己发送的消息event.mPhoneNumber等于该值，用于区分是不是真正的回调
	private String mHangupNumper = "HANGUP";//被挂断的电话，挂断后,需要特殊默认值,避免空值不可预知的错误
	private boolean interceptSurplusCallState(IVIBluetooth.CallStatus event){
		Logcat.d("status =" + event.getName() + ", phone =" + event.mPhoneNumber);
		if(event.mStatus == IVIBluetooth.CallStatus.HANGUP){
			if(TextUtils.equals(event.mPhoneNumber,HANGUP_NUMPER)){
				Logcat.d("event.mPhoneNumber = " + HANGUP_NUMPER);
				event.mPhoneNumber = "";//直接修改对象内的值， 该对象会继续向下传递，处理后续逻辑
				mHangupNumper = mTalkingNumber;
				if(mFilterHangupTimerUtil == null){
					mFilterHangupTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {

						@Override
						public void timeout() {
							mHangupNumper = HANGUP_NUMPER;//超时后重置
							Logcat.d("interceptSurplusCallState timeout");
							mFilterHangupTimerUtil.stop();
						}
					});
				}
				mFilterHangupTimerUtil.stop();
				mFilterHangupTimerUtil.start(2500);//x秒后重置状态//经测试（小米6），基本挂断后别的状态也基本0.5s内回调
				return false;
			}else{//模块真正的状态来了
				mHangupNumper = HANGUP_NUMPER;
				if(mFilterHangupTimerUtil != null){
					mFilterHangupTimerUtil.stop();
				}
				//需要让下一步继续处理以处理可能存在的界面不消失问题
				return false;
			}
		}
		//过滤同一手机的其他状态
		if(TextUtils.equals(event.mPhoneNumber,mHangupNumper)){
			return true;
		}
		return false;
	}

	/**
	 * 电话状态发生改变
	 * 调用位置：BluetoothModelUtil callPhone onSuccess
	 * BluetoothManager onCallStatus
	 * FloatWindowView hangup
	 *
	 * @param event
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventPhoneStatus(IVIBluetooth.CallStatus event) {
		if (null != event) {
			//这里需要过滤一下挂断后的消息
//			if(interceptSurplusCallState(event)){
//				Logcat.d("return   status =" + event.getName() + ", phone =" + event.mPhoneNumber);
//				return;
//			}
			if ((event.mStatus == IVIBluetooth.CallStatus.TALKING
					|| event.mStatus == IVIBluetooth.CallStatus.OUTGOING)                           // 去电状态，有时候模块会发送两次去电状态，一次没号码，导致会闪UI
					&& TextUtils.isEmpty(event.mPhoneNumber)) {                                     // 通话中，有些模块不会给出通话号码，做一下缓存
				event.mPhoneNumber = mTalkingNumber;
			}

			stopCallingUITestTimer();                                                               // 停止检测拨打电话UI是否需要消失的定时器

			Logcat.d("status " + event.getName() + " phone:" + event.mPhoneNumber);
			String contactName = BluetoothModelUtil.getInstance().getContactName(event.mPhoneNumber); // 通过号码获取联系人名字
			Logcat.d("contactName:" + contactName);
			if (event.mStatus == IVIBluetooth.CallStatus.NORMAL ||
					event.mStatus == IVIBluetooth.CallStatus.HANGUP) {

			} else {
				if (mHangupTimerUtil != null) {
					mHangupTimerUtil.stop();
				}
				createSmallWindow(event.mPhoneNumber, event.mStatus);
				updateCallStatusText(event.mStatus, event.mPhoneNumber, contactName);               // 刷新UI
			}

			if (event.mStatus == IVIBluetooth.CallStatus.THREE_TALKING ||
					event.mStatus == IVIBluetooth.CallStatus.TALKING ||
					event.mStatus == IVIBluetooth.CallStatus.THREE_OUTGOING) {                      // 当前通话的号码
				BluetoothModelUtil.getInstance().setTalkingNumber(event.mPhoneNumber);
			}
			Logcat.d("event.mStatus =" + event.mStatus);
			switch (event.mStatus) {
				case IVIBluetooth.CallStatus.INCOMING:                                              // 来电
					if (BluetoothCacheUtil.getInstance().getBluzAutoListen()) {
						BluetoothModelUtil.getInstance().listenPhone();                             // 自动应答
					}

					mStCallHistory = BluetoothModelUtil.getInstance().getCallHistory(               // 来电，未接状态
							IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS, event.mPhoneNumber, contactName);

					mTalkingNumber = event.mPhoneNumber;
					break;

				case IVIBluetooth.CallStatus.OUTGOING:                                              // 去电
					BluetoothModelUtil.getInstance().setCallNumber(event.mPhoneNumber);             // 记录拨出的号码
					Logcat.d("mInsertSql " + event.mInsertSql + " lastCallStatus " + lastCallStatus + " /// ");

					if (!event.mInsertSql) {
						// 车机端拨打电话，有时候存在一种情况，手机没有卡，UI已经刷出来，检测2S，如果此时还没有UI过来，则隐藏
						startCallingUITestTimer();
					} else if (!TextUtils.isEmpty(event.mPhoneNumber) && mStThreeCallHistory == null) {
						// 有时候第三方通话结束后会发 outgoing ，但是此时，实际是上一次的通话，不需要记录数据库
//						if(lastCallStatus != event.mStatus) {
//							insertPhoneHistoryToDb(IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName);
//						}
						if(mStCallHistory == null) {
							Logcat.d( " !!! ........ ");
							mStCallHistory = BluetoothModelUtil.getInstance().getCallHistory(               // 来电，未接状态
									IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName);
						}
					}
					mTalkingNumber = event.mPhoneNumber;
					break;

				case IVIBluetooth.CallStatus.TALKING:                                               // 通话中
					BluetoothModelUtil.getInstance().setThreeOutGoing(false);                       // 变成单方通话
					BluetoothModelUtil.getInstance().setThreeInComing(false);
					if (mStCallHistory != null && mStCallHistory.status == IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS) { // 通话了，未接状态变成已接状态
						mStCallHistory.status = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS; // 已接状态
					}
					break;

				case IVIBluetooth.CallStatus.THREE_OUTGOING:                                        // 三方去电
					BluetoothModelUtil.getInstance().setThreeOutGoing(true);
					BluetoothModelUtil.getInstance().setThreeTalking(true);
					BluetoothModelUtil.getInstance().setThreeInComing(false);
					insertPhoneHistoryToDb(IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName); // 第三方去电，也缓存到数据库中
					mStThreeCallHistory = BluetoothModelUtil.getInstance().getCallHistory(
							IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, event.mPhoneNumber, contactName); // 记录当前状态是三方去电状态
					break;

				case IVIBluetooth.CallStatus.THREE_INCOMING:                                        // 三方来电
					mStThreeCallHistory = BluetoothModelUtil.getInstance().getCallHistory(
							IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS, event.mPhoneNumber, contactName);
					BluetoothModelUtil.getInstance().setThreeTalking(true);
					BluetoothModelUtil.getInstance().setThreeInComing(true);
					BluetoothModelUtil.getInstance().setThreeOutGoing(false);
					break;

				case IVIBluetooth.CallStatus.THREE_TALKING:                                         // 三方通话
					if (mStThreeCallHistory != null &&
							mStThreeCallHistory.status != IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS) { // 去电状态不需要改变成接听状态
						mStThreeCallHistory.status = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS; // 已接状态
					}
					BluetoothModelUtil.getInstance().setThreeTalking(true);
					break;

				case IVIBluetooth.CallStatus.RETAIN:                                                // 保持状态
					// 该处标记不一定是真的不是第三方去电状态，因为获取第三方去电的位置是 THREE_TALKING 位置，如果 保持状态和三方通话状态同时存在，则认为已经非去电状态
					BluetoothModelUtil.getInstance().setThreeOutGoing(false);
					BluetoothModelUtil.getInstance().setThreeInComing(false);
					break;
				case IVIBluetooth.CallStatus.THREE_HANGUP:
					BluetoothModelUtil.getInstance().setThreeTalking(false);
					BluetoothModelUtil.getInstance().setThreeInComing(false);
					BluetoothModelUtil.getInstance().setThreeOutGoing(false);
					break;
				case IVIBluetooth.CallStatus.NORMAL:                                                // 常态
				case IVIBluetooth.CallStatus.HANGUP:                                                // 挂断
					//先不管3方状态 20201025
//					if (BluetoothModelUtil.getInstance().isThreeTalking()) {
//						BluetoothModelUtil.getInstance().setThreeTalking(false);
//					} else

						{
						//fixme
						//挂断不认为是miss电话 。 根据服务划分，需要置为已接（没有拒接状态，但是无法判断是手动挂掉还是无人接听的挂断，这里只能通过是否处于过通话状态来判断）
//						if (mStCallHistory != null && mStCallHistory.status == IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS && mLastPhoneState == IVIBluetooth.CallStatus.INCOMING) { // 通话了，未接状态变成已接状态
//							mStCallHistory.status = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS; // 已接状态
//						}
						//消失窗口不延迟 20200702
						removeSmallWindow();
						if (mHangupTimerUtil == null) {
							mHangupTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {
								@Override
								public void timeout() {
									mHangupTimerUtil.stop();
									Logcat.d(" !!!TimerCallback-hangup");
//                                    removeSmallWindow();
									// 挂断之后，如果是未接或者已接将通话记录插入数据库中
									if (mStCallHistory != null /*&&
											mStCallHistory.status != IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS*/) {
										// 去电状态在打电话时就已经记录
										insertPhoneHistoryToDb(mStCallHistory);
									}

									if (mStThreeCallHistory != null &&
											mStThreeCallHistory.status != IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS) { // 第三方通话在第一次通话之后，加载到后面
										insertPhoneHistoryToDb(mStThreeCallHistory);
									}

									mStCallHistory = null;
									mStThreeCallHistory = null;
								}
							});
						}
						mHangupTimerUtil.start(HANGUP_HIDE_WINDOW_MS);
					}
					break;
			}
			lastCallStatus = event.mStatus;
			lastCallPhoneNumber = event.mPhoneNumber;
			if (mSmallWindow != null) {                                                             // 刷新通话状态
				mSmallWindow.setCallType(event.mStatus, event.mPhoneNumber);
			}
			mLastPhoneState = event.mStatus;
		}
	}

	/**
	 * 语音切换
	 * 调用位置：BluetoothManager onVoiceChange
	 *
	 * @param event
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventVoiceChange(IVIBluetooth.EventVoiceChange event) {
		if (event != null) {
			Logcat.d("type = " + event.type);
			isVoiceOnPhone = event.type == IVIBluetooth.BluetoothAudioTransferStatus.PHONE_STATUS;
			updateCallStatusAudio(isVoiceOnPhone);
		}
	}

	/**
	 * 监听打电话过程中，是否有断开连接
	 *
	 * @param event
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventLinkDevice(IVIBluetooth.EventLinkDevice event) {
		if (event != null) {
			if (event.status != IVIBluetooth.BluetoothConnectStatus.CONNECTED) {                    // 打电话过程中断开连接
				removeSmallWindow();
			}
		}
	}

	/**
	 * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
	 *
	 * @param number   电话号码
	 * @param callType 通话类型
	 */
	private void createSmallWindow(String number, int callType) {
		if (mContext == null) return;

		if (mSmallWindow == null) {
			Logcat.d("mSmallWindow:" + mSmallWindow + " callType:" + callType);
			initWindowParams();

			mSmallWindow = new FloatWindowView(mContext, number, callType, new FloatWindowView.FloatWindowCallback() {

				@Override
				public void setMute(boolean isMute) {
					if (mIVIAudioManager != null) {
						mIVIAudioManager.setParam(AudioParam.Id.MUTE, isMute ? 1 : 0);
					}
				}

				@Override
				public boolean isMute() {
					if (mIVIAudioManager != null) {
						return mIVIAudioManager.getParamValue(AudioParam.Id.MUTE) == 1;
					}
					return false;
				}

				@Override
				public void resizeFloatWindow(int x, int y, int width, int height) {
					Logcat.d("x:" + x + " y:" + y + " width:" + width + " height:" + height);
					updateWindowLayout(x, y, width, height);
				}

				@Override
				public AndroidAutoUtil getAndroidAutoUtil() {
					return mAndroidAutoUtil;
				}
			});
			int ccd = mCarManager.getCcdStatus();
			mSmallWindow.setCcdStatus(ccd == IVICar.Ccd.Status.ON || ccd == IVICar.Ccd.Status.ALREADY_ON);
			mSmallWindow.setVoiceChanged(isVoiceOnPhone);
			mSmallWindow.findViewById(R.id.layout_call).setOnTouchListener(moveTouchListen);
			Logcat.d("windowManager addView : ccd = " + ccd);
			WindowManager windowManager = getWindowManager(mContext);
			windowManager.addView(mSmallWindow, mSmallWindowParams);
		}
	}

	/**
	 * 将小悬浮窗从屏幕上移除。
	 */
	private void removeSmallWindow() {
		Logcat.d("mSmallWindow:" + mSmallWindow);
		if (null != mSmallWindow && null != mContext) {
			mSmallWindow.stopUpdateTalkingTime();                                                   // 停止刷新通话时间
			WindowManager windowManager = getWindowManager(mContext);
			windowManager.removeView(mSmallWindow);
			mSmallWindow = null;
			mSmallWindowParams = null;

			BluetoothModelUtil.getInstance().setTalkingNumber("");                                  // 清空当前通话号码
			BluetoothModelUtil.getInstance().setThreeTalking(false);                                // 清空当前状态
			BluetoothModelUtil.getInstance().setThreeOutGoing(false);                               // 清空第三方去电状态
			BluetoothModelUtil.getInstance().setThreeInComing(false);                               // 清空第三方来电状态
		}
	}

	/**
	 * 刷新通话状态的信息
	 *
	 * @param status      状态
	 * @param phoneNumber 电话号码
	 * @param contactName 联系人名字
	 */
	public void updateCallStatusText(int status, String phoneNumber, String contactName) {
		if (mSmallWindow != null) {
			mSmallWindow.setCallType(status, phoneNumber);
			Logcat.d("status:" + IVIBluetooth.CallStatus.getName(status) + " phoneNumber:" + phoneNumber + " contactName:" + contactName);

			// 注：必须先刷新号码，再刷新名字，第三方通话，号码和名字是用同一个控件
			if (phoneNumber != null) {
				mSmallWindow.updateNumber(status, phoneNumber);
			}
			if (contactName != null) {
				if (TextUtils.isEmpty(contactName)) {
					//如果联系人名字为空显示未知
					if (phoneNumber != null) {
//						Logcat.d("updateCallStatusText:" + phoneNumber);
						mSmallWindow.updateName(status,
								mContext.getResources().
										getString(R.string.call_number_unknown));
					}
				} else {
					mSmallWindow.updateName(status, contactName);
				}
			}
		}
	}

	/**
	 * 将通话记录插入到数据库中
	 *
	 * @param status      拨号状态
	 * @param phoneNumber
	 * @param contactName
	 */
	private void insertPhoneHistoryToDb(int status, String phoneNumber, String contactName) {
		insertPhoneHistoryToDb(BluetoothModelUtil.getInstance().getCallHistory(status, phoneNumber, contactName));
	}

	/**
	 * 将一条通话记录插入数据库中
	 *
	 * @param stCallHistory
	 */
	private void insertPhoneHistoryToDb(StCallHistory stCallHistory) {
		LogUtils.callStackPrint();
		if(stCallHistory != null){
			stCallHistory.adder = StCallHistory.ADDER_HANGUP;
		}
		BluetoothModelUtil.getInstance().addCallHistory(stCallHistory);
	}

	/**
	 * 切换通话状态
	 *
	 * @param isOnPhone
	 */
	private void updateCallStatusAudio(boolean isOnPhone) {
		if (mSmallWindow != null) {
			Logcat.d("updateCallStatusAudio , isOnPhone = " + isOnPhone);
			mSmallWindow.setVoiceChanged(isOnPhone);
		}
	}

	/**
	 * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
	 *
	 * @param context 必须为应用程序的Context.
	 * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
	 */
	private WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCcdChanged(IVICar.Ccd ccd) {
		Logcat.d("ccd:" + ccd.mStatus);
		if (mSmallWindow != null) {
			mSmallWindow.setCcdStatus(ccd.isOn());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMuteChanged(IVIAudio.EventMuteChanged event) {
		if (mSmallWindow != null && event != null) {
			mSmallWindow.updateMuteIcon(event.mMute);
		}
	}

	/**
	 * 开启拨号时UI检测定时器
	 * 添加原因：为了增加体验效果，当用户点击拨打的时候，马上就会弹出通话界面
	 * 存在问题：存在小概率，拨打出去，但是手机并未拨打出去，一直卡在拨通界面的问题
	 */
	private void startCallingUITestTimer() {
		if (mCallingTimerUtil == null) {
			mCallingTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {
				@Override
				public void timeout() {
					stopCallingUITestTimer();

					EventBus.getDefault().post(new IVIBluetooth.CallStatus(
							IVIBluetooth.CallStatus.HANGUP,
							""
					));
				}
			});
		}
		mCallingTimerUtil.start(CALLING_TEST_MS);
	}

	private void stopCallingUITestTimer() {
		if (mCallingTimerUtil != null && mCallingTimerUtil.isActive()) {
			mCallingTimerUtil.stop();
		}
	}

	private void initWindowParams() {
		if (mSmallWindowParams == null) {
			mSmallWindowParams = new LayoutParams();
			mSmallWindowParams.type = /*LayoutParams.TYPE_SYSTEM_ERROR*/LayoutParams.TYPE_SYSTEM_ERROR;
			mSmallWindowParams.format = PixelFormat.RGBA_8888;
			mSmallWindowParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
			mSmallWindowParams.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
			Configuration mConfiguration = mContext.getResources().getConfiguration(); //获取设置的配置信息
			int ori = mConfiguration.orientation; //获取屏幕方向 //竖屏时不显示状态栏
			if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
				mSmallWindowParams.flags |= LayoutParams.FLAG_LAYOUT_IN_SCREEN;
			}
//			mSmallWindowParams.windowAnimations = 0;
//					mSmallWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//							| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;\
			int[] gravity = mContext.getResources().getIntArray(R.array.array_call_window_gravity);
			if (gravity.length > 0) {
				mSmallWindowParams.gravity = getGravity(gravity[0]);
				for (int i = 0; i < gravity.length; i ++) {
					mSmallWindowParams.gravity |= getGravity(gravity[i]);
				}
			} else {
				mSmallWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
			}
			mSmallWindowParams.width = PhoneWindowUtil.getScreenWidth(mContext);
			mSmallWindowParams.height = mHalfScreenHeight;
			mSmallWindowParams.x = 0;
			mSmallWindowParams.y = 0;// smallWindowParams.height;
			Logcat.d("mSmallWindowParams.width:" + mSmallWindowParams.width + " mSmallWindowParams.height:" + mSmallWindowParams.height);
		}
	}

	/**
	 *
	 * @param key 0:Top; 1:Left; 2:Right; 3:Bottom;
	 * @return
	 */
	public int getGravity(int key) {
		int gravity = Gravity.CENTER;
		switch (key) {
			case 0:
				gravity = Gravity.TOP;
				break;
			case 1:
				gravity = Gravity.LEFT;
				break;
			case 2:
				gravity = Gravity.RIGHT;
				break;
			case 3:
				gravity = Gravity.BOTTOM;
				break;
		}
		return gravity;
	}

	public synchronized void updateWindowLayout(int x, int y, int width, int height) {
		if (mSmallWindowParams != null) {
			mSmallWindowParams.x = x;
			mSmallWindowParams.y = y;
			mSmallWindowParams.width = width;
			mSmallWindowParams.height = height;
			if (mSmallWindow != null && mSmallWindow.getWindowToken() != null) {
				// 刷新窗口
				WindowManager windowManager = getWindowManager(mContext);
//				windowManager.removeView(mSmallWindow);
//				windowManager.addView(mSmallWindow,mSmallWindowParams);
				windowManager.updateViewLayout(mSmallWindow, mSmallWindowParams);
				Logcat.d("windowManager updateViewLayout height:" + mSmallWindowParams.height + " ,y " + y);
			}
		}
	}

	private boolean isActionUp = false;
	View.OnTouchListener moveTouchListen = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int max = PhoneWindowUtil.getScreenHeight(mContext) - mHalfScreenHeight - PhoneWindowUtil.getStatusBarHeight(mContext);
			int min = 0;
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					isActionUp = false;
					lastY = (int) event.getRawY();
					mlastX = (int)event.getRawX();
					mLastY = (int)event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					isActionUp = true;
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_CANCEL:
					moveY = (int) event.getRawY();
					Logcat.d("windowManager updateViewLayout my:" + moveY + " ,lastY " + lastY);
					if (Math.abs(moveY - lastY) > 20) {
						updateWindowLayout(0, getY(min, max), mSmallWindowParams.width, mSmallWindowParams.height);
						lastY = moveY;
					}else{
						Logcat.d("isActionUp =" + isActionUp);
						if(isActionUp && Math.abs((mlastX - event.getRawX())) < 10 && Math.abs((mLastY - event.getRawY())) < 10){
							if(mSmallWindow != null){
								if(!mSmallWindow.isCcdStatus()){
									Intent intent = new Intent(mContext, MainActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									mContext.startActivity(intent);
									if (isAc8257_YQQD_DY801) {
										// 8257 相同的通话状态只发一次？且未接收到ACTION_ACTIVITY_CHANGED？
										switchScreenTaking();
									}
								}
							}
						}
					}
					break;
			}
			return true;
		}
	};

	private int getY(int min, int max) {
		winY = mSmallWindowParams.y == 0 ? min : mSmallWindowParams.y;
		Logcat.d("windowManager updateViewLayout winY " + winY);
		int result = moveY - lastY + winY;
		if (isAc8257_YQQD_DY801) { // Gravity.BOTTOM
			result = lastY - moveY + winY;
		}
		if (moveY > lastY) {
			return result < max ? result : max;
		} else {
			return result > min ? result : min;
		}
	}

}
