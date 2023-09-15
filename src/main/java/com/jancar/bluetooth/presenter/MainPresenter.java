package com.jancar.bluetooth.presenter;

import com.jancar.bluetooth.contract.MainContract;
import com.jancar.bluetooth.model.MainModel;
import com.jancar.bluetooth.model.MainRepository;
import com.ui.mvp.presenter.BaseModelPresenter;

/**
 * @author Tzq
 * @date 2019-12-24 19:44:11
 */
public class MainPresenter extends BaseModelPresenter<MainContract.View, MainModel> implements MainContract.Presenter, MainModel.Callback {

    @Override
    public MainModel createModel() {
        return new MainRepository(this);
    }
}