package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.j256.ormlite.dao.Dao;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.User;

public class PostsActivity extends BaseActivity implements AdapterView.OnItemClickListener, Core.OnPostsListener{

	DBHelper db;
	Dao<Thread, Integer> threadDao;
	Dao<Post, Integer> postDao;
	Dao<User, Integer> userDao;
	int tid;
	ArrayList<Post> posts = new ArrayList<Post>();
	ListView listView;
	String titleStr;
	PostsAdapter adapter;
	boolean hasNextPage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.posts);
		db = this.getHelper();
		tid = getIntent().getIntExtra("thread_id", 0);
		setTitle(titleStr = getIntent().getStringExtra("title"));

		try {
			threadDao = db.getThreadDao();
			postDao = db.getPostDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		if (savedInstanceState != null) {
			ArrayList<Integer> ids = savedInstanceState
					.getIntegerArrayList("ids");
			if (ids != null) {
				try {
					posts.addAll((postDao.query(postDao.queryBuilder().where()
							.in("id", ids).prepare())));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		posts = new ArrayList<Post>();
		listView = (ListView) this.findViewById(R.id.posts);
		adapter = new PostsAdapter(this, posts);
		listView.setOnItemClickListener(this);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {
					fetch(page, PostsActivity.this);
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		if (posts.size() == 0) {
			fetch(1, this);
		}
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
				replyIntent.putExtra("thread_id", tid);
				replyIntent.putExtra("title", "回复：" + titleStr);
				replyIntent.putExtra("hideTitleInput", true);
				startActivity(replyIntent);
		}

		return super.onOptionsItemSelected(item);
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

		Intent replyIntent = new Intent(PostsActivity.this, EditActivity.class);
		replyIntent.putExtra("title", "回复 " + post.getAuthor().getName() + " #" + (i + 1));
		replyIntent.putExtra("hideTitleInput", true);
		startActivity(replyIntent);
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
	public void onPosts(ArrayList<Post> posts, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		this.posts.addAll(posts);
		adapter.notifyDataSetChanged();
	}
}
