package com.ladjzero.uzlee;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.Threads;
import com.ladjzero.uzlee.service.Api;
import com.ladjzero.uzlee.utils.Utils;
import com.ladjzero.uzlee.widget.HorizontalTagsView;

import java.util.List;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentNormalThreads extends FragmentThreadsAbs implements SwipeRefreshLayout.OnRefreshListener, HorizontalTagsView.TagStateChangeListener, App.OnEventListener {

	private int mFid;
	private boolean mVisibleInPager = false;
	private AsyncTask mParseTask;
	private Forum.Type mType;
	private HorizontalTagsView mTags;

	public static FragmentThreadsAbs newInstance() {
		FragmentNormalThreads f = new FragmentNormalThreads();
		f.mType = new Forum.Type();
		f.mType.setId(-1);
		return f;
	}

	private String getOrder() {
		int i = Integer.parseInt(((ActivityBase) getActivity()).getSettings().getString("sort_thread", "2"));

		switch (i) {
			case 1:
				return "dateline";
			default:
				return "lastpost";
		}
	}

	@Override
	protected void onListViewReady(ListView listView, LayoutInflater inflater) {
		List<Integer> selected = Utils.getForumsShowingTypes(App.getInstance().getSharedPreferences());

		if (selected.contains(mFid)) {
			mTags = (HorizontalTagsView) inflater.inflate(R.layout.horizontal_tags_view, null);
			mTags.setOnInterceptTouchEvent(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					return model.isFetchingAndParsing();
				}
			});

			List<Forum> forums = App.getInstance().getFlattenForums();
			Forum f = Forum.findById(forums, mFid);
			List<Forum.Type> types = f.getTypes();
			if (types != null && types.size() > 0) {
				mType = types.get(0);
				mTags.setTags(types.toArray(), mType);
				mTags.setTagActiveListener(this);
				listView.addHeaderView(mTags);
			}
		} else {
			listView.addHeaderView(new View(getActivity()));
		}
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle args = getArguments();

		mFid = args.getInt("fid", -1);

		assert mFid != -1;

		App.getInstance().addEventListener(this);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		App.getInstance().removeEventListener(this);
	}

	// Lazy load.
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		mVisibleInPager = isVisibleToUser;

		if (isVisibleToUser && getView() != null && mThreads.size() == 0) {
			fetch(1);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mVisibleInPager && mThreads != null && mThreads.size() == 0 && !model.isFetchingAndParsing()) {
			fetch(1);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	void fetchPageAt(int page) {
		App.getInstance().getApi().getThreads(page, mFid, mType.getId(), getOrder(), new Api.OnRespond() {
			@Override
			public void onRespond(Response res) {
				model.setFetchingAndParsing(false);

				if (res.isSuccess()) {
					onThreads((Threads) res.getData());
				} else {
					((ActivityBase) getActivity()).showToast(res.getData().toString());
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mParseTask != null && !mParseTask.isCancelled()) {
			mParseTask.cancel(true);
		}
	}

	@Override
	protected String keyOfThreadsToCache() {
		return "threads-normal-fid-" + mFid + "-typeId-" + (mType != null ? mType.getId() : -1);
	}

	@Override
	public void onRefresh() {
		mThreads.clear();
		mAdapter.notifyDataSetChanged();
		fetch(1);
	}

	@Override
	public void onTagActive(Object tag, int i) {
		mTags.toggle(false);
		mTags.toggle(i, true);
		mType = (Forum.Type) tag;
		mThreads.clear();
		mAdapter.notifyDataSetChanged();
		fetch(1);
	}

	@Override
	public void onTagInactive(Object tag, int i) {

	}

	@Override
	public void onEvent(Object o) {
		if ((o instanceof EventRefresh) && mVisibleInPager && !model.isFetchingAndParsing()) {
			onRefresh();
		}
	}
}
