package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ladjzero.hipda.User;
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
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(src));
		startActivity(intent);
	}

	public void onWebViewReady() {
		Logger.i("WebView is ready.");
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
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					webView.clearCache(true);
					onWebViewReady();
				}

				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
					Logger.i("shouldInterceptRequest");

					WebResourceResponse res = null;

					if (ActivityWithWebView.this.shouldInterceptRequest(url)) {
						UilUtils uil = getApp().getUilUtils();
						File cache = uil.getFile(url);

						if (cache == null) {
							Logger.t(TAG).e("cache file is null.");
						} else if (cache.exists()) {
							try {
								res = new WebResourceResponse(getMimeType(url), "binary", new FileInputStream(cache));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						} else {
							try {
								URL imgUrl = new URL(url);
								InputStream imgIn = imgUrl.openStream();
								FileOutputStream fileOs = new FileOutputStream(cache);
								PipedInputStream pipeIn = new PipedInputStream();
								PipedOutputStream pipeOs = new PipedOutputStream(pipeIn);

								new AsyncTask() {
									@Override
									protected Object doInBackground(Object[] params) {
										TeePipe.stream((InputStream) params[0], (OutputStream) params[1], (OutputStream) params[2]);
										return null;
									}
								}.execute(imgIn, fileOs, pipeOs);

								res = new WebResourceResponse(getMimeType(url), "binary", pipeIn);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					return res;
				}

				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
					return shouldInterceptRequest(view, request.getUrl().toString());
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
