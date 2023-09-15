package com.jancar.bluetooth.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.jancar.bluetooth.BtApplication;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.adapter.PairedDeviceAdapter;
import com.jancar.bluetooth.contract.DeviceContract;
import com.jancar.bluetooth.model.DeviceModel;
import com.jancar.bluetooth.model.DeviceRepository;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.btservice.bluetooth.BluetoothDevice;
import com.jancar.btservice.bluetooth.IBluetoothExecCallback;
import com.jancar.sdk.bluetooth.BluetoothManager;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.presenter.BaseModelPresenter;

import java.util.ArrayList;

/**
 * @author Tzq
 */
public class DevicePresenter extends BaseModelPresenter<DeviceContract.View, DeviceModel> implements DeviceContract.Presenter, DeviceModel.Callback {

	PairedDeviceAdapter mNewAdapter;
	PairedDeviceAdapter mPairAdapter;
	private boolean isAc8257_YQQD_DY801;

	@Override
	public DeviceModel createModel() {
		return new DeviceRepository(this);
	}

	@Override
	public PairedDeviceAdapter getNewAdapter() {
		if (mNewAdapter == null) {
			mNewAdapter = new PairedDeviceAdapter(getUiContext(), getModel().getSearchList(), isAc8257_YQQD_DY801, false);
			mNewAdapter.setItemClickListener(new PairedDeviceAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					if (isAc8257_YQQD_DY801) {
						// 选中
						mNewAdapter.notifyDataSetChanged();
						mPairAdapter.setSelectedItem(-1);
					} else {
						BluetoothDevice device = mNewAdapter.getItem(position);
						toLinkOrUnLinkDevice(device);
					}
				}

				@Override
				public void onLongClick(View view, int position) {

				}
			});
			/*mNewAdapter = new NewDeviceAdapter(getModel().getSearchList());
			mNewAdapter.setItemClickListener(new NewDeviceAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					BluetoothDevice device = getModel().getSearchList().get(position);
					toLinkOrUnLinkDevice(device);
//					BluetoothDevice curDevice = BluetoothCacheUtil.getInstance().getCurConnectDevice();
//					if (device == null) return;
//					Logcat.d("link device: " + device.addr + " connDev : " + (curDevice == null ? "null" : curDevice.addr));
//					BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
//					if (btManager == null) return;
//					if (curDevice != null && device.addr.equals(curDevice.addr)) {
//						btManager.unlinkDevice(new IBluetoothExecCallback.Stub() {
//							@Override
//							public void onSuccess(String msg) throws RemoteException {
//								Logcat.d("unlinkDevice");
//							}
//
//							@Override
//							public void onFailure(int errorCode) throws RemoteException {
//								Logcat.d("unlinkDevice errorCode: " + errorCode);
//							}
//						});
//					} else {
//						btManager.linkDevice(device.addr, new IBluetoothExecCallback.Stub() {
//							@Override
//							public void onSuccess(String msg) throws RemoteException {
//								Logcat.d();
//							}
//
//							@Override
//							public void onFailure(int errorCode) throws RemoteException {
//								Logcat.d("errorCode: " + errorCode);
//							}
//						});
//					}
				}
			});*/
		}
		return mNewAdapter;
	}

	private void toLinkOrUnLinkDevice(BluetoothDevice device) {
		if (device == null) {
			Logcat.w(" -------------------->>>>>>>>>>> link device null");
			return;
		}
//		BluetoothDevice currentConnectDevice = BluetoothCacheUtil.getInstance().getCurConnectDevice();
		BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
		if (btManager == null) return;
		stopSearch(btManager);
//		if (TextUtils.equals(device.addr, currentConnectDevice.addr)) {
		if (BluetoothCacheUtil.getInstance().isConnected()) {
			btManager.unlinkDevice(mBtExeCall);
		} else {
			Logcat.w(" -------------------->>>>>>>>>>> link " + device.addr +
					"#" + device.name);
			btManager.linkDevice(device.addr, mBtExeCall);
		}
	}

	private IBluetoothExecCallback.Stub mBtExeCall = new IBluetoothExecCallback.Stub() {
		@Override
		public void onSuccess(String msg) {
		}

		@Override
		public void onFailure(int errorCode) {
			Logcat.d("linkDevice =" + errorCode);
			if(getUi() != null){
				getUi().showToast(R.string.tips_device_unconnect);
			}
		}
	};

	@Override
	public PairedDeviceAdapter getPairAdapter() {
		if (mPairAdapter == null) {
			isAc8257_YQQD_DY801 = AppUtils.isAc8257_YQQD_DY801Platform(getUi().getUIContext());
			mPairAdapter = new PairedDeviceAdapter(getUiContext(), getModel().getPairList(), isAc8257_YQQD_DY801, true);
			mPairAdapter.setItemClickListener(new PairedDeviceAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					if (isAc8257_YQQD_DY801) {
						// 选中
						mPairAdapter.notifyDataSetChanged();
						mNewAdapter.setSelectedItem(-1);
					} else {
						BluetoothDevice device = mPairAdapter.getItem(position);
						toLinkOrUnLinkDevice(device);
					}
				}

				@Override
				public void onLongClick(View view, int position) {
					BluetoothDevice device = mPairAdapter.getItem(position);
					if (getUi() != null) {
						getUi().showUnPairDialog(device);
					}
				}
			});
		}
		return mPairAdapter;
	}

	@Override
	public void searchNewDevice() {
		getModel().loadSearchlist();
	}

	@Override
	public void loadPairDevice() {
		getModel().loadPairlist();
	}

	@Override
	public void unpairDevice(BluetoothDevice device) {
		Logcat.d("unpair device: " + device.addr);
		if (!isPowerOn()) {
			Logcat.d("!isBtConnected, return");
			return;
		}
		BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
		if (btManager == null) return;
		btManager.deleteDevice(device.addr, mBtExeCall);
	}

	@Override
	public void linkSelectedDevice(boolean link) {
		Logcat.w("link: " + link);
		if (!isPowerOn()) {
			Logcat.d("!isPowerOn, return");
			return;
		}
		BluetoothManager btManager = BtApplication.getInstance().getBluetoothManager();
		if (null == btManager) {
			Logcat.w("getBluetoothManager is null");
			return;
		}
		stopSearch(btManager);
		BluetoothDevice device = mPairAdapter.getSelectedItem();
		if (device == null) {
			device = mNewAdapter.getSelectedItem();
			if (device == null) {
				Logcat.w("device: " + device);
				return;
			}
		}

		BluetoothDevice currentConnectDevice = BluetoothCacheUtil.getInstance().getCurConnectedDevice();
		Logcat.d("addr: " + device.addr + " curr.addr: " + currentConnectDevice.addr);
		if (!link && TextUtils.equals(device.addr, currentConnectDevice.addr)) {
			btManager.unlinkDevice(mBtExeCall);
		} else if (link) {
			btManager.linkDevice(device.addr, mBtExeCall);
		}
	}

	private void stopSearch(BluetoothManager btManager) {
		btManager.stopSearchNewDevice(0, null);
		onSearchEnd();
	}

	@Override
	public boolean isPowerOn() {
		return getModel().isPowerOn();
	}

	@Override
	public boolean setPower(boolean state) {
		return BluetoothModelUtil.getInstance().setPower(state);
	}

	@Override
	public void modifyModuleName(String name) {
		BluetoothModelUtil.getInstance().modifyModuleName(name);
	}

	@Override
	public void modifyModulePIN(String pin) {
		BluetoothModelUtil.getInstance().modifyModulePIN(pin);
	}

	@Override
	public String getBluetoothDeviceName() {
		return BluetoothModelUtil.getInstance().getBluetoothName();
	}

	@Override
	public String getBluetoothDevicePin() {
		return BluetoothModelUtil.getInstance().getBluetoothPin();
	}

	@Override
	public void linkStatusChanged(String addr, int status) {
		getModel().linkStatusChanged(addr, status);
	}

	@Override
	public void onPairlistResult(ArrayList<BluetoothDevice> data) {
		if (getUi() != null)
			getUi().updatePairAdapter();
	}

	@Override
	public void onSearchlistResult(ArrayList<BluetoothDevice> data, int type, int start, int count) {
		if (getUi() != null)
			getUi().updateDiscoveryAdapter(type, start, count);
	}

	@Override
	public void onSearchEnd() {
		Logcat.d();
		if (getUi() != null)
			getUi().updateProgressState(false);
	}

	@Override
	public Context getUiContext() {
		if (getUi() != null) {
			return getUi().getUIContext();
		}
		return null;
	}
}