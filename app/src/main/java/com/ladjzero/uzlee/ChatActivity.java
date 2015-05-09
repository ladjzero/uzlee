package com.ladjzero.uzlee;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;


/**
 * Created by ladjzero on 2015/4/25.
 */
public class ChatActivity extends SwipeActivity implements Core.OnPostsListener, Core.OnRequestListener {
	private View mRootView;
	private ListView mListView;
	private EditText mMessage;
	private View mSend;
	private ChatsAdapter mAdapter;
	private Posts mCharts;
	private int uid;
	private int _heightDiff = 0;
	private String mName;

	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.chat);
		super.onCreate(savedInstanceState);

		mActionbar.setIcon(null);

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

		mSend = findViewById(R.id.send_message);

		mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = mMessage.getText().toString();

				if (message != null && message.length() > 0) {
					User user = new User().setId(Core.getUid()).setName(Core.getName());
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

					Core.sendMessage(mName, message, ChatActivity.this);

					mMessage.setText("");
				}
			}
		});

		mCharts = new Posts();
		mListView = (ListView) findViewById(R.id.chats);
		mMessage = (EditText) findViewById(R.id.message);
		mAdapter = new ChatsAdapter(this, mCharts);

		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(findViewById(R.id.empty_view));

		Intent intent = getIntent();
		uid = intent.getIntExtra("uid", -1);
		mName = intent.getStringExtra("name");
		setTitle(mName);
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
