package com.jancar.viewbase.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.fragment.ViewBaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：项目使用到的 fragmentPagerAdapter 的基类
 * @author bin.xie
 * @date 2016/11/14
 */
public class BaseFragmentPagerAdapter<T extends ViewBaseFragment> extends FragmentStatePagerAdapter {
	
	private Context mContext = null;
	private List<T> mFragments = null;
	private FragmentManager mFragmentManager = null;
	
	private int mCurPager = 0; // 保存当前页码，不设置该页码，每次刷新是全部页面刷新

	public BaseFragmentPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		
		mContext = context;
		mFragmentManager = fm;
	}
	
	/**
	 * 设置fragment页码
	 * @param fragments
	 */
	public void setFragments(List<T> fragments) {
		if (mFragments == fragments) {
			Logcat.w("mFragments == fragments");
			return;
		}
		if (null == mFragments) {
			mFragments = new ArrayList<>();
		}
		mFragments.clear();
		if (null != fragments) {
			mFragments.addAll(fragments);
		}
		notifyDataSetChanged();
	}
	
	/**
	 * 添加页码
	 * @param fragment
	 */
	public void addFragment(T fragment) {
		if (null == mFragments) {
			mFragments = new ArrayList<>();
		}
		insertFragment(mFragments.size(), fragment);
	}
	
	/**
	 * 添加一定数据量的页码
	 * @param fragments
	 */
	public void addFragments(ArrayList<T> fragments) {
		if (null == mFragments) {
			mFragments = new ArrayList<>();
		}
		insertFragments(mFragments.size(), fragments);
	}
	
	/**
	 * 在指定页后面插入一个fragment
	 * @param position
	 * @param fragment
	 */
	public void insertFragment(int position, T fragment) {
		if (null == mFragments) {
			mFragments = new ArrayList<>();
		}
		if (null != fragment) {
			mFragments.add(position, fragment);
			notifyDataSetChanged();
		}
	}
	
	/**
	 * 在指定页后面插入一个fragment列表
	 * @param position
	 * @param fragments
	 */
	public void insertFragments(int position, List<T> fragments) {
		if (null == mFragments) {
			mFragments = new ArrayList<>();
		}
		if (null != fragments) {
			mFragments.addAll(position, fragments);
			notifyDataSetChanged();
		}
	}
	
	/**
	 * 删除一页
	 * @param position
	 */
	public void removeFragment(int position) {
		if (null != mFragments) {
			if (position >= 0 && position < mFragments.size()) {
				removeFragment(mFragments.get(position));
			}
		}
	}
	
	/**
	 * 删除指定页
	 * @param fragment
	 */
	public void removeFragment(T fragment) {
		if (null != mFragments) {
			if (mFragments.contains(fragment)) {
				mFragments.remove(fragment);
				notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * 删除所有的fragment
	 */
	public void removeAllFragment() {
		if (null != mFragments) {
			mFragments.clear();
			notifyDataSetChanged();
		}
	}
	
	/**
	 * 删除最后一个fragment
	 */
	public void removeLastFragment() {
		if (null != mFragments && mFragments.size() != 0) {
			removeFragment(mFragments.get(mFragments.size() - 1));
		}
	}
	
	/**
	 * 删除最后几页，具体场景在视频，拔除U盘时，删除最后5页
	 * @param nPageCount 传需要删除的页数
	 */
	public void removeLastFragmentCount(int nPageCount) {
		if (null != mFragments && 0 < nPageCount) {
			if (mFragments.size() < nPageCount) {
				mFragments.clear();
			} else {
				for (int i = 0; i < nPageCount; i++) {
					mFragments.remove(mFragments.size() - 1);
				}
			}
			notifyDataSetChanged();
		}
	}
	
	/**
	 * 获取所有的页
	 * @return
	 */
	public List<T> getFragments() {
		return mFragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		if (null != mFragments) {
			if (arg0 < mFragments.size() && arg0 >= 0) {
				return (Fragment) mFragments.get(arg0);
			}
		}
		return null;
	}

	@Override
	public int getCount() {
		if (null != mFragments) {
			return mFragments.size();
		}
		return 0;
	}
	
	@Override
	public int getItemPosition(Object object) {
//		if (0 <= mCurPager) {
//			BaseFragment fragment = (BaseFragment)object;
//			if (fragment.getPager() == mCurPager) { // 刷新当前页
//				return POSITION_NONE;
//			} else {
//				return POSITION_UNCHANGED;
//			}
//		}
	    return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		T fragment = (T) super.instantiateItem(container, position);
		fragment.setPager(position);
		return fragment;
	}
	
	/**
	 * 设置当前的页码
	 * 注意，该方法如果不设置，每次修改数据，会全部刷新
	 * @param nPage
	 */
	public void setCurPager(int nPage) {
		mCurPager = nPage;
	}
	
	/**
	 * 获取当前页码
	 * @return
	 */
	public int getCurPager() {
		return mCurPager;
	}
	
	protected Context getContext() {
		return mContext;
	}

	@Override
	public Parcelable saveState() {
		Logcat.d("when ACC do nothing ");
		return null;
	}
}
