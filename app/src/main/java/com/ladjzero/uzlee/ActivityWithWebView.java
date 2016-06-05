package com.ladjzero.uzlee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.utils.Timeline;
import com.ladjzero.uzlee.utils.UilUtils;
import com.ladjzero.uzlee.utils.Utils;
import com.orhanobut.logger.Logger;

/**
 * Created by chenzhuo on 15-10-4.
 */
public abstract class ActivityWithWebView extends ActivityHardSlide implements ActivityBase.OnToolbarClickListener {

	private final static String TAG = "ActivityWithWebView";
	private boolean initialized;
	private boolean cancelImage;
	private Timeline mTimeline = new Timeline();

	@JavascriptInterface
	public void onProfileClick(int uid, String name) {
		if (getWebView().finishActionMode()) return;

		User me = App.getInstance().getCore().getLocalApi().getUser();

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelImage = true;
	}

	@JavascriptInterface
	public void onImageClick(String src) {
		if (getWebView().finishActionMode()) return;

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
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());
				}

				@Override
				protected boolean isCancelled() {
					return cancelImage;
				}

				@Override
				public boolean shouldDownloadImage() {
					return App.getInstance().shouldDownloadImage();
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
