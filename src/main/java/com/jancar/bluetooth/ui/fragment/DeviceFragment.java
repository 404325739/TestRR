package com.jancar.bluetooth.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.adapter.PairedDeviceAdapter;
import com.jancar.bluetooth.contract.DeviceContract;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.bluetooth.manager.RecyclerManager;
import com.jancar.bluetooth.presenter.DevicePresenter;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.utils.Constants;
import com.jancar.bluetooth.utils.DistinguishStringsUtil;
import com.jancar.bluetooth.view.DialogView;
import com.jancar.bluetooth.view.MyRecyclerView;
import com.jancar.bluetooth.view.RecycleViewDivider;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.btservice.bluetooth.IBluetoothStatusCallback;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Tzq
 * @date 2020/8/17 19:49
 */
public class DeviceFragment extends BaseFragment<DeviceContract.Presenter, DeviceContract.View> implements DeviceContract.View {

	View mView;
	MyRecyclerView mListviewPair, mListviewDiscovery;
	PairedDeviceAdapter mAdapterPair, mAdapterDiscovery;
	Button mBtnDiscovery;
	ProgressBar mProgressBarDiscovery;

	Button mBtnConnect, mBtnDisconnect;

	Switch mSwitchBtSwitch;
	Button mBtnName, mBtnPin;
	boolean mBtSwitch, mBtSwitchTmp;
	String mBtDeviceName, mBtDevicePin;
	Dialog mEditDialog;
	TextView mEditTitle;
	EditText mEditText;
	int mEditType;

	public static final int MSG_CHANGE_BTSTATU = 1000;
	public static final int TIME_CHANGE_BTSTATU = 1500;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_CHANGE_BTSTATU:
					Logcat.d("mBtSwitchTmp: " + mBtSwitchTmp + " mBtSwitch: " + mBtSwitch);
					mBtSwitch = mBtSwitchTmp;
					updateBtSwitch(mBtSwitch, true);
					break;
			}
		}
	};
	private View.OnClickListener mSearchListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Logcat.d();
			if (!getPresenter().isPowerOn()) {
				Logcat.d("!isPowerOn, return;");
				return;
			}
			if (isInProgress()) {
				// 正在扫描，停止扫描
				updateProgressState(false);
				BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
				if (btManager != null) {
					Logcat.d("isInProgress, stopSearchNewDevice");
					btManager.stopSearchNewDevice(0, null);
				}
			} else {
				updateProgressState(true);
				getPresenter().searchNewDevice();
			}
		}
	};

	public boolean isInProgress() {
		boolean inprogress = null != mProgressBarDiscovery && mProgressBarDiscovery.getVisibility() == View.VISIBLE;
		Logcat.d(": " + inprogress);
		return inprogress;
	}

	@Override
	public DeviceContract.Presenter createPresenter() {
		return new DevicePresenter();
	}

	@Override
	public DeviceContract.View getUiImplement() {
		return this;
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Logcat.d();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logcat.d();
		registerEventBus();
	}

	private void registerEventBus() {
		if (!EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().register(this);
		}
	}

	private void unregisterEventBus() {
		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().unregister(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logcat.d();
		if (mView == null) {
			mView = inflater.inflate(R.layout.fragment_device, container, false);
			init();
			initView();
			updateProgressState(getPresenter().isPowerOn());
			getPresenter().searchNewDevice();
			getPresenter().loadPairDevice();
		}
		return mView;
	}

	private void init() {
//		mBtSwitch = getPresenter().isPowerOn();
		getBtStatus();
		mBtSwitchTmp = mBtSwitch;
		mBtDeviceName = getPresenter().getBluetoothDeviceName();
		mBtDevicePin = getPresenter().getBluetoothDevicePin();
	}

	private void initView() {
		if (mView != null) {
			Logcat.d();
			mListviewPair = mView.findViewById(R.id.rv_pair);
			mListviewDiscovery = mView.findViewById(R.id.rv_search);
			mBtnDiscovery = mView.findViewById(R.id.btn_search);
			mProgressBarDiscovery = mView.findViewById(R.id.loading_device);

			mListviewPair.setNestedScrollingEnabled(false);
			mListviewDiscovery.setNestedScrollingEnabled(false);
			mListviewPair.setLayoutManager(new RecyclerManager(getContext()));
			mListviewDiscovery.setLayoutManager(new RecyclerManager(getContext()));
			if (AppUtils.isAc8257_YQQD_DY801Platform(getContext())) {
				mListviewPair.addItemDecoration(new RecycleViewDivider(getContext(),
						LinearLayoutManager.HORIZONTAL, R.drawable.iv_contact_list_div));
				mListviewDiscovery.addItemDecoration(new RecycleViewDivider(getContext(),
						LinearLayoutManager.HORIZONTAL, R.drawable.iv_contact_list_div));
			} else {
				mListviewPair.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL,
						(int) getResources().getDimension(R.dimen.line_heigth), getContext().getResources().getColor(R.color.divider)));
				mListviewDiscovery.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL,
						(int) getResources().getDimension(R.dimen.line_heigth), getContext().getResources().getColor(R.color.divider)));
			}

			mAdapterPair = getPresenter().getPairAdapter();
			mAdapterDiscovery = getPresenter().getNewAdapter();
			mListviewPair.setAdapter(mAdapterPair);
			mListviewDiscovery.setAdapter(mAdapterDiscovery);

			mBtnDiscovery.setOnClickListener(mSearchListener);
			mView.findViewById(R.id.ll_device_title).setOnClickListener(mSearchListener);

			//瑞士嘉客户要求不同翻译，乐视达不同意。
           TextView searchDeviceTitileTV =  mView.findViewById(R.id.tv_device_new);
			String searchDeviceSTR = DistinguishStringsUtil.getStringByCustomerID("search_device",getActivity());
			if(searchDeviceSTR!=null){
				searchDeviceTitileTV.setText(searchDeviceSTR);
			}


			mBtnConnect = mView.findViewById(R.id.btn_connect);
			mBtnDisconnect = mView.findViewById(R.id.btn_disconnect);
			if (null != mBtnConnect) {
				mBtnConnect.setOnClickListener(mDeviceConnectListener);
				mBtnDisconnect.setOnClickListener(mDeviceConnectListener);
			}

			// 开关
			mSwitchBtSwitch = mView.findViewById(R.id.switch_settings_btswitch);
			if (null != mSwitchBtSwitch) {
				mSwitchBtSwitch.setChecked(mBtSwitch);
				mSwitchBtSwitch.setOnClickListener(mSwitchClickListener);
//				mSwitchBtSwitch.setOnCheckedChangeListener(mOnCheckChangedListener);
			}
			// 名称
			mBtnName = mView.findViewById(R.id.btn_settings_name);
			mBtnPin = mView.findViewById(R.id.btn_settings_pin);
			Logcat.d("mBtSwitch: " + mBtSwitch + " mBtDeviceName: " + mBtDeviceName + " mBtnName: " + mBtnName);
			if (null != mBtnName) {
				mBtnName.setText(mBtDeviceName);
				mBtnPin.setText(mBtDevicePin);
				mBtnName.setOnClickListener(mRenameListener);
				mBtnPin.setOnClickListener(mRenameListener);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Logcat.d();
		/*if (!getPresenter().isPowerOn()) {
			updateProgressState(false);
		}
		getPresenter().searchNewDevice();
		getPresenter().loadPairDevice();*/
	}

//    @Override
//    public void onPause() {
//        super.onPause();
//        Logcat.d();
//    }

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			Logcat.d();
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Logcat.d();
		unregisterEventBus();
	}


	/**
	 * 该方法回调是通过 BluetoothManager 内 onConnectStatus 通过 EventBus post 调用
	 * BtService getBluzState onSuccess
	 *
	 * @param event
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventLinkDevice(IVIBluetooth.EventLinkDevice event) {
		if (event != null) {
			Logcat.d(event.status + " addr: " + event.addr);
			BluetoothCacheUtil.getInstance().setCurConnectDevice(event.status, event.name,event.addr);
			// updateDiscoveryAdapter();
			// updatePairAdapter();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					getPresenter().linkStatusChanged(event.addr, event.status);
					getPresenter().loadPairDevice();
				}
			});
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventPowerState(IVIBluetooth.EventPowerState event) {
		Logcat.d("btstate: " + event.value + " mBtSwitch: " + mBtSwitch);
		updateProgressState(event.value);
		getPresenter().searchNewDevice();
		getPresenter().loadPairDevice();
		mBtSwitchTmp = event.value;
		mHandler.removeMessages(MSG_CHANGE_BTSTATU);
		mHandler.sendEmptyMessageDelayed(MSG_CHANGE_BTSTATU, TIME_CHANGE_BTSTATU);
//		mSwitchBtSwitch.setChecked(mBtSwitch);
		// btService中除了STATE_ON，其它都发的false。会导致这里打开开关时会开-关-开
		getBtStatus();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventBtStageChange(EventClassDefine.EventBtStageChange event) {
		Logcat.d("value: " + event.value);
		mBtSwitch = event.value;
		updateBtSwitch(mBtSwitch, true);
		if (mBtSwitch) {
			updateProgressState(true);
			getPresenter().searchNewDevice();
			getPresenter().loadPairDevice();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventModifyBluzName(EventClassDefine.EventModifyBluzName event) {
		Logcat.d("name: " + event.name);
		mBtDeviceName = event.name;
		if (null != mBtnName) {
			mBtnName.setText(mBtDeviceName);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventModifyBluzPin(EventClassDefine.EventModifyBluzPin event) {
		Logcat.d("name: " + event.pin);
		mBtDevicePin = event.pin;
		if (null != mBtnPin) {
			mBtnPin.setText(mBtDevicePin);
		}
	}

	@Override
	public void updateProgressState(boolean start) {
		Logcat.d("start :" + start);
		if (start) {
			startProgress();
		} else {
			stopProgress();
		}
	}

	@Override
	public void updateDiscoveryAdapter(int type, int start, int count) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
//                Logcat.d(" mListviewDiscovery adapter: " + mListviewDiscovery.getAdapter());

				if (null != mAdapterDiscovery) {
//					mListviewDiscovery.getAdapter().notifyDataSetChanged();
					if (type == Constants.NOTIFY_ITEM_RANGE_INSERTED) {
						mAdapterDiscovery.notifyItemRangeInserted(start, count);
					} else if (type == Constants.NOTIFY_ITEM_RANGE_REMOVED) {
						mAdapterDiscovery.notifyItemRangeRemoved(start, count);
					} else if (type == Constants.NOTIFY_ITEM_CHANGED) {
						mAdapterDiscovery.notifyItemChanged(start, Constants.NOTIFY_TYPE_LINK_STATUS);
					} else if (type == Constants.NOTIFY_ITEM_RANGE_CHANGED) {
						mAdapterDiscovery.notifyItemRangeChanged(start, count);
					} else {
						mAdapterDiscovery.notifyDataSetChanged();
					}
				}
			}
		});
	}

	@Override
	public void updatePairAdapter() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mListviewPair != null && mListviewPair.getAdapter() != null) {
					mListviewPair.getAdapter().notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public void showUnPairDialog(final BluetoothDevice device) {
		try {
			AlertDialog dlg = new AlertDialog.Builder(getActivity())
					.setTitle(R.string.dv_unpair)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							getPresenter().unpairDevice(device);
						}
					})
					.setNegativeButton(android.R.string.cancel, null)
					.create();
			dlg.show();
			Button button = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
			button.setTextColor(getResources().getColor(R.color.blue_checked_color));
			Button nbutton = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setTextColor(getResources().getColor(R.color.blue_checked_color));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Context getUIContext() {
		return getContext();
	}

	public void startProgress() {
		mProgressBarDiscovery.setVisibility(View.VISIBLE);
//        mProDiscovery.setIndeterminateDrawable(getResources().getDrawable(
//                R.drawable.progress_drawble));
//        mProDiscovery.setProgressDrawable(getResources().getDrawable(
//                R.drawable.progress_drawble));
//        mProDiscovery.animate();
	}

	public void stopProgress() {
		mProgressBarDiscovery.setVisibility(View.GONE);
//        mProDiscovery.setIndeterminateDrawable(getResources().getDrawable(
//                R.drawable.progress_drawble_stop));
//        mProDiscovery.setProgressDrawable(getResources().getDrawable(
//                R.drawable.progress_drawble_stop));
	}
	@Override
	public void showToast(final int resid) {
		try{
			Logcat.d();
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					ToastUtil.getInstance().showToast(resid, Toast.LENGTH_SHORT);
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	View.OnClickListener mDeviceConnectListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_connect:
					getPresenter().linkSelectedDevice(true);
					break;
				case R.id.btn_disconnect:
					getPresenter().linkSelectedDevice(false);
					break;
			}
		}
	};

	View.OnClickListener mSwitchClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Logcat.d("curr mBtSwitch: " + mBtSwitch);
			getPresenter().setPower(!mBtSwitch);
			mSwitchBtSwitch.setEnabled(false);
			mHandler.removeMessages(MSG_CHANGE_BTSTATU);
			mHandler.sendEmptyMessageDelayed(MSG_CHANGE_BTSTATU, TIME_CHANGE_BTSTATU);
		}
	};

	CompoundButton.OnCheckedChangeListener mOnCheckChangedListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Logcat.d(isChecked + " curr mBtSwitch: " + mBtSwitch);
			switch (buttonView.getId()) {
				case R.id.switch_settings_btswitch:
					mBtSwitch = isChecked;
					mBtSwitchTmp = mBtSwitch;
					getPresenter().setPower(isChecked);
					break;
			}
		}
	};

	private View.OnClickListener mRenameListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_settings_name:
					showEditDialog(0);
					break;
				case R.id.btn_settings_pin:
					showEditDialog(1);
					break;
			}
		}
	};
	/**
	 * 显示编辑框
	 *
	 * @param edittype 0: name; 1: pin;
	 */
	private void showEditDialog(int edittype) {
		Logcat.d(0 == edittype ? "name" : "pin");
		if (!getPresenter().isPowerOn()) {
			Logcat.d("!isPowerOn, return;");
			return;
		}
		mEditType = edittype;
		if (null == mEditDialog) {
			mEditDialog = new DialogView(getContext(), R.layout.bt_edit_namepin, R.style.Dialog, Gravity.NO_GRAVITY);
			mEditTitle = mEditDialog.findViewById(R.id.tv_edit_title);
			mEditText = (EditText) mEditDialog.findViewById(R.id.et_edit);

			((Button) mEditDialog.findViewById(R.id.btn_edit_ok)).setOnClickListener(
					new View.OnClickListener() {
						public void onClick(View arg0) {
							handleClickOk(mEditType);
						}
					});
		}
		if (0 == edittype) {
			mEditTitle.setText(R.string.settings_tv_edit_btname);
			mEditText.setText(mBtDeviceName);
			mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
			mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(248)});
		} else {
			mEditTitle.setText(R.string.settings_tv_edit_btpin);
			mEditText.setText("");
			mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
		}
		mEditDialog.show();
	}

	private void handleClickOk(int edittype) {
		Logcat.d(0 == edittype ? "name" : "pin");
		String newValue = mEditText.getText().toString();
		String checkValue = newValue.replace(" ", "");
		if (!TextUtils.isEmpty(newValue) && !TextUtils.isEmpty(checkValue)) {
			if (0 == edittype) {
				getPresenter().modifyModuleName(newValue);
				mBtDeviceName = newValue;
				if (mBtnName != null) {
					mBtnName.setText(newValue);
				}
			} else {
				getPresenter().modifyModulePIN(newValue);
				mBtDevicePin = newValue;
				if (mBtnPin != null) {
					mBtnPin.setText(newValue);
				}
			}
			mEditDialog.dismiss();
		}
	}

	private void updateBtSwitch(boolean mBtSwitch, boolean enable) {
		mHandler.removeMessages(MSG_CHANGE_BTSTATU);
		mBtSwitchTmp = mBtSwitch;
		if (null != mSwitchBtSwitch) {
			mSwitchBtSwitch.setChecked(mBtSwitch);
			mSwitchBtSwitch.setEnabled(enable);
		}
	}

	private void getBtStatus() {
		BluetoothModelUtil.getInstance().getBluetoothModuleStatus(new IBluetoothStatusCallback.Stub() {
			@Override
			public void onSuccess(int status, int hfpStatus, int a2dpStatus) throws RemoteException {
				Logcat.d("status: " + status + " hfpStatus: " + hfpStatus + " a2dpStatus: " + a2dpStatus);
				if (status == BluetoothAdapter.STATE_ON || status == BluetoothAdapter.STATE_OFF) {
					mBtSwitch = status == 12;
					updateBtSwitch(mBtSwitch, true);
				}
			}

			@Override
			public void onFailure(int errorCode) throws RemoteException {
				updateBtSwitch(mBtSwitchTmp, true);
			}
		});
	}
}
