package com.jancar.viewbase.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.jancar.sdk.car.IVICar;
import com.jancar.sdk.utils.Logcat;

/**
 *      4  5  6          5 6 7 8
 *      |-----|          |-----|
 *      |     |          |     |
 *      |-----|          |-----|
 *      1  2  3          4 3 2 1 
 *      
 *     radarType=1      radarType=2
 */

public class BigRadarView extends BaseRadarView{
	
	public static final String TAG = "BigRadarView";

	private int mScreenType = 0;
	private int width_screen, height_screen;

	private int RADAR_WIDTH = 120;

	private int max_front_level, width_front_line, front_step_x, front_step_y;
	private int max_back_level, width_back_line, back_step_x, back_step_y;
	private int max_right_level, width_right_line, right_step_x, right_step_y;
	private int max_left_level, width_left_line, left_step_x, left_step_y;

	private Paint mPaintBlue;
	private Paint mPaintRed;
	private Paint mPaint;

	private RectF rect_front_top = new RectF(170, 130, 320, 300);
	private RectF rect_front_center = new RectF(165, 105, 380, 375);
	private RectF rect_front_bottom = new RectF(170, 180, 320, 350);
	
	private RectF rect_back_top = new RectF(485, 130, 635, 300);
	private RectF rect_back_center = new RectF(425, 105, 640, 375);
	private RectF rect_back_bottom = new RectF(485, 180, 635, 350);
	
	private RectF rect_right_top = new RectF(170, 130, 320, 300);
    private RectF rect_right_bottom = new RectF(485, 130, 635, 300);
    private RectF rect_left_top = new RectF(170, 180, 320, 350);
    private RectF rect_left_bottom = new RectF(485, 180, 635, 350);

	public BigRadarView(Context context) {
		this(context, null);
	}

	public BigRadarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initDrawableAndPaint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mBitmapCar != null) {
			if (mScreenType == 1) {
				canvas.drawBitmap(mBitmapCar, (1024 - mBitmapCar.getWidth()) / 2, (getHeight() - mBitmapCar.getHeight()) / 2, null);
			} else {
				canvas.drawBitmap(mBitmapCar, (800 - mBitmapCar.getWidth()) / 2, (getHeight() - mBitmapCar.getHeight()) / 2, null);
			}
		}
		
		drawFront(canvas);
		drawBack(canvas);
		drawLeft(canvas);
		drawRight(canvas);
	}

	private void initDrawableAndPaint() {
		
		DisplayMetrics dm1 = getResources().getDisplayMetrics();
		width_screen = dm1.widthPixels;
		height_screen = dm1.heightPixels;
		
		if(width_screen > 800)
			mScreenType = 1;

		mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintRed.setColor(0xFFFF0000);
		mPaintRed.setStyle(Paint.Style.STROKE);

		mPaintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintBlue.setColor(0xFF1385D1);
		mPaintBlue.setStyle(Paint.Style.STROKE);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);

		if(mScreenType == 1){
			RADAR_WIDTH = 140;

			rect_front_top = new RectF(235, 180, 385, 350);
			rect_front_center = new RectF(230, 145, 445, 455);
			rect_front_bottom = new RectF(235, 250, 385, 420);
			
			rect_back_top = new RectF(640, 182, 790, 352);
			rect_back_center = new RectF(580, 147, 795, 457);
			rect_back_bottom = new RectF(640, 252, 790, 422);
			
			rect_right_top = new RectF(235, 180, 385, 350);
            rect_right_bottom = new RectF(640, 180, 790, 350);
            rect_left_top = new RectF(235, 252, 385, 422);
            rect_left_bottom = new RectF(640, 252, 790, 422);

			if (mBitmapCar == null) {
				Logcat.w("mBitmapCar is null!");
				return;
			}
			mBitmapCar = Bitmap.createScaledBitmap(mBitmapCar, 583, 350, true);
		}
	}

	
	private void drawFront(Canvas canvas){
		if(mRadarAreas == null || max_front_level == 0)
			return;
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);
		mPaintBlue.setStrokeWidth(width_front_line);
		mPaintRed.setStrokeWidth(width_front_line);
		
		int FROM = 0;
		int TO = 0;
		
		RectF rectTop = new RectF(rect_front_top);
		RectF rectCenter = new RectF(rect_front_center);
		RectF rectBottom = new RectF(rect_front_bottom);
		
		
		switch(mRadarType){
		case IVICar.Radar.Type.NONE:
			break;	
			
		case RadarView.ONLY_END_RADAR: //mRadarAreas[3] - mRadarAreas[5]
			FROM = 3;
			TO = 6;
			break;		
			
		case RadarView.BOTH_FRONT_END_RADAR: // mRadarAreas[4] - mRadarAreas[7]
			FROM = 4;
			TO = 8;
			break;	
			
		case RadarView.FRONT_SIX_BACK_FOUR:
			break;	
			
		case RadarView.BOTH_FRONT_END_RADAR_BMW:
			break;
		case RadarView.HAVE_SIDE_RADAR:
		    FROM = 10;
            TO = 14;
		    break;
		}
		
		Log.d(TAG, "drawFront() >>>>> from = " + FROM + " to = " + TO);
		
		for(int i = 1; i <= max_front_level; i ++){
			
			for(int j = FROM; j < TO; j ++){
				RadarArea area = mRadarAreas[j];

				if (area.totalAreas >= i) {

					if (area.currentArea == i) {

						if(j == FROM){
							canvas.drawArc(rectBottom, area.startAngle, area.endAngle, false, mPaintRed);
						}else if(j == TO -1){
							canvas.drawArc(rectTop, area.startAngle, area.endAngle, false, mPaintRed);
						}else{
							canvas.drawArc(rectCenter, area.startAngle, area.endAngle, false, mPaintRed);
						}
						
					} else {
						if(j == FROM){
							canvas.drawArc(rectBottom, area.startAngle, area.endAngle, false, mPaintBlue);
						}else if(j == TO -1){
							canvas.drawArc(rectTop, area.startAngle, area.endAngle, false, mPaintBlue);
						}else{
							canvas.drawArc(rectCenter, area.startAngle, area.endAngle, false, mPaintBlue);
						}
					}

				}
			}

			rectTop.inset(-front_step_x, -front_step_y);
			rectBottom.inset(-front_step_x, -front_step_y);
			rectCenter.inset(-front_step_x, -front_step_y);
		}
	}

	private void drawBack(Canvas canvas){
		if(mRadarAreas == null || max_back_level ==0)
			return;
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);
		mPaintBlue.setStrokeWidth(width_back_line);
		mPaintRed.setStrokeWidth(width_back_line);
		int FROM = 0;
		int TO = 0;
		
		RectF rectTop = new RectF(rect_back_top);
		RectF rectCenter = new RectF(rect_back_center);
		RectF rectBottom = new RectF(rect_back_bottom);

		switch(mRadarType){
		case IVICar.Radar.Type.NONE:
			break;		
			
		case RadarView.ONLY_END_RADAR: // mRadarAreas[0] - mRadarAreas[2]
			FROM = 0;
			TO = 3;
			
			rectBottom = new RectF(rect_back_top);
			rectCenter = new RectF(rect_back_center);
			rectTop = new RectF(rect_back_bottom);
			break;		
			
		case RadarView.BOTH_FRONT_END_RADAR: // mRadarAreas[0] - mRadarAreas[3]
			FROM = 0;
			TO = 4;
			break;		
			
		case RadarView.FRONT_SIX_BACK_FOUR:
			break;	
			
		case RadarView.BOTH_FRONT_END_RADAR_BMW:
			break;
		case RadarView.HAVE_SIDE_RADAR:
		    FROM = 2;
            TO = 6;
		    break;
		}

		for(int i = 1; i <= max_back_level; i ++){
			
			for(int j = FROM; j < TO; j ++){
				RadarArea area = mRadarAreas[j];

				if (area.totalAreas >= i) {

					if (area.currentArea == i) {
						if(j == FROM){
							canvas.drawArc(rectTop, area.startAngle, area.endAngle, false, mPaintRed);
						}else if(j == TO -1){
							canvas.drawArc(rectBottom, area.startAngle, area.endAngle, false, mPaintRed);
						}else{
							canvas.drawArc(rectCenter, area.startAngle, area.endAngle, false, mPaintRed);
						}
						
					} else {
						if(j == FROM){
							canvas.drawArc(rectTop, area.startAngle, area.endAngle, false, mPaintBlue);
						}else if(j == TO -1){
							canvas.drawArc(rectBottom, area.startAngle, area.endAngle, false, mPaintBlue);
						}else{
							canvas.drawArc(rectCenter, area.startAngle, area.endAngle, false, mPaintBlue);
						}
					}

				}
				
				
			}

			rectTop.inset(-back_step_x, -back_step_y);
			rectBottom.inset(-back_step_x, -back_step_y);
			rectCenter.inset(-back_step_x, -back_step_y);
		}
	}

	private void drawRight(Canvas canvas){
	    if(mRadarAreas == null || mRadarType!=RadarView.HAVE_SIDE_RADAR)
            return;
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2);
        mPaintBlue.setStrokeWidth(width_right_line);
        mPaintRed.setStrokeWidth(width_right_line);
        
        int[] type = {0,1,14,15};
                
        RectF rectRightTop = new RectF(rect_right_top);     
        RectF rectRightBottom = new RectF(rect_right_bottom);
                                     
        for(int i = 1; i <= max_right_level; i ++){
            
            for(int j : type){
               RadarArea area = mRadarAreas[j];                           
               if(area.totalAreas >= i){                   
                   switch(j){
                   case 0:
                       if(area.currentArea == i)
                           canvas.drawLine(515, 180-right_step_y*(i-1), 675, 180-right_step_y*(i-1), mPaintRed);
                       else
                           canvas.drawLine(515, 180-right_step_y*(i-1), 675, 180-right_step_y*(i-1), mPaintBlue);
                       break;
                   case 1:
                       if(area.currentArea == i){
                           canvas.drawArc(rectRightBottom, -90, 30, false, mPaintRed);                           
                           canvas.drawLine(680, 180-right_step_y*(i-1), 716, 180-right_step_y*(i-1), mPaintRed);
                       }else {
                           canvas.drawArc(rectRightBottom, -90, 30, false, mPaintBlue);                           
                           canvas.drawLine(680, 180-right_step_y*(i-1), 716, 180-right_step_y*(i-1), mPaintBlue);
                       }
                       break;
                   case 14:
                       if(area.currentArea == i){
                           canvas.drawArc(rectRightTop, 241, 30, false, mPaintRed);                          
                           canvas.drawLine(310, 180-right_step_y*(i-1), 345, 180-right_step_y*(i-1), mPaintRed);                          
                       }else {
                           canvas.drawArc(rectRightTop, 241, 30, false, mPaintBlue);                          
                           canvas.drawLine(310, 180-right_step_y*(i-1), 345, 180-right_step_y*(i-1), mPaintBlue);
                       }
                       break;
                   case 15:
                       if(area.currentArea == i){
                           canvas.drawLine(350, 180-right_step_y*(i-1), 510, 180-right_step_y*(i-1), mPaintRed);
                       }else {
                           canvas.drawLine(350, 180-right_step_y*(i-1), 510, 180-right_step_y*(i-1), mPaintBlue);
                       }
                       break;
                   }                      
               }               
            }             
            rectRightTop.inset(-right_step_x, -right_step_y);
            rectRightBottom.inset(-right_step_x, -right_step_y);                
        }
	}
	
	private void drawLeft(Canvas canvas){
        if(mRadarAreas == null || mRadarType!=RadarView.HAVE_SIDE_RADAR)
            return;
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2);
        mPaintBlue.setStrokeWidth(width_left_line);
        mPaintRed.setStrokeWidth(width_left_line);
        
        int[] type = {6,7,8,9};
                
        RectF rectLeftTop = new RectF(rect_left_top);     
        RectF rectLeftBottom = new RectF(rect_left_bottom);
                                     
        for(int i = 1; i <= max_left_level; i ++){
            
            for(int j : type){
               RadarArea area = mRadarAreas[j];                           
               if(area.totalAreas >= i){                   
                   switch(j){
                   case 7:
                       if(area.currentArea == i)
                           canvas.drawLine(515, 422+left_step_y*(i-1), 675, 422+left_step_y*(i-1), mPaintRed);
                       else
                           canvas.drawLine(515, 422+left_step_y*(i-1), 675, 422+left_step_y*(i-1), mPaintBlue);
                       break;
                   case 6:
                       if(area.currentArea == i){
                           canvas.drawArc(rectLeftBottom, 60, 30, false, mPaintRed);                           
                           canvas.drawLine(680, 422+left_step_y*(i-1), 716, 422+left_step_y*(i-1), mPaintRed);
                       }else {
                           canvas.drawArc(rectLeftBottom, 60, 30, false, mPaintBlue);                           
                           canvas.drawLine(680, 422+left_step_y*(i-1), 716, 422+left_step_y*(i-1), mPaintBlue);
                       }
                       break;
                   case 9:
                       if(area.currentArea == i){
                           canvas.drawArc(rectLeftTop, 90, 30, false, mPaintRed);                          
                           canvas.drawLine(309, 422+left_step_y*(i-1), 345, 422+left_step_y*(i-1), mPaintRed);                          
                       }else {
                           canvas.drawArc(rectLeftTop, 90, 30, false, mPaintBlue);                          
                           canvas.drawLine(309, 422+left_step_y*(i-1), 345, 422+left_step_y*(i-1), mPaintBlue);
                       }
                       break;
                   case 8:
                       if(area.currentArea == i){
                           canvas.drawLine(350, 422+left_step_y*(i-1), 510, 422+left_step_y*(i-1), mPaintRed);
                       }else {
                           canvas.drawLine(350, 422+left_step_y*(i-1), 510, 422+left_step_y*(i-1), mPaintBlue);
                       }
                       break;
                   }                      
               }               
            }             
            rectLeftTop.inset(-left_step_x, -left_step_y);
            rectLeftBottom.inset(-left_step_x, -left_step_y);                
        }
    }

	@Override
	protected int init(IVICar.Radar radar){
		mRadarType = radar.mType;
		if(mRadarType == 0){
            mRadarAreas = null;
            mInitialied = false;
            return -2;
        }

		Logcat.d("RadarType = " + mRadarType);
		int rc = initRadarAreas(mRadarType);
		if(rc < 0)
		    return rc;
		
		int dataSize = radar.mData.length;
		for(int i = 0; i < dataSize; i ++){
			int level = (radar.mData[i] >> 4) & 0x0f;
			if (level > 8){
				level = 8;
			}

			mRadarAreas[i].totalAreas = level;
			mRadarAreas[i].currentArea = radar.mData[i] & 0x0f;
		}
		
		max_front_level = getMaxFrontLevel(mRadarType);
		max_back_level = getMaxBackLevel(mRadarType);
		max_right_level = getMaxRightLevel(mRadarType);
		max_left_level = getMaxLeftLevel(mRadarType);
		
		if(max_front_level == 0 && max_back_level == 0 && max_right_level == 0 && max_left_level == 0){
			return -1;
		}
		if(max_front_level > 0 && max_back_level == 0){
			max_back_level = max_front_level;
			copyFrontToBack(mRadarType);
		}else if(max_back_level > 0 && max_front_level == 0){
			max_front_level = max_back_level;
			copyBackToFront(mRadarType);
		}
		
		if(max_right_level < max_front_level){
		    max_right_level = max_front_level;
		}
		if(max_left_level < max_front_level){
            max_left_level = max_front_level;
		}
		
		if(max_left_level > 0 && max_right_level == 0){
		    max_right_level  = max_left_level;
            copyLeftToRight(mRadarType);
        }else if(max_right_level > 0 && max_left_level == 0){
            max_left_level = max_right_level;
            copyRightToLeft(mRadarType);
        }
		
		if(max_front_level > 0){
		    front_step_x = RADAR_WIDTH / max_front_level;
		    width_front_line = front_step_x - 4;
		    front_step_y = front_step_x - 2;
		    Log.d(TAG, "maxFrontLevel = " + max_front_level + " frontLineWidth = " + width_front_line + " frontSetpX = " + front_step_x + " frontStepY = " + front_step_y );
		}	
		if(max_back_level > 0){
		    back_step_x = RADAR_WIDTH / max_back_level;
		    width_back_line = back_step_x - 4;
		    back_step_y = back_step_x - 2;
		    Log.d(TAG, "maxBackLevel = " + max_back_level + " backLineWidth = " + width_back_line + " backSetpX = " + back_step_x + " backStepY = " + back_step_y );
		}
		if(max_right_level > 0){
		    right_step_x = RADAR_WIDTH / max_right_level;
		    width_right_line = right_step_x - 4;
		    right_step_y = right_step_x - 2;
		}
		
		if(max_left_level > 0){
		    left_step_x = RADAR_WIDTH / max_left_level;
            width_left_line = left_step_x - 4;
            left_step_y = left_step_x - 2;
		}

		mInitialied = true;
		return rc;
	}

	@Override
	public boolean update(IVICar.Radar radar){
		int rc = 0;
		if (!mInitialied || radar.mType != mRadarType) {
			rc = init(radar);
			if (rc < 0) {
				return false;
			}
		}
		
		int dataSize = radar.mData.length;
		for (int i = 0; i < dataSize; i++) {
			mRadarAreas[i].currentArea = radar.mData[i] & 0x0f;
		}

		invalidate();
		return true;
	}
	
	
	private int initRadarAreas(int _radarType){
		
		switch(_radarType){
		case IVICar.Radar.Type.NONE:
			mRadarAreas = null;
			mInitialied = false;
			return -1;
			
		case RadarView.ONLY_END_RADAR:
			mRadarAreas = new RadarArea[6];
			mRadarAreas[0] = new RadarArea(26, 30, 0);
			mRadarAreas[1] = new RadarArea(-25, 50,0);
			mRadarAreas[2] = new RadarArea(-56, 30, 0);
			mRadarAreas[3] = new RadarArea(124, 30, 0);
			mRadarAreas[4] = new RadarArea(155, 50, 0);
			mRadarAreas[5] = new RadarArea(206, 30, 0);
			return _radarType;
			
		case RadarView.BOTH_FRONT_END_RADAR:
			mRadarAreas = new RadarArea[8];
			mRadarAreas[0] = new RadarArea(-57, 30, 0);
			mRadarAreas[1] = new RadarArea(-26, 25, 0);
			mRadarAreas[2] = new RadarArea(1, 25, 0);
			mRadarAreas[3] = new RadarArea(27, 30, 0);
			mRadarAreas[4] = new RadarArea(123, 30, 0);
			mRadarAreas[5] = new RadarArea(154, 25, 0);
			mRadarAreas[6] = new RadarArea(181, 25, 0);
			mRadarAreas[7] = new RadarArea(207, 30, 0);
			return _radarType;
			
		case RadarView.FRONT_SIX_BACK_FOUR: 
			return -4;
			
		case RadarView.BOTH_FRONT_END_RADAR_BMW:
			return -3;

		case RadarView.HAVE_SIDE_RADAR:
		    mRadarAreas = new RadarArea[16];
		    mRadarAreas[0] = new RadarArea(0, 0, 0);
            mRadarAreas[1] = new RadarArea(-90, 30, 0);
            mRadarAreas[2] = new RadarArea(-57, 30, 0);
            mRadarAreas[3] = new RadarArea(-26, 25, 0);
            mRadarAreas[4] = new RadarArea(1, 25, 0);
            mRadarAreas[5] = new RadarArea(27, 30, 0);
            mRadarAreas[6] = new RadarArea(60, 30, 0);
            mRadarAreas[7] = new RadarArea(0, 0, 0);
            mRadarAreas[8] = new RadarArea(0, 0, 0);
            mRadarAreas[9] = new RadarArea(90, 30, 0);            
            mRadarAreas[10] = new RadarArea(123, 30, 0);            
            mRadarAreas[11] = new RadarArea(154, 25, 0);
            mRadarAreas[12] = new RadarArea(181, 25, 0);
            mRadarAreas[13] = new RadarArea(207, 30, 0);
            mRadarAreas[14] = new RadarArea(241, 30, 0);
            mRadarAreas[15] = new RadarArea(0, 0, 0);
		    return _radarType;
		default:
			return -2;
		}
	}

}