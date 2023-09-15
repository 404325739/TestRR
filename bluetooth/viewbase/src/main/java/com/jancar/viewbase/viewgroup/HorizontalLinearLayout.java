package com.jancar.viewbase.viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.jancar.viewbase.adapter.HorizontalLayoutAdapter;

/**
 * 实现coverFlow的group
 * 该控件需要配合 HorizontalLayoutAdapter 一起试用，水平滑动的中间的线性布局，增加适配器控制
 * @author bin.xie
 * @date 2016/11/14
 */
public class HorizontalLinearLayout extends LinearLayout implements HorizontalLayoutAdapter.ScrollViewLayoutCallback {

	private int mFristIndex = 0; // 当前第一张图片的下标

	private HorizontalLayoutAdapter mAdapter = null;
	private int mStartUpdateChild = 0; // 最左边刷新的view
	private int mLoadChildCount = 0; // 默认刷新长度 5
	private int mUpdateCount = 0; // 已经刷新的数量
	
	public HorizontalLinearLayout(Context context) {
		this(context, null);
	}

	public HorizontalLinearLayout(Context context, AttributeSet set) {
		super(context, set);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (false == changed) {
			return;
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	/**
	 * 获取最左边的index
	 * 
	 * @return 最左边的index
	 */
	public int getFirstIndex() {
		return mFristIndex;
	}
	
	/**
	 * 设置适配器
	 * @param adapter
	 */
	public void setAdapter(HorizontalLayoutAdapter adapter) {
		if (null != adapter) {
			mAdapter = adapter;
			mAdapter.setScrollViewLayoutCallback(this);
		}
	}

	@Override
	public void onUpdate(boolean isSeekToStartPos) {
		// 刷新界面
		if (isSeekToStartPos) {
			// 跳转开始的位置
			if (getParent() instanceof HorizontalScrollView){
				HorizontalScrollView scrollView = (HorizontalScrollView)getParent();
				scrollView.scrollTo(0, scrollView.getScrollY());
			}
			
			// setData过来，强制
			mStartUpdateChild = 0;
			mLoadChildCount = 0;
		}

		restoreChildView(0, 5); // 初始化所有子对象
	}
	
	/**
	 * 滑动结束
	 * @param width 父控件的宽度
	 */
	public void onScrollStop(int x, int width) {
		int nChildCount = getChildCount();
		
		if (0 != nChildCount) {
			View child = getChildAt(0);
			
			if (0 != child.getWidth()) {
				int nLoadCount = width / child.getWidth() + 2; // 加载的数量
				int nStartChild = x / child.getWidth(); // 开始加载的位置
				nStartChild = nStartChild < 0 ? 0 : nStartChild;
				updateCurChild(nStartChild, nLoadCount); // 刷新
				return;
			}
		}
		
		updateCurChild(0, 5); // 默认刷新
	}
	
	/**
	 * 滑动到指定位置
	 * @param x 当前x的位置
	 * @param y 当前y的位置
	 * @param width 父控件的宽度
	 * @param height 父控件的高度
	 */
	public void onScrollChanged(int x, int y, int width, int height) {

	}
	
	/**
	 * 恢复所有的子view
	 * @param nStartChild 开始刷新的位置
	 * @param nLoadChild 需要加载的位置
	 */
	private void restoreChildView(int nStartChild, int nLoadChild) {
		if (null == mAdapter) return;
		
		int nCount = mAdapter.getCount();
		int nChildCount = getChildCount();
		
		if (0 == nCount) {
			for (int i = 0; i < nChildCount; i++) { // 只做隐藏，不做删除
				getChildAt(i).setVisibility(View.GONE);
			}
			return;
		}
		
		if (nCount < nChildCount) {
			// 如果数量减少，去掉后面部分
			for (int i = nChildCount - 1; i >= nCount; --i) {
				getChildAt(i).setVisibility(View.GONE);
			}
			for (int i = 0; i < nCount; i++) { // 修改指定的View
				mAdapter.getView(i, getChildAt(i), this);
				
				showView(getChildAt(i));
				
				mUpdateCount = i; // 已经刷新的数量
			}
		} else {
			for (int i = 0; i < nChildCount; i++) {
				showView(getChildAt(i));
			}
			
			for (int i = 0; i < nCount; i++) {
				if (i < nChildCount) { // 存在，只需要显示
					mAdapter.getView(i, getChildAt(i), this);
					
					showView(getChildAt(i));
				} else {
					addView(mAdapter.getView(i, getChildAt(i), this));
				}
				
				mUpdateCount = i; // 已经刷新的数量
			}
		}
		
		nChildCount = getChildCount();
		for (int i = nStartChild; i < nLoadChild; i++) {
			if (i < nChildCount) {
				mAdapter.updateView(i, getChildAt(i));
			}
		}
		
		mStartUpdateChild = nStartChild;
		mLoadChildCount = nLoadChild; // 已经加载了这么多
	}
	
	/**
	 * 刷新当前显示的child
	 * @param nStartChild 最左边的位置
	 * @param nLoadChild 总共需要刷新的位置
	 */
	private void updateCurChild(int nStartChild, int nLoadChild) {
		if (null == mAdapter) return;
		
		if (mStartUpdateChild == nStartChild && mLoadChildCount == nLoadChild) {
			return;
		}
		
		int nCount = mAdapter.getCount();
		int nChildCount = getChildCount();
		int nNeedLoadChild = nStartChild + nLoadChild;
		
		if (nNeedLoadChild > nCount) {
			// 刷新的数量已经超过总数量
			nNeedLoadChild = nCount;
		}
		
		if (nCount < nChildCount) {
			// 如果数量减少，去掉后面部分
			for (int i = nChildCount - 1; i >= nCount; --i) {
				getChildAt(i).setVisibility(View.GONE);
			}
			for (int i = nStartChild; i < nNeedLoadChild; i++) { // 修改指定的View
				if (mUpdateCount < i) {
					mAdapter.getView(i, getChildAt(i), this);
					mUpdateCount = i;
				}
				
				mAdapter.updateView(i, getChildAt(i));
				showView(getChildAt(i));
			}
		} else {
			for (int i = nStartChild; i < nNeedLoadChild; i++) {
				if (i < nChildCount) {
					if (mUpdateCount < i) {
						mAdapter.getView(i, getChildAt(i), this);
						mUpdateCount = i; // 已经刷新的数量
					}
					mAdapter.updateView(i, getChildAt(i));
				} else {
					addView(mAdapter.getView(i, getChildAt(i), this));
					mAdapter.updateView(i, getChildAt(i));
				}
				showView(getChildAt(i));
			}
		}
		
		mStartUpdateChild = nStartChild;
		mLoadChildCount = nLoadChild; // 缓存
	}
	
	/**
	 * 显示一个view
	 * @param view
	 */
	private void showView(View view) {
		if (view.getVisibility() != View.VISIBLE) {
			view.setVisibility(View.VISIBLE);
		}
	}
}
