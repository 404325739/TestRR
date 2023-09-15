package com.jancar.viewbase.utils.v14;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import com.jancar.viewbase.utils.ViewHelperFactory;


public class ViewHelper14 extends ViewHelperFactory.ViewHelperDefault {

	public ViewHelper14( View view ) {
		super( view );
	}
	
	@TargetApi( Build.VERSION_CODES.ICE_CREAM_SANDWICH )
	@Override
	public void setScrollX( int value ) {
		view.setScrollX( value );
	}
	
	@TargetApi( Build.VERSION_CODES.HONEYCOMB )
	@Override
	public boolean isHardwareAccelerated() {
		return view.isHardwareAccelerated();
	}
	
}