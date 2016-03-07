package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableList;
import android.graphics.Bitmap;
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
import android.view.View;
import android.webkit.JavascriptInterface;
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
import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.LocalApi;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.PostsParser;
import com.ladjzero.hipda.ProgressReporter;
import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.model.ObservablePosts;
import com.ladjzero.uzlee.utils.CapturePhotoUtils;
import com.ladjzero.uzlee.utils.Timeline;
import com.ladjzero.uzlee.utils.Utils;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.rey.material.app.Dialog;
import com.rey.material.widget.ProgressView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ActivityPosts extends ActivityWithWebView implements AdapterView.OnItemClickListener,
		DiscreteSeekBar.OnProgressChangeListener,
		PullToRefreshBase.OnPullEventListener,
		PullToRefreshBase.OnRefreshListener2,
		TextWatcher {

	private static final String TAG = "ActivityPosts";
	private final int EDIT_CODE = 99;
	// 0 = asc, 1 = desc
	public int orderType = 0;
	PostActionsAdapter actionsAdapter;
	@Bind(R.id.posts)
	PullToRefreshWebView mPostsView;
	@Bind(R.id.quick_input)
	EditText mQuickEdit;
	@Bind(R.id.quick_reply)
	View mQuickReplyLayout;
	@Bind(R.id.quick_send)
	TextView mQuickSend;
	@Bind(R.id.progress_bar)
	ProgressView mProgressView;
	private int mThreadUserId = 0;
	private int mTid;
	private int mPage;
	private ObservablePosts mPosts;
	private WebView2 mWebView;
	private boolean mHasNextPage = false;
	private DiscreteSeekBar mSeekBar;
	private Menu mMenu;
	private boolean mIsFetching = false;
	private View mMenuView;
	private Dialog mMenuDialog;
	private boolean mInitToLastPost = false;
	// Help menu dialog to show a line.
	private int myid = 0;
	private int position = 0;
	// Scroll to this post. 0 for no scrolling. -1 for scrolling to bottom.
	private int mPid = 0;
	private boolean mQuickVisible = true;
	private boolean isFadingOut = false;
	private TextView mTitleView;
	private boolean mWebViewReady = false;
	private boolean mSkipResumeFetch = false;
	private boolean mIsPushedAfterWebReady = false;
	private PostsParser mParser;
	private HttpClient2 mHttpClient;
	private HttpApi mHttpApi;
	private LocalApi mLocalApi;
	private Timeline mTimeline = new Timeline();
	private Model model = new Model();

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

	private void toggleQuickSend(boolean on) {
		mQuickSend.setTextColor(Utils.getThemeColor(this, on ? R.attr.colorPrimary : R.attr.colorBackgroundSecondary));
		mQuickSend.setClickable(on);
	}

	// Menu item click.
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case 0:
				Intent replyIntent = new Intent(this, ActivityEdit.class);
				replyIntent.putExtra("tid", mTid);
				replyIntent.putExtra("title", "回复主题");
				replyIntent.putExtra("hideTitleInput", true);
				startActivityForResult(replyIntent, EDIT_CODE);
				break;
			case 1:
				orderType = orderType == 0 ? 1 : 0;
				fetch(1);
				// dismiss before data change, visual perfect
				mMenuDialog.dismiss();
				actionsAdapter.notifyDataSetChanged();
				break;
			case 2:
				fetch(mPage);
				break;
			case 3:
				mHttpApi.addToFavorite(mTid, new HttpClientCallback() {
					@Override
					public void onFailure(String reason) {
						showToast(reason);
					}

					@Override
					public void onSuccess(String response) {
						if (response.contains("此主题已成功添加到收藏夹中")) {
							showToast("收藏成功");
						} else {
							if (response.contains("您曾经收藏过这个主题")) {
								final Dialog dialog = new Dialog(ActivityPosts.this);

								dialog.setTitle("已经收藏过该主题");
								dialog.setCanceledOnTouchOutside(true);
								dialog.positiveAction("移除收藏").positiveActionClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										mHttpApi.removeFromFavoriate(mTid, new HttpClientCallback() {
											@Override
											public void onFailure(String reason) {
												showToast(reason);
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
			case 4:
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clipData = ClipData.newPlainText("post url", getUri());
				clipboardManager.setPrimaryClip(clipData);
				showToast("复制到剪切版");
				break;
			case 5:
				model.setOnSelection(true);
//				CapturePhotoUtils.insertImage(getContentResolver(), mWebView.toBitmap(), "webview", "webview");
				break;
			case 6:
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getUri()));
				startActivity(intent);
				break;
		}

		mMenuDialog.dismiss();
	}

	private void fetch(int page) {
		Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());

		try {
			myid = mLocalApi.getUser().getId();
		} catch (Exception e) {
		}

		mIsFetching = true;
		mProgressView.setProgress(0f);
		mProgressView.start();

		mPage = page;
		String url = getUri();

		Logger.i("Fetching: %s", url);

		mPosts.clear();

		Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());

		mHttpClient.get(url, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				mPostsView.onRefreshComplete();
				mIsFetching = false;

				Logger.i("html fetched.");

				onHtml(response);
			}

			@Override
			public void onFailure(String reason) {
				Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());
				mIsFetching = false;

				showToast(reason);
				mPostsView.onRefreshComplete();
			}
		});
	}

	private String getUri() {
		if (mPid > 0) {
			return String.format("http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=%d&ptid=%d", mPid, mTid);
		} else {
			return "http://www.hi-pda.com/forum/viewthread.php?tid=" + mTid + "&page=" + mPage + "&ordertype=" + orderType;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_posts);
		ButterKnife.bind(this);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView = mInflater.inflate(R.layout.toolbar_title, null);
		mTitleView = (TextView) customView.findViewById(R.id.title);
		mTitleView.setTextColor(Utils.getThemeColor(this, R.attr.colorTextInverse));

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toolbarClick();
			}
		});
		setSupportActionBar(toolbar);

		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(customView);

		mQuickEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mQuickReplyLayout.setAlpha(hasFocus ? 1.0f : 0.97f);
			}
		});
		mQuickEdit.addTextChangedListener(this);

		Intent intent = getIntent();
		mTid = intent.getIntExtra("tid", 0);
		mPage = intent.getIntExtra("page", 1);
		mPid = intent.getIntExtra("pid", 0);
		mThreadUserId = intent.getIntExtra("uid", 0);
		mInitToLastPost = mPage == 9999;

		mTitleView.setText(intent.getStringExtra("title"));


		mWebView = (WebView2) mPostsView.getRefreshableView();

		mPostsView.setMode(PullToRefreshBase.Mode.DISABLED);
		mPostsView.setOnRefreshListener(this);
		mPostsView.setOnPullEventListener(this);


		mMenuView = getLayoutInflater().inflate(R.layout.posts_actions_dialog, null);

		mSeekBar = (DiscreteSeekBar) mMenuView.findViewById(R.id.seekbar);
		mSeekBar.setMin(1);
		mSeekBar.setOnProgressChangeListener(this);

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

		mMenuDialog = new Dialog(this);
		ListView menuList = (ListView) mMenuView.findViewById(R.id.actions);
		actionsAdapter = new PostActionsAdapter(this);
		menuList.setAdapter(actionsAdapter);
		menuList.setOnItemClickListener(this);

		mMenuDialog.negativeActionClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuDialog.dismiss();
			}
		});

		mMenuDialog.title("")
				.titleColor(Utils.getThemeColor(this, R.attr.colorText))
				.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
				.negativeAction("取消")
				.contentView(mMenuView);

		mMenuDialog.setCanceledOnTouchOutside(true);

		mPosts = new ObservablePosts(new Posts());
		mPosts.addOnListChangedCallback(new OnListChangedCallback());

		mParser = getCore().getPostsParser();
		mHttpClient = getApp().getHttpClient();
		mHttpApi = getCore().getHttpApi();
		mLocalApi = getCore().getLocalApi();
		mProgressView.start();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPosts.size() == 0 && !mSkipResumeFetch) {
			fetch(mPage);
		}

		mSkipResumeFetch = false;
	}

	@Override
	public WebView2 getWebView() {
		return mWebView;
	}

	@Override
	public String getHTMLFilePath() {
		return "file:///android_asset/posts.html";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (!mIsFetching) {
			if (id == R.id.more) {
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
				return true;
			}
		}

		if (id == R.id.ok) {
			model.setToRender(true).setOnSelection(false);
			mWebView.postDelayed(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = mWebView.toBitmap();

					if (bitmap == null) {
						showToast("图片生成失败");
					} else {
						CapturePhotoUtils.insertImage(getContentResolver(), bitmap, "webview", "webview");
						showToast("已保存到相册");
					}

					model.setToRender(false);
				}
			}, 500);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		if (keyCode == KeyEvent.KEYCODE_MENU && (ev == null || ev.getAction() == 0) && mPosts.size() > 0) {
			mMenuDialog.show();

			return true;
		}

		return super.onKeyDown(keyCode, ev);
	}

	public void onQuickReply(View v) {
//		showToast("send");
		mQuickEdit.setEnabled(false);
		mQuickSend.setText("{md-refresh spin}");
		mQuickSend.setClickable(false);

		String reply = mQuickEdit.getText().toString();

		if (getSettings().getBoolean("use_sig", false)) {
			reply += "\t\t\t[size=1][color=Gray]有只梨[/color][/size]";
		}

		mHttpApi.sendReply(mTid, reply, null, null, new HttpClientCallback() {
			private void reset() {
				mQuickSend.setClickable(true);
				mQuickEdit.setEnabled(true);
				mQuickSend.setText("{md-send}");
			}

			@Override
			public void onSuccess(String response) {
				reset();
				mPosts.clear();
				mQuickEdit.setText("");
				mPid = -1;
				onHtml(response);
			}

			@Override
			public void onFailure(String reason) {
				showToast(reason);
				reset();
			}
		});
	}

	private void onHtml(String html) {
		Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());

		if (html != null && html.length() > 0) {
			new AsyncTask<String, Object, Posts>() {
				@Override
				protected Posts doInBackground(String... strings) {
					return mParser.parsePosts(strings[0], new ProgressReporter() {
						@Override
						public void onProgress(int i, int size, Object o) {
							publishProgress(i, size, o);
						}
					});
				}

				// Load the first post as soon as possible.
				@Override
				protected void onProgressUpdate(Object... objects) {
					Post post = (Post) objects[2];
					if (post.getId() == 1) mThreadUserId = post.getAuthor().getId();
					post.setIsLz(post.getAuthor().getId() == mThreadUserId);
					post.setTimeStr(Utils.prettyTime(post.getTimeStr()));
					mPosts.add(post);

					mPostsView.setMode(PullToRefreshBase.Mode.DISABLED);
					mProgressView.setProgress((Integer) objects[0] * 1.0f / (Integer) objects[1]);
					Logger.i("Parsed one post.");
				}

				@Override
				protected void onPostExecute(Posts posts) {
					Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());

					mPosts.replaceMeta(posts);
					model.setTitle(posts.getTitle());
					model.setUrl(getUri());
					mProgressView.stop();

					mIsFetching = false;

					mPage = posts.getPage();
					int totalPage = posts.getTotalPage(),
							currPage = posts.getPage();

					mHasNextPage = totalPage > currPage;

					mSeekBar.setMax(totalPage);
					mSeekBar.setProgress(currPage);

					if (mHasNextPage) {
						mPostsView.setMode(mPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
					} else {
						mPostsView.setMode(mPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
					}

					for (Post post : posts) {
						if (post.getId() == 1) mThreadUserId = post.getAuthor().getId();
						post.setIsLz(post.getAuthor().getId() == mThreadUserId);
					}


					if (totalPage > 1) {
						mSeekBar.setMax(totalPage);
						mSeekBar.setProgress(currPage);
					} else {
						mSeekBar.setVisibility(View.GONE);
					}

					mPage = currPage;

					if (mPid == -1) {
						mPid = mPosts.get(mPosts.size() - 1).getId();
					}

					// Locate to the specified post.
					if (mPid > 0) {
						mWebView.loadUrl("javascript:scrollToPost(" + mPid + ")");
						Logger.i("Scrolling to pid-%d", mPid);
						mPid = 0;
					}

					if (mInitToLastPost) {
						mInitToLastPost = false;
						mWebView.loadUrl("javascript:scrollToPost(" + posts.get(posts.size() - 1).getId() + ")");
					}
				}
			}.execute(html);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (requestCode == EDIT_CODE && resultCode == ActivityEdit.EDIT_SUCCESS) {
			mIsFetching = true;

			if (returnIntent != null) {
				String html = returnIntent.getStringExtra("html");
				mPosts.clear();
				mPid = -1;
				mSkipResumeFetch = true;
				onHtml(html);
			}
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;

		getMenuInflater().inflate(R.menu.posts, menu);

		menu.findItem(R.id.more)
				.setIcon(new IconDrawable(this, MaterialIcons.md_more_vert)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize())
				.setVisible(!model.isOnSelection());

		menu.findItem(R.id.ok)
				.setIcon(new IconDrawable(this, MaterialIcons.md_check)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize())
				.setVisible(model.isOnSelection());

		menu.findItem(R.id.cancel)
				.setIcon(new IconDrawable(this, MaterialIcons.md_close)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize())
				.setVisible(model.isOnSelection());

		return super.onCreateOptionsMenu(menu);
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
			fetch(seekBar.getProgress());
			mMenuDialog.dismiss();
		}
	}

	@JavascriptInterface
	public void onPostClick(final int pid) {
		User me = mLocalApi.getUser();

		if (me == null || me.getId() == 0) {
			showToast(getResources().getString(R.string.error_login_required));
		} else {
			startEditActivity((Post) CollectionUtils.find(mPosts, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ((Post) o).getId() == pid;
				}
			}));
		}

	}

	private void startEditActivity(Post post) {
		if (myid == 0) return;

		int uid = post.getAuthor().getId();
		int postIndex = post.getPostIndex();
		Intent intent = new Intent(this, ActivityEdit.class);
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

	public boolean onLinkClick(String href) {
		Uri uri = Uri.parse(href);

		if ("www.hi-pda.com".equals(uri.getHost())) {
			String fid = uri.getQueryParameter("fid"),
					tid = uri.getQueryParameter("tid"),
					pid = uri.getQueryParameter("pid"),
					page = uri.getQueryParameter("page");

			if (tid == null)
				tid = uri.getQueryParameter("ptid");

			int iTid = Utils.parseInt(tid);
			int iFid = Utils.parseInt(fid);
			final int iPid = Utils.parseInt(pid);
			int iPage = Utils.parseInt(page);

			if (iPage == 0) iPage = 1;

			if (iTid != 0) {
				if (iTid == mTid && CollectionUtils.exists(mPosts, new Predicate() {
					@Override
					public boolean evaluate(Object o) {
						return iPid == ((Post) o).getId();
					}
				})) {
					mWebView.post(new Runnable() {
						@Override
						public void run() {
							mWebView.loadUrl("javascript:scrollToPost(" + iPid + ")");
						}
					});
				} else {
					Intent intent = new Intent(this, this.getClass());
					intent.putExtra("tid", iTid);
					intent.putExtra("fid", iFid);
					intent.putExtra("pid", iPid);
					intent.putExtra("page", iPage);
					startActivity(intent);
				}

				return true;
			}
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(uri);
		startActivity(intent);
		return true;
	}

	@Override
	public void onPullEvent(PullToRefreshBase refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
		Logger.d("PL %s", state);

		if (state == PullToRefreshBase.State.RESET) {
			getSlidrInterface().unlock();
			Logger.d("unlock");


			if (direction == PullToRefreshBase.Mode.PULL_FROM_END) {
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
			getSlidrInterface().lock();
			Logger.d("lock");


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

	@Override
	public void onPullDownToRefresh(PullToRefreshBase refreshView) {
		fetch(mPosts.getPage() - 1);
		position = 0;
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase refreshView) {
		fetch(mPosts.getPage() + 1);
		position = 0;
	}

	@Override
	public void onWebViewReady() {
		super.onWebViewReady();

		mWebViewReady = true;

		if (!mIsPushedAfterWebReady) {
			for (Post post : mPosts) {
				mWebView.loadUrl("javascript:_postsData.posts.push(" + JSON.toJSONString(post) + ")");
				mIsPushedAfterWebReady = true;
			}
		}

		mWebView.loadUrl("javascript:var s=_postsData.postsStyle;s.theme='" + setting.getString("theme", DefaultTheme) + "';" +
				"s.fontsize='" + setting.getString("font_size", "normal") + "';" +
				"s.showSig=" + setting.getBoolean("show_sig", false));
	}

	private class Model {
		private boolean isOnSelection;
		private boolean toRender;
		private String title;

		public String getUrl() {
			return url;
		}

		public Model setUrl(String url) {
			this.url = url;
			mWebView.loadUrl("javascript:_postsData.url='" + url + "'");
			return this;
		}

		private String url;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
			mWebView.loadUrl("javascript:_postsData.title='" + title + "'");
			mTitleView.setText(title);
		}

		public boolean isOnSelection() {
			return isOnSelection;
		}

		public Model setOnSelection(boolean onSelection) {
			isOnSelection = onSelection;
			mWebView.loadUrl("javascript:_postsData.postsStyle.selection=" + onSelection);
			invalidateOptionsMenu();
			return this;
		}

		public boolean isToRender() {
			return toRender;
		}

		public Model setToRender(boolean toRender) {
			this.toRender = toRender;
			mWebView.loadUrl("javascript:_postsData.prepareRender=" + toRender);
			return this;
		}
	}

	class OnListChangedCallback extends ObservableList.OnListChangedCallback {

		@Override
		public void onChanged(ObservableList sender) {

		}

		@Override
		public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {

		}

		@Override
		public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
			Logger.i("onItemRangeInserted, positionStart %d itemCount %d", positionStart, itemCount);
			// Assume that only PUSH operation inserts posts.
			if (mWebViewReady) {
				for (int i = mIsPushedAfterWebReady ? positionStart : 0; i < positionStart + itemCount; i++) {
					Post post = (Post) sender.get(i);
					mWebView.loadUrl("javascript:_postsData.posts.push(" + JSON.toJSONString(post) + ")");
					mIsPushedAfterWebReady = true;
				}
			}
		}

		@Override
		public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {

		}

		@Override
		public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
			Logger.i("onItemRangeRemoved, positionStart %d itemCount %d", positionStart, itemCount);
			mWebView.loadUrl("javascript:_postsData.posts.splice(" + positionStart + ", " + itemCount + ")");
		}
	}
}