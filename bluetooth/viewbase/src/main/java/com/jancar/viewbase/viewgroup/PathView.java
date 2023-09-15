package com.jancar.viewbase.viewgroup;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jancar.viewbase.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于处理文件路径中含有希伯来等从右向左的文字并且与英文混合时，显示乱序的情况，目前没有找到更好的解决方法，
 * 采用在LinearLayout中添加多个TextView显示的方法，每个TextView显示一层文件的文件名，<br/> <br/>
 * 比如路径为a/b.ma3 其中a、/、b、.、mp3 各用一个TextView，共5个TextView显示路径 <br/> <br/>
 * 还要注意布局不够长的时候显示一个省略号{@link #setUsableWidth(int)}
 */
public class PathView extends LinearLayout {

    private static final int DEFAULT_END_LIMIT = 0;
    private LayoutInflater mInflater;
    private int mEndLimit = DEFAULT_END_LIMIT; // 不设置时的默认值，表示不用处理文本超过
    private int mUsableWidth = 100000; // 不设置时的默认值，表示不用处理文本超过
    private final String ELLIPSIS = "...";
    private int mTvId = -1;

    public PathView(Context context) {
        this(context, null);
    }

    public PathView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);

        if (attrs != null && context != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PathView);
            if (ta != null) {
                mTvId = ta.getInt(R.styleable.PathView_text_view_layout_id, -1);
                mEndLimit = ta.getInt(R.styleable.PathView_end_limit, DEFAULT_END_LIMIT);
                ta.recycle();
            }
        }
    }

    /**
     * 设置用来显示文件名的TextView的xml文件的ID，通过它设置文字的各种样式字体等
     */
    public void setTextViewId(@IdRes int id) {
        mTvId = id;
    }

    /**
     * {@link PathView}
     * 设置显示省略号的长度，当最后一个TextView与布局边界的距离小于此长度就开始显示省略号，不再加入TextView
     */
    public void setEndLimit(int endLimit) {
        mEndLimit = endLimit;
    }

    /**
     * 设置可用宽度，即用来显示所有的TextView的外控件剩余的宽度，除去Padding，Margin，其它子控件的所有宽度，
     * 因为添加TextView时无法获知此视图的总长度，若在ListView中使用，当ListView加载时能获外布局取总长度的时候添加视图又无效，
     * 所以需要手动测量然后设置
     * @param usableWidth 可用宽度
     */
    public void setUsableWidth(int usableWidth) {
        mUsableWidth = usableWidth;
    }

    /**
     * {@link #showPath(String, int)}
     * 显示文件名，只有文件名的，不同于显示路径，文件名、中间的. 、 文件后缀名各用一个TextView显示
     * @return 是否超长显示了省略号
     */
    public boolean showName(String fullName) {
        if (fullName == null) return false;
        int[] childViewLen = new int[]{0};
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            ((TextView) getChildAt(i)).setText(""); // 将所有TextView的内容去掉
        }
        int sid = fullName.lastIndexOf(".");
        if (0 <= sid && sid < fullName.length()) {
            String name = fullName.substring(0, sid);
            String suffix = fullName.substring(sid + 1, fullName.length());
            if (updateTv(childCount, 0, name, childViewLen))
                return true;
            if (updateTv(childCount, 1,  ".", childViewLen)) {
                return true;
            }
            if (updateTv(childCount, 2, suffix, childViewLen)) {
                return true;
            }
        } else {
            if (updateTv(childCount, 0, fullName, childViewLen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 显示路径，由于不同方向的语言混合时，显示错乱，设置文字方向等没解决，
     * 目前的解决方法是用TextView一个一个文件名的显示
     * {@link PathView}
     * @return 是否超长显示了省略号
     */
    public boolean showPath(String path, int size) {
        if (path == null) return false;
        final int[] childViewLen = new int[]{0};
        // step 1 获取子View数并清空内容
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            ((TextView) getChildAt(i)).setText(""); // 将所有TextView的内容去掉
        }

        // step 2 获取文件的名的列表
        List<String> nameList = splitFilePath(path);

        // step3 用已存在的或者添加TextView更新设置内容
        for (int i = 0; i < nameList.size(); ++i) {
            if (updateTv(childCount, i * 2, nameList.get(i), childViewLen)) {
                return true;
            }
            if (i < nameList.size() - 1) {
                if (updateTv(childCount, i * 2 + 1, File.separator, childViewLen)) {
                    return true;
                }
            }
        }
        if (updateTv(childCount, nameList.size() * 2, " (" + size + ")",
                childViewLen)) {
            return true;
        }
        return false;
    }

    /**
     * 更新LinearLayout中表示文件名的TextView的内容，重用TextView，
     * <p>如果TextView足够，就设置其内容，不够，就加一个，并设置内容
     * @return 是否超长，文本替换成省略号了
     */
    private boolean updateTv(final int childCount, int updateId, String name,
                             int[] childViewWid) {
        if (name == null || childViewWid == null || childViewWid.length < 1)
            return false;

        final TextView tvName;
        if (childCount > updateId) { // TextView 足够
            tvName = ((TextView) getChildAt(updateId));
            tvName.setText(name);
        } else {
            View view = mInflater.inflate(mTvId, null);
            if (view == null || !(view instanceof TextView)) { // 没有设置TextView 返回
                return false;
            }
            tvName = (TextView) view;
            addView(tvName);
            tvName.setText(name);
        }

        int tvWidth = getViewWidth(tvName);
        int usableTextWidth = mUsableWidth - mEndLimit; //  mUsableWidth - mEndLimit 不能超过这个长度
        if (childViewWid[0] + tvWidth > usableTextWidth) {
            // --调试时可打开--
            // Logcat.d("exceeding with = " + (childViewWid[0] + tvWidth - usableTextWidth));
            fixWidth(name, tvName, usableTextWidth - childViewWid[0]);
            return true;
        }
        childViewWid[0] += tvWidth;
        return false;
    }

    /**
     * 设置合适的文本长度，二分
     */
    private void fixWidth(@NonNull String name, @NonNull TextView tvName, int width) {
        int s = 0, e = name.length() - 1;
        // --调试时可打开--
        // Logcat.d(s + " " + e + " " + "width = " + width);
        while (s < e) {
            int m = (s + e) >> 1;
            tvName.setText(name.substring(0, m + 1));
            int mLen = getViewWidth(tvName);
            if (mLen < width) {
                s = m + 1;
            } else if (mLen >= width) {
                e = m;
            }
            // --调试时可打开--
        }
        String newName = name.substring(0, s) + ELLIPSIS; // 用...等替换掉不能显示的字符
        // --调试时可打开--
        // Logcat.d("show new name: " + newName);
        tvName.setText(newName);
    }

    /**
     * 获取父组件的固定长度
     * @param root 顶层的视图，即ListView中Item的根视图
     */
    public static int getParentFixWidth(View root, View parentView) {
        if (parentView == null || root == null) return 0;
        int totalWidth = 0;
        while (parentView != null && parentView != root) { // 得到所有父组件的Padding和margin
            totalWidth += parentView.getPaddingStart() + parentView.getPaddingEnd()
                          + getViewMargin(parentView);
            parentView = (View) parentView.getParent();
        }
        totalWidth += root.getPaddingStart() + root.getPaddingEnd() + getViewMargin(root);
        return totalWidth;
    }

    public static int getViewWidth(View view) {
        if (view == null) return 0;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredWidth() + getViewMargin(view);
    }

    public static int getViewMargin(View view) {
        if (view == null) return 0;
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams marginLayoutParams = ((MarginLayoutParams) layoutParams);
            return marginLayoutParams.getMarginStart() + marginLayoutParams.getMarginEnd();
        }
        return 0;
    }

    /**
     * 将路径按一个个的文件名分开
     */
    private static List<String> splitFilePath(String path) {
        ArrayList<String> names = new ArrayList<>();
        if (path == null) return names;
        // split第一个为分隔符时会分解出一个"", 而最后一个不会
        String[] nodeArray = path.split(File.separatorChar + "");
        for (int i = 0;i < nodeArray.length; ++i) {
            if (TextUtils.isEmpty(nodeArray[i]))
                continue;
            names.add(nodeArray[i]);
        }
        return names;
    }
}
