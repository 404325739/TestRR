package com.jancar.viewbase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.jancar.viewbase.viewgroup.AbsHListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：所有应用adapter继承该类
 * @author bin.xie
 * 
 * 修改备注：增加选中接口
 * @author bin.xie
 * @date 2016/11/14
 */
public abstract class RRBaseAdapter<T> extends BaseAdapter {
    private List<T> mDataList = new ArrayList<T>();
    private LayoutInflater mInflater;
    protected Context mContext;
    private int mCurSelectedIndex = -1; // 选中哪个
    private List<ViewHolder> mViewHolders = new ArrayList<ViewHolder>();
    private int mFirstVisibleItem = 0; // 在界面显示的第一个
    private int mVisibleCount = 0; // 在界面显示的总个数
    private boolean mIsInit = false; // 是否已经初始化完成，主要是判断是否滑动过
    private List<Integer> mCurUpdatePostions = new ArrayList<Integer>(); // 记录当前已经刷新了的位置
    
    public RRBaseAdapter(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 会清除之前的数据
     * @param data
     */
    public void setData(List<T> data) {
    	if (mDataList == data) { // 如果数据一样，不重新加载
    		return;
    	}
    	mIsInit = false;
    	
        if(null == mDataList)
            mDataList = new ArrayList<T>();
        
        mViewHolders.clear();
        mDataList.clear();
        if(null != data)
            mDataList.addAll(data);
        notifyDataSetChanged();
    }
    
    /**
     * 修改数据，不刷新界面
     * @param data
     */
    public void changeData(List<T> data) {
    	if (mDataList == data) { // 如果数据一样，不重新加载
    		return;
    	}
    	mIsInit = false;
    	
        if(null == mDataList)
            mDataList = new ArrayList<T>();
        
        mViewHolders.clear();
        mDataList.clear();
        if(null != data)
            mDataList.addAll(data);
    }
    
    /**
     * 在原始数据上添加新数据
     * @param data
     */
    public void addData(List<T> data) {
    	mIsInit = false;
    	
    	if(null == mDataList)
            mDataList = new ArrayList<T>();
        if(null != data)
            mDataList.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 在原始数据上添加新数据
     * @param data
     */
    public void addData(List<T> data,int position) {
    	mIsInit = false;
    	
    	if(null == mDataList)
            mDataList = new ArrayList<T>();
        if(null != data)
            mDataList.addAll(position,data);
        notifyDataSetChanged();
    }

    /**
     * 添加一条数据
     * @param data
     * @param position
     */
    public void addData(T data, int position) {
        mIsInit = false;

        if(null == mDataList)
            mDataList = new ArrayList<T>();
        if (null != data)
            mDataList.add(position, data);
        notifyDataSetChanged();
    }
    
    /**
     * 从position位置插入数据
     * @param position
     * @param data
     */
    public void insertData(int position, List<T> data) {
    	mIsInit = false;
    	
    	if(null == mDataList)
            mDataList = new ArrayList<T>();
        if(data != null) {
        	mDataList.addAll(position, data);
            notifyDataSetChanged();
        }
    }
    
    /**
     * 从position位置插入数据
     * @param position
     * @param data
     */
    public void insertData(int position, T data) {
    	mIsInit = false;
    	
        if(null == mDataList)
            mDataList = new ArrayList<T>();
        if(data != null) {
        	mDataList.add(position, data);
            notifyDataSetChanged();
        }
    }
    
    /**
     * 删除所有数据
     */
    public void removeAllData() {
    	if (null != mDataList) {
    		mDataList.clear();
    	}
    	if(null != mViewHolders) {
    		mViewHolders.clear();
    	}
        notifyDataSetChanged();
    }
    
    /**
     * 删除数据
     * @param position
     */
    public void removeData(int position) {
    	if (null == mDataList) {
    		return;
    	}
    	if (position < mDataList.size() && position >= 0) {
    		mDataList.remove(position);
    		notifyDataSetChanged();
    	}
    }
    
    /**
     * 设置当前选中的index
     * @param index
     */
    public void setSelectedIndex(int index) {
    	mCurSelectedIndex = index;
    	notifyDataSetChanged();
    }
    
    public int getSelectedIndex() {
    	return mCurSelectedIndex;
    }
    
    public List<T> getData() {
    	return mDataList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if(null != mDataList) {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public T getItem(int position) {
        // TODO Auto-generated method stub
        if(null != mDataList && position >= 0 && position < mDataList.size()) {
        	return mDataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    
    /**
     * 设置ListView，设置了该方法可以使用加载图片优化
     * 如果传递了该参数，在滑动的时候可以不更新图片，最后会统一调用刷新界面
     * @param viewGroup
     */
    public void setListView(ViewGroup viewGroup) {
    	if (null == viewGroup) {
    		return;
    	}
    	if (viewGroup instanceof AbsHListView) { // 水平滑动ListView
    		AbsHListView listView = (AbsHListView) viewGroup;
    		listView.setOnScrollListener(new AbsHListView.OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsHListView view, int scrollState) {
					mIsInit = true;
					switch (scrollState) {
					case AbsHListView.OnScrollListener.SCROLL_STATE_IDLE:// 滑动停止
						onScrollStop();
						break;
					}
				}
				
				@Override
				public void onScroll(AbsHListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					mFirstVisibleItem = firstVisibleItem;
					mVisibleCount = visibleItemCount;
				}
			});
    	} else if (viewGroup instanceof AbsListView) { // 垂直滑动ListView
    		AbsListView listView = (AbsListView) viewGroup;
    		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					mIsInit = true;
					
					switch (scrollState) {
					case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 滑动停止
						onScrollStop();
						break;
					}
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					mFirstVisibleItem = firstVisibleItem;
					mVisibleCount = visibleItemCount;
				}
			});
    	}
    }
    
    /**
     * 滑动停止，通知子adpter刷新图片
     */
    private void onScrollStop() {
//    	List<Integer> savePos = new ArrayList<Integer>();
    	for (int i = 0; i < mViewHolders.size(); i++) {
    		int position = mViewHolders.get(i).getTag();
			if (position >= mFirstVisibleItem && position <= mFirstVisibleItem + mVisibleCount) { // 刷新图片，通知界面，滑动已经结束，刷新当前页的图片
				if (!mCurUpdatePostions.contains(position)) { // 已经刷新过的，不刷新
					mViewHolders.get(i).updateImage(getItem(position), position);
//					savePos.add(position);
				}
			}
		}
    	
    	mCurUpdatePostions.clear();
//    	mCurUpdatePostions.addAll(savePos); // 记录上一次刷新的记录，如果当前需要刷新的和上一次的一样，不刷新
    }
    
    @Override
    public void notifyDataSetChanged() {
    	mCurUpdatePostions.clear();
    	
    	super.notifyDataSetChanged();
    }

    public abstract int getConvertViewId(int position);

    public abstract ViewHolder<T> getNewHolder(int position);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder<T> holder;
        if (convertView == null){
            holder = getNewHolder(position);
            convertView = mInflater.inflate(getConvertViewId(position), null);
            holder.initHolder(convertView,position);
            convertView.setTag(holder);
            mViewHolders.add(holder);
        } else {
            holder = (ViewHolder<T>) convertView.getTag();
            
            if (!mViewHolders.contains(holder)) { // 修改有时，清理掉holder在加载之后的bug
            	mViewHolders.add(holder);
            }
        }
        try {
        	holder.setTag(position); // 标识position
        	holder.loadData(getItem(position), position);
        	
        	if (!mIsInit) {
        		mCurUpdatePostions.add(position); // 防止第一次滑动时会闪屏的问题
        		holder.updateImage(getItem(position), position);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return convertView;
    }

    public abstract class ViewHolder<T> {

    	private int mTag = -1;

        /**
         * 初始化ViewHolder
         * @param view
         */
        public abstract void initHolder(View view, int position);

        /**
         * 装载数据
         * @param data
         */
        public void loadData(T data, int position) {
        	loadData(data, position, getSelectedIndex() == position);
        }
        
        /**
         * 装载数据
         * @param data 数据
         * @param position 加载第几个
         * @param isSelected 是否选中
         */
        public void loadData(T data, int position, boolean isSelected) {
        	
        }
        
        /**
         * 刷新图片，如果设置了setListView接口，在滑动结束时会调用该方法进行刷新图片
         * @param data
         * @param position
         */
        public void updateImage(T data, int position) {
        	
        }
        
        /**
         * 设置一个标识符
         * @param tag
         */
        public void setTag(int tag) {
        	mTag = tag;
        }
        
        public int getTag() {
        	return mTag;
        }
    }
}
