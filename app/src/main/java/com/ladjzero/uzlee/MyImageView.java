package com.ladjzero.uzlee;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by chenzhuo on 15-2-15.
 */
public class MyImageView extends ImageViewTouch{
	float x = -1f;

	private SwipeActivity.OnSwipeToggle onSwipeToggle;

	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}
	public MyImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setSwipeToggle(SwipeActivity.OnSwipeToggle onSwipeToggle) {
		this.onSwipeToggle = onSwipeToggle;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float xDelta = -1f;

		if (x < 0) {
			x = e.getX();
		} else {
			xDelta = e.getX() - x;
			x = e.getX();
		}

		RectF bitmapRect = getBitmapRect();
		boolean canScroll = bitmapRect.left < -0.0000000001f;
		boolean canSwipe = !(xDelta < 0 || canScroll);

		if (onSwipeToggle != null) onSwipeToggle.setEnableSwipe(canSwipe);
		return super.onTouchEvent(e);
//		return !canScroll(1);
	}
}
