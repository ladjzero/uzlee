package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by chenzhuo on 16-1-31.
 */
public class WebView2 extends WebView implements Runnable {
	private final String JS_INTERFACE_NAME = "WebView2";
	OnScrollListener onScrollListener;
	boolean isEverScrolled;
	long mLastScroll = 0;
	long mScrollDebounce = 300;

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

	public void setOnScrollListener(OnScrollListener l) {
		onScrollListener = l;
	}

	@Override
	public void run() {
		if (System.currentTimeMillis() - mLastScroll > mScrollDebounce) {
			onScrollStateChanged(false);
			mLastScroll = 0;
		} else {
			this.postDelayed(this, mScrollDebounce);
		}
	}

	interface OnScrollListener {
		int SCROLL_STATE_TOUCH_SCROLL = 1;
		int SCROLL_STATE_IDLE = 2;

		void onScrollStateChanged(WebView2 webView, int state);
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

		if (onScrollListener != null) {
			if (mLastScroll == 0) {
				onScrollStateChanged(true);
				this.postDelayed(this, mScrollDebounce);
			}

			mLastScroll = System.currentTimeMillis();
		}
	}


	private void onScrollStateChanged(boolean scroll) {
		onScrollListener.onScrollStateChanged(this,
				scroll ? OnScrollListener.SCROLL_STATE_TOUCH_SCROLL : OnScrollListener.SCROLL_STATE_IDLE);
	}
}
