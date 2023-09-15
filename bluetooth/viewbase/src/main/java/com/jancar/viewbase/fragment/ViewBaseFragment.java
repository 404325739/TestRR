package com.jancar.viewbase.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.utils.AbsToast;

/**
 * 类描述：项目使用到的 fragment 的基类，试用与重复的Fragment，每一页与每一页都是一样的东西，修改数据的时候可以试用 setData
 * @author bin.xie
 * @date 2016/11/14
 */
public abstract class ViewBaseFragment<T> extends Fragment {
	protected AbsToast mToast = null;
	private Object mTag = null; // 标签
	private T mData = null; // 保存数据
	private int mCurPager = -1; // 当前页

	private boolean mViewCreated = false; // 是否已经创建view

	public ViewBaseFragment() {
		super();
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mToast = getToast();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(getLayoutId(), container, false);//关联布局文件
		onCreateView(rootView, savedInstanceState);
		if (null != mData) {
			if (isAdded()) {
				loadData(mData);
			}
		}
		mViewCreated = true;
		return rootView;
	}

	/**
	 * 获取Toast实例
	 * @return
     */
	protected abstract AbsToast getToast();

	public abstract int getLayoutId();

	protected abstract void onCreateView(View rootView, Bundle savedInstanceState);
	
	/**
	 * 进行页面数据的刷新
	 * @param data
	 */
	protected abstract void loadData(T data);
	
	/**
	 * 设置数据，进行页面刷新
	 * @param data
	 */
	public void setData(T data) {
		mData = data;
		if (mViewCreated) {
			if (isAdded()) {
				loadData(data);
			}
		}
	}

	/**
	 * 获取数据
	 * @return
     */
	public T getData() {
		return mData;
	}

	public Context getContext() {
		Activity activity = getActivity();
		return activity == null ? null : activity.getApplicationContext();
	}
	
	/**
	 * 设置标签
	 * @param tag
	 */
	public void setTagObject(Object tag) {
		mTag = tag;
	}
	
	public Object getTagObject() {
		return mTag;
	}
	
	/**
	 * 当前是第几页
	 * @param nPager
	 */
	public void setPager(int nPager) {
		mCurPager = nPager;
	}
	
	public int getPager() {
		return mCurPager;
	}
	
    /**
     * 删除泡泡
     */
    protected final void cancelToast() {
		if (mToast != null) {
			mToast.cancelToast();
		} else {
			Logcat.e("Toast is not initialized, Please overide the method getToast");
		}
    }

    protected final void showToast(int resId) {
		if (mToast != null) {
			mToast.showToast(resId, getContext());
		} else {
			Logcat.e("Toast is not initialized, Please overide the method getToast");
		}
    }

    /**
     * 弹出泡泡，统一使用的方法
     * @param text
     */
    protected final void showToast(CharSequence text) {
		if (mToast != null) {
			mToast.showToast(text, getContext());
		} else {
			Logcat.e("Toast is not initialized, Please overide the method getToast");
		}
    }
}
