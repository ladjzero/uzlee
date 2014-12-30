package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.ladjzero.hipda.PostScanner;
import com.ladjzero.hipda.User;
import com.ladjzero.hipda.cb.ScannerIsReadyCB;
import com.ladjzero.hipda.cb.UserStatsCB;

public class PostsActivity extends BaseActivity {

	Core core;
	DBHelper db;
	Dao<Thread, Integer> threadDao;
	Dao<Post, Integer> postDao;
	Dao<User, Integer> userDao;
	int tid;
	PostScanner ps;
	int pageCount;
	final ArrayList<Post> posts = new ArrayList<Post>();
	ListView lv;
	ViewGroup title;
	String titleStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		tid = getIntent().getIntExtra("thread_id", 0);
		titleStr = getIntent().getStringExtra("title");

		db = this.getHelper();
		try {
			threadDao = db.getThreadDao();
			postDao = db.getPostDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
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

		this.setContentView(R.layout.posts);

		lv = (ListView) this.findViewById(R.id.post_detail_list);
		title = ((ViewGroup) getLayoutInflater().inflate(
				R.layout.posts_header, lv, false));
		lv.addHeaderView(title, null, false);

		TextView _title = (TextView) title
				.findViewById(R.id.posts_header);
		_title.setText(titleStr);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				Post post = (Post) adapterView.getAdapter().getItem(position);

				Intent replyIntent = new Intent(PostsActivity.this, EditActivity.class);
				replyIntent.putExtra("title", "回复：#" + position + " " + post.getAuthor().getName());
				replyIntent.putExtra("hideTitleInput", true);
				startActivity(replyIntent);
			}
		});

		setTitle(titleStr);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (posts.size() == 0) {
			ps = new PostScanner(tid, new UserStatsCB() {

				@Override
				public void onMsg() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onOffline() {
					// TODO Auto-generated method stub

				}

			}, new ScannerIsReadyCB() {

				@Override
				public void onReady() {
					pageCount = ps.getPageCount();
					posts.addAll(ps.getPageAt(0));

					(new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							for (Post p : posts) {
								try {
									userDao.createOrUpdate(p.getAuthor());
									postDao.createOrUpdate(p);
								} catch (SQLException e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
								}
							}
							return null;
						}

					}).execute();


					lv.setAdapter(new PostsAdapter(PostsActivity.this, posts));
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.posts, menu);

		menu.findItem(R.id.post_reply).setIcon(new IconDrawable(this, Iconify.IconValue.fa_comment_o).colorRes(android.R.color.white).actionBarSize());

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
}
