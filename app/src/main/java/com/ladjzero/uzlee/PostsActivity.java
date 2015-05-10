package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.j256.ormlite.dao.Dao;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.r0adkll.slidr.Slidr;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;


public class PostsActivity extends BaseActivity implements AdapterView.OnItemClickListener,
		Core.OnPostsListener,
		DiscreteSeekBar.OnProgressChangeListener {

	private static final String TAG = "PostsActivity";
	private final int EDIT_CODE = 99;
	// 0 = asc, 1 = desc
	public int orderType = 0;
	private DBHelper db;
	private int mTid;
	private int mPage;
	private Posts mPosts = new Posts();
	private PullToRefreshListView mListView;
	private PostsAdapter mAdapter;
	private boolean mHasNextPage = false;
	private DiscreteSeekBar mSeekBar;
	private Menu mMenu;
	private boolean mIsFetching = false;
	private View mMenuView;
	private AlertDialog mMenuDialog;
	private boolean mInitToLastPost = false;
	// Help menu dialog to show a line.
	private View _justALine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.posts);

//		mActionbar.setIcon(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		mTid = intent.getIntExtra("tid", 0);
		mPage = intent.getIntExtra("page", 1);
		mInitToLastPost = mPage == 9999;

		setTitle(intent.getStringExtra("title"));

		Log.d("POST_ID", ",tid=" + mTid + " page=" + mPage);

		mListView = (PullToRefreshListView) this.findViewById(R.id.posts);
		mAdapter = new PostsAdapter(this, mPosts);
		mListView.setOnItemClickListener(this);
		registerForContextMenu(mListView.getRefreshableView());
		mListView.setAdapter(mAdapter);
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				fetch(mPosts.getPage() - 1, PostsActivity.this);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				fetch(mPosts.getPage() + 1, PostsActivity.this);
			}
		});
		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

		mMenuView = getLayoutInflater().inflate(R.layout.posts_actions_dialog, null);

		mSeekBar = (DiscreteSeekBar) mMenuView.findViewById(R.id.seekbar);
		mSeekBar.setMin(1);
		mSeekBar.setOnProgressChangeListener(this);
		_justALine = mMenuView.findViewById(R.id.just_a_line);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setView(mMenuView)
				.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
						if (keyCode == KeyEvent.KEYCODE_MENU && keyEvent.getAction() == 0) {
							mMenuDialog.dismiss();
							return true;
						}

						return false;
					}
				});

		mMenuDialog = alertDialogBuilder.create();
		mMenuDialog.setCanceledOnTouchOutside(true);

		ListView menuList = (ListView) mMenuView.findViewById(R.id.actions);
		final PostActionsAdapter actionsAdapter = new PostActionsAdapter(this);
		menuList.setAdapter(actionsAdapter);
		menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				switch (position) {
					case 0:
						orderType = orderType == 0 ? 1 : 0;
						fetch(1, PostsActivity.this);
						// dismiss before data change, visual perfect
						mMenuDialog.dismiss();
						actionsAdapter.notifyDataSetChanged();
						break;
					case 1:
						mAdapter.notifyDataSetChanged();
						fetch(mPage, PostsActivity.this);
						break;
					case 2:
						Core.addToFavorite(mTid, new Core.OnRequestListener() {
							@Override
							public void onError(String error) {
								showToast(error);
							}

							@Override
							public void onSuccess(String html) {
								if (html.contains("此主题已成功添加到收藏夹中")) {
									showToast("收藏成功");
								} else {
									if (html.contains("您曾经收藏过这个主题")) {
										final MaterialDialog dialog = new MaterialDialog(PostsActivity.this);
										dialog.setCanceledOnTouchOutside(true)
												.setMessage("已经收藏过该主题")
												.setPositiveButton("移除收藏", new View.OnClickListener() {
													@Override
													public void onClick(View v) {
														dialog.dismiss();

														Core.removeFromFavoriate(mTid, new Core.OnRequestListener() {
															@Override
															public void onError(String error) {
																showToast(error);
															}

															@Override
															public void onSuccess(String html) {
																if (html.contains("此主题已成功从您的收藏夹中移除")) {
																	showToast("移除成功");
																} else {
																	showToast("移除失败");
																}
															}
														});
													}
												});

										dialog.show();
									} else {
										showToast("收藏失败");
									}
								}
							}
						});
						break;
					case 3:
						ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clipData = ClipData.newPlainText("post url", getUri());
						clipboardManager.setPrimaryClip(clipData);
						showToast("复制到剪切版");
						break;
					case 4:
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(getUri()));
						startActivity(intent);
						break;
				}

				mMenuDialog.dismiss();
			}
		});

		Slidr.attach(this);
	}

	private String getUri() {
		return "http://www.hi-pda.com/forum/viewthread.php?tid=" + mTid + "&page=" + mPage;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPosts.size() == 0) fetch(mPage, this);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		mAdapter.clearViewCache();
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPosts != null) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (Post p : mPosts) {
				ids.add(p.getId());
			}

			outState.putIntegerArrayList("ids", ids);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// Has header
		Post post = mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) menuInfo).position - 1);

		menu.add(0, 0, 0, "复制正文");
		menu.add(0, 1, 1, post.getAuthor().getId() == Core.getUid() ? "编辑" : "回复");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		// Has header
		Post post = mAdapter.getItem(info.position - 1);

		switch (item.getItemId()) {
			case 0:
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				StringBuilder builder = new StringBuilder();

				for (Map.Entry<Post.BodyType, String> body : post.getNiceBody()) {
					if (body.getKey() == Post.BodyType.TXT) {
						if (builder.length() > 0) builder.append("\n");
						builder.append(body.getValue());
					}
				}

				ClipData clipData = ClipData.newPlainText("post content", builder.toString());
				clipboardManager.setPrimaryClip(clipData);
				showToast("复制到剪切版");
				break;
			case 1:
				startEditActivity(post);
				break;
		}

		return super.onContextItemSelected(item);
	}

	private void startEditActivity(Post post) {
		int uid = post.getAuthor().getId();
		int postIndex = post.getPostIndex();
		Intent intent = new Intent(PostsActivity.this, EditActivity.class);
		intent.putExtra("title", Core.getUid() == uid ? "编辑" : "回复" + (postIndex == 1 ? "楼主" : postIndex + "楼"));

		if (Core.getUid() == uid) {
			intent.putExtra("fid", mPosts.getFid());
		}

		intent.putExtra("pid", post.getId());
		intent.putExtra("tid", mTid);
		intent.putExtra("uid", uid);
		intent.putExtra("no", postIndex);
		intent.putExtra("userName", post.getAuthor().getName());
		intent.putExtra("hideTitleInput", postIndex != 1);
		startActivityForResult(intent, EDIT_CODE);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Post post = (Post) adapterView.getAdapter().getItem(i);
		startEditActivity(post);
	}

	private void fetch(int page, final Core.OnPostsListener onPostsListener) {
		mIsFetching = true;
		setProgressBarIndeterminateVisibility(true);
		toggleMenus(false);

		Core.getHtml("http://www.hi-pda.com/forum/viewthread.php?tid=" + mTid + "&page=" + page + "&ordertype=" + orderType, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				mIsFetching = false;
				setProgressBarIndeterminateVisibility(false);
				toggleMenus(true);


				onPostsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {

				new AsyncTask<String, Void, Posts>() {
					@Override
					protected Posts doInBackground(String... strings) {
						return Core.parsePosts(strings[0]);
					}

					@Override
					protected void onPostExecute(Posts posts) {
						mIsFetching = false;
						setProgressBarIndeterminateVisibility(false);
						toggleMenus(true);
						onPostsListener.onPosts(posts);
					}
				}.execute(html);
			}
		});
	}

	@Override
	public void onPosts(final Posts posts) {
		setTitle(posts.getTitle());
		toggleMenus(true);

		mListView.onRefreshComplete();
		mAdapter.clearViewCache();

		int totalPage = posts.getTotalPage(),
				currPage = posts.getPage();

		if (totalPage > 1) {
			mSeekBar.setMax(totalPage);
			mSeekBar.setProgress(currPage);
		} else {
			_justALine.setVisibility(View.GONE);
			mSeekBar.setVisibility(View.GONE);
		}

		mHasNextPage = totalPage > currPage;
		mPage = currPage;

		if (mHasNextPage) {
			mListView.setMode(currPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
		} else {
			mListView.setMode(currPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
		}


		mPosts.merge(posts);
		mAdapter.notifyDataSetChanged();
		mListView.getRefreshableView().setSelection(mInitToLastPost ? posts.size() - 1 : 0);
		mInitToLastPost = false;
	}

	@Override
	public void onError(String error) {
		showToast(error);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (requestCode == EDIT_CODE && resultCode == EditActivity.EDIT_SUCCESS) {
			mIsFetching = true;
			setProgressBarIndeterminateVisibility(true);
			toggleMenus(false);

			if (returnIntent != null) {
				String html = returnIntent.getStringExtra("html");

				if (html != null && html.length() > 0) {
					new AsyncTask<String, Void, Posts>() {
						@Override
						protected Posts doInBackground(String... strings) {
							return Core.parsePosts(strings[0]);
						}

						@Override
						protected void onPostExecute(Posts posts) {
							mIsFetching = false;
							setProgressBarIndeterminateVisibility(false);
							toggleMenus(true);

							mPage = posts.getPage();
							int totalPage = posts.getTotalPage(),
									currPage = posts.getPage();

							mHasNextPage = totalPage > currPage;

							mSeekBar.setMax(totalPage);
							mSeekBar.setProgress(currPage);

							if (mHasNextPage) {
								mListView.setMode(mPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
							} else {
								mListView.setMode(mPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
							}

							mPosts.merge(posts);
							mAdapter.notifyDataSetChanged();
							mListView.getRefreshableView().setSelection(posts.size() - 1);
						}
					}.execute(html);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;

		getMenuInflater().inflate(R.menu.posts, menu);
		menu.findItem(R.id.more).setIcon(new IconDrawable(this, Iconify.IconValue.fa_ellipsis_v).colorRes(android.R.color.white).actionBarSize());
		menu.findItem(R.id.reply).setIcon(new IconDrawable(this, Iconify.IconValue.fa_comment_o).colorRes(android.R.color.white).actionBarSize());

		menu.setGroupVisible(0, !mIsFetching);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.more) {
			onKeyDown(KeyEvent.KEYCODE_MENU, null);

			return true;
		} else if (id == R.id.reply) {
			Intent replyIntent = new Intent(this, EditActivity.class);
			replyIntent.putExtra("tid", mTid);
			replyIntent.putExtra("title", "回复主题");
			replyIntent.putExtra("hideTitleInput", true);
			startActivityForResult(replyIntent, EDIT_CODE);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void toggleMenus(boolean visible) {
		if (mMenu != null) {
			invalidateOptionsMenu();
			mMenu.setGroupVisible(0, visible);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		if (keyCode == KeyEvent.KEYCODE_MENU && (ev == null || ev.getAction() == 0) && mPosts.size() > 0) {
			mMenuDialog.show();
			return true;
		}

		return super.onKeyDown(keyCode, ev);
	}

	@Override
	public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

	}

	@Override
	public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
		if (mPage != seekBar.getProgress()) {
			fetch(seekBar.getProgress(), PostsActivity.this);
			mMenuDialog.dismiss();
		}
	}
}
