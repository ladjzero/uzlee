package com.ladjzero.uzlee.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ladjzero.uzlee.R;

/**
 * Created by chenzhuo on 16/5/22.
 */
public class TagView extends TextView {
	private int mBorderColor;
	private int mInactiveBorderColor;
	private int mColor;
	private int mInactiveColor;
	private boolean mActive;
	private float mBorderWidth = 1;

	public TagView(Context context) {
		super(context);

		mBorderColor = Color.TRANSPARENT;
		mInactiveBorderColor = Color.TRANSPARENT;
		mColor = Color.BLACK;
		mInactiveBorderColor = Color.GRAY;
		mActive = false;
	}

	public TagView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagView, 0, 0);

		mBorderColor = a.getColor(R.styleable.TagView_borderColor, Color.TRANSPARENT);
		mInactiveBorderColor = a.getColor(R.styleable.TagView_inactiveBorderColor, Color.TRANSPARENT);
		mColor = a.getColor(R.styleable.TagView_textColor, Color.BLACK);
		mInactiveColor = a.getColor(R.styleable.TagView_inactiveTextColor, Color.GRAY);
		mActive = a.getBoolean(R.styleable.TagView_active, false);
		mBorderWidth = a.getDimension(R.styleable.TagView_borderWidth, 1);
	}

	public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TagView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		mActive = active;
		this.invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint p = new Paint();
		p.setStrokeWidth(mBorderWidth);
		p.setColor(mActive ? mBorderColor : mInactiveBorderColor);

		int width = canvas.getWidth(),
				height = canvas.getHeight();

		float halfStroke = mBorderWidth / 2;

		canvas.drawLine(0, halfStroke, width, halfStroke, p);
		canvas.drawLine(halfStroke, 0, halfStroke, height, p);
		canvas.drawLine(0, height - mBorderWidth / 2 , width, height - mBorderWidth / 2, p);
		canvas.drawLine(width - mBorderWidth / 2, 0, width - mBorderWidth / 2, height, p);

		setTextColor(mActive ? mColor : mInactiveColor);
		setTypeface(null, mActive ? Typeface.BOLD : Typeface.NORMAL);
	}
}
