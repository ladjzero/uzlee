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
	int[] snowTheme = {};
	int[] blueTheme = {};
	int white;
	int darkersnow;
	View mSpinner;
	TextView mTitleView;
	private View mRootView;
	private EditText mMessage;
	private TextView mSend;
	private Posts mCharts;
	private int uid;
	private int _heightDiff = 0;
	private String mName;
	private WebView mWebView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_chat);

		Resources res = getResources();

		snowTheme = new int[]{
				res.getColor(R.color.snow_darker),
				res.getColor(R.color.snow_dark),
				res.getColor(R.color.snow_primary),
				res.getColor(R.color.snow_light)
		};

		blueTheme = new int[]{
				res.getColor(R.color.sky_darker),
				res.getColor(R.color.sky_dark),
				res.getColor(R.color.sky_primary),
				res.getColor(R.color.sky_light)
		};

		white = res.getColor(android.R.color.white);
		darkersnow = res.getColor(R.color.snow_darker);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView = mInflater.inflate(R.layout.toolbar_title_for_post, null);
		mSpinner = customView.findViewById(R.id.spinner);
		mTitleView = (TextView) customView.findViewById(R.id.title);
		mTitleView.setVisibility(View.INVISIBLE);

		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(customView);

		mWebView = (WebView) findViewById(R.id.webview);

		mRootView = findViewById(R.id.root);
		mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				//r will be populated with the coordinates of your view that area still visible.
				mRootView.getWindowVisibleDisplayFrame(r);

				int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);

				if (heightDiff != _heightDiff && heightDiff > 100) {
				}

				_heightDiff = heightDiff;
			}
		});

		mSend = (TextView) findViewById(R.id.send_message);

		mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = mMessage.getText().toString();

				if (message != null && message.trim().length() > 0) {
					User user = Core.getUser();
					Post.NiceBody body = new Post.NiceBody();
					body.add(new AbstractMap.SimpleEntry<Post.BodyType, String>(Post.BodyType.TXT, message));
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					String date = dateFormat.format(new Date());

					Post chat = new Post().setPending(true).setRead(false).setAuthor(user)
							.setNiceBody(body)
							.setTimeStr(date);

					mCharts.add(chat);


					Core.sendMessage(mName, message, ActivityChat.this);

					mMessage.setText("");
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
		mTitleView.setText(mName);
	}

	private void enableSend(boolean enable) {
		Resources res = getResources();
		mSend.setTextColor(res.getColor(R.color.snow_dark));
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
		return "file:///android_asset/chats.html";
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
						mSpinner.setVisibility(View.GONE);
						mTitleView.setVisibility(View.VISIBLE);

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
		menu.findItem(R.id.browser).setIcon(new IconDrawable(this, MaterialIcons.md_open_in_browser).colorRes(android.R.color.white).actionBarSize());
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Utils.openInBrowser(this, "http://www.hi-pda.com/forum/pm.php?uid=" + uid + "&filter=privatepm&daterange=5");

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onError(String error) {
		Logger.e(error);
	}

	@Override
	public void onSuccess(String html) {
		for (Post chat : mCharts) {
			if (chat.isPending()) {
				chat.setPending(false);
				chat.setRead(false);
			}
		}
	}
}
