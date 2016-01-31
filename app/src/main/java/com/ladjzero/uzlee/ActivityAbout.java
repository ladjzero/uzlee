package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.r0adkll.slidr.Slidr;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenzhuo on 16-1-1.
 */
public class ActivityAbout extends ActivityWithWebView {
	@Bind(R.id.webview)
	WebView2 mWebView;

	@OnClick(R.id.logs)
	void showLogs() {
		Intent intent = new Intent(this, ActivityPosts.class);
		intent.putExtra("fid", 2);
		intent.putExtra("tid", 1566769);
//		intent.putExtra("title", t.getTitle());
//		intent.putExtra("pid", t.getToFind());
		intent.putExtra("uid", 592617);

		startActivity(intent);
	}

	@OnClick(R.id.checkUpdate)
	public void checkUpdate() {
		super.checkUpdate(true);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_about);
		ButterKnife.bind(this);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		Slidr.attach(this);
	}

	@Override
	public WebView2 getWebView() {
		return mWebView;
	}

	@Override
	public String getHTMLFilePath() {
		return "https://cdn.rawgit.com/ladjzero/uzlee/master/release/readme.html";
	}
}
