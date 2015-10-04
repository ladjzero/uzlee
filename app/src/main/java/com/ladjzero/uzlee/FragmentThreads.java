package com.ladjzero.uzlee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Core.OnThreadsListener;
import com.ladjzero.hipda.Thread;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.orhanobut.logger.Logger;
import com.r0adkll.slidr.model.SlidrInterface;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

public class FragmentThreads extends Fragment implements OnRefreshListener, AdapterView.OnItemClickListener, OnThreadsListener {

	public static final int DATA_SOURCE_THREADS = 0;
	public static final int DATA_SOURCE_USER = 1;
	public static final int DATA_SOURCE_SEARCH = 2;
	private static final String TAG = "FragmentThreads";

	private ActivityBase mActivity;
	private final ArrayList<Thread> mThreads = new ArrayList<Thread>();
	private SwipeRefreshLayout mSwipe;
	private ListView listView;
	private ThreadsAdapter adapter;
	private boolean hasNextPage = false;
	private int fid;
	private boolean mIsAnimating = false;
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
	private View mTitleView;
	private SlidrInterface slidrInterface;
	private boolean mRenderOnCreate = true;
	private boolean mVisible;

	public interface OnFetch {
		void fetchStart();

		void fetchEnd();
	}

	public static FragmentThreads newInstance(Bundle bundle) {
		int fid = bundle.getInt("fid", ActivityMain.D_ID);
		typeId = bundle.getInt("bs_type_id", 0);

		FragmentThreads fragment = new FragmentThreads();
		Bundle args = new Bundle();
		args.putBoolean("enablePullToRefresh", true);
		args.putInt("fid", fid);
		fragment.setArguments(args);
		fragment.fid = fid;
		fragment.mRenderOnCreate = bundle.getBoolean("renderOnCreate", true);

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
		adapter.notifyDataSetChanged();
		fetch(1, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (ActivityBase) getActivity();
//		mTitleView = mActivity.mTitleView;

		if (mTitleView != null) {
			mTitleView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listView != null) listView.setSelection(0);
				}
			});
		}

		Bundle args = getArguments();
		mDataSource = args.getInt("dataSource");
		mUserName = args.getString("userName");
		mEnablePullToRefresh = args.getBoolean("enablePullToRefresh");
		mTitle = args.getString("title");
		if (mTitle != null) mActivity.setTitle(mTitle);
		mQuery = args.getString("query");

		View rootView = inflater.inflate(R.layout.threads_can_refresh, container, false);

		mSwipe = (SwipeRefreshLayout) rootView.findViewById(R.id.thread_swipe);
		mSwipe.setOnRefreshListener(this);
		mSwipe.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);

		Logger.i("enable pull to fresh %b", mEnablePullToRefresh);
		mSwipe.setEnabled(mEnablePullToRefresh);

		listView = (ListView) rootView.findViewById(R.id.threads);

		if (mActivity instanceof ActivityThreads) {
			slidrInterface = ((ActivityThreads)mActivity).slidrInterface;
		} else if (mActivity instanceof ActivitySearch) {
			slidrInterface = ((ActivitySearch)mActivity).slidrInterface;
		}

		adapter = new ThreadsAdapter(mActivity, mThreads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		listView.setOnScrollListener(
				new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new EndlessScrollListener() {
					@Override
					public void onLoadMore(int page, int totalItemsCount) {
						if (hasNextPage) {
							mActivity.showToast("载入下一页");

							setRefreshSpinner(true);
							if (mDataSource == DATA_SOURCE_USER) {
								Core.getUserThreadsAtPage(mUserName, page, FragmentThreads.this);
							} else if (mDataSource == DATA_SOURCE_SEARCH) {
								Core.search(mQuery, page, FragmentThreads.this);
							} else {
								fetch(page, FragmentThreads.this);
							}
						}
					}
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
						if (slidrInterface != null) {
							if (scrollState == SCROLL_STATE_IDLE) {
								slidrInterface.unlock();
							} else {
								slidrInterface.lock();
							}
						}
					}

				}));

		registerForContextMenu(listView);
		return rootView;
	}

	private String getOrder() {
		int i = Integer.parseInt(mActivity.setting.getString("sort_thread", "2"));

		switch (i) {
			case 1:
				return "dateline";
			default:
				return "lastpost";
		}
	}

	private void fetch(int page, final OnThreadsListener onThreadsListener) {
		mIsFetching = true;

		setRefreshSpinner(true);

		if (mDataSource == DATA_SOURCE_USER) {
			Core.getUserThreadsAtPage(mUserName, page, this);
		} else if (mDataSource == DATA_SOURCE_SEARCH) {
			if (mQuery != null && mQuery.length() > 0)
				Core.search(mQuery, page, FragmentThreads.this);
		} else {
			Core.getHtml("http://www.hi-pda.com/forum/forumdisplay.php?fid=" + getArguments().getInt("fid") + "&page=" + page + "&filter=type&typeid=" + typeId + "&orderby=" + getOrder(), new Core.OnRequestListener() {
				@Override
				public void onError(String error) {
					onThreadsListener.onError(error);

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
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		mVisible = isVisibleToUser;

		if (mVisible && getView() != null) {
			if (mThreads.size() == 0 && mDataSource != DATA_SOURCE_SEARCH) fetch(1, this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mVisible && mThreads.size() == 0 && mDataSource != DATA_SOURCE_SEARCH) fetch(1, this);
		adapter.notifyDataSetChanged();

		mSwipe.setEnabled(mEnablePullToRefresh);

		EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
			super.onPause();
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

		Intent intent = new Intent(mActivity, ActivityPosts.class);
		intent.putExtra("fid", fid);
		intent.putExtra("tid", t.getId());
		intent.putExtra("title", t.getTitle());
		intent.putExtra("pid", t.getToFind());
		intent.putExtra("uid", t.getAuthor().getId());

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
		public void onError (String error){
			mActivity.showToast(error);
		}

		@Override
		public void onRefresh () {
			fetch(1, new OnThreadsListener() {
				@Override
				public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
					mIsFetching = false;
					FragmentThreads.this.hasNextPage = hasNextPage;
					FragmentThreads.this.mThreads.clear();
					FragmentThreads.this.mThreads.addAll(threads);
					adapter.notifyDataSetChanged();
					setRefreshSpinner(false);
				}

				@Override
				public void onError(String error) {
				mIsFetching = false;
				setRefreshSpinner(false);
				mActivity.showToast(error);
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(0, 1, 0, "复制标题");
		menu.add(0, 2, 0, "查看最新回复");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (getUserVisibleHint()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			Thread thread = adapter.getItem(info.position);

			switch (item.getItemId()) {
				case 1:

					ClipboardManager clipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
					StringBuilder builder = new StringBuilder();

					ClipData clipData = ClipData.newPlainText("post content", thread.getTitle());
					clipboardManager.setPrimaryClip(clipData);
					mActivity.showToast("复制到剪切版");
					break;
				case 2:

					Intent intent = new Intent(mActivity, ActivityPosts.class);
					intent.putExtra("tid", thread.getId());
					intent.putExtra("page", 9999);
					intent.putExtra("title", thread.getTitle());
					intent.putExtra("uid", thread.getAuthor().getId());

					startActivity(intent);
			}

			return super.onContextItemSelected(item);
		} else {
			return false;
		}
	}

	private void setRefreshSpinner(boolean visible) {
		Logger.i("visible %b, enable refresh %b, is fetching %b", visible, mEnablePullToRefresh, mIsFetching);

		if (visible) {
			if (mOnFetch != null) mOnFetch.fetchStart();

			if (mEnablePullToRefresh) {
				// Hack. http://stackoverflow.com/questions/26858692/swiperefreshlayout-setrefreshing-not-showing-indicator-initially
				mSwipe.postDelayed(new Runnable() {
					@Override
					public void run() {
						Logger.i("is fetching %b", mIsFetching);

						if (mIsFetching) mSwipe.setRefreshing(true);
					}
				}, 100);
			} else {
				mActivity.setProgressBarIndeterminateVisibility(true);
			}
		} else {
			if (mOnFetch != null) mOnFetch.fetchEnd();

			if (mEnablePullToRefresh) {
				mSwipe.setRefreshing(false);
			}
		}
	}

	public void onEventMainThread(Core.UserEvent userEvent) {
		Logger.i("EventBus.onEventMainThread.statusChangeEvent : user is null? %b", userEvent.user == null);

		if (userEvent.user == null) {
			mThreads.clear();
			adapter.notifyDataSetChanged();
		}
	}
}
