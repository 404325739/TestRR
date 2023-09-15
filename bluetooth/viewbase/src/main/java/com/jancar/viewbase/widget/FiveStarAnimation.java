package com.jancar.viewbase.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.jancar.viewbase.R;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/***
 * 五角星形状的check动画，放到布局中使用即可，
 * <P>会显示出一个五角星，选中显示实心，不选中显示空心，由未选中到选中过程的执行五角星缩放，小圆点散开并消失等动画
 * <P>可以通过xml，或者相应的设置项设置颜色，动画持续时间，是否选中，内部视图比例等选项
 *
 * 内部的执行的方式是：
 * 这个动画内部分两种动画，一种是五角星的缩放动画，另一种是一圈小圆点的发散动画，用两个View
 * {@link FiveStarsView} 和 {@link DiffusePointView}，两个动画过程通过View的缩放功能，缩放这两视图实现
 * FiveStarAnimation是一个FrameLayout，两部装入了这两个View
 * 具体计算方式见动画执行过程{@link FiveStarAnimation#initAnimation()}
 */
public class FiveStarAnimation extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "CollectView";
    /************* 动画持续时间，大小等的默认值，可以用来参考进行设置 ***************/
    private static final int COLOR_FIVE_STAR_DEFAULT = Color.YELLOW;
    private static final int COLOR_SIX_START_DEFAULT = Color.BLUE;
    private static final int DEFAULT_ANIMATION_DURATION = 400;
    private static final float DEFAULT_FIVE_STAR_RATIO = 0.66f;
    private static final float DEFAULT_DIFFUSE_POINT_RATIO = 1/25f;

    private float mFiveStarRatio = DEFAULT_FIVE_STAR_RATIO;
    private float mPointRatio = DEFAULT_DIFFUSE_POINT_RATIO;

    /** 五角星View */
    private FiveStarsView mFiveStarView;
    /** 动画过程中发散出去的点的View */
    private DiffusePointView mDiffusePointView;

    private ValueAnimator mAnimator;
    private int mSixStarRadius = 8;
    private boolean mIsChecked;

    /** 动画和点击监听器 */
    private Animator.AnimatorListener mAnimatorListener;
    private OnClickListener mClickListener;

    public FiveStarAnimation(@NonNull Context context) {
        this(context, null);
    }

    public FiveStarAnimation(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveStarAnimation(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mFiveStarView = new FiveStarsView(context, null);
        mDiffusePointView = new DiffusePointView(context, null);
        mDiffusePointView.setVisibility(GONE);
        addView(mDiffusePointView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        addView(mFiveStarView, layoutParams);
        initAnimation();


        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FiveStarAnimation);
            if (ta != null) {
                int fiveColor = ta.getColor(R.styleable.FiveStarAnimation_five_star_color, COLOR_FIVE_STAR_DEFAULT);
                int sixColor = ta.getColor(R.styleable.FiveStarAnimation_diffuse_point_color, COLOR_SIX_START_DEFAULT);
                long animationDuration = ta.getInt(R.styleable.FiveStarAnimation_five_star_animation_duration, DEFAULT_ANIMATION_DURATION);
                float fiveStarRatio = ta.getFloat(R.styleable.FiveStarAnimation_five_star_ratio, DEFAULT_FIVE_STAR_RATIO);
                float diffusePointRatio = ta.getFloat(R.styleable.FiveStarAnimation_diffuse_point_ratio, DEFAULT_DIFFUSE_POINT_RATIO);
                setFiveStarColor(fiveColor);
                setDiffusePointColor(sixColor);
                setAnimationDuration(animationDuration);
                setFiveStarRatio(fiveStarRatio);
                setDiffusePointRatio(diffusePointRatio);
                mIsChecked = ta.getBoolean(R.styleable.FiveStarAnimation_collect_view_checked, false);
                mFiveStarView.setChecked(mIsChecked);
                ta.recycle();
            }
        }
        super.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = getMeasuredWidth(), measureHeight = getMeasuredHeight();
        int squareWidth = Math.min(measureWidth, measureHeight);
        int fiveStarWidth = Math.round(squareWidth * mFiveStarRatio);
        FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width =  fiveStarWidth;
        layoutParams.height =  fiveStarWidth;
        mFiveStarView.setLayoutParams(layoutParams);
        mSixStarRadius = Math.round(squareWidth * mPointRatio);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    private void initAnimation() {
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curTime = (float) animation.getAnimatedValue();
                // 五角星的动画过程分三个阶段，由正常尺寸缩小->放大到超过正常尺寸->缩小到正常尺寸,
                // 通过两个时间节点分成三个阶段，然后每个阶段根据时间计算View的缩放比例
                float timeNode1 = 0.3f; //
                float timeNode2 = 0.8f;
                final float timeInterval3 = 0.2f;
                if (curTime < timeNode1) { // 缩小并unchecked
                    mFiveStarView.setScaleX(-(curTime - timeNode1) / timeNode1);
                    mFiveStarView.setScaleY(-(curTime - timeNode1) / timeNode1);
                } else if (curTime < timeNode2) {  // 放大并checked
                    setChecked(true);
                    float ratio = (curTime - timeNode1) / 0.35f;
                    mFiveStarView.setScaleX(ratio);
                    mFiveStarView.setScaleY(ratio);
                } else { // 第二部放大超过一点，这一步缩小回去
                    float e = 1 + 0.15f / 0.35f;
                    float v = 0.15f / 0.35f / timeInterval3;
                    float dt = curTime - timeNode2;
                    float s = v * dt;
                    float ratio = e - s;
                    mFiveStarView.setScaleX(ratio);
                    mFiveStarView.setScaleY(ratio);
                }
                if (curTime > timeNode1) {
                    processSixAnimation((curTime - timeNode1) / (1 - timeNode1));
                }
                invalidate();
            }

            /**
              * 发散的点的放大，也分为三个阶段，圆点大小不变并向外扩散->圆点减小并向外扩散->圆点减速扩散并慢慢消失
             *  根据时间和时间节点计算尺寸比例
              */
            private void processSixAnimation(float time) {
                Log.e("CollectView", "onAnimationUpdate: " + time);
                float timeNode1 = 0.5f;
                float timeNode2 = 0.80f;
                if (time < timeNode1) { // 大小不变
                    mDiffusePointView.setStarRadius(mSixStarRadius);
                    mDiffusePointView.setLayoutParams(setSixLayoutParams((LayoutParams) mDiffusePointView.getLayoutParams(),
                            (int)(getWidth() * time), (int)(getHeight() * time)));
                } else if (time < timeNode2) { // 逐渐减小
                    float v  = (1 - 0.8f) / (timeNode2 - timeNode1);
                    float dt = time - timeNode1;
                    float s  = v * dt;
                    mDiffusePointView.setStarRadius(mSixStarRadius * (1 - s));
                    mDiffusePointView.setLayoutParams(setSixLayoutParams((LayoutParams) mDiffusePointView.getLayoutParams(),
                            (int)(getWidth() * time), (int)(getHeight() * time)));
                } else { // 低速运动，等待消失
                    float t = time - timeNode2;
                    float v = 0.05f / (1 - timeNode2);
                    float s = v * t;
                    mDiffusePointView.setLayoutParams(setSixLayoutParams((LayoutParams) mDiffusePointView.getLayoutParams(),
                            (int)(getWidth() * timeNode2 + s), (int)(getHeight() * timeNode2 + s)));
                }
                Log.e(TAG, "onAnimationUpdate: " + (mDiffusePointView.getVisibility() == VISIBLE));
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDiffusePointView.setLayoutParams(setSixLayoutParams((LayoutParams) mDiffusePointView.getLayoutParams(),
                        0, 0));
                mDiffusePointView.setVisibility(VISIBLE);
                if (mAnimatorListener != null) {
                    mAnimatorListener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mDiffusePointView.setVisibility(GONE);
                if (mAnimatorListener != null) {
                    mAnimatorListener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mAnimatorListener != null) {
                    mAnimatorListener.onAnimationCancel(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (mAnimatorListener != null) {
                    mAnimatorListener.onAnimationRepeat(animation);
                }
            }
        });
        mAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
    }

    /**
     * 设置点击监听器，仍然科技监听点击事件
     */
    @Override
    public void setOnClickListener(@Nullable final OnClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null)
            mClickListener.onClick(v);
        if (mAnimator.isRunning())
            return;
        setChecked(!mIsChecked);
        if (mIsChecked) {
            mAnimator.start();
        } else {
            mDiffusePointView.setVisibility(GONE);
        }
    }

    /**
     * 设置是否选中，不产生动画
     */
    public void setChecked(boolean isChecked) {
        if (mIsChecked != isChecked) {
            mIsChecked = isChecked;
            mFiveStarView.setChecked(mIsChecked);
        }
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        mAnimator.end();
    }

    /**
     * 设置五角星的颜色
     * @param color
     */
    public void setFiveStarColor(int color) {
        mFiveStarView.setColor(color);
    }

    public int getFiveStarColor() {
        return mFiveStarView.getColor();
    }

    /**
     * 设置发散出去的点的颜色
     */
    public void setDiffusePointColor(int color) {
        mDiffusePointView.setColor(color);
    }

    /**
     * 设置动画持续时间, 必须大于 0, 动画过程中设置无效
     */
    public void setAnimationDuration(long duration) {
        if (duration > 0 && !mAnimator.isRunning()) {
            mAnimator.setDuration(duration);
        }
    }

    public long getAnimatinDuration() {
        return mAnimator.getDuration();
    }

    /**
     * 设置五角星大小占整个视图的比例，
     * @param ratio 必须在0-1之间
     */
    public void setFiveStarRatio(float ratio) {
        if (0 < ratio && ratio < 1) {
            mFiveStarRatio = ratio;
        }
    }

    /**
     * 设置发散出去的点的颜色
     * @param ratio  必须在0-1之间
     */
    public void setDiffusePointRatio(float ratio) {
        if (0 < ratio && ratio < 1) {
            mPointRatio = ratio;
        }
    }

    /**
     * 设置动画的监听器
     */
    public void setAnnimationListener(Animator.AnimatorListener animationListener) {
        this.mAnimatorListener = animationListener;
    }

    public int getDiifusePointColor() {
        return mDiffusePointView.getColor();
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    private LayoutParams setSixLayoutParams(LayoutParams sixLayoutParams, int w, int h) {
        sixLayoutParams.width = w;
        sixLayoutParams.height = h;
        sixLayoutParams.setMargins((getWidth() - w) / 2, (getHeight() - h) / 2, (getWidth() - w) / 2, (getWidth() - w) / 2);
        return sixLayoutParams;
    }

    /**
     * 动画过程中发散出去的点，形式上是一圈圆点
     */
    private class DiffusePointView extends View {
        PointF A = new PointF(), B = new PointF(), C = new PointF(), D = new PointF(), E = new PointF(), F = new PointF();
        private Paint mPaint;
        private float paddingW;
        private float paddingH;

        public DiffusePointView(Context context) {
            this(context, null);
        }

        public DiffusePointView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public DiffusePointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            mPaint = new Paint();
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setDither(true);
            mPaint.setAntiAlias(true);
        }

        public void setColor(int color) {
            mPaint.setColor(color);
        }

        public int getColor() {
            return mPaint.getColor();
        }

        public void setStarRadius(float radius) {
            Log.e("SixStarView", "setStarRadius: " + radius);
            mPaint.setStrokeWidth(radius);
        }

        public float getStarRadius() {
            return mPaint.getStrokeWidth();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            float nh = h * 0.8f;
            float nw = (float) (nh / 2 * Math.sqrt(3.0));
            paddingW = (w - nw) / 2;
            paddingH = h * 0.1f;

            A.set(nw / 2, 0);
            float FG = (float) (nw / 2 / Math.sqrt(3.0));
            F.set(0, FG);
            B.set(nw, FG);
            E.set(0, nh - FG);
            C.set(nw, nh - FG);
            D.set(nw / 2, nh);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            canvas.translate(paddingW, paddingH);
            canvas.drawPoint(A.x, A.y, mPaint);
            canvas.drawPoint(B.x, B.y, mPaint);
            canvas.drawPoint(C.x, C.y, mPaint);
            canvas.drawPoint(D.x, D.y, mPaint);
            canvas.drawPoint(E.x, E.y, mPaint);
            canvas.drawPoint(F.x, F.y, mPaint);
            Log.e("SixStarView", "draw: " + mPaint.getStrokeWidth());
        }
    }


    /**
     * 显示一个五角星，可以设置是否颜色、选中等，选中是空心，否则实心的，宽高必须一样
     */
    public static class FiveStarsView extends View {

        private float mInnerPadding;
        private float sw;
        private Path mPath;
        private Paint mPaint;
        /** 外围顶点 **/
        private PointF mA = new PointF(), mB = new PointF(), mC = new PointF(), mD = new PointF(), mE = new PointF();
        /** 内部顶点 **/
        private PointF mH = new PointF(), mI = new PointF(), mJ = new PointF(), mK = new PointF(), mL = new PointF();
        private boolean mIsChecked;

        /**
         * 五角星宽比高长，位移补足，使其居中
         */
        private float mHeightOffset;

        public FiveStarsView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public FiveStarsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initData();
            if (attrs != null) {
                TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FiveStarsView);
                if (ta != null) {
                    int color = ta.getColor(R.styleable.FiveStarsView_color_five_star, COLOR_FIVE_STAR_DEFAULT);
                    setColor(color);
                    mIsChecked = ta.getBoolean(R.styleable.FiveStarsView_checked_five_star, false);
                    setChecked(mIsChecked);
                    ta.recycle();
                }
            }
        }

        public void toggle() {
            setChecked(!isChecked());
        }

        public boolean isChecked() {
            return mIsChecked;
        }

        public void setChecked(boolean checked) {
            mIsChecked = checked;
            if (checked) {
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
            }
            invalidate();
        }

        private void initData() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPath = new Path();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // 内部边距
            mInnerPadding = w / 10;
            sw = w - mInnerPadding * 2;
            setValues(sw);
            mPath.rewind();
            mPath.moveTo(mA.x, mA.y);
            mPath.lineTo(mH.x, mH.y);
            mPath.lineTo(mB.x, mB.y);
            mPath.lineTo(mI.x, mI.y);
            mPath.lineTo(mC.x, mC.y);

            mPath.lineTo(mJ.x, mJ.y);
            mPath.lineTo(mD.x, mD.y);
            mPath.lineTo(mK.x, mK.y);
            mPath.lineTo(mE.x, mE.y);
            mPath.lineTo(mL.x, mL.y);
            mPath.close();
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.translate(mInnerPadding, mInnerPadding + mHeightOffset);
            if (isChecked()) mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            else mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
        }

        private void setValues(float w) {
            // 五角星个各点
            float sin36 = (float) sin(36d / 180d * PI), cos36 = (float) cos(36d / 180d * PI);
            float sin72 = (float) sin(72d / 180d * PI), cos72 = (float) cos(72d / 180d * PI);
            float AL = w / 2 / (1 + cos72); // 五角星一个三角形角的边长
            float AN =  AL * sin72;
            mA.set(w / 2, 0);
            mE.set(0, AN);
            mB.set(w, AN);
            mL.set(AL, AN);
            mH.set(w - AL, AN);
            float EM = AL * cos36;
            float AK = AN + AL * sin36;
            mK.set(EM, AK);
            mI.set(w - EM, AK);
            float AP = AK + AL * sin72;
            float DC = w * cos36;
            mC.set(DC, AP);
            mD.set(w - DC, AP);
            mJ.set(w / 2, AP - AL * sin36);

            // 五角星宽比高长，位移补足，使其居中
            mHeightOffset = w * (1 - sin72) / 2;

            mPaint.setStrokeWidth(w / 15);
        }

        public void setColor(int color) {
            mPaint.setColor(color);
        }

        public int getColor() {
            return mPaint.getColor();
        }
    }
}
