package com.jancar.fragment;

import com.jancar.ui.utils.ToastUtil;
import com.jancar.viewbase.fragment.ViewBaseFragment;
import com.jancar.viewbase.utils.AbsToast;

public abstract class BaseFragment<T> extends ViewBaseFragment<T> {

	@Override
	protected AbsToast getToast() {
		return ToastUtil.getInstance();
	}

}
