package com.jancar.bluetooth.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.jancar.bluetooth.event.EventClassDefine;
import com.jancar.bluetooth.ui.fragment.BtMusicFragment;
import com.jancar.bluetooth.ui.fragment.SettingsFragment;
import com.jancar.bluetooth.utils.BluetoothModelUtil;
import com.jancar.bluetooth.MyApplication;
import com.jancar.bluetooth.contract.BaseMainContract;
import com.jancar.bluetooth.presenter.BaseMainPresenter;
import com.jancar.bluetooth.ui.fragment.CollectionFragment;
import com.jancar.bluetooth.ui.fragment.ContactsFragment;
import com.jancar.bluetooth.ui.fragment.DeviceFragment;
import com.jancar.bluetooth.ui.fragment.DialerFragment;
import com.jancar.bluetooth.ui.fragment.RecordsFragment;
import com.jancar.bluetooth.R;
import com.jancar.bluetooth.utils.AppUtils;
import com.jancar.bluetooth.utils.Constants;
import com.jancar.bluetooth.utils.NoDoubleClickUtils;
import com.jancar.bluetooth.utils.NumberFormatUtil;
import com.jancar.bluetooth.utils.ToastUtil;
import com.jancar.sdk.bluetooth.IVIBluetooth;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.view.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author Tzq
 * @date 2019-12-24 19:46:00
 */
public class BaseMainActivity extends BaseActivity<BaseMainContract.Presenter, BaseMainContract.View> implements BaseMainContract.View, View.OnClickListener {
    private static final String TAG = "BaseMainActivity";
    private android.app.FragmentManager mFragmentManager;
    private boolean isFistLaunch = false;
    /**
     * tab btn
     */
//    int[] tabBtn = new int[]{R.id.tab_dial_manager, R.id.tab_contact_manager, R.id.tab_records_manager/*, R.id.tab_collection_manager, R.id.tab_device_manager*/};
    int[] tabBtn = new int[]{R.id.tab_dial_manager, R.id.tab_contact_manager, R.id.tab_records_manager,
            R.id.tab_btmusic_manager, R.id.tab_device_manager, R.id.tab_setting_manager/*, R.id.tab_collection_manager*/};
    int[] tabTvs = new int[]{R.id.tab_tv_dial, R.id.tab_tv_contact, R.id.tab_tv_records,
            R.id.tab_tv_btmusic, R.id.tab_tv_device, R.id.tab_tv_setting/*, R.id.tab_tv_collection*/};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        BluetoothModelUtil.getInstance().setIsAc8257_YQQD_DY801(AppUtils.isAc8257_YQQD_DY801Platform(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logcat.d("++onCreate++");
        setContentView(R.layout.activity_main);
        isFistLaunch = true;
        NumberFormatUtil.isZh(this);
        initView();
        handleIntent(getIntent());
        getPresenter().runService();
        EventBus.getDefault().register(this);

//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName(IVISystem.PACKAGE_BT_SERVICE, IVISystem.SERVICE_BLUETOOTH));
//        startService(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            Logcat.d(" action: " + intent.getAction());
        }
        if(isFistLaunch){
            isFistLaunch = false;
            showTab(R.id.tab_dial_manager);
        }
//        if (is8227SettingAction(intent)) {
//            showTab(R.id.tab_device_manager);
//        } else {
//            showTab(R.id.tab_dial_manager);
//        }
    }

    private boolean is8227SettingAction(Intent intent) {
        if (intent == null) return false;
        return intent.getAction().equals(Constants.ACTION_BLUETOOTH_SETTINGS) && AppUtils.isAc8227Platform(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logcat.d("++onStart++");
    }

    @Override
    protected void onResume() {
        super.onResume();
        NumberFormatUtil.isZh(this);
        Logcat.d("++onResume++");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logcat.d("++onPause++");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logcat.d("++onStop++");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapPages.clear();
        lastPage = null;
//        removeAllFragment();
        EventBus.getDefault().unregister(this);
        Logcat.d("++onDestroy++");
    }

    /**
     * tab page
     */
    enum PAGE {
        DIALER, CONTACTS, RECORDS, COLLECTION, DEVICE, BTMUSIC, SETTINGS
    }

    private PAGE lastPage = null;

    private LinkedHashMap<PAGE, Fragment> mapPages = new LinkedHashMap<>();

    @Override
    public BaseMainContract.Presenter createPresenter() {
        return new BaseMainPresenter();
    }

    @Override
    public BaseMainContract.View getUiImplement() {
        return this;
    }

    private int getPageIndex(PAGE page) {
        int index = 0;
        switch (page) {
            case DIALER:
                index = 0;
                break;
            case CONTACTS:
                index = 1;
                break;
            case RECORDS:
                index = 2;
                break;
            case BTMUSIC:
                index = 3;
                break;
            case DEVICE:
                index = 4;
                break;
            case SETTINGS:
                index = 5;
                break;
        }
        return index;
    }
    /**
     * 初始化View
     */
    private void initView() {
        for (int id : tabBtn) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(this);
            }
        }

        onScreenSizeChange(isInMultiWindowMode(), getResources().getConfiguration());
    }

    public void showTab(int tab) {
        for (int id : tabBtn) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setSelected(tab == id);
            }
        }
        switch (tab) {
            case R.id.tab_dial_manager:
                replaceFragment(PAGE.DIALER);
                break;
            case R.id.tab_contact_manager:
                replaceFragment(PAGE.CONTACTS);
                break;
            case R.id.tab_records_manager:
                replaceFragment(PAGE.RECORDS);
                break;
            case R.id.tab_collection_manager:
                replaceFragment(PAGE.COLLECTION);
                break;
            case R.id.tab_device_manager:
                replaceFragment(PAGE.DEVICE);
                break;
            case R.id.tab_btmusic_manager:
                replaceFragment(PAGE.BTMUSIC);
                break;
            case R.id.tab_setting_manager:
                replaceFragment(PAGE.SETTINGS);
                break;
        }
    }

    private void attachFragment(PAGE page) {
        Fragment fm = getView(page);
        if (fm != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.FrameLayout, fm);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        //这里必须使用传入的outState，否则onRestoreInstanceState无法读取
//        Bundle mysavebundle = new Bundle();
//        mysavebundle.putInt("tab",getPageIndex(lastPage));
//        super.onSaveInstanceState(mysavebundle);
        MyApplication.TAG_INDEX = getPageIndex(lastPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        if(savedInstanceState == null){
//            return;
//        }
//        int tabindex = savedInstanceState.getInt("tab",-1);
        int tabindex = MyApplication.TAG_INDEX;
        Logcat.d("tabid =" + tabindex);
        if(tabindex != -1){
            showTab(tabBtn[tabindex]);
        }
    }

    private void removeAllFragment(){
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        List<Fragment> fragments = fragmentManager.getFragments();
        if( fragments != null){
//            Logcat.d("size =" + fragmentManager.getFragments().size());
            for(Fragment fragment:fragments){
                ft.remove(fragment);
                Logcat.d("fragment =" + fragment);

            }
            ft.commitAllowingStateLoss();
        }
    }

    private synchronized void replaceFragment(PAGE newPage) {
        if(lastPage == newPage){//过滤重复点击
            return;
        }
        if(mFragmentManager == null){
            mFragmentManager = getFragmentManager();
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        List<Fragment> fragments = mFragmentManager.getFragments();
        if( fragments != null){
            Logcat.d("size =" + fragments.size());
        }
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment fmNew = getView(newPage);
        if (fmNew != null) {
            if (lastPage != null) {
                Fragment fm = getView(lastPage);
                if (fm != null) {
                    ft.hide(fm);
                }
            }
            if (!fmNew.isAdded()) {
                ft.add(R.id.FrameLayout, fmNew);
            }
            ft.show(fmNew);
//            ft.replace(R.id.FrameLayout, fmNew);
            lastPage = newPage;
        }
        ft.commitAllowingStateLoss();
    }

    private synchronized Fragment getView(PAGE page) {
        Fragment fm = null;
        if (mapPages.containsKey(page)) {
            fm = mapPages.get(page);
        } else {
            switch (page) {
                case DIALER:
                    fm = new DialerFragment();
                    break;
                case CONTACTS:
                    fm = new ContactsFragment();
                    break;
                case RECORDS:
                    fm = new RecordsFragment();
                    break;
                case COLLECTION:
                    fm = new CollectionFragment();
                    break;
                case DEVICE:
                    fm = new DeviceFragment();
                    break;
                case BTMUSIC:
                    fm = new BtMusicFragment();
                    break;
                case SETTINGS:
                    fm = new SettingsFragment();
                    break;
                default:
                    break;
            }
            if (fm != null) {
                mapPages.put(page, fm);
            }
        }
        return fm;
    }

    @Override
    public void onClick(View v) {
        if (NoDoubleClickUtils.isIllegalClick()) {
            Logcat.d("isIllegalClick, return");
            return;
        }
        showTab(v.getId());
    }

    @Override
    public Context getUIContext() {
        return this;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN &&
                getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);

    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void quitApp(EventClassDefine.EventQuitApp event) {
        Logcat.d("mSystemManager quitApp" + event.source);
//        if (event.source == IVIMedia.Type.PHONE) {
        finish();
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        goBackToDesktop();//这里可以判断是否在下载状态，如果真正退出，会造成下载时各种状态混乱(回调没设置上去)
    }

    private void goBackToDesktop() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        onScreenSizeChange(isInMultiWindowMode, newConfig);
    }

    boolean lastMultiWindowMode;
    private void onScreenSizeChange(boolean isInMultiWindowMode, Configuration newConfig) {
        Logcat.d("isInMultiWindowMode: "+ isInMultiWindowMode);
        Logcat.d("screenWidthDp: " + newConfig.screenWidthDp + " screenHeightDp: " + newConfig.screenHeightDp);
        if (!(isInMultiWindowMode & lastMultiWindowMode)) {
            for (int id : tabTvs) {
                View btn = findViewById(id);
                if (btn != null) {
                    btn.setVisibility(isInMultiWindowMode ? View.GONE : View.VISIBLE);
                }
            }
            lastMultiWindowMode = isInMultiWindowMode;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventCallBackFail(EventClassDefine.EventCallBackFail event) {
        if (null != event) {
            switch (event.errorCode) {
                case IVIBluetooth.BluetoothExecErrorMsg.ERROR_NO_ALLOW_BT:
                    ToastUtil.getInstance().showToast(R.string.external_device_disable_bt, Toast.LENGTH_SHORT);
                    break;
            }
        }
    }
}
