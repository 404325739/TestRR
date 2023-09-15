package com.jancar.viewbase.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.jancar.sdk.utils.TimerUtil;
import com.jancar.sdk.utils.TimerUtil.TimerCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 可以播放gif 的ImageView，同时可以显示gif图片和png,jpg图片
 * @author bin.xie
 * @date 2016/11/14
 */
public class GifImageView extends ImageView {
	
    private Movie mMovie = null; // 播放GIF动画的关键类   
    
    private long mMovieStart = 0; // 动画开始
    private int mMovieCount = 0; // 绘制次数
    private int mMovieFrame = 1000 / 24; // 帧率，如果CPU占用率过高，可以调低帧率，1S 24帧为肉眼可以分辨的帧率
    private boolean mIsPlaying = false; // 是否开始播放
    private TimerUtil mDrawGifTimer = null; // 绘制gif的定时器
    
    /**  
     * GIF图片的宽度  
     */ 
    private int mImageWidth;  
 
    /**  
     * GIF图片的高度  
     */ 
    private int mImageHeight;  
    
    public interface GifCallback {
    	void onStart(); // 动画开始
    	void onStop(); // 动画结束
    }
    private GifCallback mGifCallback = null;

	public GifImageView(Context context) {
		this(context, null);
	}

	public GifImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GifImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		
		mDrawGifTimer = new TimerUtil(new TimerCallback() {
			
			@Override
			public void timeout() {
				if (++mMovieStart > mMovieCount) {
					mDrawGifTimer.stop();
					mIsPlaying = false;
					mMovieStart = 0;
					
					if (null != mGifCallback) {
						// 绘制结束
						mGifCallback.onStop(); 
					}
					start();
				}
				invalidate(); // 绘制
			}
		});
	}
	
	/**
	 * 设置gif动画回调
	 * @param callback
	 */
	public void setGifCallback(GifCallback callback) {
		mGifCallback = callback;
	}

	/**
	 * 设置gif图片路径
	 * @param path 文件路径，可以是jpg, png, gif 等格式
	 */
	public void setFilePath(String path) {
		if (null != mGifCallback) { // 绘制开始
			mGifCallback.onStart();
		}
		
		InputStream is = null;
		try {
			File file = new File(path);
			if (file.exists()) {
				is = new BufferedInputStream(new FileInputStream(file), 16 * 1024);
				
				Bitmap bitmap = BitmapFactory.decodeStream(is);  
				if (null != bitmap) {
		            mImageWidth = bitmap.getWidth();  
		            mImageHeight = bitmap.getHeight();  
		            bitmap.recycle();
		            bitmap = null;
		            
		            try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		            
		            is = new BufferedInputStream(new FileInputStream(file), 16 * 1024);
		            is.mark(16 * 1024);
					mMovie = Movie.decodeStream(is);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (null != mMovie) {
				start();
			} else {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(path, options);
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inSampleSize = calculateInSampleSize(options, mImageWidth, mImageHeight);
				options.inJustDecodeBounds = false;
				setImageBitmap(BitmapFactory.decodeFile(path, options));
				if (null != mGifCallback) {
					// 图片，直接绘制结束
					mGifCallback.onStop(); 
				}
			}
		}
	}
	
	@Override 
    protected void onDraw(Canvas canvas) {  
        if (mMovie == null) {  
            // mMovie等于null，说明是张普通的图片，则直接调用父类的onDraw()方法  
            super.onDraw(canvas);  
        } else {  
        	if (mIsPlaying) {  
        		drawMovie(canvas);
        	} else {
        		mMovie.setTime(0);  
                mMovie.draw(canvas, (getWidth() - mImageWidth) / 2, (getHeight() - mImageHeight) / 2);  
        	}
        }  
    }  
	 
	/**  
     * 绘制gif图片
     * @param canvas  
     */ 
    private void drawMovie(Canvas canvas) {  
    	mMovie.setTime((int) (mMovieFrame * mMovieStart));
    	mMovie.draw(canvas, (getWidth() - mImageWidth) / 2, (getHeight() - mImageHeight) / 2);
    }  
    
    /**
     * 开始播放flash，开启一个定时器，进行绘制
     */
    private void start() {
    	mMovieStart = 0;
    	mIsPlaying = true;
    	
    	invalidate(); // 先绘制一帧
    	if (null != mMovie) {
    		mMovieCount = mMovie.duration() / mMovieFrame + 1;
    		mDrawGifTimer.start(mMovieFrame); // 1s 24帧
    	}
    }

	/**
	 * 计算缩放比
	 * @param op
	 * @param reqWidth
	 * @param reqheight
     * @return
     */
	private int calculateInSampleSize(BitmapFactory.Options op, int reqWidth,
									  int reqheight) {
		int originalWidth = op.outWidth;
		int originalHeight = op.outHeight;
		int inSampleSize = 1;
		if (originalWidth > reqWidth || originalHeight > reqheight) {
			int halfWidth = originalWidth / 2;
			int halfHeight = originalHeight / 2;
			while ((halfWidth / inSampleSize > reqWidth)
					&&(halfHeight / inSampleSize > reqheight)) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
//		if (mMovie != null) {  
//            // 如果是GIF图片则重写设定PowerImageView的大小  
//            setMeasuredDimension(mImageWidth, mImageHeight);  
//        }  
	}

	@Override
	protected void onDetachedFromWindow() {
		mDrawGifTimer.stop();
		mMovie = null;
		super.onDetachedFromWindow();
	}
}
