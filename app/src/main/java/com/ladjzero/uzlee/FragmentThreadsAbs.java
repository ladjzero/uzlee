package com.ladjzero.uzlee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class FragmentThreadsAbs extends Fragment implements
		AdapterView.OnItemClickListener,
		OnThreadsListener,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "FragmentThreadsAbs";

	private ActivityBase mActivity;
	protected ArrayList<Thread> mThreads;

	@Bind(R.id.thread_swipe)
	SwipeRefreshLayout mSwipe;
	@Bind(R.id.threads)
	ListView listView;
	@Bind(R.id.error_info)
	View errorInfo;

	protected AdapterThreads mAdapter;
	private boolean hasNextPage = false;
	private boolean mIsFetching = false;
	private boolean mEnablePullToRefresh;
	private OnFetch mOnFetch;
	private SlidrInterface slidrInterface;
	private OnScrollUpOrDown mOnScrollUpOrDown;

	@OnClick(R.id.toLogin)
	void login() {
		Utils.replaceActivity(getActivity(), ActivityLogin.class);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (mAdapter != null && ("font_size".equals(key) || "highlight_unread".equals(key))) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setScrollUpOrDownListener(OnScrollUpOrDown onScrollUpOrDown) {
		this.mOnScrollUpOrDown = onScrollUpOrDown;
	}

	public void setFetchListener(OnFetch onFetch) {
		mOnFetch = onFetch;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (ActivityBase) getActivity();

		LruCache<String, String> cache = ((MyApplication) getActivity().getApplication()).getMemCache();
		String cached = cache.get(keyOfThreadsToCache());
		List<Thread> threads = null;

		try {
			threads = JSON.parseArray(cached, Thread.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (threads == null) {
			mThreads = new ArrayList<>();
		} else {
			mThreads = new ArrayList<>(threads);
		}

		Bundle args = getArguments();

		mEnablePullToRefresh = args.getBoolean("enablePullToRefresh", true);

		View rootView = inflater.inflate(R.layout.threads_can_refresh, container, false);
		ButterKnife.bind(this, rootView);

		if (this instanceof OnRefreshListener) {
			mSwipe.setOnRefreshListener((OnRefreshListener) this);
		}

		mSwipe.setProgressBackgroundColorSchemeResource(
				mActivity.getThemeId() == R.style.AppBaseTheme_Night ?
						R.color.dark_light : android.R.color.white);

		int primaryColor = Utils.getThemeColor(getActivity(), R.attr.colorPrimary);
		mSwipe.setColorSchemeColors(primaryColor, primaryColor, primaryColor, primaryColor);
		mSwipe.setProgressViewOffset(false, -Utils.dp2px(mActivity, 12), Utils.dp2px(mActivity, 60));

		Logger.i("enable pull to fresh %b", mEnablePullToRefresh);
		mSwipe.setEnabled(mEnablePullToRefresh);


		if (mActivity instanceof ActivityUserThreads) {
			slidrInterface = ((ActivityUserThreads) mActivity).slidrInterface;
		} else if (mActivity instanceof ActivitySearch) {
			slidrInterface = ((ActivitySearch) mActivity).slidrInterface;
		}

		mAdapter = new AdapterThreads(mActivity, mThreads);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);

//        if (mDataSource != DATA_SOURCE_THREADS) {
//            listView.setPadding(0, 0, 0, 0);
//        }

		listView.setOnScrollListener(
				new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new DirectionDetectScrollListener()));

		registerForContextMenu(listView);
		return rootView;
	}

	@Override
	public void onDestroyView() {
		LruCache<String, String> cache = ((MyApplication) getActivity().getApplication()).getMemCache();
		String toCache = JSON.toJSONString(mThreads);

		if (toCache != null) {
			cache.put(keyOfThreadsToCache(), toCache);
		}

		super.onDestroyView();
	}

	interface OnScrollUpOrDown {
		void onUp(int ms);

		void onDown(int ms);
	}

	class DirectionDetectScrollListener extends EndlessScrollListener {
		private int mLastFirstVisibleItem = -1;
		private int mState = SCROLL_STATE_IDLE;

		@Override
		public void onLoadMore(int page, int totalItemsCount) {
			if (hasNextPage) {
				mActivity.showToast("载入下一页");

				setRefreshSpinner(true);
				fetch(page);
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

			mState = scrollState;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
							 int visibleItemCount, int totalItemCount) {

			super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

			if (mState == SCROLL_STATE_IDLE) return;

			if (mLastFirstVisibleItem != -1 && mOnScrollUpOrDown != null) {
				if (mLastFirstVisibleItem < firstVisibleItem && firstVisibleItem > 3) {
					mOnScrollUpOrDown.onDown(300);
				}
				if (mLastFirstVisibleItem > firstVisibleItem) {
					mOnScrollUpOrDown.onUp(300);
				}
			}

			mLastFirstVisibleItem = firstVisibleItem;

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.getSettings().registerOnSharedPreferenceChangeListener(this);
		mSwipe.setEnabled(mEnablePullToRefresh);
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.getSettings().unregisterOnSharedPreferenceChangeListener(this);
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
		intent.putExtra("fid", getArguments().getInt("fid"));
		intent.putExtra("tid", t.getId());
		intent.putExtra("title", t.getTitle());
		intent.putExtra("pid", t.getToFind());
		intent.putExtra("uid", t.getAuthor().getId());

		startActivity(intent);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		setRefreshSpinner(false);
		mIsFetching = false;

		if (threads.size() == 0) {
			errorInfo.setVisibility(View.VISIBLE);
		} else {
			errorInfo.setVisibility(View.GONE);

			this.hasNextPage = hasNextPage;
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
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onError(String error) {
		setRefreshSpinner(false);
		mActivity.showToast(error);
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
			Thread thread = mAdapter.getItem(info.position);

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

	public void fetch(int page) {
		setRefreshSpinner(true);
		fetchPageAt(page);
	}

	abstract void fetchPageAt(int page);

	protected void setRefreshSpinner(boolean visible) {
		Logger.i("visible %b, enable refresh %b, is fetching %b", visible, mEnablePullToRefresh, mIsFetching);

		if (visible) {
			if (mOnFetch != null) mOnFetch.fetchStart();

			// Hack. http://stackoverflow.com/questions/26858692/swiperefreshlayout-setrefreshing-not-showing-indicator-initially
			mSwipe.postDelayed(new Runnable() {
				@Override
				public void run() {
					Logger.i("is fetching %b", mIsFetching);
					mSwipe.setRefreshing(true);
				}
			}, 100);
		} else {
			if (mOnFetch != null) mOnFetch.fetchEnd();

			mSwipe.setRefreshing(false);
		}
	}

	protected abstract String keyOfThreadsToCache();
}

interface OnFetch {
	void fetchStart();

	void fetchEnd();
}