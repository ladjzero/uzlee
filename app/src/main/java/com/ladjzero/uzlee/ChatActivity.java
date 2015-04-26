package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Posts;
import com.ladjzero.uzlee.BaseActivity;
import com.ladjzero.uzlee.PostsAdapter;
import com.ladjzero.uzlee.R;

/**
 * Created by ladjzero on 2015/4/25.
 */
public class ChatActivity extends SwipeActivity implements Core.OnPostsListener{
	private ListView mListView;
	private ArrayAdapter mAdapter;
	private Posts mCharts;
	private int uid;

	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.chat);
		super.onCreate(savedInstanceState);

		getActionBar().setIcon(null);

		mCharts = new Posts();
		mListView = (ListView) findViewById(R.id.chats);
		mAdapter = new ChatsAdapter(this, mCharts);

		mListView.setAdapter(mAdapter);

		Intent intent = getIntent();
		uid = intent.getIntExtra("uid", -1);
		setTitle(intent.getStringExtra("title"));
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mCharts.size() == 0) fetch(1, this);
		mAdapter.notifyDataSetChanged();
	}

	private void fetch(int page, final Core.OnPostsListener onPostsListener) {
		setProgressBarIndeterminateVisibility(true);

		Core.getHtml("http://www.hi-pda.com/forum/pm.php?uid=" + uid + "&filter=privatepm&daterange=5", new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				setProgressBarIndeterminateVisibility(false);

				onPostsListener.onError();
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
	}

	@Override
	public void onError() {

	}
}
