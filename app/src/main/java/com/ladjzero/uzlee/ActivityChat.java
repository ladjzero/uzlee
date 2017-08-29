package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.entities.Posts;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.uzlee.api.HttpClientCallback;
import com.ladjzero.uzlee.utils.Utils;
import com.orhanobut.logger.Logger;


/**
 * Created by ladjzero on 2015/4/25.
 */
public class ActivityChat extends ActivityWithWebView implements OnRespondCallback {
	private EditText mMessage;
	private TextView mSend;
	private Posts mCharts;
	private int uid;
	private String mName;
	private WebView2 mWebView;
	private boolean mWebViewReady;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_chat);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toolbarClick();
			}
		});
		setSupportActionBar(toolbar);

		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setDisplayHomeAsUpEnabled(true);

		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);

		mWebView = (WebView2) findViewById(R.id.webview);

		mSend = (TextView) findViewById(R.id.send_message);

		mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = mMessage.getText().toString();

				if (message != null && message.trim().length() > 0) {
					mSend.setText("{md-refresh spin}");
					mSend.setClickable(false);
					mMessage.setText("");

					App.getInstance().getApi().sendMessage(mName, message, ActivityChat.this);
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
	public WebView2 getWebView() {
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

		App.getInstance().getApi().getRawMessages(new OnRespondCallback() {
			@Override
			public void onRespond(Response res) {
				String html = (String) res.getData();

				while (!mWebViewReady) {
					try {
						java.lang.Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				String js = "javascript:loadHTML(\"" + html.replaceAll("\"", "\\\\\"").replaceAll("[\\t\\n\\r]", "") + "\")";
				Logger.d(js);
				mWebView.loadUrl(js);
			}
		});
	}

	@Override
	public void onWebViewReady() {
		super.onWebViewReady();
		mWebViewReady = true;
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
	public void onRespond(Response res) {
		if (res.isSuccess()) {
			mSend.setText("{md-send}");
			mSend.setClickable(true);

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					fetch(1);
				}
			}, 1000);
		} else {
			showToast(res.getData().toString());
		}
	}
}
