package com.jancar.bluetooth.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jancar.bluetooth.utils.BluetoothCacheUtil;
import com.jancar.bluetooth.contract.ContactsContract;
import com.jancar.bluetooth.contract.DownStateEvent;
import com.jancar.bluetooth.manager.RecyclerManager;
import com.jancar.bluetooth.presenter.ContactsPresenter;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.ThreadUtils;
import com.jancar.bluetooth.utils.ToastUtil;
import com.jancar.bluetooth.view.LoadingView;
import com.jancar.bluetooth.view.RecycleViewDivider;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.bean.StPhoneBook;
import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.widget.LetterSlideBar;
import com.ui.mvp.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * @author Tzq
 * @date 2019-12-26 20:06:24
 */
public class ContactsFragment extends BaseFragment<ContactsContract.Presenter, ContactsContract.View> implements ContactsContract.View, View.OnClickListener {
    private static final String TAG = "ContactsFragment";
    private View mView = null;
    private RecyclerView mRecyclerView;
    private ImageView mImLoad;
    private ImageView mImSearch;
    private EditText mEditInput;
    private TextView mTvTip;                        //界面提示语
    private LoadingView mLoadingView;               //下载提示框
    private boolean mIsRegisteredEventBus = false;
    protected boolean mIsLoadContract = false;      // 是否已经加载过 联系人
    private boolean isFistLaunch = false;//是否首次加载，下载电话本的回调在退出后已经消失


    public static final int SEEK_CONTACT_LEN = 3;   // 用户输入的搜索联系人的号码长度
    public static final int UPDATE_VIEW = 1000;     // 下载完成，更新UI

    private LetterSlideBar mLetterSlideBar; // 字母导航控件
    private ImageView mIvCall, mIvDel; // 拨号、删除

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
            mView = inflater.inflate(R.layout.fragment_contacts, container, false);
            initView();
        }
        isFistLaunch = true;
        return mView;
    }

    @Override
    public void onDestroyView() {
        getPresenter().stopContactOrHistoryLoad();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logcat.d("++onStart++");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logcat.d("++onResume++");
        updateConnState(getPresenter().isBtConnected());
    }

    private void loadPhoneListFirst() {
        Logcat.d("++loadPhoneListFirst++ mIsLoadContract :" + mIsLoadContract + " isDownloading =" + getPresenter().getDownloadState());
        if (getPresenter().isDownloading()) {
            if(getPresenter().getDownloadState() != 2){//在下载通话记录，不要显示窗口，但是如果首次进入，需要把回调重新设置上去
                showLoading(true);
            }
//            if(isFistLaunch){
//                onSyncContact();
//            }
//        } else if (!mIsLoadContract) {
        } else{
            //界面在后台连上不要去主动先下载电话本
            if(isHidden()){
               return;
            }
            mIsLoadContract = true;
//            showLoading(true);
            ThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    getPresenter().loadPhoneListFirst();
                }
            });
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        Logcat.d(" onHiddenChanged  ：" + hidden);
        if (hidden) {
            showLoading(false);
        } else {
            updateConnState(getPresenter().isBtConnected());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logcat.d("++onPause++");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logcat.d("++onStop++");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logcat.d("++onDestroy++");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != mLoadingView) {
            mLoadingView.release();
        }
        unregisterEventBus();
    }

    public ContactsFragment() {

    }

    @Override
    public ContactsContract.Presenter createPresenter() {
        return new ContactsPresenter();
    }

    @Override
    public ContactsContract.View getUiImplement() {
        return this;
    }

    private void initView() {
        if (mView != null) {
            mLoadingView = new LoadingView(getContext());

            mRecyclerView = mView.findViewById(R.id.contact_recycleView);
            mLetterSlideBar = mView.findViewById(R.id.letterbar);
            mEditInput = mView.findViewById(R.id.ed_contact);
            mImLoad = mView.findViewById(R.id.iv_contact_load);
            mImSearch = mView.findViewById(R.id.iv_contact_search);
            mTvTip = mView.findViewById(R.id.tv_contact_tip);
            mIvCall = mView.findViewById(R.id.iv_contact_call);
            mIvDel = mView.findViewById(R.id.iv_contact_del);

            mImSearch.setOnClickListener(this);
            mImLoad.setOnClickListener(this);
            mEditInput.setOnClickListener(this);
            mIvCall.setOnClickListener(this);
            mIvDel.setOnClickListener(this);

            initAdapter();
            mLetterSlideBar.setRecyclerView(mRecyclerView);
            //是否正在加载联系人
            mIsLoadContract = false;
            //输入监听
            mEditInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //todo 优化方案:touch间隔大于300s 才search by 20200427 lp
                    String inputKey = s.toString().trim();
                    requestSearch(inputKey);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private void initAdapter() {
        // 如果要用LetterSlideBar，一定要setLayoutManager
        mRecyclerView.setLayoutManager(new RecyclerManager(getUIContext()));
        if (AppUtils.isAc8257_YQQD_DY801Platform(getContext())) {
            mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(),
                    LinearLayoutManager.HORIZONTAL, R.drawable.iv_contact_list_div));
        } else {
            mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(),
                    LinearLayoutManager.HORIZONTAL, (int) getResources().getDimension(R.dimen.line_heigth),
                    getContext().getResources().getColor(R.color.divider)));
        }
        mRecyclerView.setAdapter(getPresenter().getAdapter());
    }

    /**
     * 根据输入关键字过滤联系人
     *
     * @param inputKey 如果为空或者长度小于3，则请求所有联系人
     */
    private void requestSearch(final String inputKey) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(inputKey)) {
                    //搜索联系人
                    getPresenter().searchPhoneList(inputKey);
                } else {
                    requestAllList();
                }
            }
        });
    }

    /**
     * 请求所有的联系人
     */
    private void requestAllList() {
        getPresenter().requestAllPhoneList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_contact_load:
                //同步电话本
                onSyncContact();
                break;
            case R.id.iv_contact_search:
                //搜索
                Logcat.d("++contact search++");
                String searchKey = mEditInput.getText().toString().trim();
                requestSearch(searchKey);
                break;
            case R.id.ed_contact:
                mEditInput.setShowSoftInputOnFocus(true);
                mEditInput.requestFocus();
                break;
            case R.id.iv_contact_call:
                getPresenter().callSelectedContact();
                break;
            case R.id.iv_contact_del:
                getPresenter().delSelectedContact();
                break;
        }
    }

    private void onSyncContact() {
        if (getPresenter().isBtConnected()) {

            String intputKey = mEditInput.getText().toString().trim();
            if (null != intputKey) {
                //清空输入框
                mEditInput.setText("");
            }
            mEditInput.setShowSoftInputOnFocus(false);
            AppUtils.hideIputKeyboard(getActivity());
            int downstate = getPresenter().getDownloadState();
            Logcat.d("downstate =" + downstate);
            if(getPresenter().isDownloading()){
                if(downstate == 2){//正在下载通话记录
                    String tip = getString(R.string.menu_history) + " " + getString(R.string.dialog_downing);
//                    Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
                    ToastUtil.getInstance().showToast(tip,Toast.LENGTH_SHORT);
                }
                if(downstate != 2){
                    showLoading(true);
                }
                return;
//                if(!isFistLaunch){
//                    return;
//                }
            }
            isFistLaunch = false;
            if(downstate != 2){
                showLoading(true);
            }
            getPresenter().clearContactList();
            ThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    getPresenter().onSyncContact();
                }
            });
        } else {
            Logcat.d("++onSyncContact++");
            if (null != getContext()) {
//                Toast.makeText(getContext(), R.string.tv_dial_not_conn, Toast.LENGTH_SHORT).show();
                ToastUtil.getInstance().showToast(R.string.tv_dial_not_conn, Toast.LENGTH_SHORT);
            }
        }
    }


    @Override
    public Context getUIContext() {
        return getContext();
    }

    @Override
    public void runOnUIThread(Runnable runnable) {
        if (null != mHandler) {
            mHandler.post(runnable);
        }
    }

    @Override
    public void synchronousFinish(int code, String msg, int listSize) {
        Logcat.d("++synchronousFinish code:" + code + "   ++listSize:" + listSize);
//        mIsLoadContract = false;
        Message message = new Message();
        message.what = UPDATE_VIEW;
        message.arg1 = listSize;
        if (null != mHandler) {
            mHandler.sendMessage(message);
        }
    }


    @Override
    public void onProgress(List<StPhoneBook> stPhoneBook) {
        Logcat.d("++onProgress++：" + stPhoneBook.size());
    }

    @Override
    public LetterSlideBar getLetterSlideBar() {
        return mLetterSlideBar;
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
     * 联系人改变
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBtContactChange(EventClassDefine.EventBtContactChange event) {
        Logcat.d();
        showLoading(getPresenter().isBtConnected() && (getPresenter().getDownloadState() == 1));
        updateTip(getPresenter().isBtConnected());
        getPresenter().updatePhoneBook(event.stPhoneBooks, true);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDownState(DownStateEvent event){
        if(event != null && getPresenter().isBtConnected()){
            if(event.mType == DownStateEvent.TYPE_DOWNPHONEBOOK){
                showLoading(true);
            }
            String tips = getString(R.string.dialog_downing) + event.mMessage;
            if(mLoadingView != null){
                mLoadingView.setLoadingText(tips);
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
            Logcat.d("++onEventLinkDevice++:" + event.status
                    + " isNewConntectedDevice " + BluetoothCacheUtil.getInstance().isNewConntectedDevice());
            BluetoothCacheUtil.getInstance().setCurConnectDevice(event.status, event.name,event.addr);
            //正在连接时，不处理，否则触发多次重复调用 @LBH 20201019
            if(event.status == IVIBluetooth.BluetoothConnectStatus.CONNECTING){
                return;
            }
            //清除标记，需要load
            if (!event.isConnected()) {
                mIsLoadContract = false;
            }
            /*BluetoothCacheUtil.getInstance().setBluzConnectedStatus(event.status);
            if(event.isConnected()){//需要保证去读数据库的时候有连接设备地址，一般来说这个消息是在BtBaseService服务里面存的，但是我们无法保证其时序
                BluetoothCacheUtil.getInstance().setCurConnectDevice(event.name,event.addr);
            }else{
                BluetoothCacheUtil.getInstance().setCurConnectDevice("", "");
            }*/

            //清空上个设备//不用在这里清 ， 连接新设备自动下载会清除,而且这个判断不合理，时序不确定
//            if (BluetoothCacheUtil.getInstance().isNewConntectedDevice()) {
//                mIsLoadContract = false;
//                getPresenter().clearContactList();
//            }

            updateConnState(event.isConnected());

        }
    }

    private void updateView(int listSize) {
        if (null != mTvTip && null != mRecyclerView) {
            Logcat.d();
            showLoading(false);
            if (listSize > 0) {
                mTvTip.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else if (getPresenter().getDownloadState() != 1) {
                mRecyclerView.setVisibility(View.GONE);
                mTvTip.setVisibility(View.VISIBLE);
                mTvTip.setText(R.string.tv_contact_null);
            }
        }
        if(!getPresenter().isBtConnected()){
            updateTip(false);
        }
    }

    /**
     * 蓝牙未连接时，界面提示语
     *
     * @param isConnected
     */
    private void updateTip(boolean isConnected) {
        Logcat.d("isConnected =" + isConnected);
        if (null != mTvTip && null != mRecyclerView) {
            if (isConnected) {
                mTvTip.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mTvTip.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mTvTip.setText(R.string.tv_contact_blu_tip);
            }
        }
    }

    private void showLoading(boolean flag) {
        if (mLoadingView == null) return;
        if (flag && !isHidden()) {
            mLoadingView.setLoadingText(getString(R.string.dialog_downing));
            mLoadingView.show();
        } else {
            mLoadingView.dismiss();
        }
    }

    private void updateConnState(boolean isConnected) {
        if (isConnected) {
            loadPhoneListFirst();
        } else {
            showLoading(false);
            //重置load标记
            mIsLoadContract = false;
            //不要清数据库
//            getPresenter().clearContactList();
        }
        updateTip(isConnected);
    }

}
