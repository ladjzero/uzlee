package com.ladjzero.uzlee;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;

/**
 * Created by chenzhuo on 16-1-31.
 */
public class PullToRefreshWebView2 extends PullToRefreshWebView {
	public PullToRefreshWebView2(Context context) {
		super(context);
	}

	public PullToRefreshWebView2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshWebView2(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshWebView2(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	protected WebView createRefreshableView(Context context, AttributeSet attrs) {
		return new WebView2(context, attrs);
	}
}
