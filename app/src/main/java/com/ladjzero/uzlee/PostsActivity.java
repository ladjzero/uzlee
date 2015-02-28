package com.ladjzero.uzlee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
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
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class PostsActivity extends SwipeActivity implements AdapterView.OnItemClickListener, Core.OnPostsListener, SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "PostsActivity";
	final int EDIT_CODE = 99;
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
	TextView hint;
	private SwipeRefreshLayout swipe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.posts);


		super.onCreate(savedInstanceState);

//		enableBackAction();
		getActionBar().setIcon(null);

		db = this.getHelper();
		fid = getIntent().getIntExtra("fid", 0);
		tid = getIntent().getIntExtra("tid", 0);
		titleStr = getIntent().getStringExtra("title");

		try {
			threadDao = db.getThreadDao();
			postDao = db.getPostDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		posts = new ArrayList<Post>();
		listView = (ListView) this.findViewById(R.id.posts);
		adapter = new PostsAdapter(this, posts, titleStr);
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
					hint.setVisibility(View.VISIBLE);
					fetch(page, PostsActivity.this);
				}
			}
		});

		swipe = (SwipeRefreshLayout) findViewById(R.id.post_swipe);
		swipe.setOnRefreshListener(this);
		swipe.setColorSchemeResources(R.color.deep_darker, R.color.deep_dark, R.color.deep_light, android.R.color.white);

		hint = (TextView) findViewById(R.id.hint);
		hint.setVisibility(View.GONE);

		findViewById(R.id.posts_action_reply).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent replyIntent = new Intent(PostsActivity.this, EditActivity.class);
				replyIntent.putExtra("tid", tid);
				replyIntent.putExtra("title", "回复主题");
				replyIntent.putExtra("hideTitleInput", true);
				startActivityForResult(replyIntent, EDIT_CODE);
			}
		});

		registerForContextMenu(listView);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (posts.size() == 0) fetch(1, this);
		adapter.notifyDataSetChanged();
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(0, 1, 0, "复制正文");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Post post = adapter.getItem(info.position);
		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		StringBuilder builder = new StringBuilder();

		for (String str : post.getNiceBody()) {
			if (str.startsWith("txt:")) {
				if (builder.length() > 0) builder.append("\n");
				builder.append(str.substring(4));
			}
		}

		ClipData clipData = ClipData.newPlainText("post content", builder.toString());
		clipboardManager.setPrimaryClip(clipData);
		showToast("复制到剪切版");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Post post = (Post) adapterView.getAdapter().getItem(i);
		int uid = post.getAuthor().getId();

		Intent intent = new Intent(PostsActivity.this, EditActivity.class);
		intent.putExtra("title", Core.getUid() == uid ? "编辑" : "回复#" + (i + 1));
		if (Core.getUid() == uid) {
			intent.putExtra("fid", fid);
		}
		intent.putExtra("pid", post.getId());
		intent.putExtra("tid", tid);
		intent.putExtra("uid", uid);
		intent.putExtra("no", i + 1);
		intent.putExtra("userName", post.getAuthor().getName());
		intent.putExtra("hideTitleInput", i != 0);
		startActivityForResult(intent, EDIT_CODE);
	}

	private void fetch(int page, final Core.OnPostsListener onPostsListener) {
		Core.getHtml("http://www.hi-pda.com/forum/viewthread.php?tid=" + tid + "&page=" + page, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				onPostsListener.onError();
				hint.setVisibility(View.GONE);
			}

			@Override
			public void onSuccess(String html) {
				new AsyncTask<String, Void, Core.PostsRet>() {
					@Override
					protected Core.PostsRet doInBackground(String... strings) {
						return Core.parsePosts(strings[0]);
					}

					@Override
					protected void onPostExecute(Core.PostsRet ret) {
						onPostsListener.onPosts(ret.posts, ret.page, ret.hasNextPage);
						hint.setVisibility(View.INVISIBLE);
					}
				}.execute(html);
			}
		});
	}

	@Override
	public void onPosts(final ArrayList<Post> posts, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		this.posts.addAll(posts);
		if (this.posts.size() > 0) this.posts.get(0).setTitle(titleStr);
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
	public void onError() {
		showToast("请求错误");
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
				listView.setOnScrollListener(null);
				listView.setOnScrollListener(new EndlessScrollListener() {
					@Override
					public void onLoadMore(int page, int totalItemsCount) {
						if (hasNextPage) {
							hint.setVisibility(View.VISIBLE);
							fetch(page, PostsActivity.this);
						}
					}
				});
			}

			@Override
			public void onError() {
				swipe.setRefreshing(false);
				showToast("请求错误");
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (returnIntent != null) {
			String html = returnIntent.getStringExtra("html");

			if (html != null && html.length() > 0) {
				new AsyncTask<String, Void, Core.PostsRet>() {
					@Override
					protected Core.PostsRet doInBackground(String... strings) {
						return Core.parsePosts(strings[0]);
					}

					@Override
					protected void onPostExecute(Core.PostsRet ret) {
						final Collection<Integer> ids = CollectionUtils.collect(PostsActivity.this.posts, new Transformer() {
							@Override
							public Object transform(Object o) {
								return ((Post) o).getId();
							}
						});

						ret.posts = (ArrayList<Post>) CollectionUtils.select(ret.posts, new Predicate() {
							@Override
							public boolean evaluate(Object o) {
								Post post = (Post) o;

								return !ids.contains(post.getId());
							}
						});

						PostsActivity.this.posts.addAll(ret.posts);
						hasNextPage = ret.hasNextPage;
						adapter.notifyDataSetChanged();
					}
				}.execute(html);
			}
		}
	}
}
