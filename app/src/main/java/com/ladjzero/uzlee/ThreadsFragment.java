package com.ladjzero.uzlee;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Core.OnThreadsListener;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;
import com.nineoldandroids.animation.Animator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class ThreadsFragment extends Fragment implements OnRefreshListener, AdapterView.OnItemClickListener, OnThreadsListener {

	public static final int DATA_SOURCE_THREADS = 0;
	public static final int DATA_SOURCE_USER = 1;
	public static final int DATA_SOURCE_SEARCH = 2;

	private BaseActivity mActivity;
	private final ArrayList<Thread> mThreads = new ArrayList<Thread>();
	private SwipeRefreshLayout mSwipe;
	private DBHelper db;
	private Dao<Thread, Integer> threadDao;
	private Dao<User, Integer> userDao;
	private ListView listView;
	private ThreadsAdapter adapter;
	private boolean hasNextPage = false;
	private int fid;
	private View mGoTop;
	private boolean mIsAnimating = false;
	private boolean mGoTopVisible = false;
	private int mPage = 1;
	private boolean mIsFetching = false;
	private int mDataSource;
	// Search target user.
	private String mUserName;
	private boolean mEnablePullToRefresh;
	private String mTitle;
	private String mQuery;
	private OnFetch mOnFetch;
	private static int typeId = 0;

	public interface OnFetch{
		void fetchStart();
		void fetchEnd();
	}

	public static ThreadsFragment newInstance(Bundle bundle) {
		int fid = bundle.getInt("fid", MainActivity.D_ID);
		typeId = bundle.getInt("bs_type_id", 0);

		ThreadsFragment fragment = new ThreadsFragment();
		Bundle args = new Bundle();
		args.putBoolean("enablePullToRefresh", true);
		args.putInt("fid", fid);
		fragment.setArguments(args);
		fragment.fid = fid;

		return fragment;
	}

	public void setOnFetch(OnFetch onFetch) {
		mOnFetch = onFetch;
	}

	public void updateSearch(String query) {
		mThreads.clear();
		mQuery = query;
		fetch(1, this);
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
		mThreads.clear();
		fetch(1, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (BaseActivity) getActivity();

		Bundle args = getArguments();
		mDataSource = args.getInt("dataSource");
		mUserName = args.getString("userName");
		mEnablePullToRefresh = args.getBoolean("enablePullToRefresh");
		mTitle = args.getString("title");
		if (mTitle != null) mActivity.setTitle(mTitle);
		mQuery = args.getString("query");

		if (db == null) db = mActivity.getHelper();

		try {
			threadDao = db.getThreadDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		View rootView = inflater.inflate(R.layout.threads_can_refresh, container, false);

		mSwipe = (SwipeRefreshLayout) rootView.findViewById(R.id.thread_swipe);
		mSwipe.setOnRefreshListener(this);
		mSwipe.setColorSchemeResources(R.color.dark_primary, R.color.grape_primary, R.color.deep_primary, R.color.snow_dark);
		mSwipe.setEnabled(mEnablePullToRefresh);

		listView = (ListView) rootView.findViewById(R.id.threads);
		adapter = new ThreadsAdapter(mActivity, mThreads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {
					mActivity.showToast("载入下一页");

					setRefreshSpinner(true);
					if (mDataSource == DATA_SOURCE_USER) {
						Core.getUserThreadsAtPage(mUserName, page, ThreadsFragment.this);
					} else if (mDataSource == DATA_SOURCE_SEARCH) {
						Core.search(mQuery, page, ThreadsFragment.this);
					} else {
						fetch(page, ThreadsFragment.this);
					}
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				super.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);

				if (firstVisibleItem > 10 && !mGoTopVisible) {
					YoYo.with(Techniques.FadeIn)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mGoTop.setVisibility(View.VISIBLE);
									mGoTopVisible = true;
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
							.playOn(mGoTop);
				} else if (firstVisibleItem <= 10 && mGoTopVisible) {
					YoYo.with(Techniques.FadeOut)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mGoTop.setVisibility(View.GONE);
									mGoTopVisible = false;
									mIsAnimating = false;
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mGoTop);
				}
			}
		});

		mGoTop = rootView.findViewById(R.id.go_top);
		mGoTop.setVisibility(View.GONE);
		mGoTop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mGoTopVisible) listView.setSelection(0);
			}
		});

		registerForContextMenu(listView);
		return rootView;
	}

	private void fetch(int page, final OnThreadsListener onThreadsListener) {
		mIsFetching = true;

		setRefreshSpinner(true);

		if (mDataSource == DATA_SOURCE_USER) {
			Core.getUserThreadsAtPage(mUserName, page, this);
		} else if (mDataSource == DATA_SOURCE_SEARCH) {
			if (mQuery != null && mQuery.length() > 0) Core.search(mQuery, page, ThreadsFragment.this);
		} else {
			Core.getHtml("http://www.hi-pda.com/forum/forumdisplay.php?fid=" + getArguments().getInt("fid") + "&page=" + page + "&filter=type&typeid=" + typeId, new Core.OnRequestListener() {
				@Override
				public void onError(String error) {
					onThreadsListener.onError();

					setRefreshSpinner(false);

					mIsFetching = false;
				}

				@Override
				public void onSuccess(String html) {
					new AsyncTask<String, Void, Core.ThreadsRet>() {
						@Override
						protected Core.ThreadsRet doInBackground(String... strings) {
							return Core.parseThreads(strings[0]);
						}

						@Override
						protected void onPostExecute(Core.ThreadsRet ret) {
							setRefreshSpinner(false);

							onThreadsListener.onThreads(ret.threads, ret.page, ret.hasNextPage);
						}
					}.execute(html);
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mThreads.size() == 0 && mDataSource != DATA_SOURCE_SEARCH) fetch(1, this);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mThreads != null) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (Thread t : mThreads) {
				ids.add(t.getId());
			}
			outState.putIntegerArrayList("ids", ids);
		}
		outState.putInt("index", listView.getFirstVisiblePosition());
		View v = listView.getChildAt(0);
		outState.putInt("top", v == null ? 0 : v.getTop());
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Thread t = (Thread) adapterView.getAdapter().getItem(i);
		t.setNew(false);

		Intent intent = new Intent(mActivity, PostsActivity.class);
		intent.putExtra("fid", fid);
		intent.putExtra("tid", t.getId());
		intent.putExtra("title", t.getTitle());

		startActivity(intent);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		mPage = page;
		mIsFetching = false;
		setRefreshSpinner(false);

		final Collection<Integer> ids = CollectionUtils.collect(mThreads, new Transformer() {
			@Override
			public Object transform(Object o) {
				return ((Thread) o).getId();
			}
		});

		threads = (ArrayList<Thread>) CollectionUtils.selectRejected(threads, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return ids.contains(((Thread) o).getId());
			}
		});

		mThreads.addAll(threads);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onError() {
		mActivity.showToast("请求错误");
	}

	@Override
	public void onRefresh() {
		fetch(1, new OnThreadsListener() {
			@Override
			public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
				mIsFetching = false;
				ThreadsFragment.this.hasNextPage = hasNextPage;
				ThreadsFragment.this.mThreads.clear();
				ThreadsFragment.this.mThreads.addAll(threads);
				adapter.notifyDataSetChanged();
				setRefreshSpinner(false);
			}

			@Override
			public void onError() {
				mIsFetching = false;
				setRefreshSpinner(false);
				mActivity.showToast("请求错误");
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(0, 1, 0, "复制标题");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Thread thread = adapter.getItem(info.position);
		ClipboardManager clipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
		StringBuilder builder = new StringBuilder();

		ClipData clipData = ClipData.newPlainText("post content", thread.getTitle());
		clipboardManager.setPrimaryClip(clipData);
		mActivity.showToast("复制到剪切版");
		return super.onContextItemSelected(item);
	}

	class SaveData extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private void setRefreshSpinner(boolean visible) {
		if (visible) {
			if (mOnFetch != null) mOnFetch.fetchStart();

			if (mEnablePullToRefresh) {
				// Hack. http://stackoverflow.com/questions/26858692/swiperefreshlayout-setrefreshing-not-showing-indicator-initially
				mSwipe.post(new Runnable() {
					@Override
					public void run() {
						if (mIsFetching) mSwipe.setRefreshing(true);
					}
				});
			} else {
				mActivity.setProgressBarIndeterminateVisibility(true);
			}
		} else {
			if (mOnFetch != null) mOnFetch.fetchEnd();

			if (mEnablePullToRefresh) {
				mSwipe.setRefreshing(false);
			} else {
				mActivity.setProgressBarIndeterminateVisibility(false);
			}
		}
	}
}
