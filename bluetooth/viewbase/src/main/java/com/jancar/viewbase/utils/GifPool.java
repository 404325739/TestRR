package com.jancar.viewbase.utils;

import android.content.Context;

import com.jancar.utils.ObjectPool;
import com.jancar.viewbase.widget.GifImageView;

/**
 * GifImageView对象池
 * Created by hongaifeng on 2018/2/1.
 */

public class GifPool extends ObjectPool<GifImageView> {

    private Context mContext = null;

    public GifPool(Context context) {
        mContext = context;
    }

    @Override
    protected GifImageView initObject() {
        return new GifImageView(mContext);
    }
}
