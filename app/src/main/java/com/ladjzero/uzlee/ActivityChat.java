package com.ladjzero.uzlee;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatButton;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.r0adkll.slidr.Slidr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;


/**
 * Created by ladjzero on 2015/4/25.
 */
public class ActivityChat extends BaseActivity implements Core.OnPostsListener, Core.OnRequestListener {
	private View mRootView;
	private ListView mListView;
	private EditText mMessage;
	private FlatButton mSend;
	private ChatsAdapter mAdapter;
	private Posts mCharts;
	private int uid;
	private int _heightDiff = 0;
	private String mName;
	int[] snowTheme = {};
	int[] blueTheme = {};
	int white;
	int darkersnow;
	TextView mTitleView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.chat);

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

		Slidr.attach(this);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView =  mInflater.inflate(R.layout.toolbar_title_for_post, null);

		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_blue_dark)));
		mActionbar.setCustomView(customView);

		mTitleView = (TextView) customView.findViewById(R.id.title);

		mTitleView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListView != null) mListView.setSelection(0);
			}
		});

		mRootView = findViewById(R.id.root);
		mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				//r will be populated with the coordinates of your view that area still visible.
				mRootView.getWindowVisibleDisplayFrame(r);

				int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);

				if (heightDiff != _heightDiff && heightDiff > 100) {
					mListView.setSelection(mAdapter.getCount() - 1);
				}

				_heightDiff = heightDiff;
			}
		});

		mSend = (FlatButton) findViewById(R.id.send_message);

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
					mAdapter.notifyDataSetChanged();

					mListView.setSelection(mAdapter.getCount() - 1);

					Core.sendMessage(mName, message, ActivityChat.this);

					mMessage.setText("");
				}
			}
		});

		mCharts = new Posts();
		mListView = (ListView) findViewById(R.id.chats);
		mMessage = (EditText) findViewById(R.id.message);
		mAdapter = new ChatsAdapter(this, mCharts);

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

		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(findViewById(R.id.empty_view));

		enableSend(false);

		Intent intent = getIntent();
		uid = intent.getIntExtra("uid", -1);
		mName = intent.getStringExtra("name");
		setTitle(mName);
	}

	private void enableSend(boolean enable) {
		if (enable) {
			mSend.getAttributes().setColors(blueTheme);
			mSend.setTextColor(white);
		} else {
			mSend.getAttributes().setColors(snowTheme);
			mSend.setTextColor(darkersnow);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mCharts.size() == 0) fetch(1, this);
	}

	private void fetch(int page, final Core.OnPostsListener onPostsListener) {
		setProgressBarIndeterminateVisibility(true);

		Core.getHtml("http://www.hi-pda.com/forum/pm.php?uid=" + uid + "&filter=privatepm&daterange=5", new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				setProgressBarIndeterminateVisibility(false);

				onPostsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {

				new AsyncTask<String, Void, Posts>() {
					@Override
					protected Posts doInBackground(String... strings) {
						return Core.parseMessages(strings[0]);
					}

					@Override
					protected void onPostExecute(Posts posts) {
						setProgressBarIndeterminateVisibility(false);
						onPostsListener.onPosts(posts);
					}
				}.execute(html);
			}
		});
	}

	@Override
	public void onPosts(Posts posts) {
		mCharts.merge(posts);
		mAdapter.notifyDataSetChanged();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				mListView.setSelection(mAdapter.getCount() - 1);
			}
		});
	}

	@Override
	public void onError(String error) {

	}

	@Override
	public void onSuccess(String html) {
		for (Post chat : mCharts) {
			if (chat.isPending()) {
				chat.setPending(false);
				chat.setRead(false);
			}
		}

		mAdapter.notifyDataSetChanged();
	}
}
