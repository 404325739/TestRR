package com.jancar.viewbase.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.jancar.sdk.utils.TimerUtil;
import com.jancar.viewbase.R;

/**
 * Created by chensheqiu on 2017/9/7.
 */

public class RoundProgressBar extends View {

	private Paint mPaint = new Paint();

    // 圆环的颜色
	private int mRringColor;

	// 圆环进度的颜色
	private int mRingProgressColor;

	// 设置圆心进度条中间的背景色
	private int mCentreColor;

	// 中间进度百分比的字符串的颜色
	private int mTextColor;

	// 中间进度百分比的字符串的字体
	private float mTextSize;

	// 底层大圆环的宽度
	private float mSideRingWidth;

    // 高亮圆环的宽度
	private float mRingWidth;

    // 最大进度
	private int mMax;

    // 当前进度
	private int mProgress;

    // 进度开始的角度数
	private int mStartAngle;

    // 是否显示中间的数字进度
	private boolean mTextIsDisplayable;

    // 进度的风格，实心或者空心
	private int mStyle;

	// 转1°需要的时间  单位为毫秒
	private int mSpeed;

	public static final int STROKE = 0;
	public static final int FILL = 1;

	private TimerUtil mTimerUtil;
	private int mZProgress = 0;
	private int mFProgress = 360;

	public RoundProgressBar(Context context) {
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		if (context != null && attrs != null) {
			TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
			// 圆环的颜色
			mRringColor = mTypedArray.getColor(R.styleable.RoundProgressBar_ringColor, Color.WHITE);
			// 圆环进度条的颜色
			mRingProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_ringProgressColor, Color.BLACK);
			// 文字的颜色
			mTextColor = mTypedArray.getColor(R.styleable.RoundProgressBar_centreTextColor, Color.WHITE);
			// 文字的大小
			mTextSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_centreTextSize, 25);
			// 高亮圆环的宽度
			mRingWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_ringWidth, 10);
			// 底层大圆环的宽度
			mSideRingWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_sideRingWidth, 10);
			// 最大进度
			mMax = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 360);
			// 当前进度
			mProgress = mTypedArray.getInt(R.styleable.RoundProgressBar_progress, 0);
			// 是否显示中间的进度
			mTextIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_centreTextIsDisplayable, true);
			// 进度的风格，实心或者空心
			mStyle = mTypedArray.getInt(R.styleable.RoundProgressBar_style, STROKE);
			// 进度开始的角度数
			mStartAngle = mTypedArray.getInt(R.styleable.RoundProgressBar_startAngle, -90);
			// 圆心的颜色
			mCentreColor = mTypedArray.getColor(R.styleable.RoundProgressBar_centreColor, Color.TRANSPARENT);
			// 回收资源
			mSpeed = mTypedArray.getInt(R.styleable.RoundProgressBar_roundingspeed, 2);
			mTypedArray.recycle();
		} else {
			mRringColor = Color.WHITE;
			mRingProgressColor = Color.BLACK;
			mTextColor = Color.WHITE;
			mTextSize = 25;
			mRingWidth = 10;
			mSideRingWidth = 10;
			mMax = 360;
			mProgress = 0;
			mTextIsDisplayable = true;
			mStyle = STROKE;
			mStartAngle = -90;
			mCentreColor = Color.TRANSPARENT;
			mSpeed = 2;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int centre = getWidth() / 2 ; // 获取圆心的x坐标
		int radius = (int) (centre - mSideRingWidth / 2 - (mRingWidth - mSideRingWidth) / 2); // 圆环的半径

		/**
		 * 画中心的颜色
		 */
		if (mCentreColor != 0) {
			mPaint.setAntiAlias(true);
			mPaint.setColor(mCentreColor);
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(centre, centre, radius, mPaint);
		}


		/**
		 * 画最外层的大圆环
		 */
		mPaint.setColor(mRringColor); // 设置圆环的颜色
		mPaint.setStyle(Paint.Style.STROKE); // 设置空心
		mPaint.setStrokeWidth(mSideRingWidth); // 设置圆环的宽度
		mPaint.setAntiAlias(true);  // 消除锯齿
		mPaint.setDither(true); // 启用抗颜色抖动（可以让渐变更平缓）
		canvas.drawCircle(centre, centre, radius, mPaint); // 画出圆环

		/**
		 * 画圆弧 ，画圆环的进度
		 */
		// 设置进度是实心还是空心
		centre = getWidth() / 2 ;
		radius = (int) (centre - mRingWidth / 2 ); // 圆环的半径
		mPaint.setStrokeWidth(mRingWidth); // 设置圆环的宽度
		mPaint.setColor(mRingProgressColor);  // 设置进度的颜色
		RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);  // 用于定义的圆弧的形状和大小的界限
		//先创建一个渲染器
		SweepGradient mSweepGradient = new SweepGradient(canvas.getWidth() / 2, canvas.getHeight() / 2, new int[] { 0xffffe31d, 0xfff68929}, null);//以圆弧中心作为扫描渲染的中心以便实现需要的效果
		Matrix matrix = new Matrix();
		matrix.setRotate(-90f, canvas.getWidth() / 2, canvas.getHeight() / 2);
		mSweepGradient.setLocalMatrix(matrix);

		mPaint.setShader(mSweepGradient);// 把渐变设置到笔刷


		switch (mStyle) {
			case STROKE: {
				canvas.drawArc(oval, mStartAngle, 360 * mProgress / mMax, false, mPaint);  // 根据进度画圆弧
				break;
			}
			case FILL: {
				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				if (mProgress != 0)
					canvas.drawArc(oval, mStartAngle, 360 * mProgress / mMax, true, mPaint);  // 根据进度画圆弧
				break;
			}
		}
		mPaint.setShader(null); // 清空渐变

		/**
		 * 画进度百分比
		 */
		mPaint.setStrokeWidth(0);
		mPaint.setColor(mTextColor);
		mPaint.setTextSize(mTextSize);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
		int percent = (int) (((float) mProgress / (float) mMax) * 100);  // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
		float textWidth = mPaint.measureText(percent + "%");   // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间

		if (mTextIsDisplayable && percent != 0 && mStyle == STROKE) {
			canvas.drawText(percent + "%", centre - textWidth / 2, centre + mTextSize / 2, mPaint); // 画出进度百分比
		}
	}

	public synchronized int getMax() {
		return mMax;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.mMax = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized int getProgress() {
		return mProgress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if(progress < 0){
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > mMax){
			progress = mMax;
		}
		if(progress <= mMax){
			this.mProgress = progress;
			postInvalidate();
		}
	}

	public void startRoundProgress() {
		mZProgress = 0;
		mFProgress = 360;
		if (mTimerUtil == null) {
			mTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {
				@Override
				public void timeout() {
					if (mZProgress > 360) {

					} else {
						mZProgress ++;
						mStartAngle = -90;
						setProgress(mZProgress);
						return;
					}
					if ( mFProgress <= 360 && mFProgress > 0) {
						mFProgress --;
						mStartAngle ++;
						setProgress(mFProgress);
						setStartAngle(mStartAngle);
					} else {
						mZProgress = 0;
						mFProgress = 360;
					}
				}
			});
			mTimerUtil.start(mSpeed);
		}
	}

	public void stopRoundProgress() {
		if (mTimerUtil != null) {
			mTimerUtil.stop();
			mTimerUtil = null;
		}
	}

	public int getCircleColor() {
		return mRringColor;
	}

	public void setCircleColor(int CircleColor) {
		this.mRringColor = CircleColor;
	}

	public int getCircleProgressColor() {
		return mRingProgressColor;
	}

	public void setCircleProgressColor(int CircleProgressColor) {
		this.mRingProgressColor = CircleProgressColor;
	}

	public int getTextColor() {
		return mTextColor;
	}

	public void setTextColor(int textColor) {
		this.mTextColor = textColor;
	}

	public float getTextSize() {
		return mTextSize;
	}

	public void setTextSize(float textSize) {
		this.mTextSize = textSize;
	}

	public float getringWidth() {
		return mRingWidth;
	}

	public void setStartAngle(int angle) {
		this.mStartAngle = angle;
	}

	public void setringWidth(float ringWidth) {
		this.mRingWidth = ringWidth;
	}
}
