package com.jancar.bluetooth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.contract.DialerContract;
import com.jancar.bluetooth.presenter.DialerPresenter;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Tzq
 * @date 2019-12-26 20:07:30
 */
public class DialerFragment extends BaseFragment<DialerContract.Presenter, DialerContract.View> implements DialerContract.View,
        View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "DialerFragment";
    int[] mNums = new int[]{R.id.dial_num_1, R.id.dial_num_2, R.id.dial_num_3, R.id.dial_num_4, R.id.dial_num_5, R.id.dial_num_6,
            R.id.dial_num_7, R.id.dial_num_8, R.id.dial_num_9, R.id.dial_num_star, R.id.dial_num_0, R.id.dial_num_10, R.id.iv_btn_del};
    private View mView = null;
    private ImageView mImSetting;
    private ImageView mImCall;
    //电话号码输入显示
    private TextView mTvNum;
    private String mPhoneNumber = "";

    // 根布局
    private LinearLayout mllDialRoot;
    // 用来占位置的空View
    private View mFillviewDial;
    private boolean mFullHeight = true;

    public DialerFragment() {

    }

    /**
     * Handles secret codes to launch arbitrary activities in the form of *#*#<code>#*#*.
     * If a secret code is encountered an Intent is started with the android_secret_code://<code>
     * URI.
     *
     * @param context the context to use
     * @param input   the text to check for a secret code in
     * @return true if a secret code was encountered
     */
    static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*
        int len = input.length();
        if (len > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {

            // com.android.internal.telephony.TelephonyIntents.SECRET_CODE_ACTION
            // sdk >= 28 -> android.provider.Telephony.Sms.Intents.SECRET_CODE_ACTION

            context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE",
                    Uri.parse("android_secret_code://" + input.substring(4, len - 4))));
            return true;
        }

        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logcat.d("++onAttach++");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logcat.d("++onCreate++");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logcat.d("++onCreateView++");
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_dialer, container, false);
            initView();
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logcat.d("++onResume++");
        if (TextUtils.isEmpty(mPhoneNumber)) {
            updateBtConnectedStatus(getPresenter().isBtConnected());
        } else {
            mTvNum.setText(mPhoneNumber);
        }

//        if (AppUtils.isAc8227Platform(getContext())) {
//				mImCall.setVisibility(View.VISIBLE);
//				mImSetting.setVisibility(View.GONE);
//		}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logcat.d("++onDestroy++");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public DialerContract.Presenter createPresenter() {
        return new DialerPresenter();
    }

    @Override
    public DialerContract.View getUiImplement() {
        return this;
    }

    private void initView() {
        if (mView != null) {
            for (int i = 0; i < mNums.length; i++) {
                mView.findViewById(mNums[i]).setOnClickListener(this);
            }
            mImCall = mView.findViewById(R.id.iv_btn_call);
            mImSetting = mView.findViewById(R.id.iv_btn_set);
            mTvNum = mView.findViewById(R.id.tv_dial_num);
//			mTvNum.setMovementMethod(ScrollingMovementMethod.getInstance());
            mImCall.setOnClickListener(this);
            mImSetting.setOnClickListener(this);
            mView.findViewById(R.id.iv_btn_del).setOnLongClickListener(this);
            mView.findViewById(R.id.dial_num_0).setOnLongClickListener(this);

            mTvNum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String phoneNum = s.toString();
                    if (TextUtils.isEmpty(phoneNum)) {
                        if (getPresenter().isBtConnected()) {
                            mTvNum.setText(R.string.tv_dial_conn);
                        } else {
                            mTvNum.setText(R.string.tv_dial_not_conn);
                        }
                    } else {
                        if (handleSecretCode(DialerFragment.this.getContext(), phoneNum)) {
                            mPhoneNumber = "";
                            if (getPresenter().isBtConnected()) {
                                mTvNum.setText(R.string.tv_dial_conn);
                            } else {
                                mTvNum.setText(R.string.tv_dial_not_conn);
                            }
                        }
                    }
//					float lenght = ((DialerPresenter)getPresenter()).getTextLenght(phoneNum,mTvNum.getTextSize());
//					int width = mTvNum.getWidth();
//
//					if(width != 0 && lenght > width){
//						mTvNum.scrollTo((int)(lenght - width),0);
//					}
//					Logcat.d(TAG,"width =" + width + ", lenght = " + lenght + ", scrollx ="+ mTvNum.getScrollX());

                }
            });

            mFillviewDial = mView.findViewById(R.id.fillview_dial);
            mllDialRoot = mView.findViewById(R.id.ll_dial_root);
        }
    }

    @Override
    public void onClick(View v) {
        int position = -1;
        switch (v.getId()) {
            case R.id.dial_num_0:
                position = IVIBluetooth.KeyPositionDefined.ZERO_POSITION;
                break;
            case R.id.dial_num_1:
                position = IVIBluetooth.KeyPositionDefined.ONE_POSITION;
                break;
            case R.id.dial_num_2:
                position = IVIBluetooth.KeyPositionDefined.TWO_POSITION;
                break;
            case R.id.dial_num_3:
                position = IVIBluetooth.KeyPositionDefined.THREE_POSITION;
                break;
            case R.id.dial_num_4:
                position = IVIBluetooth.KeyPositionDefined.FOUR_POSITION;
                break;
            case R.id.dial_num_5:
                position = IVIBluetooth.KeyPositionDefined.FIVE_POSITION;
                break;
            case R.id.dial_num_6:
                position = IVIBluetooth.KeyPositionDefined.SIX_POSITION;
                break;
            case R.id.dial_num_7:
                position = IVIBluetooth.KeyPositionDefined.SEVEN_POSITION;
                break;
            case R.id.dial_num_8:
                position = IVIBluetooth.KeyPositionDefined.EIGHT_POSITION;
                break;
            case R.id.dial_num_9:
                position = IVIBluetooth.KeyPositionDefined.NINE_POSITION;
                break;
            case R.id.dial_num_star:
                position = IVIBluetooth.KeyPositionDefined.ASTERISK_POSITION;
                break;
            case R.id.dial_num_10:
                position = IVIBluetooth.KeyPositionDefined.POUND_POSITION;
                break;
            case R.id.iv_btn_del:
                if (!TextUtils.isEmpty(mPhoneNumber)) {
                    mPhoneNumber = mPhoneNumber.substring(0, mPhoneNumber.length() - 1);
                    mTvNum.setText(mPhoneNumber);
                }
                Logcat.d("del:" + mPhoneNumber);
                break;
            case R.id.iv_btn_call:
                callPhone();
                break;
            case R.id.iv_btn_set:
                if (AppUtils.isAc8257_YQQD_DY801Platform(getContext())) {
                    // Ac8257_YQQD_DY801挂断功能
                } else {
                    getPresenter().go2Setting(getContext());
                }
                break;
        }
        //将结果显示到TextView中
        if (position >= 0 && position < IVIBluetooth.BluetoothDTMFCode.DTMF_CODE.length) {
            /*if (mTvNum.getText().length() >= FloatWindowView.MAX_NUMBER_LEN) {
                Toast.makeText(getContext(), R.string.number_len_max, Toast.LENGTH_SHORT).show();
                return;
            }*/
            mPhoneNumber += IVIBluetooth.BluetoothDTMFCode.DTMF_CODE[position];// 添加一个字符
            mTvNum.setText(mPhoneNumber);
        }
    }

    /**
     * 拨打电话
     */
    private void callPhone() {
        Logcat.d("callPhone " + mPhoneNumber);
        if (TextUtils.isEmpty(mPhoneNumber)) {
            //如果为空,则拨打上一次的电话号码
            mPhoneNumber = getPresenter().getLastCallNum();
            mTvNum.setText(mPhoneNumber);
        } else {
            //拨打电话
            getPresenter().callPhone(mPhoneNumber);
            //拨打完成之后,更新TextView显示
            mPhoneNumber = "";
            mTvNum.setText(mPhoneNumber);
        }
    }

    /**
     * 当拨打的电话号码过短时的提示
     */
    @Override
    public void showCallTip(int strId) {
        if (getContext() != null) {
            Toast.makeText(getContext(), strId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getUIContext() {
        return getContext();
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.iv_btn_del:
                //长按删除按钮,清空号码,显示默认提示
                onLongDel();
                break;
            case R.id.dial_num_0:
                mPhoneNumber += IVIBluetooth.BluetoothDTMFCode.DTMF_CODE[IVIBluetooth.KeyPositionDefined.PLUS_POSITION];
                mTvNum.setText(mPhoneNumber);
                break;
        }

        return true;
    }

    private void onLongDel() {
        if (!TextUtils.isEmpty(mPhoneNumber)) {
            mPhoneNumber = "";
            if (getPresenter().isBtConnected()) {
                mTvNum.setText(R.string.tv_dial_conn);
            } else {
                mTvNum.setText(R.string.tv_dial_not_conn);
            }
        }
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
            Logcat.d("++onEventLinkDevice++:" + event.status);
            updateBtConnectedStatus(event.isConnected());
        }
    }

    /**
     * 更新界面
     *
     * @param status
     */
    private void updateBtConnectedStatus(boolean status) {
        Logcat.d("++updateBtConnectedStatus++:" + status);
        if (status) {
            //蓝牙连接
            if (!AppUtils.isAc8257_YQQD_DY801Platform(getContext())) {
                mImSetting.setVisibility(View.GONE);
                mImCall.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(mPhoneNumber)) {
                mTvNum.setText(R.string.tv_dial_conn);
            } else {
                mTvNum.setText(mPhoneNumber);
            }
        } else {
//            if (!AppUtils.isAc8227Platform(getContext())) {
//                mImSetting.setVisibility(View.VISIBLE);
//                mImCall.setVisibility(View.GONE);
//            }
            if (!AppUtils.isAc8257_YQQD_DY801Platform(getContext())) {
                mImSetting.setVisibility(View.VISIBLE);
                mImCall.setVisibility(View.GONE);
            }
            if (TextUtils.isEmpty(mPhoneNumber)) {
                mTvNum.setText(R.string.tv_dial_not_conn);
            } else {
                mTvNum.setText(mPhoneNumber);
            }
        }
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int width = mllDialRoot.getWidth();
        int height = mllDialRoot.getHeight();
    }
}
