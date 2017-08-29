package com.ladjzero.uzlee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableList;
import android.graphics.Bitmap;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.ladjzero.hipda.entities.Post;
import com.ladjzero.hipda.entities.Posts;
import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.uzlee.model.ObservablePosts;
import com.ladjzero.uzlee.service.Api;
import com.ladjzero.uzlee.utils.CapturePhotoUtils;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.NotificationUtils;
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

import static com.ladjzero.uzlee.App.*;


public class ActivityPosts extends ActivityWithWebView implements AdapterView.OnItemClickListener,
		DiscreteSeekBar.OnProgressChangeListener,
		PullToRefreshBase.OnPullEventListener,
		PullToRefreshBase.OnRefreshListener2,
		TextWatcher {

	private static final String TAG = "ActivityPosts";
	private final int EDIT_CODE = 99;
	AdapterMenuItem actionsAdapter;
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
	// Never assign new value to mPosts!!
	private ObservablePosts mPosts;
	private WebView2 mWebView;
	private DiscreteSeekBar mSeekBar;
	private Menu mMenu;
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
	private boolean mSkipResumeFetch = false;
	private boolean mIsPushedAfterWebReady = false;
	private LocalApi mLocalApi;
	private Timeline mTimeline = new Timeline();
	private Model model = new Model();
	private long ms;

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		boolean on = count + start >= 5;

		mQuickSend.setTextColor(Utils.getThemeColor(this, on ? R.attr.colorPrimary : R.attr.colorBackgroundSecondary));
		mQuickSend.setClickable(on);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	// Menu item click.
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (getInstance().getCore().getApiStore().getUser().getId() == 0) {
			switch (position) {
				case 0:
					fetch(mPage);
					break;
				case 1:
					ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clipData = ClipData.newPlainText("post url", getUri(mPosts.getMeta().getPage()));
					clipboardManager.setPrimaryClip(clipData);
					showToast("复制到剪切版");
					break;
				case 2:
					model.setOnSelection(true);
					break;
				case 3:
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getUri(mPosts.getMeta().getPage())));
					startActivity(intent);
					break;
			}
		} else {
			switch (position) {
				case 0:
					Intent replyIntent = new Intent(this, ActivityEdit.class);
					replyIntent.putExtra("tid", mTid);
					replyIntent.putExtra("title", "回复主题");
					replyIntent.putExtra("hideTitleInput", true);
					startActivityForResult(replyIntent, EDIT_CODE);
					break;
				case 1:
					fetch(mPage);
					break;
				case 2:
					App.getInstance().getApi().addToFavorite(mTid, new Api.OnRespond() {
						@Override
						public void onRespond(Response res) {
							if (res.isSuccess()) {
								String content = (String) res.getData();

								if (content.equals("您曾经收藏过这个主题")) {
									final Dialog dialog = new Dialog(ActivityPosts.this);

									dialog.setTitle("已经收藏过该主题");
									dialog.setCanceledOnTouchOutside(true);
									dialog.positiveAction("移除收藏").positiveActionClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
											App.getInstance().getApi().removeFromFavoriate(mTid, new Api.OnRespond() {
												@Override
												public void onRespond(Response res) {
													showToast(res.getData().toString());
												}
											});
										}
									});

									dialog.show();
								}
							} else {
								showToast(res.getData().toString());
							}
						}
					});
					break;
				case 3:
					ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clipData = ClipData.newPlainText("post url", getUri(mPosts.getMeta().getPage()));
					clipboardManager.setPrimaryClip(clipData);
					showToast("复制到剪切版");
					break;
				case 4:
					model.setOnSelection(true);
					break;
				case 5:
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getUri(mPosts.getMeta().getPage())));
					startActivity(intent);
					break;
			}
		}

		mMenuDialog.dismiss();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	private void fetch(int page) {
		Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());

		model.setFetching(true);


		try {
			myid = mLocalApi.getUser().getId();
		} catch (Exception e) {
		}

		mPosts.clear();

		String url = getUri(page);
		model.setUrl(url);

		getInstance().getApi().getPosts(url, new Api.OnRespond() {
			@Override
			public void onRespond(Response res) {
				if (res.isSuccess()) {
					Posts posts = (Posts) res.getData();
					mPosts.clear();
					mPosts.addAll(posts);
					mPosts.setMeta(posts.getMeta());
					model.setTitle(posts.getMeta().getTitle());
				} else {
					showToast(res.getData().toString());
				}

				model.setFetching(false);
			}
		});
	}

	private String getUri(int page) {
		if (mPid > 0) {
			return String.format("http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=%d&ptid=%d", mPid, mTid);
		} else {
			return "http://www.hi-pda.com/forum/viewthread.php?tid=" + mTid + "&page=" + page;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_posts);
		ButterKnife.bind(this);

		if (getInstance().getCore().getApiStore().getUser().getId() == 0) {
			mQuickReplyLayout.setVisibility(View.INVISIBLE);
		}

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

		model.setTitle(intent.getStringExtra("title"));

		mWebView = (WebView2) mPostsView.getRefreshableView();

		mPostsView.setOnRefreshListener(this);
		mPostsView.setOnPullEventListener(this);


		mMenuView = getLayoutInflater().inflate(R.layout.posts_actions_dialog, null);

		mSeekBar = (DiscreteSeekBar) mMenuView.findViewById(R.id.seekbar);
		mSeekBar.setMin(1);
		mSeekBar.setOnProgressChangeListener(this);

		mMenuDialog = new Dialog(this);
		ListView menuList = (ListView) mMenuView.findViewById(R.id.actions);

		if (getInstance().getCore().getApiStore().getUser().getId() == 0) {
			actionsAdapter = new AdapterMenuItem(this, new String[]{
					"刷新",
					"复制链接",
					"截图",
					"从浏览器打开"
			}, new String[]{
					"{md-refresh}",
					"{md-link}",
					"{md-crop}",
					"{md-open-in-browser}"
			});
		} else {
			actionsAdapter = new AdapterMenuItem(this, new String[]{
					"回复",
					"刷新",
					"收藏",
					"复制链接",
					"截图",
					"从浏览器打开"
			}, new String[]{
					"{md-reply}",
					"{md-refresh}",
					"{md-bookmark}",
					"{md-link}",
					"{md-crop}",
					"{md-open-in-browser}"
			});
		}


		menuList.setAdapter(actionsAdapter);
		menuList.setOnItemClickListener(this);

		mMenuDialog.title("")
				.titleColor(Utils.getThemeColor(this, R.attr.colorText))
				.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
				.negativeAction("取消")
				.negativeActionClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mMenuDialog.dismiss();
					}
				})
				.contentView(mMenuView)
				.canceledOnTouchOutside(true);


		mPosts = new ObservablePosts();
		mPosts.addOnListChangedCallback(new OnListChangedCallback());

		mLocalApi = getInstance().getCore().getLocalApi();
		mProgressView.start();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPosts.size() == 0 && !mSkipResumeFetch && !model.isFetching()) {
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

		if (!model.isFetching()) {
			if (id == R.id.more) {
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
				return true;
			}
		}

		if (id == R.id.ok) {
			if (model.getMessage() == null || model.getMessage().length() == 0) {
				showToast("没有选择帖子");
				return true;
			} else {
				model.setToRender(true).setOnSelection(false).setMessage(null);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Bitmap bitmap = mWebView.toBitmap();

						if (bitmap == null) {
							showToast("图片生成失败，请减少帖子数量");
						} else {
							String url = CapturePhotoUtils.insertImage(getContentResolver(), bitmap, "webview", "webview");
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.parse(url), "image/*");
							NotificationUtils.Notification noti = new NotificationUtils.Notification();
							noti.intent = intent;
							noti.title = "已捕获帖子截图";
							noti.text = "点击打开";
							new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
							NotificationUtils.nofity(ActivityPosts.this, noti);
						}

						model.setToRender(false);
					}
				}, 600);
			}
		}

		if (id == R.id.cancel) {
			model.setToRender(false).setOnSelection(false).setMessage(null);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		if (keyCode == KeyEvent.KEYCODE_MENU && (ev == null || ev.getAction() == MotionEvent.ACTION_DOWN) && !model.isFetching()) {
			mMenuDialog.show();

			return true;
		}

		return super.onKeyDown(keyCode, ev);
	}

	public void onQuickReply(View v) {
		mQuickEdit.setEnabled(false);
		mQuickSend.setText("{md-refresh spin}");
		mQuickSend.setClickable(false);

		String reply = mQuickEdit.getText().toString();

		if (getSettings().getBoolean("use_sig", false)) {
			reply += "\t\t\t[size=1][color=Gray]有只梨[/color][/size]";
		}

		App.getInstance().getApi().sendReply(mTid, reply, null, null, new Api.OnRespond() {
			@Override
			public void onRespond(Response res) {
				if (res.isSuccess()) {
					mQuickSend.setClickable(true);
					mQuickEdit.setEnabled(true);
					mQuickSend.setText("{md-send}");

					Posts posts = (Posts) res.getData();
					mPosts.clear();
					mQuickEdit.setText("");
					mPid = -1;
					mPosts.addAll(posts);
				} else {
					showToast(res.getData().toString());
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (requestCode == EDIT_CODE && resultCode == ActivityEdit.EDIT_SUCCESS) {
			model.setFetching(true);

			if (returnIntent != null) {
				String html = returnIntent.getStringExtra("posts-html");
				Posts posts = (Posts) getInstance().getApi().getPostsParser().parse(html).getData();
				mPosts.clear();
				mPosts.addAll(posts);
				mPid = -1;
				mSkipResumeFetch = true;
			}

			model.setFetching(false);
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
		if (mPosts.getMeta().getPage() != seekBar.getProgress()) {
			fetch(seekBar.getProgress());
			mMenuDialog.dismiss();
		}
	}

	@JavascriptInterface
	public void onPostClick(final int pid) {
		if (getWebView().finishActionMode()) return;

		if (ms == 0) {
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

		ms = System.currentTimeMillis();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() - ms >= 1000) ms = 0;
			}
		}, 1000);
	}

	private void startEditActivity(Post post) {
		if (myid == 0) return;

		int uid = post.getAuthor().getId();
		int postIndex = post.getPostIndex();
		Intent intent = new Intent(this, ActivityEdit.class);
		intent.putExtra("title", myid == uid ? "编辑" : "回复" + (postIndex == 1 ? "楼主" : postIndex + "楼"));

		if (myid == uid) {
			intent.putExtra("fid", mPosts.getMeta().getFid());
		}

		intent.putExtra("pid", post.getId());
		intent.putExtra("tid", mTid);
		intent.putExtra("uid", uid);
		intent.putExtra("no", postIndex);
		intent.putExtra("userName", post.getAuthor().getName());
		intent.putExtra("hideTitleInput", postIndex != 1);
		startActivityForResult(intent, EDIT_CODE);
	}

	@JavascriptInterface
	public void onLinkClick(String href) {
		if (getWebView().finishActionMode()) return;

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

				return;
			}
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(uri);
		startActivity(intent);
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
		fetch(mPosts.getMeta().getPage() - 1);
		position = 0;
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase refreshView) {
		fetch(mPosts.getMeta().getPage() + 1);
		position = 0;
	}

	@Override
	public void onWebViewReady() {
		super.onWebViewReady();

		model.setWebviewReady(true);

		if (!mIsPushedAfterWebReady) {
			for (Post post : mPosts) {
				mWebView.loadUrl("javascript:_postsData.posts.push(" + JSON.toJSONString(post) + ")");
				mIsPushedAfterWebReady = true;
			}
		}

		mWebView.loadUrl("javascript:var s=_postsData.postsStyle;s.theme='" + setting.getString("theme", DefaultTheme) + "';" +
				"s.fontsize='" + setting.getString("font_size", "normal") + "';" +
				"s.showSig=" + setting.getBoolean("show_sig", false) + ";" +
				"s.showProfileImage=" + setting.getBoolean(Constants.PREF_KEY_SHOW_PROFILE_IMAGE, true));
	}

	@JavascriptInterface
	public void onSelect(String selection) {
		model.setMessage(selection);
	}

	protected class Model {
		private boolean webviewReady;
		private boolean isOnSelection;
		private float parsingPercentage;
		private boolean isFetching;
		private boolean isParsing;
		private boolean toRender;
		private String title;
		private String message;
		private String url;

		public boolean isWebviewReady() {
			return webviewReady;
		}

		public void setWebviewReady(boolean webviewReady) {
			this.webviewReady = webviewReady;
			setTitle(title);
			setUrl(url);
		}

		public float getParsingPercentage() {
			return parsingPercentage;
		}

		public void setParsingPercentage(float parsingPercentage) {
			this.parsingPercentage = parsingPercentage;
			mProgressView.setProgress(parsingPercentage);
		}

		public boolean isFetching() {
			return isFetching;
		}

		public void setFetching(boolean fetching) {
			isFetching = fetching;

			if (fetching) {
				mProgressView.setProgress(0f);
				mProgressView.start();
			} else {
				mPostsView.onRefreshComplete();

				if (mPosts.size() > 0) {
					int lastPostId = mPosts.get(mPosts.size() - 1).getId();

					if (mPid == -1) {
						mPid = lastPostId;
					}

					// Locate to the specified post.
					if (mPid > 0) {
						mWebView.loadUrl("javascript:scrollToPost(" + mPid + ")");
						Logger.i("Scrolling to pid-%d", mPid);
						mPid = 0;
					}

					if (mInitToLastPost) {
						mInitToLastPost = false;
						mWebView.loadUrl("javascript:scrollToPost(" + lastPostId + ")");
					}
				}

				int page = mPosts.getMeta().getPage();
				int totalPage = mPosts.getMeta().getTotalPage();

				if (mPosts.getMeta().isHasNextPage()) {
					mPostsView.setMode(page == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
				} else {
					mPostsView.setMode(page == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
				}

				if (totalPage > 1) {
					mSeekBar.setMax(totalPage);
					mSeekBar.setProgress(page);
				} else {
					mSeekBar.setVisibility(View.GONE);
				}

				mProgressView.stop();
			}
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getUrl() {
			return url;
		}

		public Model setUrl(String url) {
			this.url = url;
			if (webviewReady) mWebView.loadUrl("javascript:_postsData.url='" + url + "'");
			return this;
		}

		public String getTitle() {
			return title;
		}

		public Model setTitle(String title) {
			this.title = title;
			if (webviewReady) mWebView.loadUrl("javascript:_postsData.title='" + title + "'");
			mTitleView.setText(title);
			return this;
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

			if (toRender) {
				mWebView.loadUrl("javascript:_postsData.postsStyle.theme='dark'");
			} else {
				mWebView.loadUrl("javascript:_postsData.postsStyle.theme='" + setting.getString("theme", DefaultTheme) + "'");
				mWebView.loadUrl("javascript:_postsData.selected.splice(0, _postsData.selected.length)");
			}
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
			if (model.isWebviewReady()) {
				for (int i = mIsPushedAfterWebReady ? positionStart : 0; i < positionStart + itemCount; i++) {
					Post post = (Post) sender.get(i);
					Log.i("lalala", JSON.toJSONString(post));
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