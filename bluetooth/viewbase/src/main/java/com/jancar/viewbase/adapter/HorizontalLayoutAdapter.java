package com.jancar.viewbase.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 水平滚动的HorizontalLinearLayout的适配器
 * @author bin.xie
 * @date 2016/4/22
 * @param <T>
 */
public abstract class HorizontalLayoutAdapter<T> {
	private List<T> dataList = new ArrayList<T>();
	
	private LayoutInflater mInflater;
	protected Context mContext;
	
	public interface ScrollViewLayoutCallback {
		void onUpdate(boolean isSeekToStartPos); // 整体刷新
	} 
	private ScrollViewLayoutCallback mScrollViewLayoutCallback = null;
	
	public HorizontalLayoutAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	
	/**
	 * 设置与layout通信的回调
	 * @param callback
	 */
	public void setScrollViewLayoutCallback(ScrollViewLayoutCallback callback) {
		mScrollViewLayoutCallback = callback;
	}

    /**
     * 会清除之前的数据
     * @param data
     */
    public void setData(List<T> data){
    	if (dataList == data) { // 如果数据一样，不重新加载
    		return;
    	}
    	
        if(dataList == null)
            dataList = new ArrayList<T>();
        dataList.clear();
        if(data != null)
            dataList.addAll(data);
        notifyDataSetChanged(true);
    }
    
    /**
     * 在原始数据上添加新数据
     * @param data
     */
    public void addData(List<T> data){
        if(dataList == null)
            dataList = new ArrayList<T>();
        if(data != null)
            dataList.addAll(data);
        notifyDataSetChanged(false);
    }
    
    public int getCount() {
        if(dataList!=null)
            return dataList.size();
        return 0;
    }
    
    public T getItem(int position) {
        // TODO Auto-generated method stub
        if(dataList!=null)
            try{
                return dataList.get(position);
            }catch(Exception g){
                return null;
            }
        return null;
    }
    
    public abstract int getConvertViewId(int position);

    public abstract ViewHolder<T> getNewHolder(int position);

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder<T> holder;
        if(convertView==null){
            holder = getNewHolder(position);
            convertView = mInflater.inflate(getConvertViewId(position), null);
            holder.initHolder(convertView,position);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder<T>)convertView.getTag();
        }
      
        holder.setUpdate(false); // 重新getView 重新初始化
        try {
          	holder.loadData(getItem(position),position, false);
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
        
        return convertView;
    }
    
    /**
     * 刷新View，初始化和刷新分开，水平滑动图片太多太卡的问题
     * @param position
     * @param convertView
     */
    public void updateView(int position, View convertView) {
    	if (null != convertView) {
    		ViewHolder<T> holder = (ViewHolder<T>)convertView.getTag();
    		if (holder.getUpdate()) { // 已经刷新过
    			return;
    		}
    		holder.setUpdate(true);
	    	try {
	          	holder.loadData(getItem(position),position, true);
	  		} catch (Exception e) {
	  			e.printStackTrace();
	  		}
    	}
    }
    
    public abstract class ViewHolder<T> {
    	private boolean isUpdate = false; // 是否已经刷新
    	public boolean getUpdate() {
    		return isUpdate;
    	}
    	public void setUpdate(boolean isUpdate) {
    		this.isUpdate = isUpdate;
    	}
    	
        /**
         * 初始化ViewHolder
         * @param view
         */
        public abstract void initHolder(View view, int position);
   
        /**
         * 装载数据 
         * @param data 数据
         * @param position 位置
         * @param isUpdateImage 是否刷新图片
         */
        public abstract void loadData(T data, int position, boolean isUpdateImage);
    }
    
    /**
     * 刷新界面
     * @param isSeekToStartPos 是否跳转到开始位置
     */
    public void notifyDataSetChanged(boolean isSeekToStartPos) {
    	if (null != mScrollViewLayoutCallback) {
    		mScrollViewLayoutCallback.onUpdate(isSeekToStartPos);
    	}
    }
}
