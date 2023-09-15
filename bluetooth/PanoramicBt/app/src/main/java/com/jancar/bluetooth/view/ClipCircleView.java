package com.jancar.bluetooth.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

public class ClipCircleView extends View {

	private Drawable empty;
	private Drawable full;
	Path path;
	PaintFlagsDrawFilter filter;

	public ClipCircleView(Context paramContext, AttributeSet attrs) {
		super(paramContext, attrs);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);// 软件加速

	}

	public void init(Context mContext) {
		try {
			empty = getDrawable(mContext, "voice_empty.png");
			full = getDrawable(mContext, "voice_full.png");
			this.empty.setBounds(new Rect(-this.empty.getIntrinsicWidth() / 2,
					-this.empty.getIntrinsicHeight() / 2, this.empty
							.getIntrinsicWidth() / 2, this.empty
							.getIntrinsicHeight() / 2));
			this.full.setBounds(new Rect(-this.full.getIntrinsicWidth() / 2,
					-this.full.getIntrinsicHeight() / 2, this.full
							.getIntrinsicWidth() / 2, this.full
							.getIntrinsicHeight() / 2));
			filter = new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
					Paint.FILTER_BITMAP_FLAG);
			path = new Path();
			initPath(0);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public ClipCircleView initPath(int paramInt) {
		this.path.reset();

		this.path.addCircle(0.0F, 0.0F, this.empty.getIntrinsicWidth()
				* paramInt / 12, Path.Direction.CCW);
		return this;
	}

	public void onDraw(Canvas paramCanvas) {
		paramCanvas.save();
		paramCanvas.setDrawFilter(filter);
		paramCanvas.translate(getWidth() / 2, getHeight() / 2);
		if (empty != null)
			empty.draw(paramCanvas);
		if (path != null)
			paramCanvas.clipPath(path);
		if (full != null)
			full.draw(paramCanvas);
		paramCanvas.restore();
	}

	public void finalize() throws Throwable {
		this.empty = null;
		this.full = null;
		super.finalize();
	}

	protected void onMeasure(int paramInt1, int paramInt2) {
		super.onMeasure(paramInt1, paramInt2);
		int i = MeasureSpec.getSize(paramInt1);
		int j = MeasureSpec.getSize(paramInt2);
		Drawable localDrawable = getBackground();
		if (localDrawable != null) {
			i = localDrawable.getMinimumWidth();
			j = localDrawable.getMinimumHeight();
		}
		setMeasuredDimension(resolveSize(i, paramInt1),
				resolveSize(j, paramInt2));
	}


	public static Bitmap getDrawable(Resources res, TypedValue value,
									 InputStream stream, Rect rect, BitmapFactory.Options options) {
		if (options == null)
			options = new BitmapFactory.Options();
		if ((options.inDensity == 0) && (value != null)) {
			int i = value.density;
			if (i == 0)
				options.inDensity = 160;
			else if (i != 65535)
				options.inDensity = i;
		}
		if ((options.inTargetDensity == 0) && (res != null))
			options.inTargetDensity = res.getDisplayMetrics().densityDpi;
		return BitmapFactory.decodeStream(stream, rect, options);
	}
	private static Drawable getDrawable(Resources res, Bitmap bitmap,
										byte[] paramArrayOfByte, Rect rect, String path) {
		if (paramArrayOfByte != null)
			return new NinePatchDrawable(res, bitmap, paramArrayOfByte, rect,
					path);
		return new BitmapDrawable(res, bitmap);
	}
	public static Drawable getDrawable(Resources res, TypedValue value,
									   InputStream stream, String path, BitmapFactory.Options options) {
		if (stream == null)
			return null;
		Rect rect = new Rect();
		if (options == null)
			options = new BitmapFactory.Options();
		Bitmap bitmap = getDrawable(res, value, stream, rect, options);
		if (bitmap != null) {
			byte[] arrayOfByte = bitmap.getNinePatchChunk();
			if ((arrayOfByte == null)
					|| (!NinePatch.isNinePatchChunk(arrayOfByte))) {
				arrayOfByte = null;
				rect = null;
			}
			return getDrawable(res, bitmap, arrayOfByte, rect, path);
		}
		return null;
	}
	public static Drawable getDrawable(Context context, String path) {
		try {
			Resources res = context.getResources();
			InputStream stream = context.getAssets().open(path);
			TypedValue value = new TypedValue();
			value.density = 240;
			return getDrawable(res, value, stream, path, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
