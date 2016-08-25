package com.andrew.timetracker.commons;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by andrew on 18.08.2016.
 */
public abstract class OnDoubleTouchListener implements View.OnTouchListener {

	protected abstract boolean onDoubleTap();

	private GestureDetector gestureDetector;
	private boolean mOnTouchReturn;

	public OnDoubleTouchListener(Context context, boolean onTouchReturn) {
		mOnTouchReturn = onTouchReturn;
		gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				Log.d("tt: commons", "onDoubleTap");
				return OnDoubleTouchListener.this.onDoubleTap() || super.onDoubleTap(e);
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("tt: commons", "onTouch");
		gestureDetector.onTouchEvent(event);
		return mOnTouchReturn;
	}
}
