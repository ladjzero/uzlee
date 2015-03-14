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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.j256.ormlite.dao.Dao;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;
import com.nineoldandroids.animation.Animator;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class PostsActivity extends SwipeActivity implements AdapterView.OnItemClickListener,
		Core.OnPostsListener,
		DiscreteSeekBar.OnProgressChangeListener {

	private static final String TAG = "PostsActivity";
	private final int EDIT_CODE = 99;
	private DBHelper db;
	private Dao<Thread, Integer> threadDao;
	private Dao<Post, Integer> postDao;
	private Dao<User, Integer> userDao;
	private int mFid;
	private int mTid;
	private int mPage;
	private ArrayList<Post> mPosts = new ArrayList<Post>();
	private PullToRefreshListView mListView;
	private String titleStr;
	private PostsAdapter mAdapter;
	private boolean mHasNextPage = false;
	private TextView mHint;
	private View mActions;
	private View mMask;
	private boolean mIsAnimating = false;
	private DiscreteSeekBar mSeekBar;
	private View mSeekBarContainer;
	private boolean mActionsVisibility = false;
	private Comparator<Post> mComparator = new Comparator<Post>() {
		@Override
		public int compare(Post post1, Post post2) {
			return post1.getPostIndex() - post2.getPostIndex();
		}
	};
	private Transformer mGetId = new Transformer() {
		@Override
		public Object transform(Object o) {
			return ((Post) o).getId();
		}
	};
	private Menu mMenu;
	private boolean mIsFetching = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.posts);
		super.onCreate(savedInstanceState);

		getActionBar().setIcon(null);

		db = this.getHelper();
		mFid = getIntent().getIntExtra("fid", 0);
		mTid = getIntent().getIntExtra("tid", 0);
		titleStr = getIntent().getStringExtra("title");
		setTitle(titleStr);

		Log.d("POST_ID", "fid=" + mFid + ",tid=" + mTid);

		try {
			threadDao = db.getThreadDao();
			postDao = db.getPostDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		mListView = (PullToRefreshListView) this.findViewById(R.id.posts);
		mAdapter = new PostsAdapter(this, mPosts, titleStr);
		mListView.setOnItemClickListener(this);
		registerForContextMenu(mListView.getRefreshableView());
		mListView.setAdapter(mAdapter);
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				fetch(mPage - 1, PostsActivity.this);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				fetch(mPage + 1, PostsActivity.this);
			}
		});

		mHint = (TextView) findViewById(R.id.hint);
		mHint.setVisibility(View.GONE);

		mMask = findViewById(R.id.mask);
		mMask.setVisibility(View.GONE);
		mMask.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN &&
						mMask.getVisibility() == View.VISIBLE)
					onKeyDown(KeyEvent.KEYCODE_MENU, null);

				return mActionsVisibility;
			}
		});

		mActions = findViewById(R.id.posts_actions);
		mActions.setVisibility(View.GONE);

		mSeekBarContainer = findViewById(R.id.seekbar_container);
		mSeekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
		mSeekBar.setMin(1);
		mSeekBar.setOnProgressChangeListener(this);
		mSeekBarContainer.setVisibility(View.GONE);


		findViewById(R.id.posts_action_share).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showToast("暂不可用");
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
			}
		});

		findViewById(R.id.posts_action_favorite).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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
								AlertDialog.Builder alert = new AlertDialog.Builder(PostsActivity.this);
								alert.setTitle("提醒");
								alert.setMessage("已经收藏过该主题");
								alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}

								});
								alert.setPositiveButton("移除收藏", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
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
								alert.show();
							} else {
								showToast("收藏失败");
							}
						}
					}
				});
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
			}
		});

		findViewById(R.id.posts_action_link).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clipData = ClipData.newPlainText("post url", getUri());
				clipboardManager.setPrimaryClip(clipData);
				showToast("复制到剪切版");
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
			}
		});

		findViewById(R.id.posts_action_browser).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getUri()));
				startActivity(intent);
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
			}
		});
	}

	private String getUri() {
		return StringUtils.join(
				new String[]{"http://www.hi-pda.com/forum/viewthread.php?", "tid=" + mTid, "&page=" + mPage});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPosts.size() == 0) fetch(1, this);
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
		Post post = mAdapter.getItem(((AdapterView.AdapterContextMenuInfo)menuInfo).position - 1);

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

				for (Map.Entry<Core.BodyType, String> body : post.getNiceBody()) {
					if (body.getKey() == Core.BodyType.TXT) {
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
			intent.putExtra("fid", mFid);
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

		Core.getHtml("http://www.hi-pda.com/forum/viewthread.php?tid=" + mTid + "&page=" + page, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				mIsFetching = false;
				setProgressBarIndeterminateVisibility(false);
				toggleMenus(true);


				onPostsListener.onError();
				mHint.setVisibility(View.GONE);
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
						mIsFetching = false;
						setProgressBarIndeterminateVisibility(false);
						toggleMenus(true);
						onPostsListener.onPosts(ret.posts, ret.page, Math.max(ret.totalPage, ret.page));
						mHint.setVisibility(View.INVISIBLE);
					}
				}.execute(html);
			}
		});
	}

	@Override
	public void onPosts(final ArrayList<Post> posts, int currPage, int totalPage) {
		toggleMenus(true);
		mListView.onRefreshComplete();
		mAdapter.clearViewCache();

		if (totalPage > 1) mSeekBarContainer.setVisibility(View.VISIBLE);
		mSeekBar.setMax(totalPage);
		mSeekBar.setProgress(currPage);

		mHasNextPage = totalPage > currPage;
		mPage = currPage;

		if (mHasNextPage) {
			mListView.setMode(currPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
		} else {
			mListView.setMode(currPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
		}

//		for (final Object id : CollectionUtils.collect(posts, mGetId)) {
//			mPosts.remove(CollectionUtils.find(mPosts, new Predicate() {
//				@Override
//				public boolean evaluate(Object o) {
//					return ((Post) o).getId() == id;
//				}
//			}));
//		}

		mPosts.clear();
		mPosts.addAll(posts);
		Collections.sort(mPosts, mComparator);
		mAdapter.setWindow(posts.get(0).getPostIndex(), posts.get(posts.size() - 1).getPostIndex() + 1);
		if (mPosts.size() > 0) mPosts.get(0).setTitle(titleStr);
		mAdapter.notifyDataSetChanged();
		mListView.getRefreshableView().setSelection(0);


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
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (mActions.getVisibility() == View.VISIBLE) onKeyDown(KeyEvent.KEYCODE_MENU, null);

		if (requestCode == EDIT_CODE && resultCode == EditActivity.EDIT_SUCCESS) {
			mIsFetching = true;
			setProgressBarIndeterminateVisibility(true);
			toggleMenus(false);

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
							mIsFetching = false;
							setProgressBarIndeterminateVisibility(false);
							toggleMenus(true);

							mPage = ret.page;
							mHasNextPage = ret.totalPage > ret.page;

							mSeekBar.setMax(Math.max(ret.page, ret.totalPage));
							mSeekBar.setProgress(ret.page);

							if (mHasNextPage) {
								mListView.setMode(mPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
							} else {
								mListView.setMode(mPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
							}

							mPosts.clear();
							mPosts.addAll(ret.posts);
							mHasNextPage = ret.hasNextPage;
							mAdapter.notifyDataSetChanged();
							mListView.getRefreshableView().setSelection(ret.posts.size() - 1);
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
		menu.findItem(R.id.more).setIcon(new IconDrawable(this, Iconify.IconValue.fa_ellipsis_h).colorRes(android.R.color.white).actionBarSize());
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
		if (keyCode == KeyEvent.KEYCODE_MENU && !mIsAnimating) {
			int visibility = mActions.getVisibility();

			if (visibility == View.GONE) {
				YoYo.with(Techniques.FadeIn)
						.duration(200)
						.withListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {
								mActions.setVisibility(View.VISIBLE);
								mActionsVisibility = true;
								mIsAnimating = true;
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								mIsAnimating = false;
							}

							@Override
							public void onAnimationCancel(Animator animation) {

							}

							@Override
							public void onAnimationRepeat(Animator animation) {

							}
						})
						.playOn(mActions);

				YoYo.with(Techniques.FadeIn)
						.duration(200)
						.withListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {
								mMask.setVisibility(View.VISIBLE);
							}

							@Override
							public void onAnimationEnd(Animator animation) {
							}

							@Override
							public void onAnimationCancel(Animator animation) {

							}

							@Override
							public void onAnimationRepeat(Animator animation) {

							}
						})
						.playOn(mMask);
			} else {
				YoYo.with(Techniques.FadeOut)
						.duration(200)
						.withListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {
								mIsAnimating = true;
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								mActions.setVisibility(View.GONE);
								mActionsVisibility = false;
								mIsAnimating = false;
							}

							@Override
							public void onAnimationCancel(Animator animation) {

							}

							@Override
							public void onAnimationRepeat(Animator animation) {

							}
						})
						.playOn(mActions);

				YoYo.with(Techniques.FadeOut)
						.duration(200)
						.withListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								mMask.setVisibility(View.GONE);
							}

							@Override
							public void onAnimationCancel(Animator animation) {

							}

							@Override
							public void onAnimationRepeat(Animator animation) {
							}
						})
						.playOn(mMask);
			}


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
			onKeyDown(KeyEvent.KEYCODE_MENU, null);
		}
	}
}
