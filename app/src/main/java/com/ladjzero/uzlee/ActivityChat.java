package com.ladjzero.uzlee;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;


/**
 * Created by ladjzero on 2015/4/25.
 */
public class ActivityChat extends ActivityWithWebView implements Core.OnRequestListener {
	private EditText mMessage;
	private TextView mSend;
	private Posts mCharts;
	private int uid;
	private String mName;
	private WebView mWebView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_chat);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setDisplayHomeAsUpEnabled(true);

		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);

		mWebView = (WebView) findViewById(R.id.webview);

		mSend = (TextView) findViewById(R.id.send_message);

		mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = mMessage.getText().toString();

				if (message != null && message.trim().length() > 0) {
					mSend.setText("{md-refresh spin}");
					mSend.setClickable(false);
					mMessage.setText("");

					Core.sendMessage(mName, message, ActivityChat.this);
				}
			}
		});

		mCharts = new Posts();
		mMessage = (EditText) findViewById(R.id.message);

		mMessage.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				enableSend(s.toString().trim().length() > 0);
			}
		});


		enableSend(false);

		Intent intent = getIntent();
		uid = intent.getIntExtra("uid", -1);
		mName = intent.getStringExtra("name");
		setTitle(mName);
	}

	private void enableSend(boolean enable) {
		mSend.setTextColor(Utils.getThemeColor(this, enable ? R.attr.colorPrimary : R.attr.colorBackgroundSecondary));
		mSend.setClickable(enable);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mCharts.size() == 0) fetch(1);
	}

	@Override
	public WebView getWebView() {
		return mWebView;
	}

	@Override
	public String getHTMLFilePath() {
		return "file:///android_asset/chats.html?theme=" +
				setting.getString("theme", DefaultTheme) +
				"&fontsize=" + setting.getString("font_size", "normal");
	}

	private void fetch(int page) {
		Logger.i("fetching chats uid " + uid);

		Core.getHtml("http://www.hi-pda.com/forum/pm.php?uid=" + uid + "&filter=privatepm&daterange=5", new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				setProgressBarIndeterminateVisibility(false);

				Logger.e(error);
			}

			@Override
			public void onSuccess(String html) {

				new AsyncTask<String, Void, String>() {
					@Override
					protected String doInBackground(String... strings) {
						return Core.parseMessagesToHtml(strings[0]);
					}

					@Override
					protected void onPostExecute(String html) {
						String js = "javascript:loadHTML(\"" + html.replaceAll("\"", "\\\\\"").replaceAll("[\\t\\n\\r]", "") + "\")";
						Logger.d(js);
						mWebView.loadUrl(js);
					}
				}.execute(html);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat, menu);
		menu.findItem(R.id.browser)
				.setIcon(new IconDrawable(this, MaterialIcons.md_open_in_browser)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize());
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.browser) {
			Utils.openInBrowser(this, "http://www.hi-pda.com/forum/pm.php?uid=" + uid + "&filter=privatepm&daterange=5");
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onError(String error) {
		Logger.e(error);
		showToast(error);
	}

	@Override
	public void onSuccess(String html) {
		mSend.setText("{md-send}");
		mSend.setClickable(true);

		mSend.postDelayed(new Runnable() {
			@Override
			public void run() {
				fetch(1);
			}
		}, 300);
	}
}
