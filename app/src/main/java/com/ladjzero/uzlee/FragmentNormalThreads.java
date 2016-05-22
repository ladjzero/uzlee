package com.ladjzero.uzlee;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Threads;
import com.ladjzero.uzlee.widget.HorizontalTagsView;

import java.util.List;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentNormalThreads extends FragmentThreadsAbs implements SwipeRefreshLayout.OnRefreshListener, HorizontalTagsView.TagActiveListener {

	private int mFid;
	private boolean mVisibleInPager = false;
	private AsyncTask mParseTask;
	private Forum.Type mType;

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
		HorizontalTagsView tags = (HorizontalTagsView) inflater.inflate(R.layout.horizontal_tags_view, null);
		List<Forum> forums = Application2.getInstance().getFlattenForums();
		Forum f = Forum.findById(forums, mFid);
		List<Forum.Type> types = f.getTypes();

		if (types != null && types.size() > 0) {
			mType = types.get(0);
			tags.setTags(types.toArray(), mType);
			listView.addHeaderView(tags);
			tags.setTagActiveListener(this);
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

		return super.onCreateView(inflater, container, savedInstanceState);
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
		getCore().getHttpApi().getThreads(page, mFid, mType.getId(), getOrder(), new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				mParseTask = new AsyncTask<String, Void, Threads>() {
					@Override
					protected Threads doInBackground(String... strings) {
						return getCore().getThreadsParser().parseThreads(strings[0], getSettings().getBoolean("show_fixed_threads", false));
					}

					@Override
					protected void onPostExecute(Threads threads) {
						model.setFetchingAndParsing(false);
						onThreads(threads);
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
			}

			@Override
			public void onFailure(String reason) {
				model.setFetchingAndParsing(false);
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
		return "threads-normal-fid-" + mFid + "-typeId-" + mType.getId();
	}

	@Override
	public void onRefresh() {
		fetch(1);
	}

	@Override
	public void onTagActive(Object tag) {
		mType = (Forum.Type) tag;
		mThreads.clear();
		mAdapter.notifyDataSetChanged();
		fetch(1);
	}
}
