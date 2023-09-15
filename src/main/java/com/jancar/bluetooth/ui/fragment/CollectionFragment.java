package com.jancar.bluetooth.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.contract.CollectionContract;
import com.jancar.bluetooth.manager.RecyclerManager;
import com.jancar.bluetooth.presenter.CollectionPresenter;
import com.jancar.bluetooth.utils.ThreadUtils;
import com.jancar.bluetooth.view.RecycleViewDivider;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Tzq
 * @date 2019-12-26 20:10:01
 */
public class CollectionFragment extends BaseFragment<CollectionContract.Presenter, CollectionContract.View> implements CollectionContract.View {


    private View mView = null;
    private RecyclerView mRecyclerView;
    private TextView mTvTip;                              //提示信息
    private volatile boolean mIsRegisteredEventBus = false;
    private static final int UPDATE_VIEW = 10002;                                                   //下载完成后更新View


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
                mTvTip.setText(R.string.tv_collection_null);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logcat.d("++onAttach++");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logcat.d("++onCreate++");
        registerEventBus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logcat.d("++onCreateView++");
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_collection, container, false);
            initView();
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logcat.d("++onResume++");
        updateTip(getPresenter().isBtConnected());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateTip(getPresenter().isBtConnected());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logcat.d("++onDestroy++");
        unregisterEventBus();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 蓝牙未连接时，界面提示语
     *
     * @param isConnected
     */
    private void updateTip(boolean isConnected) {
        if (null != mTvTip && null != mRecyclerView) {
            if (isConnected) {
                mTvTip.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                loadCollectionList();
            } else {
                mTvTip.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mTvTip.setText(R.string.tv_contact_blu_tip);
                //蓝牙未连接，清空收藏列表
                getPresenter().clearCollectList();
            }
        }
    }


    /**
     * 获取收藏列表
     */
    private void loadCollectionList() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                getPresenter().loadCollectListFirst();
            }
        });
    }

    /**
     * 初始化View
     */
    private void initView() {
        if (mView != null) {
            Logcat.d("++initView++");
            mRecyclerView = mView.findViewById(R.id.collection_recycler);
            mTvTip = mView.findViewById(R.id.tv_collection_tip);
            mTvTip.setMovementMethod(ScrollingMovementMethod.getInstance());
            mRecyclerView.setLayoutManager(new RecyclerManager(getUIContext()));
            mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, (int) getResources().getDimension(R.dimen.line_heigth),
                    getContext().getResources().getColor(R.color.divider)));
            mRecyclerView.setAdapter(getPresenter().getAdapter());
        }
    }

    public CollectionFragment() {

    }

    @Override
    public CollectionContract.Presenter createPresenter() {
        return new CollectionPresenter();
    }

    @Override
    public CollectionContract.View getUiImplement() {
        return this;
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
    public void synchronousFinish(int code, String msg, int listSize) {
        Logcat.d("++synchronousFinish listSize:" + listSize);
        Message message = new Message();
        message.what = UPDATE_VIEW;
        message.arg1 = listSize;
        if (null != mHandler) {
            mHandler.sendMessage(message);
        }
    }


    /**
     * 注册EventBus
     */
    private void registerEventBus() {
        if (!mIsRegisteredEventBus) {
            mIsRegisteredEventBus = true;
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 反注册EventBus
     */
    private void unregisterEventBus() {
        if (mIsRegisteredEventBus) {
            mIsRegisteredEventBus = false;
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 联系人改变监听
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBtContactChange(EventClassDefine.EventBtContactChange event) {
        Logcat.d("++BtContactChange++");
        updateTip(getPresenter().isBtConnected());
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
                getPresenter().clearCollectList();
            }
            updateTip(event.isConnected());
        }
    }
}
