package com.jancar.viewbase.viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import com.jancar.sdk.utils.TimerUtil;
import com.jancar.sdk.utils.TimerUtil.TimerCallback;

/**
 * 图片水平滑动，该类需要和 HorizontalLinearLayout 以及 HorizontalLayoutAdapter类一起使用
 * 主要用于优化水平滑动，增加水平滑动过程中，不更新图片，防止滑动过快，太卡的问题
 * @author 谢彬
 * @date 2016/11/14
 */
public class ImageHorizontalScrollView extends HorizontalScrollView {

	private HorizontalLinearLayout mCoverFlowGroup = null; // lineGroup
	
	private TimerUtil mDetectionScrollTimer = null; // 检测滑动时候结束

	public ImageHorizontalScrollView(Context context) {
		this(context, null);
	}

	public ImageHorizontalScrollView(Context context, AttributeSet set) {
		this(context, set, 0);
	}

	public ImageHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		mDetectionScrollTimer = new TimerUtil(new TimerCallback() {
			
			@Override
			public void timeout() {
				mDetectionScrollTimer.stop();
				
				mCoverFlowGroup.onScrollStop(getScrollX(), getWidth());
			}
		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override  
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		if (null == mCoverFlowGroup && getChildAt(0) instanceof HorizontalLinearLayout) {
			mCoverFlowGroup = (HorizontalLinearLayout) getChildAt(0);
		}
		if (null != mCoverFlowGroup) {
			mCoverFlowGroup.onScrollChanged(x, y, getWidth(), getHeight());
		}
		
		mDetectionScrollTimer.start(50); // 50ms检测一次
	}
}
