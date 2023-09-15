package com.jancar.viewbase.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.jancar.sdk.utils.Logcat;
import com.jancar.sdk.utils.TimerUtil;
import com.jancar.viewbase.R;

/**
 * 字体滑动条
 * @调用流程 LetterSlideBar slideBar = (LetterSlideBar) findViewById(R.id.lsb_letter);
 *          slideBar.setListView(listView); // 设置该listView，则能自动关联 slideBar
 *          slideBar.setTvBigLetter(textView); // 设置该方法，则会在滑动时，自动刷新该TextView
 *          slideBar.setOnLetterChangeLister(lister); // getPositionLetter getLetterPosition 两个方法需要实现
 */

public class LetterSlideBar extends View implements AbsListView.OnScrollListener {

    private int mTextColor, mTextLightColor;
    private float mTextSize;
    private Bitmap mLetterBg;
    private static final float DEF_TEXT_SIZE = 16;
    private static final float DEF_BG_SIZE = 20;
    private static final float DEF_MOVE_VALUE = 5;
    private TextView mTvBigLetter; // 大字母
    private TimerUtil mTimerUtil;
    private ListView mListView; // 列表
    private float mBgWidth, mBgHeight, mMoveX, mMoveY; // 字母背景的长，宽，左右移动的值，上下移动的值
    private boolean mIsSideBarMove = false;
    private boolean mIsStartWithSpecial = false;

    private static String[] LETTER = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z"
    };

    private Paint mPaint = new Paint();
    private int mChoose = -1;
    private String mCurFirstLetter = "";

    private static final int TIMER_OUT = 2 * 1000; // 定时器超时时间

    public void hideLetterTip() {
        if (mTvBigLetter != null) {
            mTvBigLetter.setVisibility(GONE);
            mChoose = -1;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mOnLetterChangeListener != null) {
            Logcat.d("onScroll", "firstVisibleItem = "+firstVisibleItem + ", totalItemCount = " +totalItemCount);
            mCurFirstLetter = getFistLetter(mOnLetterChangeListener.getPositionLetter(firstVisibleItem));
            if (totalItemCount > 0) {
                setCurLetter(mCurFirstLetter);
            }
        }

        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
	/**
	 * 首字母非[a-z] 显示为#
     * @param letter
     * @return
     */
    private String getFistLetter(String letter) {
        String firstLetter = "#";
        if (!TextUtils.isEmpty(letter)) {
            String sortString = letter.substring(0, 1).toLowerCase();
            if (sortString.matches("[a-z]")) {
                firstLetter = sortString;
            }
        }
        return firstLetter;
    }

    /**
     * 字母变化监听
     */
    public interface OnLetterChangeLister {
        void onLetterChange(String letter, int position);
        /**
         * 获取listView指定position的字母
         * @param position
         * @return
         */
        String getPositionLetter(int position);
        /**
         * 获取指定字母在listView的位置
         * @param letter
         * @return
         */
        int getLetterPosition(String letter);
    }
    private OnLetterChangeLister mOnLetterChangeListener = null;

    public void setOnLetterChangeLister(OnLetterChangeLister listener) {
        mOnLetterChangeListener = listener;
    }

    /**
     * 列表滑动监听，回调给绑定listview的fragment或activity
     */
    public interface OnScrollChangeListener {
        /**
         * 列表滑动时调用
         * @param view              listview列表
         * @param scrollState       滑动状态
         */
        void onScrollStateChanged(AbsListView view, int scrollState);
        /**
         * 滑动完成后调用
         * @param view              listview列表
         * @param firstVisibleItem  第一个可见的item
         * @param visibleItemCount  可见item总数
         * @param totalItemCount    所有item总数
         */
        void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }

    private OnScrollChangeListener mScrollListener = null;

    public void setScrollListener(OnScrollChangeListener mScrollListener) {
        this.mScrollListener = mScrollListener;
    }

    public LetterSlideBar(Context context) {
        this(context, null);
    }

    public LetterSlideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterSlideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null && context != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LetterSlideBar);
            mTextColor = ta.getColor(R.styleable.LetterSlideBar_textColor, Color.WHITE);
            mTextLightColor = ta.getColor(R.styleable.LetterSlideBar_textLightColor, Color.WHITE);
            mTextSize = ta.getDimension(R.styleable.LetterSlideBar_textSize, DEF_TEXT_SIZE);
            mBgWidth = ta.getDimension(R.styleable.LetterSlideBar_bgletter_width, DEF_BG_SIZE);
            mBgHeight = ta.getDimension(R.styleable.LetterSlideBar_bgletter_height, DEF_BG_SIZE);
            mMoveX = ta.getDimension(R.styleable.LetterSlideBar_bgletter_x, DEF_MOVE_VALUE);
            mMoveY = ta.getDimension(R.styleable.LetterSlideBar_bgletter_y, DEF_MOVE_VALUE);
            mIsStartWithSpecial = ta.getBoolean(R.styleable.LetterSlideBar_startwithspecial, false);
            mLetterBg = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.LetterSlideBar_bgletter, 0));
        } else {
            mTextLightColor = mTextColor = Color.WHITE;
            mTextSize = DEF_TEXT_SIZE;
            mBgWidth = mBgHeight = DEF_BG_SIZE;
            mMoveY = mMoveX = DEF_MOVE_VALUE;
            mIsStartWithSpecial = false;
        }

        if (mIsStartWithSpecial) {
            LETTER = new String[]{"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                    "W", "X", "Y", "Z"};
        } else {
            LETTER = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                    "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
                    "X", "Y", "Z", "#"};
        }
        mChoose = 0;

        mTimerUtil = new TimerUtil(new TimerUtil.TimerCallback() {
            @Override
            public void timeout() {
                mTimerUtil.stop();

                if (mTvBigLetter != null) {
                    mTvBigLetter.setVisibility(View.GONE);
                    mChoose = -1;
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // get the height
        int height = getHeight();
        // get the width
        int width = getWidth();
        // get one letter height
        int singleHeight = height / LETTER.length;
        Rect mSrcRect, mDestRect;
        for (int i = 0; i < LETTER.length; i++) {
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(mTextSize);

            // if choosed
            float x = width / 2 - mPaint.measureText(LETTER[i]) / 2;
            float y = singleHeight * i + singleHeight;
            if(i == mChoose) {
                mPaint.setColor(mTextLightColor);
                if (mLetterBg != null) {
                    mSrcRect = new Rect(0, 0, (int)mBgWidth, (int)mBgHeight);
                    mDestRect = new Rect((int)(x - mMoveX), (int)(y - mBgHeight + mMoveY), (int)(x + mBgWidth - mMoveX), (int)(y + mMoveY));
                    canvas.drawBitmap(mLetterBg, mSrcRect, mDestRect, mPaint);
                }
            } else {
                mPaint.setColor(mTextColor);
            }

            // draw text
            canvas.drawText(LETTER[i], x, y, mPaint);
            mPaint.reset();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY(); // get the Y
        final int oldChoose = mChoose;
        int letterPos = (int)(y / getHeight() * LETTER.length);

        switch (action) {
            case MotionEvent.ACTION_UP:
                mIsSideBarMove = false;
                Logcat.d("dispatchTouchEvent","mCurFirstLetter = "+mCurFirstLetter);
                setCurLetter(mCurFirstLetter);
                mTimerUtil.start(TIMER_OUT); // 2s后消失
                break;

            default:
                mIsSideBarMove = true;
                mTimerUtil.stop();
                if (oldChoose != letterPos) {
                    if (letterPos < 0) {
                        letterPos = 0;
                    } else if (letterPos >= LETTER.length) {
                        letterPos = LETTER.length - 1;
                    }
                    if (mOnLetterChangeListener != null) {
                        mOnLetterChangeListener.onLetterChange(LETTER[letterPos], letterPos);

                        int position = mOnLetterChangeListener.getLetterPosition(LETTER[letterPos]);
                        if (mListView != null && position != -1) {
                            mListView.setSelection(position);
                        }
                    }

                    mChoose = letterPos;
                    invalidate();

                    updateTvBigLetter();
                }
                break;
        }
        return true;
    }

    /**
     * 如果存在大字母显示，设置该参数
     * @param tvBigLetter
     */
    public void setTvBigLetter(TextView tvBigLetter) {
        mTvBigLetter = tvBigLetter;
    }

    /**
     * 设置当前选中的 letter
     * @param letter
     */
    public void setCurLetter(String letter) {
        if (!TextUtils.isEmpty(letter)) {
            for (int i = 0; i < LETTER.length; ++i) {
                if (TextUtils.equals(letter, LETTER[i].toLowerCase())) {
                    if (mChoose != i && !mIsSideBarMove) {
                        mChoose = i;
                        invalidate();

                        onLetterChange();
                    }
                    break;
                }
            }
        }
    }

    public void setListView(ListView listView) {
        mListView = listView;
        if (mListView != null) {
            mListView.setOnScrollListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mTimerUtil != null) {
            mTimerUtil.stop();
        }
    }

    private void updateTvBigLetter() {
        if (mTvBigLetter != null && mChoose >= 0 && mChoose < LETTER.length) {
            mTvBigLetter.setText(LETTER[mChoose]);
            mTvBigLetter.setVisibility(View.VISIBLE);
        }
    }

    private void onLetterChange() {
        updateTvBigLetter();

        mTimerUtil.start(TIMER_OUT); // 2s后消失
    }
}
