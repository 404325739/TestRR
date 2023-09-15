package com.jancar.viewbase.utils;

import android.content.Context;

import com.jancar.utils.ObjectPool;
import com.jancar.viewbase.widget.TouchImageView;

/**
 * ImagePool对象池
 * Created by hongaifeng on 2018/2/1.
 */

public class ImagePool extends ObjectPool<TouchImageView> {

	private Context mContext = null;

	public ImagePool(Context context) {
		mContext = context;
	}

	@Override
	protected TouchImageView initObject() {
		return new TouchImageView(mContext);
	}
}
