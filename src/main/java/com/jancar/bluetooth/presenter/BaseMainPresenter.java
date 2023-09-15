package com.jancar.bluetooth.presenter;

import android.content.Context;
import android.content.Intent;

import com.jancar.bluetooth.contract.BaseMainContract;
import com.jancar.bluetooth.model.BaseMainModel;
import com.jancar.bluetooth.model.BaseMainRepository;
import com.jancar.bluetooth.services.BtAppService;
import com.jancar.sdk.utils.Logcat;
import com.ui.mvp.presenter.BaseModelPresenter;

/**
 * @author Tzq
 * @date 2019-12-24 19:46:00
 */
public class BaseMainPresenter extends BaseModelPresenter<BaseMainContract.View, BaseMainModel> implements BaseMainContract.Presenter, BaseMainModel.Callback {

    @Override
    public BaseMainModel createModel() {
        return new BaseMainRepository(this);
    }

    @Override
    public void runService() {
        Logcat.d("getUi() " + getUi());
        if (getUi() == null) return;
        Context context = getUi().getUIContext();
        if (context != null) {
            try {
                // 蓝牙服务，要一直在后台开启
                context.startService(new Intent(context, BtAppService.class));
            } catch (Exception e) {

            }
        }
    }
}