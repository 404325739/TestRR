package com.jancar.bluetooth.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jancar.bluetooth.contract.DownStateEvent;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.contract.RecordsContract;
import com.jancar.bluetooth.manager.RecyclerManager;
import com.jancar.bluetooth.presenter.RecordsPresenter;
import com.jancar.bluetooth.utils.PBDownLoadStateUtil;
import com.jancar.bluetooth.utils.ThreadUtils;
import com.jancar.bluetooth.utils.ToastUtil;
import com.jancar.bluetooth.view.RecycleViewDivider;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.bean.StCallHistory;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tzq
 * @date 2019-12-26 20:09:21
 */
public class RecordsFragment extends BaseFragment<RecordsContract.Presenter, RecordsContract.View> implements RecordsContract.View, View.OnClickListener {

    private View mView = null;
    private RecyclerView mRecyclerView;
    private boolean mIsRegisteredEventBus = false;                                                  // 是否已经注册EventBus
    private TextView mTvTip;                                                                        //状态等提示
    private static final int UPDATE_VIEW = 10001;                                                   //下载完成后更新View

    private ImageView mIvCall, mIvReserve,  mIvDel; // 拨号、空白键、删除
    private ImageView mIvRecordOut, mIvRecordIn,  mIvRecordMissed; // 去电、来电、未接
    private int mCurrType = IVIBluetooth.BluetoothCallHistoryStatus.ALL_STATUS;
    boolean hasLoad = false;
    Map<Integer, View> mTypeBtns = new HashMap<>();;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerEventBus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_records, container, false);
            initView();
        }
        return mView;
    }

    @Override
    public void onDestroyView() {
        getPresenter().stopContactOrHistoryLoad();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTip(getPresenter().isBtConnected());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Logcat.d(" onHiddenChanged  ：" + hidden);
        if (hidden) {
//            showLoading(false);
        } else {
            updateTip(getPresenter().isBtConnected());
        }
    }

    /**
     * 蓝牙未连接时，界面提示语
     *
     * @param isConnected
     */
    private void updateTip(boolean isConnected) {
        if (null != mTvTip && null != mRecyclerView) {
            if (isConnected && !isHidden()) {
                if(PBDownLoadStateUtil.isDownLoading()){
                    ToastUtil.getInstance().showToast(R.string.download_wait_history_tip, Toast.LENGTH_LONG);
                }
                mTvTip.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                loadCallHistoryList();
            } else {
                mTvTip.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mTvTip.setText(R.string.tv_contact_blu_tip);
                getPresenter().clearHistoryList();
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

    public RecordsFragment() {

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_VIEW:
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
        if (null != mTvTip && null != mRecyclerView) {
            if (listSize > 0) {
                mTvTip.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mTvTip.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mTvTip.setText(R.string.tv_record_null);
            }
        }
    }


    @Override
    public RecordsContract.Presenter createPresenter() {
        return new RecordsPresenter();
    }

    @Override
    public RecordsContract.View getUiImplement() {
        return this;
    }

    /**
     * 下载通话记录
     */
    private void loadCallHistoryList() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                loadCallHistoryList(mCurrType);
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDownState(DownStateEvent event){
        if(event != null && event.mState == DownStateEvent.STATE_START){
            if(getPresenter() != null){
                getPresenter().clearHistoryList();
            }
        }
    }

    /**
     * 初始化View
     */
    private void initView() {
        if (mView != null) {
            mRecyclerView = mView.findViewById(R.id.records_recycler);
            mTvTip = mView.findViewById(R.id.tv_records_tip);
            mRecyclerView.setLayoutManager(new RecyclerManager(getUIContext()));
            if (AppUtils.isAc8257_YQQD_DY801Platform(getContext())) {
                mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(),
                        LinearLayoutManager.HORIZONTAL, R.drawable.iv_contact_list_div));
            } else {
                mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, (int) getResources().getDimension(R.dimen.line_heigth),
                        getContext().getResources().getColor(R.color.divider)));
            }
            mRecyclerView.setAdapter(getPresenter().getAdapter());
            mIvCall = mView.findViewById(R.id.iv_contact_call);
            mIvDel = mView.findViewById(R.id.iv_contact_del);
            mIvRecordOut = mView.findViewById(R.id.iv_record_type_out);
            mIvRecordIn = mView.findViewById(R.id.iv_record_type_in);
            mIvRecordMissed = mView.findViewById(R.id.iv_record_type_miss);
            mTypeBtns.put(IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS, mIvRecordOut);
            mTypeBtns.put(IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS, mIvRecordIn);
            mTypeBtns.put(IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS, mIvRecordMissed);
            mIvRecordOut.setOnClickListener(mRecordTypeListener);
            mIvRecordIn.setOnClickListener(mRecordTypeListener);
            mIvRecordMissed.setOnClickListener(mRecordTypeListener);
            mIvCall.setOnClickListener(this);
            mIvDel.setOnClickListener(this);

            // Ac8257_YQQD_DY801，空白按键？
            mIvReserve = mView.findViewById(R.id.iv_contact_load);
            if (null != mIvReserve) {
                mIvReserve.setBackgroundResource(R.drawable.iv_records_reserve);
            }
        }
    }


    @Override
    public void synchronousFinish(int code, String msg, int listSize) {
        Logcat.d("++synchronousFinish code++:" + code + "  ++msg++:" + msg);
        Message message = new Message();
        message.what = UPDATE_VIEW;
        message.arg1 = listSize;
        if (null != mHandler) {
            mHandler.sendMessage(message);
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

    @Override
    public void onProgress(List<StCallHistory> callHistory) {
        Logcat.d("++onProgress++:" + callHistory.size());

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
                getPresenter().clearHistoryList();
                hasLoad = false;
            }
            updateTip(event.isConnected());
        }
    }





    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHistoryReachedOnce(EventHistoryReachedOnce event) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_contact_call:
                getPresenter().callSelectedRecord();
                break;
            case R.id.iv_contact_del:
                getPresenter().delSelectedRecord();
                break;
        }
    }

    private View.OnClickListener mRecordTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int type = -1;
            switch (v.getId()) {
                case R.id.iv_record_type_out:
                    type = IVIBluetooth.BluetoothCallHistoryStatus.CALLED_STATUS;
                    break;
                case R.id.iv_record_type_in:
                    type = IVIBluetooth.BluetoothCallHistoryStatus.LISTEN_STATUS;
                    break;
                case R.id.iv_record_type_miss:
                    type = IVIBluetooth.BluetoothCallHistoryStatus.MISS_STATUS;
                    break;
            }
            int lastType = mCurrType;
            Logcat.d("lastType: " + lastType + " clickType: " + type);
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                View view = mTypeBtns.get(lastType);
                if (null != view) {
                    view.setSelected(false);
                }
                mCurrType = type;
            } else {
                mCurrType = IVIBluetooth.BluetoothCallHistoryStatus.ALL_STATUS;
            }
            if (lastType != mCurrType) {
                getPresenter().setRecordType(mCurrType);
            }
            loadCallHistoryList(mCurrType);
        }
    };
    /**
     * 避免首次后台刷新列表切到页面后显示错位
     */
    public static class EventHistoryReachedOnce {

        public static void onEvent() {
            EventBus.getDefault().post(new EventHistoryReachedOnce());
        }

        private EventHistoryReachedOnce() {
            // Empty
        }
    }

    public void loadCallHistoryList(int type) {
        boolean isGetBluzModel = hasLoad;
        hasLoad = true;
//        getPresenter().loadCallHistoryList(isGetBluzModel);
        getPresenter().loadCallHistoryList(false);
    }
}
