package com.jancar.bluetooth.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Switch;
import android.widget.TextView;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.contract.SettingsContract;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.bluetooth.presenter.SettingsPresenter;
import com.jancar.bluetooth.view.DialogView;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SettingsFragment extends BaseFragment<SettingsContract.Presenter, SettingsContract.View> implements SettingsContract.View {

    View mView;
    Switch mSwitchBtSwitch, mSwitchAutoConnect, mSwitchAutoAnswer;
    boolean mBtSwitch, mAutoLink, mAutoAnwer;
    Button mBtnName, mBtnPin;
    String mBtDeviceName, mBtDevicePin;
    Dialog mEditDialog;
    TextView mEditTitle;
    EditText mEditText;
    int mEditType;

    @Override
    public SettingsContract.Presenter createPresenter() {
        return new SettingsPresenter();
    }

    @Override
    public SettingsContract.View getUiImplement() {
        return this;
    }

    @Override
    public Context getUIContext() {
        return getContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logcat.d();
        registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Logcat.d();
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_settings, container, false);
            init();
            initView();
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logcat.d();
        unregisterEventBus();
    }

    View.OnClickListener mSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Logcat.d("curr mBtSwitch: " + mBtSwitch + " mAutoConnect: " + mAutoLink + " mAutoAnwer: " + mAutoAnwer);
            /*switch (v.getId()) {
                case R.id.switch_settings_btswitch:
                    getPresenter().setPower(!mBtSwitch);
                    break;
                case R.id.switch_settings_autoconnect:
                    getPresenter().setAutoLink(!mAutoLink);
                    break;
                case R.id.switch_settings_autoanswer:
                    getPresenter().setAutoAnswer(!mAutoAnwer);
                    break;
            }*/
        }
    };

    CompoundButton.OnCheckedChangeListener mOnCheckChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Logcat.d(isChecked + " curr mBtSwitch: " + mBtSwitch + " mAutoConnect: " + mAutoLink + " mAutoAnwer: " + mAutoAnwer);
            switch (buttonView.getId()) {
                case R.id.switch_settings_btswitch:
                    mBtSwitch = isChecked;
                    getPresenter().setPower(isChecked);
                    break;
                case R.id.switch_settings_autoconnect:
                    mAutoLink = isChecked;
                    getPresenter().setAutoLink(isChecked);
                    break;
                case R.id.switch_settings_autoanswer:
                    mAutoAnwer = isChecked;
                    getPresenter().setAutoListen(isChecked);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventPowerState(IVIBluetooth.EventPowerState state) {
        Logcat.d("btstate: " + state.value + " mBtSwitch: " + mBtSwitch);
        mBtSwitch = state.value;
        mSwitchBtSwitch.setChecked(mBtSwitch);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBtStageChange(EventClassDefine.EventBtStageChange event) {
        Logcat.d("value: " + event.value);
        mBtSwitch = event.value;
        if (null != mSwitchBtSwitch) {
            mSwitchBtSwitch.setChecked(mBtSwitch);
        }

        updateState();
    }

    private void updateState() {
        mAutoLink = getPresenter().isAutoLinkOn();
        mAutoAnwer = getPresenter().isAutoListen();
        if (null != mSwitchAutoConnect) {
            mSwitchAutoConnect.setChecked(mAutoLink);
            mSwitchAutoAnswer.setChecked(mAutoAnwer);
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

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventAutoLink(IVIBluetooth.EventAutoLink state) {
        Logcat.d("btautolink: " + state + " mAutoLink: " + mAutoLink);
        mAutoLink = state.value;
        mSwitchAutoConnect.setChecked(mAutoLink);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventAutoAnswer(IVIBluetooth.EventAutoAnswer state) {
        Logcat.d("btautoanser: " + state);
        mSwitchAutoAnswer.setChecked(state.value);
    }*/

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

    private void init() {
        mBtSwitch = getPresenter().isPowerOn();
        mAutoLink = getPresenter().isAutoLinkOn();
        mAutoAnwer = getPresenter().isAutoListen();
        mBtDeviceName = getPresenter().getBluetoothDeviceName();
        mBtDevicePin = getPresenter().getBluetoothDevicePin();
    }

    private void initView() {
        if (mView != null) {
            Logcat.d();
            mSwitchBtSwitch = mView.findViewById(R.id.switch_settings_btswitch);
            mSwitchAutoConnect = mView.findViewById(R.id.switch_settings_autoconnect);
            mSwitchAutoAnswer = mView.findViewById(R.id.switch_settings_autoanswer);
            if (null != mSwitchBtSwitch) {
                mSwitchBtSwitch.setChecked(mBtSwitch);
                mSwitchAutoConnect.setChecked(mAutoLink);
                mSwitchAutoAnswer.setChecked(mAutoAnwer);

                mSwitchBtSwitch.setOnClickListener(mSwitchClickListener);
                mSwitchAutoConnect.setOnClickListener(mSwitchClickListener);
                mSwitchAutoAnswer.setOnClickListener(mSwitchClickListener);

                mSwitchBtSwitch.setOnCheckedChangeListener(mOnCheckChangedListener);
                mSwitchAutoConnect.setOnCheckedChangeListener(mOnCheckChangedListener);
                mSwitchAutoAnswer.setOnCheckedChangeListener(mOnCheckChangedListener);
            }

            mBtnName = mView.findViewById(R.id.btn_settings_name);
            mBtnPin = mView.findViewById(R.id.btn_settings_pin);
            Logcat.d(mBtDeviceName + " " + mBtDevicePin);
            if (null != mBtnName) {
                mBtnName.setText(mBtDeviceName);
                mBtnPin.setText(mBtDevicePin);
                mBtnName.setOnClickListener(mRenameListener);
                // 不支持设置Pin？
                // mBtnPin.setOnClickListener(mRenameListener);
            }
        }
    }

    /**
     * 显示编辑框
     *
     * @param edittype 0: name; 1: pin;
     */
    private void showEditDialog(int edittype) {
        mEditType = edittype;
        Logcat.d(0 == edittype ? "name" : "pin");
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
        } else {
//                            BtUtils.showToast(R.string.bluetooth_device_info_toast);
        }
    }
}
