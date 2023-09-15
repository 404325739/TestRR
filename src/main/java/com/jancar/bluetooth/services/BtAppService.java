package com.jancar.bluetooth.services;

import android.widget.Toast;

import com.jancar.bluetooth.service.BtBaseService;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.floatbar.FloatPhoneCallWindowManager;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;

/**
 * 蓝牙App服务.
 */

public class BtAppService extends BtBaseService {

	int lastConnectStatus = IVIBluetooth.BluetoothConnectStatus.DISCONNECTED;
	@Override
	public void onCreate() {
		super.onCreate();
		Logcat.d();
	}

	@Override
	protected void createPhoneCallWindowManager() {
		FloatPhoneCallWindowManager.getInstance().init(this);
	}

	@Override
	protected void removePhoneCallWindowManager() {
		FloatPhoneCallWindowManager.removeInstance();
	}

	@Override
	public void onEventLinkDevice(IVIBluetooth.EventLinkDevice event) {
		// 可能存在时序问题，这里自己记录吧
//		int lastConnectStatus = BluetoothCacheUtil.getInstance().getBluzConnectedStatus();
		Logcat.d(" ConnectedStatus " + event.status + " /// " + lastConnectStatus);
//        if (BluetoothCacheUtil.getInstance().getBluzConnectedStatus() != event.status) {
//            if (event.status == IVIBluetooth.BluetoothConnectStatus.CONNECTED) {
//                Toast.makeText(this, getString(R.string.bt_link_success), Toast.LENGTH_SHORT).show();
//            } else if (event.status == IVIBluetooth.BluetoothConnectStatus.DISCONNECTED) {
//                Toast.makeText(this, getString(R.string.bt_unlink_success), Toast.LENGTH_SHORT).show();
//            }
//        }

		if (lastConnectStatus == IVIBluetooth.BluetoothConnectStatus.CONNECTED
				&& (event.status == IVIBluetooth.BluetoothConnectStatus.DISCONNECTED
				|| event.status == IVIBluetooth.BluetoothConnectStatus.CONNECTFAIL)) {
			// un link bt
			Toast.makeText(this, getString(R.string.bt_unlink_success), Toast.LENGTH_SHORT).show();
		} else if (lastConnectStatus != IVIBluetooth.BluetoothConnectStatus.CONNECTED &&
				event.status == IVIBluetooth.BluetoothConnectStatus.CONNECTED) {
			// link bt
			Toast.makeText(this, getString(R.string.bt_link_success), Toast.LENGTH_SHORT).show();

		}
		if (IVIBluetooth.BluetoothConnectStatus.CONNECTED == lastConnectStatus &&
				(IVIBluetooth.BluetoothConnectStatus.BOND_BONDING == event.status
						|| IVIBluetooth.BluetoothConnectStatus.BOND_BONDED == event.status
						|| IVIBluetooth.BluetoothConnectStatus.BOND_BONDNONE == event.status
				)) {
			Logcat.w("has connected, not handle bond state: " + event.status);

		} else {
			lastConnectStatus = event.status;
		}
		super.onEventLinkDevice(event);
	}
}
