package com.jancar.uibase;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.jancar.viewbase.adapter.BaseFragmentPagerAdapter;

/**
 * viewpager的基类，并提供禁止滑动接口
 * @author bin.xie
 * @date 2016/11/14
 */
public class BaseViewPager extends ViewPager {
	
	private OnPageChangeListener mOnPageChangeListener = null;
	private BaseFragmentPagerAdapter mFragmentPagerAdapter = null; // 基类的fragment
	
	private boolean mIsScrollable = true; // 是否允许滑动
	
	public BaseViewPager(Context context) {
		this(context, null);
	}

	// 滑动事件接口回调
	public interface OnScrollEventListener {
		void onScrollEnd(int position);
	}
	private OnScrollEventListener mOnScrollEventListener = null;

	public void setOnScrollEventListener(OnScrollEventListener listener) {
		mOnScrollEventListener = listener;
	}

	public BaseViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		super.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				if (null != mOnPageChangeListener) {
					mOnPageChangeListener.onPageSelected(arg0);
				}
				
				if (null != mFragmentPagerAdapter) {
					mFragmentPagerAdapter.setCurPager(arg0);
				}
				// 该位置在滑动过程中回调，如果做APP做UI刷新，或者耗时操作会影响滑动
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (null != mOnPageChangeListener) {
					mOnPageChangeListener.onPageScrolled(arg0, arg1, arg2);
				}
				if (null != mOnScrollEventListener) {
					if (0.0 == arg1) { // 该位置在滑动结束回调，此时做部分UI刷新等动作，不会影响滑动效果
						mOnScrollEventListener.onScrollEnd(arg0);
					}
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				if (null != mOnPageChangeListener) {
					mOnPageChangeListener.onPageScrollStateChanged(arg0);
				}
			}
		});
	}
	
	/**
	 * 设置适配器
	 * @param adapter
	 */
	public void setAdapter(BaseFragmentPagerAdapter adapter) {
		mFragmentPagerAdapter = adapter;
		
		super.setAdapter(adapter);
	}

	/**
	 * 设置监听
	 */
	public void setOnPageChangeListener(OnPageChangeListener callback) {
		mOnPageChangeListener = callback;
	}
	
	/**
	 * 获取当前页
	 * @return
	 */
	public int getCurPager() {
		if (null != mFragmentPagerAdapter) {
			return mFragmentPagerAdapter.getCurPager();
		}
		return 0;
	}
	
	/**
	 * 设置是否允许滑动，默认允许
	 * @param isScroolable
	 */
	public void setScroolable(boolean isScroolable) {
		mIsScrollable = isScroolable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (getCurrentItem() == 0 && getChildCount() == 0) {
			return false;
		}

		if (!mIsScrollable) {
			if (arg0.getAction() ==  MotionEvent.ACTION_MOVE) {
				// 只禁止移动事件
				return false;	
			}
		}
		return super.onTouchEvent(arg0);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (getCurrentItem() == 0 && getChildCount() == 0) {
			return false;
		}

		if (!mIsScrollable) {
			if (arg0.getAction() ==  MotionEvent.ACTION_MOVE) {
				// 只禁止移动事件
				return false;	
			}
		}
		return super.onInterceptTouchEvent(arg0);
	}

	@Override
	protected boolean canScroll(View view, boolean arg1, int arg2, int arg3,
			int arg4) {
//		if (view instanceof ListView) {
//			return true;
//		}
		return super.canScroll(view, arg1, arg2, arg3, arg4);
	}

	@Override
	public boolean executeKeyEvent(KeyEvent event) {
		if (mIsScrollable) {
			return super.executeKeyEvent(event);
		} else {
			return false;
		}
	}
}
