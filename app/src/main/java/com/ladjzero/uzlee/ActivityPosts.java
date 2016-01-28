package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.webkit.WebView;
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
import com.ladjzero.hipda.User;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.rey.material.app.Dialog;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

import static com.ladjzero.hipda.Core.OnPostsListener;
import static com.ladjzero.hipda.Core.OnProgress;
import static com.ladjzero.hipda.Core.OnRequestListener;
import static com.ladjzero.hipda.Core.addToFavorite;
import static com.ladjzero.hipda.Core.getHtml;
import static com.ladjzero.hipda.Core.getUser;
import static com.ladjzero.hipda.Core.parsePosts;
import static com.ladjzero.hipda.Core.removeFromFavoriate;
import static com.ladjzero.hipda.Core.sendReply;


public class ActivityPosts extends ActivityWithWebView implements AdapterView.OnItemClickListener,
		OnPostsListener,
		DiscreteSeekBar.OnProgressChangeListener,
		PullToRefreshBase.OnPullEventListener,
		PullToRefreshBase.OnRefreshListener2,
		View.OnTouchListener,
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
	private View mSpinner;
	private int mThreadUserId = 0;
	private int mTid;
	private int mPage;
	private Posts mPosts = new Posts();
	private WebView mWebView;
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
	// Scroll to this post.
	private int mPid = 0;
	private boolean mQuickVisible = true;
	private boolean isFadingOut = false;
	private TextView mTitleView;
	private boolean mWebViewReady = false;

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
			case 1:
				orderType = orderType == 0 ? 1 : 0;
				fetch(1, ActivityPosts.this);
				// dismiss before data change, visual perfect
				mMenuDialog.dismiss();
				actionsAdapter.notifyDataSetChanged();
				break;
			case 2:
				mWebView.post(new Runnable() {
					@Override
					public void run() {
						mWebView.loadUrl("javascript:removeAll();");
					}
				});
				fetch(mPage, ActivityPosts.this);
				break;
			case 3:
				addToFavorite(mTid, new OnRequestListener() {
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

												removeFromFavoriate(mTid, new OnRequestListener() {
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
			case 4:
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clipData = ClipData.newPlainText("post url", getUri());
				clipboardManager.setPrimaryClip(clipData);
				showToast("复制到剪切版");
				break;
			case 5:
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getUri()));
				startActivity(intent);
				break;
		}

		mMenuDialog.dismiss();
	}

	private void fetch(int page, final OnPostsListener onPostsListener) {
		try {
			myid = getUser().getId();
		} catch (Exception e) {
		}

		mIsFetching = true;
		toogleSipnner(true);

		mPage = page;
		String url = getUri();

		Logger.i("Fetching: %s", url);

		getHtml(url, new OnRequestListener() {
			@Override
			public void onError(String error) {
				mIsFetching = false;
//				toggleMenus(true);
				toogleSipnner(false);

				onPostsListener.onError(error);
				mPostsView.onRefreshComplete();

			}

			@Override
			public void onSuccess(String html) {
				mPostsView.onRefreshComplete();

				mIsFetching = false;

				new AsyncTask<String, Object, Posts>() {
					@Override
					protected Posts doInBackground(String... strings) {
						return parsePosts(strings[0], new OnProgress() {
							@Override
							public void progress(int current, int total, Object post) {
								if (current == 1) publishProgress(current, total, post);
							}
						});
					}

					// Load the first post as soon as possible.
					@Override
					protected void onProgressUpdate(Object... objects) {
						Post post = (Post) objects[2];
						if (post.getId() == 1) mThreadUserId = post.getAuthor().getId();
						post.setIsLz(post.getAuthor().getId() == mThreadUserId);
					}

					@Override
					protected void onPostExecute(Posts posts) {
						mPosts = posts;
						mIsFetching = false;

						for (Post post : posts) {
							if (post.getId() == 1) {
								try {
									mThreadUserId = post.getAuthor().getId();
								} catch (Exception e) {
									mThreadUserId = 0;
								}
							}

							boolean isLz = false;

							try {
								isLz = post.getAuthor().getId() == mThreadUserId;
							} catch (Exception e) {

							}

							post.setIsLz(isLz);
						}

						toogleSipnner(false);
						onPostsListener.onPosts(posts);
					}
				}.execute(html);
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

	private void toogleSipnner(boolean on) {
		Utils.fadeOut(on ? mTitleView : mSpinner);
		Utils.fadeIn(on ? mSpinner : mTitleView);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_posts);
		ButterKnife.bind(this);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView = mInflater.inflate(R.layout.toolbar_title_for_post, null);
		mSpinner = customView.findViewById(R.id.spinner);
		mTitleView = (TextView) customView.findViewById(R.id.title);
		mTitleView.setVisibility(View.INVISIBLE);
		mTitleView.setTextColor(Utils.getThemeColor(this, R.attr.colorTextInverse));

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

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


		mWebView = mPostsView.getRefreshableView();

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
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPosts.size() == 0) {
			fetch(mPage, this);
		}
	}

	@Override
	public WebView getWebView() {
		return mWebView;
	}

	@Override
	public String getHTMLFilePath() {
		SharedPreferences setting = getSettings();

		return "file:///android_asset/posts.html" +
				"?theme=" + setting.getString("theme", DefaultTheme) +
				"&fontsize=" + setting.getString("font_size", "normal") +
				"&showsig=" + setting.getBoolean("show_sig", false);
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
		showToast("send");
		mQuickEdit.setEnabled(false);
		mQuickSend.setText("{md-refresh spin}");
		mQuickSend.setClickable(false);

		String reply = mQuickEdit.getText().toString();

		if (getSettings().getBoolean("use_sig", false)) {
			reply += "[size=1][color=Gray]有只梨[/color][/size]";
		}

		sendReply(mTid, reply, null, null, new OnRequestListener() {
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
				mQuickEdit.setText("");
				onHtml(html);
			}
		});
	}

	private void onHtml(String html) {
		if (html != null && html.length() > 0) {
			new AsyncTask<String, Float, Posts>() {
				@Override
				protected Posts doInBackground(String... strings) {
					return parsePosts(strings[0], null);
				}

				@Override
				protected void onPostExecute(Posts posts) {
					mIsFetching = false;
					mPosts = posts;

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

					commitPosts(posts);
					mPosts = posts;
				}
			}.execute(html);
		}
	}

	@Override
	public void onPosts(final Posts posts) {
		mTitleView.setText(posts.getTitle());

		for (Post p : posts) {
			String timeStr = p.getTimeStr();
			timeStr = Utils.prettyTime(timeStr);
			p.setTimeStr(timeStr);
		}

		int totalPage = posts.getTotalPage(),
				currPage = posts.getPage();

		if (totalPage > 1) {
			mSeekBar.setMax(totalPage);
			mSeekBar.setProgress(currPage);
		} else {
			mSeekBar.setVisibility(View.GONE);
		}

		mHasNextPage = totalPage > currPage;
		mPage = currPage;

		if (mHasNextPage) {
			mPostsView.setMode(currPage == 1 ? PullToRefreshBase.Mode.PULL_FROM_END : PullToRefreshBase.Mode.BOTH);
		} else {
			mPostsView.setMode(currPage == 1 ? PullToRefreshBase.Mode.DISABLED : PullToRefreshBase.Mode.PULL_FROM_START);
		}

		commitPosts(posts);

		// Locate to the specified post.
		if (mPid != 0) {
			mWebView.loadUrl("javascript:scrollToPost(" + mPid + ")");
			mPid = 0;
		}

		if (mInitToLastPost) {
			mInitToLastPost = false;
			mWebView.loadUrl("javascript:scrollToPost(" + posts.get(posts.size() - 1).getId() + ")");
		}
	}

	@Override
	public void onError(String error) {
		showToast(error);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (requestCode == EDIT_CODE && resultCode == ActivityEdit.EDIT_SUCCESS) {
			mIsFetching = true;

			if (returnIntent != null) {
				String html = returnIntent.getStringExtra("html");
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
						.actionBarSize());

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
			fetch(seekBar.getProgress(), ActivityPosts.this);
			mMenuDialog.dismiss();
		}
	}

	@JavascriptInterface
	public void onProfileClick(int uid, String name) {
		User me = Core.getUser();

		if (me == null || me.getId() == 0) {
			showToast(getResources().getString(R.string.error_login_required));
		} else {
			showToast("user id is " + uid);

			Intent intent = new Intent(this, ActivityUser.class);
			intent.putExtra("uid", uid);
			intent.putExtra("name", name);
			startActivity(intent);
		}
	}

	@JavascriptInterface
	public void onPostClick(final int pid) {
		User me = Core.getUser();

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

	@JavascriptInterface
	public void onLinkClick(String href) {
//		showToast(href);
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
		mWebView.post(new Runnable() {
			@Override
			public void run() {
				mWebView.loadUrl("javascript:removeAll();");
			}
		});

		fetch(mPosts.getPage() - 1, ActivityPosts.this);
		position = 0;
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase refreshView) {
		mWebView.post(new Runnable() {
			@Override
			public void run() {
				mWebView.loadUrl("javascript:removeAll();");
			}
		});

		fetch(mPosts.getPage() + 1, ActivityPosts.this);
		position = 0;
	}

	@Override
	public void onWebViewReady() {
		super.onWebViewReady();

		mWebViewReady = true;
		commitPosts(mPosts);
	}

	private void commitPosts(final Posts posts) {
		if (mWebViewReady) {
			mWebView.post(new Runnable() {
				@Override
				public void run() {
					mWebView.loadUrl("javascript:removeAll();");

					for (Post post : posts) {
						mWebView.loadUrl("javascript:addPost(" + JSON.toJSONString(post) + ")");
					}
				}
			});
		}
	}
}