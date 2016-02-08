package com.ladjzero.uzlee;

import android.content.Intent;
import android.net.Uri;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.orhanobut.logger.Logger;

/**
 * Created by chenzhuo on 15-10-4.
 */
public abstract class ActivityWithWebView extends ActivityHardSlide implements OnToolbarClickListener {

	private boolean initialized;

	@JavascriptInterface
	public void onProfileClick(int uid, String name) {
		User me = Core.getUser();

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

	@JavascriptInterface
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
			WebView2 webView = getWebView();

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
			settings.setCacheMode(WebSettings.LOAD_DEFAULT);
			settings.setBlockNetworkImage(disableImageFromNetwork());
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
}
