package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.utils.Timeline;
import com.ladjzero.uzlee.stream.TeePipe;
import com.ladjzero.uzlee.utils.UilUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ladjzero.uzlee.utils.Utils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

/**
 * Created by chenzhuo on 15-10-4.
 */
public abstract class ActivityWithWebView extends ActivityHardSlide implements ActivityBase.OnToolbarClickListener {

	private final static String TAG = "ActivityWithWebView";
	private boolean initialized;
	private Timeline mTimeline = new Timeline();

	@JavascriptInterface
	public void onProfileClick(int uid, String name) {
		User me = getCore().getLocalApi().getUser();

		if (me == null || me.getId() == 0) {
			showToast(getResources().getString(R.string.error_login_required));
		} else {
//			showToast("user id is " + uid);

			Intent intent = new Intent(this, ActivityUser.class);
			intent.putExtra("uid", uid);
			intent.putExtra("name", name);
			startActivity(intent);
		}
	}

	@JavascriptInterface
	public void onImageClick(String src) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(UilUtils.getInstance().getFile(src)), "image/*");
		startActivity(intent);
	}

	public void onWebViewReady() {
		Logger.i("WebView is ready.");
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupWebView();
	}

	public void setupWebView() {
		if (!initialized) {
			final WebView2 webView = getWebView();

			webView.addJavascriptInterface(this, "UZLEE");
			webView.setWebViewClient(new WebView2.ImageCacheClient() {
				@Override
				protected boolean shouldInterceptRequest(String uri) {
					return super.shouldInterceptRequest(uri);
				}

				@Override
				public boolean shouldDownloadImage() {
					return getApp().shouldDownloadImage();
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());
					super.onPageFinished(view, url);
					webView.clearCache(true);
					onWebViewReady();
				}
			});
			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage cm) {
					Logger.t("WebView").d(cm.message());
					return true;
				}
			});

			WebSettings settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);
			settings.setAppCacheEnabled(false);
			settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			// All images loadings will handled by WebViewClient.
			settings.setBlockNetworkLoads(true);
			webView.setBackgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground));

			webView.loadUrl(getHTMLFilePath());
			initialized = true;
		}
	}

	public abstract WebView2 getWebView();

	public abstract String getHTMLFilePath();

	public boolean onLinkClick(String url) {
		return false;
	}

	@Override
	public void toolbarClick() {
		getWebView().scrollTo(0, 0);
	}

	protected boolean shouldInterceptRequest(String uri) {
		return uri.startsWith("http") && (uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".png") || uri.endsWith(".gif"));
	}

	protected String getMimeType(String uri) {
		if (uri.endsWith(".jpg") && uri.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (uri.endsWith(".png")) {
			return "image/png";
		} else if (uri.endsWith(".gif")) {
			return "image/gif";
		} else {
			return "image/*";
		}
	}
}
