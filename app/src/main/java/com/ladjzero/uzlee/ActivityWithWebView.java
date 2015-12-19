package com.ladjzero.uzlee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.logger.Logger;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

/**
 * Created by chenzhuo on 15-10-4.
 */
public abstract class ActivityWithWebView extends ActivityBase implements OnTouchListener {

	private CurrentState currentState = new CurrentState();
	private SlidrInterface slidrInterface;
	private boolean mWebviewTouchFirstMove;
	private float downXValue, downYValue;
	private boolean initialized;

	@JavascriptInterface
	public void onScroll(String state) {
		if (state.equals("start")) {
			onStateChange(State.SCROLL_START);
		} else if (state.equals("end")) {
			onStateChange(State.SCROLL_END);
		}
	}

	@JavascriptInterface
	public void onImageClick(String src) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(src));
		startActivity(intent);
	}

	@JavascriptInterface
	public void onWebViewReady() {
		Logger.i("WebView is ready.");
	}

	public void onStateChange(State state) {
		String TAG = "onStateChange";

		Logger.t(TAG).d("WebView state: " + state);

		switch (state) {
			case TOUCH_START:
				currentState.onTouch = true;
				if (!currentState.onScroll) currentState.enableSlidr = true;
				break;
			case TOUCH_END:
				currentState.onTouch = false;
				break;
			case SCROLL_START:
				currentState.onScroll = true;
				currentState.enableSlidr = false;
				break;
			case SCROLL_END:
				currentState.onScroll = false;
				if (!currentState.onTouch) currentState.enableSlidr = true;
				break;
			case SLIDE_START:
				currentState.onSlide = true;
				break;
			case SLIDE_END:
				currentState.onSlide = false;
				break;
		}

		Logger.t(TAG).d("new current state: " + currentState);

		if (currentState.enableSlidr != currentState.enableSlidrBefore) {
			if (currentState.enableSlidr)
				slidrInterface.unlock();
			else
				slidrInterface.lock();

			currentState.enableSlidrBefore = !currentState.enableSlidrBefore;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		slidrInterface = Slidr.attach(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupWebView();
	}

	public void setupWebView() {
		if (!initialized) {
			WebView webView = getWebView();

			webView.setOnTouchListener(this);
			webView.addJavascriptInterface(this, "UZLEE");
			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage cm) {
					Logger.t("WebView").d(cm.message());
					return true;
				}
			});

			WebSettings settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);
			settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

			webView.loadUrl(getHTMLFilePath());
			initialized = true;
		}
	}

	public abstract WebView getWebView();

	public abstract String getHTMLFilePath();

	public SlidrInterface getSlidrInterface() {
		return slidrInterface;
	}

	// Lock slidr while view is on touch but not scrolling.
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				onStateChange(State.TOUCH_START);
			}
			case MotionEvent.ACTION_MOVE: {
				Logger.d("touch move");

				if (mWebviewTouchFirstMove) {
					float x = event.getX(), y = event.getY();

					if (Math.abs(x - downXValue) * 1.3 < Math.abs(y - downYValue)) {
						onStateChange(State.SCROLL_START);
					}

					mWebviewTouchFirstMove = false;
				} else {
					downXValue = event.getX();
					downYValue = event.getY();
					mWebviewTouchFirstMove = true;
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				mWebviewTouchFirstMove = true;
				onStateChange(State.TOUCH_END);
				break;
			}

		}

		return false;
	}

	private enum State {
		TOUCH_START,
		TOUCH_END,
		SCROLL_START,
		SCROLL_END,
		SLIDE_START,
		SLIDE_END
	}

	private class CurrentState {
		boolean onTouch;
		boolean onScroll;
		boolean onSlide;
		boolean enableSlidrBefore = true;
		boolean enableSlidr = true;

		@Override
		public String toString() {
			return "onTouch: " + onTouch + ", onScroll: " + onScroll + ", onSlide: " + onSlide + ", enableSlidr: " + enableSlidr + ", before" + enableSlidrBefore;
		}
	}
}
