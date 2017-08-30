package com.ladjzero.uzlee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.ladjzero.hipda.entities.Thread;
import com.ladjzero.hipda.entities.Threads;
import com.ladjzero.uzlee.utils.Json;
import com.ladjzero.uzlee.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.orhanobut.logger.Logger;
import com.r0adkll.slidr.model.SlidrInterface;
import com.rey.material.app.Dialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class FragmentThreadsAbs extends FragmentBase implements
		AdapterView.OnItemClickListener,
		SharedPreferences.OnSharedPreferenceChangeListener,
		AdapterView.OnItemLongClickListener,
		ActivityBase.OnToolbarClickListener {

	private static final String TAG = "FragmentThreadsAbs";
	protected Threads mThreads;
	protected AdapterThreads mAdapter;
	@Bind(R.id.thread_swipe)
	SwipeRefreshLayout mSwipe;
	@Bind(R.id.threads)
	ListView listView;
	private ActivityBase mActivity;
	private boolean mEnablePullToRefresh;
	private SlidrInterface slidrInterface;
	private OnScrollUpOrDown mOnScrollUpOrDown;
	protected final Model model = new Model();

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

	public abstract int layout();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LruCache<String, String> cache = App.getInstance().getMemCache();
		String cached = cache.get(keyOfThreadsToCache());
		List<Thread> threads = null;

		try {
			threads = Json.fromJson(cached, new TypeToken<ArrayList<Thread>>(){}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}

		mThreads = new Threads();

		if (threads != null) {
			mThreads.addAll(threads);
		}
	}

	protected void onListViewReady(ListView listView, LayoutInflater inflater) {};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (ActivityBase) getActivity();

		Bundle args = getArguments();

		mEnablePullToRefresh = args.getBoolean("enablePullToRefresh", true);

		View rootView = inflater.inflate(layout(), container, false);
		ButterKnife.bind(this, rootView);
		onListViewReady(listView, inflater);

		if (this instanceof OnRefreshListener) {
			mSwipe.setOnRefreshListener((OnRefreshListener) this);
		}

		mSwipe.setProgressBackgroundColorSchemeColor(Utils.getThemeColor(mActivity, R.attr.colorTextInverse));
		int primaryColor = Utils.getThemeColor(mActivity, R.attr.colorPrimary);
		mSwipe.setColorSchemeColors(primaryColor, primaryColor, primaryColor, primaryColor);
//		mSwipe.setProgressViewOffset(false, -Utils.dp2px(mActivity, 12), Utils.dp2px(mActivity, 60));

		Logger.i("enable pull to fresh %b", mEnablePullToRefresh);
		mSwipe.setEnabled(mEnablePullToRefresh);


		if (mActivity instanceof ActivityUserThreads) {
			slidrInterface = ((ActivityUserThreads) mActivity).getSlidrInterface();
		} else if (mActivity instanceof ActivitySearch) {
			slidrInterface = ((ActivitySearch) mActivity).getSlidrInterface();
		}

		mAdapter = new AdapterThreads(mActivity, mThreads);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);

//        if (mDataSource != DATA_SOURCE_THREADS) {
//            listView.setPadding(0, 0, 0, 0);
//        }

		listView.setOnScrollListener(
				new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new DirectionDetectScrollListener()));


		mActivity.getSettings()
				.registerOnSharedPreferenceChangeListener(this);

		listView.setOnItemLongClickListener(this);
		registerForContextMenu(listView);

		return rootView;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final Thread thread = (Thread) parent.getItemAtPosition(position);
		final Dialog dialog = new Dialog(mActivity);
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.threads_actions_dialog, null);
		ListView listView = (ListView) v.findViewById(R.id.actions);
		listView.setDivider(null);
		listView.setAdapter(new ArrayAdapter<>(mActivity, R.layout.list_item_of_dialog, R.id.text, new String[]{"复制标题", "查看最新回复"}));
		dialog.negativeActionClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.title("")
				.titleColor(Utils.getThemeColor(mActivity, R.attr.colorText))
				.backgroundColor(Utils.getThemeColor(mActivity, android.R.attr.colorBackground))
				.contentView(v)
				.show();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
					case 0:
						ClipboardManager clipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
						StringBuilder builder = new StringBuilder();

						ClipData clipData = ClipData.newPlainText("post content", thread.getTitle());
						clipboardManager.setPrimaryClip(clipData);
						mActivity.showToast("复制到剪切版");
						break;
					case 1:

						Intent intent = new Intent(mActivity, ActivityPosts.class);
						intent.putExtra("tid", thread.getId());
						intent.putExtra("page", 9999);
						intent.putExtra("title", thread.getTitle());
						intent.putExtra("uid", thread.getAuthor().getId());

						startActivity(intent);
				}

				dialog.dismiss();
			}
		});

		return true;
	}

	@Override
	public void onDestroyView() {
		LruCache<String, String> cache = App.getInstance().getMemCache();
		String toCache = Json.toJson(mThreads);

		if (toCache != null) {
			cache.put(keyOfThreadsToCache(), toCache);
		}

		mActivity.getSettings()
				.unregisterOnSharedPreferenceChangeListener(this);
		unregisterForContextMenu(listView);

		super.onDestroyView();
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
		mSwipe.setEnabled(mEnablePullToRefresh);
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

	public void onThreads(Threads threads) {
		mThreads.setMeta(threads.getMeta());
		model.setFetchingAndParsing(false);

		if (threads.size() != 0) {
			if (threads.getMeta().getPage() == 1) {
				mThreads.clear();
			}

			final Collection<Integer> ids = CollectionUtils.collect(mThreads, new Transformer() {
				@Override
				public Object transform(Object o) {
					return ((Thread) o).getId();
				}
			});

			mThreads.addAll(CollectionUtils.selectRejected(threads, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ids.contains(((Thread) o).getId());
				}
			}));

			mAdapter.notifyDataSetChanged();
		}
	}

	public void fetch(int page) {
		model.setFetchingAndParsing(true);
		fetchPageAt(page);
	}

	abstract void fetchPageAt(int page);

	protected void setRefreshSpinner(boolean visible) {
		Logger.i("visible %b, enable refresh %b, is fetching %b", visible, mEnablePullToRefresh, model.isFetchingAndParsing());

		if (visible) {
			// Hack. http://stackoverflow.com/questions/26858692/swiperefreshlayout-setrefreshing-not-showing-indicator-initially
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Logger.i("is fetching %b", model.isFetchingAndParsing());
					mSwipe.setRefreshing(true);
				}
			}, 100);
		} else {
			mSwipe.setRefreshing(false);
		}
	}

	protected abstract String keyOfThreadsToCache();

	@Override
	public void toolbarClick() {
		listView.setSelection(0);
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
			if (mThreads.getMeta().hasNextPage() && totalItemsCount > 1 /* list header */) {
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

	protected class Model {
		public boolean isFetchingAndParsing() {
			return isFetchingAndParsing;
		}

		public void setFetchingAndParsing(boolean fetchingAndParsing) {
			isFetchingAndParsing = fetchingAndParsing;

			if (fetchingAndParsing) {
				setRefreshSpinner(true);
			} else {
				setRefreshSpinner(false);
			}
		}

		private boolean isFetchingAndParsing;
	}

	public static class EventRefresh {};
}
