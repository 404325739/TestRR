package com.jancar.bluetooth.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.contract.BtMusicContract;
import com.jancar.bluetooth.presenter.BtMusicPresenter;
import com.jancar.bluetooth.services.A2DPService;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.utils.PBDownLoadStateUtil;
import com.jancar.bluetooth.utils.ToastUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Tzq
 * @date 2019-12-26 20:09:21
 */
public class BtMusicFragment extends BaseFragment<BtMusicContract.Presenter, BtMusicContract.View> implements BtMusicContract.View, View.OnClickListener {

    private View mView = null;
    private TextView mTvTitle, mTvArtist; // 歌曲名和歌手名
    private ImageView mIvPlay; // 播放按钮
    private boolean mIsRegisteredEventBus = false;                                                  // 是否已经注册EventBus
    private TextView mTvTip;
    private boolean mIsThisShow = false; // 当前显示的是否为蓝牙音乐

    private A2DPService.A2DPCallback mA2DPCallback = new A2DPService.A2DPCallback() {
        @Override
        public void onId3InfoChanged(String name, String artist, String album, long duration) {
            Logcat.d("name: " + name + " artist: " + artist);
            if (mTvTitle != null) {
                mTvTitle.setText(name);
            }
            if (mTvArtist != null) {
                mTvArtist.setText(artist);
            }
        }

        @Override
        public void onA2dpStatusChanged(int status) {
            Logcat.d("mIsThisShow: " + mIsThisShow + " status: " + status);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerEventBus();
        mIsThisShow = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logcat.d("isBtConnected: " + getPresenter().isBtConnected());
        getPresenter().setA2DPCallback(mA2DPCallback);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_btmusic, container, false);
            initView();
        }
        return mView;
    }

    @Override
    public void onDestroyView() {
        getPresenter().release();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logcat.d("isBtConnected: " + getPresenter().isBtConnected() + " mIsThisShow: " + mIsThisShow);
        updateTip(getPresenter().isBtConnected());
        if (mIsThisShow) {
            getPresenter().setBtMusicOn(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Logcat.d("isBtConnected: " + getPresenter().isBtConnected());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Logcat.d(" onHiddenChanged  ：" + hidden);
        mIsThisShow = !hidden;
        if (hidden) {
//            showLoading(false);
        } else {
            updateTip(getPresenter().isBtConnected());
        }
        getPresenter().setBtMusicOn(mIsThisShow);
    }

    /**
     * 蓝牙未连接时，界面提示语
     *
     * @param isConnected
     */
    private void updateTip(boolean isConnected) {
        if (null != mTvTip) {
            if (isConnected && !isHidden()) {
                if (PBDownLoadStateUtil.isDownLoading()) {
                    ToastUtil.getInstance().showToast(R.string.download_wait_history_tip, Toast.LENGTH_LONG);
                }
                mTvTip.setVisibility(View.GONE);
            } else {
                mTvTip.setVisibility(View.VISIBLE);
                mTvTip.setText(R.string.tv_contact_blu_tip);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterEventBus();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public BtMusicFragment() {

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    updateView(msg.arg1);
                    break;

            }
        }
    };

    /**
     * 下载完成之后更新View
     *
     * @param listSize
     */
    private void updateView(int listSize) {
        if (null != mTvTip) {
            if (listSize > 0) {
                mTvTip.setVisibility(View.GONE);
            } else {
                mTvTip.setVisibility(View.VISIBLE);
                mTvTip.setText(R.string.tv_record_null);
            }
        }
    }


    @Override
    public BtMusicContract.Presenter createPresenter() {
        return new BtMusicPresenter();
    }

    @Override
    public BtMusicContract.View getUiImplement() {
        return this;
    }

    /**
     * 初始化View
     */
    private void initView() {
        if (mView != null) {
            mTvTip = mView.findViewById(R.id.tv_records_tip);

            // 歌手名，歌曲名
            mTvTitle = (TextView) mView.findViewById(R.id.tv_title);
            mTvArtist = (TextView) mView.findViewById(R.id.tv_artist);

            // 按键监听
            mIvPlay = (ImageView) mView.findViewById(R.id.iv_play);
            mView.findViewById(R.id.iv_pre).setOnClickListener(this);
            mView.findViewById(R.id.iv_next).setOnClickListener(this);
            mIvPlay.setOnClickListener(this);
        }
    }

    @Override
    public Context getUIContext() {
        return getContext();
    }

    @Override
    public void runOnUIThread(Runnable runnable) {
        if (mHandler != null) {
            mHandler.post(runnable);
        }
    }

    private void registerEventBus() {
        if (!mIsRegisteredEventBus) {
            mIsRegisteredEventBus = true;
            EventBus.getDefault().register(this);
        }
    }

    private void unregisterEventBus() {
        if (mIsRegisteredEventBus) {
            mIsRegisteredEventBus = false;
            EventBus.getDefault().unregister(this);
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
            if (BluetoothCacheUtil.getInstance().isNewConntectedDevice()) {

            }
            updateTip(event.isConnected());
        }
    }

    @Override
    public void onClick(View v) {
        if (!getPresenter().isBtConnected()) {
//            showToast(R.string.bt_connect_device);
            return;
        }
        switch (v.getId()) {
            case R.id.iv_pre:
                getPresenter().prev();
                break;
            case R.id.iv_play:
                getPresenter().playAndPause();
                break;
            case R.id.iv_next:
                getPresenter().next();
                break;
        }
    }
}
