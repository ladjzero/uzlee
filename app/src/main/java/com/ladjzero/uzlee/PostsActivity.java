package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.User;

public class PostsActivity extends BaseActivity implements AdapterView.OnItemClickListener, Core.OnPostsListener, SwipeRefreshLayout.OnRefreshListener {

	private SwipeRefreshLayout swipe;

	DBHelper db;
	Dao<Thread, Integer> threadDao;
	Dao<Post, Integer> postDao;
	Dao<User, Integer> userDao;
	int fid;
	int tid;
	ArrayList<Post> posts = new ArrayList<Post>();
	ListView listView;
	String titleStr;
	PostsAdapter adapter;
	boolean hasNextPage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableBackAction();

		this.setContentView(R.layout.posts);
		db = this.getHelper();
		fid = getIntent().getIntExtra("fid", 0);
		tid = getIntent().getIntExtra("tid", 0);
		setTitle(titleStr = getIntent().getStringExtra("title"));

		try {
			threadDao = db.getThreadDao();
			postDao = db.getPostDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		posts = new ArrayList<Post>();
		listView = (ListView) this.findViewById(R.id.posts);
		adapter = new PostsAdapter(this, posts);
		listView.setOnItemClickListener(this);
		ViewGroup title = ((ViewGroup) getLayoutInflater().inflate(R.layout.posts_header, listView, false));
		TextView titleView = (TextView) title.findViewById(R.id.posts_header);
		titleView.setText(titleStr);
//		listView.addHeaderView(title, null, false);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {
					fetch(page, PostsActivity.this);
				}
			}
		});

		swipe = (SwipeRefreshLayout) findViewById(R.id.post_swipe);
		swipe.setOnRefreshListener(this);
		swipe.setColorSchemeResources(R.color.deep_darker, R.color.deep_dark, R.color.deep_light, android.R.color.white);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (posts.size() == 0) {
			fetch(1, this);
		}

		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.posts, menu);

		menu.findItem(R.id.post_reply).setIcon(
				new IconDrawable(this, Iconify.IconValue.fa_comment_o)
						.colorRes(android.R.color.white)
						.actionBarSize()
		);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.post_reply:
				Intent replyIntent = new Intent(this, EditActivity.class);
				replyIntent.putExtra("tid", tid);
				replyIntent.putExtra("title", "回复：" + titleStr);
				replyIntent.putExtra("hideTitleInput", true);
				startActivity(replyIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (posts != null) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (Post p : posts) {
				ids.add(p.getId());
			}

			outState.putIntegerArrayList("ids", ids);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Post post = (Post) adapterView.getAdapter().getItem(i);
		int uid = post.getAuthor().getId();

		Intent intent = new Intent(PostsActivity.this, EditActivity.class);
		intent.putExtra("title", Core.getUid() == uid ? "编辑" : "回复#" + (i + 1));
		intent.putExtra("fid", fid);
		intent.putExtra("pid", post.getId());
		intent.putExtra("tid", tid);
		intent.putExtra("uid", uid);
		intent.putExtra("no", i + 1);
		intent.putExtra("userName", post.getAuthor().getName());
		intent.putExtra("hideTitleInput", i != 0);
		startActivity(intent);
	}

	private void fetch(int page, final Core.OnPostsListener onPostsListener) {
		Core.getHtml("http://www.hi-pda.com/forum/viewthread.php?tid=" + tid + "&page=" + page, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Core.parsePosts(html, onPostsListener);
			}
		});
	}

	@Override
	public void onPosts(final ArrayList<Post> posts, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		this.posts.addAll(posts);
		adapter.notifyDataSetChanged();

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				for (Post p : posts) {
					try {
						userDao.createOrUpdate(p.getAuthor());
					} catch (SQLException e) {
						// TODO Auto-generated catch
						e.printStackTrace();
					}
				}
				return null;
			}
		}.execute();
	}

	@Override
	public void onRefresh() {
		adapter.clearViewCache();

		fetch(1, new Core.OnPostsListener() {
			@Override
			public void onPosts(ArrayList<Post> _posts, int page, boolean _hasNextPage) {
				hasNextPage = _hasNextPage;
				posts.clear();
				posts.addAll(_posts);
				adapter.notifyDataSetChanged();
				swipe.setRefreshing(false);
			}
		});
	}
}
