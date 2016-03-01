package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import java.util.ArrayList;

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

	public static class ImageClickEvent {
		private int index;
		private ArrayList<String> srcs;

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public ArrayList<String> getSrcs() {
			return srcs;
		}

		public void setSrcs(ArrayList<String> srcs) {
			this.srcs = srcs;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mCanvas = canvas;
	}

	public Bitmap toBitmap() {
		this.setDrawingCacheEnabled(true);
		this.buildDrawingCache();

		return toBitmap(1);
	}

	public Bitmap toBitmap(float radio) {
		int width = (int) (this.getWidth() * radio);
		int height = (int) (this.getContentHeight() * this.getScale() * radio);

		if (width < 100) {
			return null;
		}

		Bitmap bitmap;

		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.scale(radio, radio);
			this.draw(canvas);
		} catch (OutOfMemoryError error) {
			bitmap = toBitmap(radio * 0.9f);
		}

		return bitmap;
	}
}
