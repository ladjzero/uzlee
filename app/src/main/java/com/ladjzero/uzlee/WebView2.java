package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by chenzhuo on 16-1-31.
 */
public class WebView2 extends WebView {
	private final String JS_INTERFACE_NAME = "WebView2";
	boolean isEverScrolled;
	private Canvas mCanvas;

	public WebView2(Context context) {
		super(context);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public WebView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public WebView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	@TargetApi(21)
	public WebView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public WebView2(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
		super(context, attrs, defStyleAttr, privateBrowsing);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean toReturn = super.onTouchEvent(event);
		int action = event.getAction();

		if (action == MotionEvent.ACTION_MOVE) {
			if (isEverScrolled) {
				requestDisallowInterceptTouchEvent(true);
				toReturn = true;
			}
		} else {
			isEverScrolled = false;
		}

		return toReturn;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		isEverScrolled = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mCanvas = canvas;
	}

	public Bitmap toBitmap() {
		this.setDrawingCacheEnabled(true);
		this.buildDrawingCache();
		int width = this.getWidth();
		int height = (int) (this.getContentHeight() * this.getScale());
		if (height > width * 10) height = width * 10;
		Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		this.draw(canvas);
		return bitmap;
	}
}
