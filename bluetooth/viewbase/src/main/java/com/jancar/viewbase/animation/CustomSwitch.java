package com.jancar.viewbase.animation;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;

import com.jancar.sdk.utils.Logcat;
import com.jancar.viewbase.R;
import com.jancar.viewbase.utils.Channel;




/**
 * 自定义switch（动画）开关,基本用法，
 * 将布局中CheckBox替换为com.roadrover.viewbase.animation.CustomSwitch之后，将android:button="@null"
 * 其他部分没必要修改，本控件会根据不同的渠道修改为不同的颜色，如果想要自定义颜色也可以在布局中通过
 *  RRSwitch:switch_check_color="@color/xxxxxxx"          选中颜色
 *  RRSwitch:switch_un_check_color="@color/xxxxxxx"       未选中颜色
 *  RRSwitch:switch_circle_color="@color/xxxxxxx"         圆圈的颜色
 *  RRSwitch:switch_un_enable_color="@color/xxxxxxx"      不能点击状态的颜色
 *  上面这些属性来自定义
 */
@SuppressLint("AppCompatCustomView")
public class CustomSwitch extends CheckBox {
    private Context mContext;
    private View.OnClickListener mOnClickListener;
    /*** 圆形移动动画*/
    private ObjectAnimator mProcessAnimator;
    /*** 开关是否选中状态*/
    private boolean mIsChecked = false;
    /*** 是否第一次设置*/
    private boolean mIsFirstSet = true;
    /*** 是否第一次点击*/
    private boolean mIsFirstClick = true;
    /*** 边框画笔*/
    @Deprecated
    private Paint mFramePaint;
    /*** 背景画笔*/
    private Paint mBackPaint;
    /*** 圆形画笔*/
    private Paint mRoundPaint;
    /*** 画背景、边框需要的矩形*/
    private RectF mRectF;
    /*** 画背景、边框距控件的偏移*/
    private int mOffset = 0;
    /*** 动画进度数值0~1*/
    private float mProcess = 0;
    /*** 当前背景填充颜色*/
    private int mFillColor;
    /*** 圆圈填充颜色*/
    private int mFillCircleColor;
    /*** 选择颜色变量*/
    private int mCheckedColor;
    /*** 未选择颜色变量*/
    private int mUnCheckedColor;
    /*** 圆圈的颜色变量*/
    private int mCircleColor;
    /*** 不能点击状态UnEnable的颜色变量*/
    private int mUnEnableColor;
    /*** 选择颜色*/
    private static int sColorChecked = 0;
    /*** 未选择颜色*/
    private static int sColorUnchecked = 0;
    /*** 圆圈的颜色*/
    private static int sColorCircle = 0;
    /*** 不能点击状态UnEnable的颜色*/
    private static int sColorUnEnable = 0;
    /*** 边框颜色常量，因为效果不好，所以没有画边框*/
    @Deprecated
    private static final int COLOR_RADIUS = Color.parseColor("#BDBDBD");
    /*** 动画执行时间*/
    private static final int ANIMATION_DURATION = 400;
    /*** 动画初始化执行时间,为了快速执行*/
    private static final int INIT_DURATION = 10;
    /*** 控件默认宽度*/
    private static final int DEFAULT_WIDTH = 110;
    /*** 控件默认高度*/
    private static final int DEFAULT_HEIGHT = 60;
    private static final float HALF_VALUE = 0.5f;
    public static final String TAG = "switch";

    public CustomSwitch(Context context) {
        this(context, null);
    }

    public CustomSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }

    /**
     * 初始化控件
     *
     * @param attrs attrs
     */
    private void init(AttributeSet attrs) {
        initDefaultColorByChannel();
        initParm(attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initAnimation();
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            //AT_MOST或者UNSPECIFIED，即用户没有指定宽度时，显示默认宽度
            width = DEFAULT_WIDTH;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            //AT_MOST或者UNSPECIFIED，即用户没有指定高度时，显示默认高度
            height = DEFAULT_HEIGHT;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mOffset = height / 4;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画边框
        //drawFrameRoundRect(canvas);
        //画背景
        drawRect(canvas);
        //画小球
        drawCircle(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不可点击
        if (!isEnabled()) {
            return false;
        }
        //第一次直接设置,设置动画时长
        if (mIsFirstClick) {
            mIsFirstClick = false;
            mProcessAnimator.setDuration(ANIMATION_DURATION);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setChecked(!isChecked());
                if (mOnClickListener != null) { // 为了实现onClick的监听
                    mOnClickListener.onClick(this);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = listener;
    }

    @Override
    public void setChecked(boolean checked) {
//        Logcat.d("tag setChecked " + checked + ";mIsChecked " + mIsChecked + ";mIsFirstSet " + mIsFirstSet);
        if (mIsChecked != checked) {
            // 把这个方法从上面移到这里是为了解决双击开关，开关状态和实际状态相反的问题
            super.setChecked(checked);

            mIsChecked = checked;
            animateSwitch(mIsChecked);
        } else {
            // 第一次false需要绘制
            if (mIsFirstSet && !checked) {
                // 把这个方法从上面移到这里是为了解决双击开关，开关状态和实际状态相反的问题
                super.setChecked(checked);

                mIsFirstSet = false;
                animateSwitch(mIsChecked);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Logcat.d("tag setEnabled " + enabled);
        if (enabled) {
            if (isChecked()) {
                mFillColor = mCheckedColor;
            } else {
                mFillColor = mUnCheckedColor;
            }
            mFillCircleColor = mCircleColor;
        } else {
            mFillColor = mUnEnableColor;
            mFillCircleColor = mUnEnableColor;
        }
        invalidate();
    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }

    /**
     * 根据不同的渠道初始化不同的默认颜色
     * （包括 选中状态、未选中状态、以及圆圈的颜色）
     */
    private void initDefaultColorByChannel() {
        if (Channel.isChannelGray(getContext())) {
            sColorChecked = getResources().getColor(R.color.gray_checked_color);
            sColorUnchecked = getResources().getColor(R.color.gray_unchecked_color);
            sColorCircle = getResources().getColor(R.color.gray_circle_color);
            sColorUnEnable = getResources().getColor(R.color.gray_un_enable_color);
        } else if (Channel.isChannelCHR(getContext())) {
            sColorChecked = getResources().getColor(R.color.chr_checked_color);
            sColorUnchecked = getResources().getColor(R.color.chr_unchecked_color);
            sColorCircle = getResources().getColor(R.color.chr_circle_color);
            sColorUnEnable = getResources().getColor(R.color.chr_un_enable_color);
        } else if (Channel.isChannelBlack(getContext())) {
            sColorChecked = getResources().getColor(R.color.black_checked_color);
            sColorUnchecked = getResources().getColor(R.color.black_unchecked_color);
            sColorCircle = getResources().getColor(R.color.black_circle_color);
            sColorUnEnable = getResources().getColor(R.color.black_un_enable_color);
        } else if (Channel.isChannelBlue(getContext())) {
            sColorChecked = getResources().getColor(R.color.blue_checked_color);
            sColorUnchecked = getResources().getColor(R.color.blue_unchecked_color);
            sColorCircle = getResources().getColor(R.color.blue_circle_color);
            sColorUnEnable = getResources().getColor(R.color.blue_un_enable_color);
        } else if (Channel.isChannelMetal(getContext())) {
            sColorChecked = getResources().getColor(R.color.metal_checked_color);
            sColorUnchecked = getResources().getColor(R.color.metal_unchecked_color);
            sColorCircle = getResources().getColor(R.color.metal_circle_color);
            sColorUnEnable = getResources().getColor(R.color.metal_un_enable_color);
        } else if (Channel.isChannelVertical(getContext())) {
            sColorChecked = getResources().getColor(R.color.vertical_checked_color);
            sColorUnchecked = getResources().getColor(R.color.vertical_unchecked_color);
            sColorCircle = getResources().getColor(R.color.vertical_circle_color);
            sColorUnEnable = getResources().getColor(R.color.vertical_un_enable_color);
        } else {
            sColorChecked = getResources().getColor(R.color.default_checked_color);
            sColorUnchecked = getResources().getColor(R.color.default_unchecked_color);
            sColorCircle = getResources().getColor(R.color.default_circle_color);
            sColorUnEnable = getResources().getColor(R.color.default_un_enable_color);
        }
    }

    /**
     * 初始化xml参数
     *
     * @param attrs attrs
     */
    private void initParm(AttributeSet attrs) {
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.RRSwitch);
        mCheckedColor = ta.getColor(R.styleable.RRSwitch_switch_check_color, sColorChecked);
        mUnCheckedColor = ta.getColor(R.styleable.RRSwitch_switch_un_check_color, sColorUnchecked);
        mCircleColor = ta.getColor(R.styleable.RRSwitch_switch_circle_color, sColorCircle);
        mUnEnableColor = ta.getColor(R.styleable.RRSwitch_switch_un_enable_color, sColorUnEnable);
        ta.recycle();
    }

    /**
     * 初始化动画对象
     */
    @SuppressLint("ObjectAnimatorBinding")
    private void initAnimation() {
        mProcessAnimator = ObjectAnimator.ofFloat(this, "mProcess", 0, 1);
        mProcessAnimator.setDuration(INIT_DURATION);
        mProcessAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        //边框画笔
        mFramePaint = new Paint();
        mFramePaint.setAntiAlias(true);
        mFramePaint.setColor(COLOR_RADIUS);
        mFramePaint.setAlpha(255);
        mFramePaint.setStrokeWidth(1);
        mFramePaint.setStyle(Paint.Style.STROKE);

        //填充画笔
        mBackPaint = new Paint();
        mBackPaint.setAntiAlias(true);
        mBackPaint.setDither(true);
        mBackPaint.setStrokeWidth(0);
        mBackPaint.setStyle(Paint.Style.FILL);

        //圆圈画笔
        mRoundPaint = new Paint();
        mRoundPaint.setAntiAlias(true);
        mRoundPaint.setDither(true);
        mRoundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRoundPaint.setStrokeWidth(1);
    }

    /**
     * 获取滑动的进度，属性动画反射调用
     *
     * @return 进度
     */
    public float getMProcess() {
        return mProcess;
    }

    /**
     * 设置滑动的进度，属性动画反射调用
     * 通过反射被调用的，工程师不要主动调佣
     * @param mProcess 进度
     */
    public void setMProcess(float mProcess) {
        if (mProcess >= 1f) {
            this.mProcess = 1;
            mIsChecked = true;
        } else if (mProcess <= 0f) {
            this.mProcess = 0;
            mIsChecked = false;
        } else {
            this.mProcess = mProcess;
        }
        if (this.mProcess > HALF_VALUE) {
            mFillColor = mCheckedColor;
        } else {
            mFillColor = mUnCheckedColor;
        }
        mFillCircleColor = mCircleColor;
        // 不可点击状态下的开关的颜色
        if (!isEnabled()) {
            mFillColor = mUnEnableColor;
            mFillCircleColor = mUnEnableColor;
        }
        postInvalidate();
    }

    /**
     * 开关动画
     *
     * @param checked 是否选中
     */
    private void animateSwitch(boolean checked) {
        Log.d(TAG, "checked = " + checked);
        if (mProcessAnimator.isRunning()) {
            mProcessAnimator.cancel();
        }

        if (checked) {
            mProcessAnimator.setFloatValues(mProcess, 1f);
        } else {
            mProcessAnimator.setFloatValues(mProcess, 0f);
        }
        mProcessAnimator.start();
    }

    /**
     * 获取滑动时的alpha值
     *
     * @return alpha值
     */
    private int getColorAlpha() {
        int alpha;
        if (getMProcess() >= 0 && getMProcess() < HALF_VALUE) {
            alpha = (int) (255 * (1 - getMProcess()));
        } else {
            alpha = (int) (255 * getMProcess());
        }
        int colorAlpha = Color.alpha(mFillColor);
        colorAlpha = colorAlpha * alpha / 255;
        return colorAlpha;
    }

    /**
     * 绘制边框线条
     *
     * @param canvas 画布
     */
    private void drawFrameRoundRect(Canvas canvas) {
        if (mRectF == null) {
            mRectF = new RectF(mOffset, mOffset, getWidth() - mOffset, getHeight() - mOffset);
        }
        canvas.drawRoundRect(mRectF, (getHeight() - mOffset) / 2, (getHeight() - mOffset) / 2, mFramePaint);
    }

    /**
     * 绘制填充的色值
     *
     * @param canvas 画布
     */
    private void drawRect(Canvas canvas) {
        mBackPaint.setARGB(getColorAlpha(), Color.red(mFillColor), Color.green(mFillColor), Color.blue(mFillColor));
        if (mRectF == null) {
            mRectF = new RectF(mOffset, mOffset, getWidth() - mOffset, getHeight() - mOffset);
        }
        canvas.drawRoundRect(mRectF, (getHeight() - mOffset) / 2, (getHeight() - mOffset) / 2, mBackPaint);
    }

    /**
     * 绘制点击的圆形
     *
     * @param canvas 画布
     */
    private void drawCircle(Canvas canvas) {
        // 圆形右边
        float cx = getHeight() / 2 + (getWidth() - getHeight()) * mProcess;
        // 圆形右边上限
        float cxRight = getHeight() / 2 + (getWidth() - getHeight());
        // 圆形上边
        float cy = getHeight() / 2;
        // 圆角矩形半径
        float radius = (float) getHeight();
        // 小球位置偏移
        float radiusOffset = getHeight() / 14;
        // 移动变化量
        float change;

        if (mProcess < HALF_VALUE) {
            change = getHeight() * mProcess;
        } else {
            change = getHeight() - (getHeight() * mProcess);
        }

        float left = mOffset + cx - cy - radiusOffset;
        float top = mOffset - radiusOffset;
        float right = mOffset + cx + change + radiusOffset;
        float buttom = cy + mOffset + radiusOffset;
        if (right > mOffset + radiusOffset + cxRight) {
            right = mOffset + radiusOffset + cxRight;
        }

        mRoundPaint.setColor(mFillCircleColor);
        canvas.drawRoundRect(new RectF(left, top, right, buttom), radius, radius, mRoundPaint);
    }
}
