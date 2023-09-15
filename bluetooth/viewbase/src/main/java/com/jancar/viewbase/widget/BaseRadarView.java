package com.jancar.viewbase.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;

import com.jancar.sdk.car.IVICar;
import com.jancar.viewbase.R;


/**
 *
 *               [ 车头 ]
 *
 *      4  5  6          5 6 7 8
 *      |-----|          |-----|
 *      |     |          |     |
 *      |-----|          |-----|
 *      1  2  3          4 3 2 1
 *
 *     radarType=1      radarType=2
 *
 *               [ 车尾 ]
 *
 *   车头和车尾的弧线各由3个矩形决定，以车头向左横放，上面为 top，中间为 center，下面为 bottom
 *   onDraw分两个步骤 drawFront 和 drawBack
 *
 *   drawFront（）：radarType = 1 时， 4 对应bottom，5 对应center，6 对应top
 *   			   radarType = 2  时     5 对应bottom，6和7 对应 center，8 对应top
 *
 *   drawBack（）：  radarType = 1 时， 1 对应bottom，2 对应center，3 对应top
 *   			   radarType = 2  时     4 对应bottom，3和2 对应 center，1 对应top
 *
 *   画的时候是按照 RadarArea[] mRadarAreas 顺序画的 1，2，3...6，7，8（对应数组下标 0 -7) 所以radarType = 1 和 radarType = 2时，
 *   top 和 bottom 对应顺序是颠倒的。(详见代码)
 */
public abstract class BaseRadarView extends View{
	protected RadarArea[] mRadarAreas;
    protected int mRadarType = IVICar.Radar.Type.NONE;
    protected boolean mInitialied = false;
	protected Bitmap mBitmapCar;

	public static class RadarArea {
		public RadarArea(float sa, float ea, int ta) {
			startAngle = sa;
			endAngle = ea;
			totalAreas = ta;
			currentArea = -1;
		}

		public float startAngle;
		public float endAngle;
		public int totalAreas;
		public int currentArea;
	}

	public BaseRadarView(Context context) {
		super(context);
	}
	public BaseRadarView(Context context, AttributeSet attrs){
		super(context, attrs);

		if (attrs != null && context != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RadarView);
			int resId = ta.getResourceId(R.styleable.RadarView_car_res, -1);

			if (resId != -1) {
				mBitmapCar = BitmapFactory.decodeResource(getResources(), resId);
			}
		}
	}

	public boolean update(IVICar.Radar radar) {
        if (radar.mType != mRadarType) {
            setVisibility(radar.mType == IVICar.Radar.Type.NONE ?
                View.INVISIBLE : View.VISIBLE);
			return true;
        }
		return false;
    }

	abstract int init(IVICar.Radar radar);

	protected int getMaxFrontLevel(int radarType){
		int count = 0;
		switch(radarType){
			case IVICar.Radar.Type.NONE:
				count = 0;
				break;

			case RadarView.ONLY_END_RADAR:
				for(int i = 3; i < 6; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				for(int i = 4; i < 8; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;

			case IVICar.Radar.Type.F6R4:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;

			case RadarView.HAVE_SIDE_RADAR:
				for(int i = 10; i < 14; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;
		}

		return count;
	}

    protected int getMaxBackLevel(int _radarType){
		int count = 0;
		switch(_radarType){

			case IVICar.Radar.Type.NONE:
				count = 0;
				break;

			case RadarView.ONLY_END_RADAR:
				for(int i = 0; i < 3; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				for(int i = 0; i < 4; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;

			case RadarView.HAVE_SIDE_RADAR:
				for(int i = 2; i < 6; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;

		}

		return count;
	}

    protected int getMaxRightLevel(int _radarType){
		int count = 0;
		int count1 = 0;
		switch(_radarType){

			case IVICar.Radar.Type.NONE:
				count = 0;
				break;

			case RadarView.ONLY_END_RADAR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;
			case RadarView.HAVE_SIDE_RADAR:
				for(int i = 0; i < 2; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				for(int j = 14; j < 16; j ++){
					if(mRadarAreas[j].totalAreas > count1)
						count1 = mRadarAreas[j].totalAreas;
				}
				if(count1 > count){
					count = count1;
				}
				break;
		}

		return count;
	}

    protected int getMaxLeftLevel(int _radarType){
		int count = 0;
		switch(_radarType){

			case IVICar.Radar.Type.NONE:
				count = 0;
				break;

			case RadarView.ONLY_END_RADAR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;
			case RadarView.HAVE_SIDE_RADAR:
				for(int i = 6; i < 10; i ++){
					if(mRadarAreas[i].totalAreas > count)
						count = mRadarAreas[i].totalAreas;
				}
				break;
		}

		return count;
	}

    protected void copyBackToFront(int _radarType){
		switch(_radarType){
			case IVICar.Radar.Type.NONE:
				break;

			case RadarView.ONLY_END_RADAR:
				for(int i = 3; i < 6; i ++){
					mRadarAreas[i].totalAreas = mRadarAreas[i-3].totalAreas;
				}
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				for(int i = 4; i < 8; i ++){
					mRadarAreas[i].totalAreas = mRadarAreas[i-4].totalAreas;
				}
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;
			case RadarView.HAVE_SIDE_RADAR:
				mRadarAreas[13].totalAreas = mRadarAreas[2].totalAreas;
				mRadarAreas[12].totalAreas = mRadarAreas[3].totalAreas;
				mRadarAreas[11].totalAreas = mRadarAreas[4].totalAreas;
				mRadarAreas[10].totalAreas = mRadarAreas[5].totalAreas;
				break;
		}
	}

    protected void copyFrontToBack(int _radarType){
		switch(_radarType){
			case IVICar.Radar.Type.NONE:
				break;

			case RadarView.ONLY_END_RADAR:
				for(int i = 0; i < 3; i ++){
					mRadarAreas[i].totalAreas = mRadarAreas[i + 3].totalAreas;
				}
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				for(int i = 0; i < 4; i ++){
					mRadarAreas[i].totalAreas = mRadarAreas[i + 4].totalAreas;
				}
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;

			case RadarView.HAVE_SIDE_RADAR:
				mRadarAreas[2].totalAreas = mRadarAreas[13].totalAreas;
				mRadarAreas[3].totalAreas = mRadarAreas[12].totalAreas;
				mRadarAreas[4].totalAreas = mRadarAreas[11].totalAreas;
				mRadarAreas[5].totalAreas = mRadarAreas[10].totalAreas;
				break;
		}
	}

    protected void copyLeftToRight(int _radarType){
		switch(_radarType){
			case IVICar.Radar.Type.NONE:
				break;

			case RadarView.ONLY_END_RADAR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;
			case RadarView.HAVE_SIDE_RADAR:
				mRadarAreas[0].totalAreas = mRadarAreas[7].totalAreas;
				mRadarAreas[1].totalAreas = mRadarAreas[6].totalAreas;
				mRadarAreas[14].totalAreas = mRadarAreas[9].totalAreas;
				mRadarAreas[15].totalAreas = mRadarAreas[8].totalAreas;
				break;
		}
	}

    protected void copyRightToLeft(int _radarType){
		switch(_radarType){
			case IVICar.Radar.Type.NONE:
				break;

			case RadarView.ONLY_END_RADAR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR:
				break;

			case RadarView.FRONT_SIX_BACK_FOUR:
				break;

			case RadarView.BOTH_FRONT_END_RADAR_BMW:
				break;
			case RadarView.HAVE_SIDE_RADAR:
				mRadarAreas[7].totalAreas = mRadarAreas[0].totalAreas;
				mRadarAreas[6].totalAreas = mRadarAreas[1].totalAreas;
				mRadarAreas[9].totalAreas = mRadarAreas[14].totalAreas;
				mRadarAreas[8].totalAreas = mRadarAreas[15].totalAreas;
				break;
		}
	}
}
