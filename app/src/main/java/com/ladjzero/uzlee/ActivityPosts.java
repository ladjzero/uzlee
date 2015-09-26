package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.rey.material.widget.ProgressView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;


public class ActivityPosts extends BaseActivity implements AdapterView.OnItemClickListener,
		Core.OnPostsListener,
		DiscreteSeekBar.OnProgressChangeListener {

	private static final String TAG = "ActivityPosts";
	private final int EDIT_CODE = 99;
	// 0 = asc, 1 = desc
	public int orderType = 0;
	private int mThreadUserId = 0;
	private int mTid;
	private int mPage;
	private Posts mPosts = new Posts();
	private PullToRefreshWebView mListView;
	private WebView mWebView;
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
	private ProgressView mProgressBar;
	private int myid;
	SlidrInterface slidrInterface;
	private int position = 0;
	// Scroll to this post.
	private int mPid = 0;
	float downXValue, downYValue;
	boolean mFirstTouch = true;
	boolean mWebviewIsOnTouching = false;
	boolean mWebviewIsScrolling = false;
	private EditText mQuickEdit;
	private PullToRefreshBase.State mPtrState;
	private View mQuickReplyLayout;
	private boolean mQuickVisible = true;
	private boolean isFadingOut = false;
	private TextView mQuickSend;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_posts);

		slidrInterface = Slidr.attach(this, slidrConfig);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView = mInflater.inflate(R.layout.toolbar_title_for_post, null);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(customView);

		mTitleView = (TextView) customView.findViewById(R.id.title);
		mQuickEdit = (EditText) findViewById(R.id.quick_input);
		mQuickEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mQuickReplyLayout.setAlpha(hasFocus ? 1.0f : 0.97f);
			}
		});
		mQuickEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				toggleQuickSend(count + start >= 5);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mQuickReplyLayout = findViewById(R.id.quick_reply);
		mQuickSend = (TextView) findViewById(R.id.quick_send);


		Intent intent = getIntent();
		mTid = intent.getIntExtra("tid", 0);
		mPage = intent.getIntExtra("page", 1);
		mPid = intent.getIntExtra("pid", 0);
		mThreadUserId = intent.getIntExtra("uid", 0);
		mInitToLastPost = mPage == 9999;

		setTitle(intent.getStringExtra("title"));

		mProgressBar = (ProgressView) findViewById(R.id.progress_bar);

		mListView = (PullToRefreshWebView) this.findViewById(R.id.posts);

		mWebView = mListView.getRefreshableView();

		mWebView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {

					case MotionEvent.ACTION_DOWN: {
						Logger.d("touch down");
						mWebviewIsOnTouching = true;
					}
					case MotionEvent.ACTION_MOVE: {
						Logger.d("touch move");

						float x = event.getX(), y = event.getY();

						if (mFirstTouch) {
							downXValue = x;
							downYValue = y;
						} else {
							if (Math.abs(x - downXValue) * 1.5 < Math.abs(y - downYValue)) {
								slidrInterface.lock();
							}
						}

						mFirstTouch = false;
						// store the X value when the user's finger was pressed down

						Logger.d("webview touch", "= " + downYValue);
						break;
					}

					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP: {
						Logger.d("touch up");

						mWebviewIsOnTouching = false;
						mFirstTouch = true;
						if (!mWebviewIsScrolling) mListView.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (mPtrState == null || mPtrState == PullToRefreshBase.State.RESET)
									slidrInterface.unlock();
							}
						}, 100);

						// Get the X value when the user released his/her finger
						float currentX = event.getX();
						float currentY = event.getY();
						// check if horizontal or vertical movement was bigger

						if (Math.abs(downXValue - currentX) > Math.abs(downYValue
								- currentY)) {
							Logger.d("webview touch", "x");
							// going backwards: pushing stuff to the right
							if (downXValue < currentX) {
								Logger.d("webview touch", "right");

							}

							// going forwards: pushing stuff to the left
							if (downXValue > currentX) {
								Logger.d("webview touch", "left");

							}

						} else {
							Logger.d("webview touch", "y ");

							if (downYValue < currentY) {
								Logger.d("webview touch", "down");

							}
							if (downYValue > currentY) {
								Logger.d("webview touch", "up");

							}
						}
						break;
					}

				}

				return false;
			}
		});

		mWebView.loadUrl("file:///android_asset/posts.html");
//		mWebView.loadUrl("file:///mnt/sdcard/airdroid/transfer/activity_posts.html");
		mWebView.addJavascriptInterface(this, "ActivityPosts");
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Logger.d(cm.message());
				return true;
			}
		});

		mAdapter = new PostsAdapter(this, mPosts);
		registerForContextMenu(mListView.getRefreshableView());
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);

		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<WebView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<WebView> refreshView) {
				fetch(mPosts.getPage() - 1, ActivityPosts.this);
				position = 0;
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<WebView> refreshView) {
				fetch(mPosts.getPage() + 1, ActivityPosts.this);
				position = 0;
			}
		});

		mListView.setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<WebView>() {
			@Override
			public void onPullEvent(PullToRefreshBase<WebView> refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
				Logger.d("PL %s", state);
				mPtrState = state;

				if (state == PullToRefreshBase.State.RESET) {
					slidrInterface.unlock();

					if (direction == PullToRefreshBase.Mode.PULL_FROM_END && !mQuickVisible) {
						Logger.d("Quick Edit FadeIn");
						YoYo.with(Techniques.FadeIn).duration(100).withListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								mQuickReplyLayout.setAlpha(0.97f);
								mQuickVisible = true;
							}

							@Override
							public void onAnimationCancel(Animator animation) {

							}

							@Override
							public void onAnimationRepeat(Animator animation) {

							}
						}).playOn(mQuickReplyLayout);
					}
				} else {
					slidrInterface.lock();

					if (direction == PullToRefreshBase.Mode.PULL_FROM_END && mQuickVisible && !isFadingOut) {
						Logger.d("Quick Edit FadeOut");

						isFadingOut = true;

						YoYo.with(Techniques.FadeOut).duration(100).withListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								mQuickReplyLayout.setAlpha(0);
								mQuickVisible = false;
								isFadingOut = false;
							}

							@Override
							public void onAnimationCancel(Animator animation) {

							}

							@Override
							public void onAnimationRepeat(Animator animation) {

							}
						}).playOn(mQuickReplyLayout);

					}
				}
			}
		});


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
						fetch(1, ActivityPosts.this);
						// dismiss before data change, visual perfect
						mMenuDialog.dismiss();
						actionsAdapter.notifyDataSetChanged();
						break;
					case 1:
//						ActivityPosts.this.position = mListView.getRefreshableView().getFirstVisiblePosition();
//						mAdapter.notifyDataSetChanged();
//						fetch(mPage, ActivityPosts.this);
//						break;
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
										final MaterialDialog dialog = new MaterialDialog(ActivityPosts.this);
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
	}

	private void toggleQuickSend(boolean on) {
		Resources res = getResources();
		mQuickSend.setTextColor(res.getColor(on ? R.color.primary : R.color.snow_dark));
		mQuickSend.setClickable(on);
	}

	private String getUri() {
		if (mPid > 0) {
			return String.format("http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=%d&ptid=%d", mPid, mTid);
		} else {
			return "http://www.hi-pda.com/forum/viewthread.php?tid=" + mTid + "&page=" + mPage + "&ordertype=" + orderType;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPosts.size() == 0) {
			fetch(mPage, this);
		}

		mWebView.loadUrl("javascript:loadPosts(" + JSON.toJSONString(mPosts) + ")");

//		mAdapter.notifyDataSetChanged();
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

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
	// Has header
//		Post post = mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) menuInfo).position - 1);

//		menu.add(0, 0, 0, "复制正文");
//		menu.add(0, 1, 1, post.getAuthor().getId() == myid ? "编辑" : "回复");
//		super.onCreateContextMenu(menu, v, menuInfo);
//	}

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
		Intent intent = new Intent(ActivityPosts.this, EditActivity.class);
		intent.putExtra("title", myid == uid ? "编辑" : "回复" + (postIndex == 1 ? "楼主" : postIndex + "楼"));

		if (myid == uid) {
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

	public void onQuickReply(View v) {
		showToast("send");
		mQuickEdit.setEnabled(false);
		mQuickSend.setText("{md-refresh spin}");
		mQuickSend.setClickable(false);

		Core.sendReply(mTid, mQuickEdit.getText().toString(), null, null, new Core.OnRequestListener() {
			private void reset() {
				mQuickSend.setClickable(true);
				mQuickEdit.setEnabled(true);
				mQuickSend.setText("{md-send}");
			}

			@Override
			public void onError(String error) {
				reset();
			}

			@Override
			public void onSuccess(String html) {
				reset();
				// TODO: add this text to posts, do not parse all html
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Post post = (Post) adapterView.getAdapter().getItem(i);
		startEditActivity(post);
	}

	private void fetch(int page, final Core.OnPostsListener onPostsListener) {
		myid = Core.getUser().getId();

		mIsFetching = true;
//		toggleMenus(false);

		mProgressBar.start();

		mPage = page;
		String url = getUri();

		Logger.i("Fetching: %s", url);

		Core.getHtml(url, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				mIsFetching = false;
//				toggleMenus(true);


				onPostsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				mIsFetching = false;

				new AsyncTask<String, Object, Posts>() {
					@Override
					protected Posts doInBackground(String... strings) {
						return Core.parsePosts(strings[0], new Core.OnProgress() {
							@Override
							public void progress(int current, int total, Object post) {
								float progress = 1.0f * current / total;

								Logger.i("Progressing %f current %d total %d", progress, current, total);

								publishProgress(current, total, post);

							}
						});
					}

					@Override
					protected void onProgressUpdate(Object... objects) {
						Integer current = (Integer) objects[0];
						Integer total = (Integer) objects[1];
						Post post = (Post) objects[2];

						mProgressBar.setProgress(current * 1.0f / total);

						if (current == 1) {
							mWebView.loadUrl("javascript:loadPosts([])");
						}

						mWebView.loadUrl("javascript:addPost(" + JSON.toJSONString(post) + ")");
					}

					@Override
					protected void onPostExecute(Posts posts) {
						mIsFetching = false;
//						toggleMenus(true);
						onPostsListener.onPosts(posts);
						mProgressBar.stop();
					}
				}.execute(html);
			}
		});
	}

	@Override
	public void onPosts(final Posts posts) {
		setTitle(posts.getTitle());
//		toggleMenus(true);

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

		Logger.d("current page: " + currPage);

		if (mHasNextPage) {
			mListView.setMode(currPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
		} else {
			mListView.setMode(currPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
		}

		mPosts.merge(posts);
		mAdapter.notifyDataSetChanged();

		if (mPid != 0) {
			Post post = (Post) CollectionUtils.find(mPosts.getLastMerged(), new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ((Post) o).getId() == mPid;
				}
			});

			if (post != null) {
				// +1 for header.
//				mListView.getRefreshableView().setSelection(mPosts.getLastMerged().indexOf(post) + 1);
			}
		} else {
//			mListView.getRefreshableView().setSelection(mInitToLastPost ? activity_posts.size() - 1 : position);
			mInitToLastPost = false;
		}

		mPid = 0;


//		mWebView.loadUrl("javascript:loadPosts({activity_posts:" + JSON.toJSONString(mPosts) + "})");
	}

	@Override
	public void onError(String error) {
		showToast(error);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (requestCode == EDIT_CODE && resultCode == EditActivity.EDIT_SUCCESS) {
			mIsFetching = true;
//			toggleMenus(false);

			if (returnIntent != null) {
				String html = returnIntent.getStringExtra("html");

				mProgressBar.start();

				if (html != null && html.length() > 0) {
					new AsyncTask<String, Float, Posts>() {
						@Override
						protected Posts doInBackground(String... strings) {
							return Core.parsePosts(strings[0], new Core.OnProgress() {
								@Override
								public void progress(int current, int total, Object post) {
									float progress = 1.0f * current / total;

									Logger.i("Progressing %f current %d total %d", progress, current, total);

									publishProgress(progress);
								}
							});
						}

						@Override
						protected void onProgressUpdate(Float... floats) {
							Logger.i("onProgressUpdate %f", floats[0]);
							mProgressBar.setProgress(floats[0]);
						}

						@Override
						protected void onPostExecute(Posts posts) {
							mIsFetching = false;
							setProgressBarIndeterminateVisibility(false);
//							toggleMenus(true);

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
//							mListView.getRefreshableView().setSelection(activity_posts.size() - 1);
							mProgressBar.stop();
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
		menu.findItem(R.id.more).setIcon(new IconDrawable(this, MaterialIcons.md_more_vert).colorRes(android.R.color.white).actionBarSize());

//		menu.setGroupVisible(0, !mIsFetching);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (!mIsFetching) {
			if (id == R.id.more) {
				onKeyDown(KeyEvent.KEYCODE_MENU, null);

				return true;
//		} else if (id == R.id.reply) {
//			Intent replyIntent = new Intent(this, EditActivity.class);
//			replyIntent.putExtra("tid", mTid);
//			replyIntent.putExtra("title", "回复主题");
//			replyIntent.putExtra("hideTitleInput", true);
//			startActivityForResult(replyIntent, EDIT_CODE);
//			return true;
			}
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
			fetch(seekBar.getProgress(), ActivityPosts.this);
			mMenuDialog.dismiss();
		}
	}

	@JavascriptInterface
	public void onProfileClick(int uid, String name) {
		showToast("user id is " + uid);

		Intent intent = new Intent(this, ActivityUser.class);
		intent.putExtra("uid", uid);
		intent.putExtra("name", name);
		startActivity(intent);
	}

	@JavascriptInterface
	public void onPostClick(final int pid) {
		showToast("postclick " + pid);
		startEditActivity((Post) CollectionUtils.find(mPosts, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return ((Post) o).getId() == pid;
			}
		}));
	}

	@JavascriptInterface
	public void onScroll(String state) {
		Logger.d("scroll " + state);
		if (mWebviewIsScrolling = state.equals("start")) {
//			slidrInterface.lock();
		} else {
//			if (!mWebviewIsOnTouching) slidrInterface.unlock();
		}
	}

	@JavascriptInterface
	public void onLinkClick(String href) {
		showToast(href);
		Uri uri = Uri.parse(href);

		if ("www.hi-pda.com".equals(uri.getHost())) {

		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);
		}
	}
}