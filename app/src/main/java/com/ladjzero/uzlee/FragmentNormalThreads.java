package com.ladjzero.uzlee;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Threads;
import com.ladjzero.hipda.ThreadsParser;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentNormalThreads extends FragmentThreadsAbs implements SwipeRefreshLayout.OnRefreshListener {

	private int mFid;
	private int mTypeId;
	private boolean mVisibleInPager = false;

	public static FragmentThreadsAbs newInstance() {
		return new FragmentNormalThreads();
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

	public void setTypeId(int typeId) {
		mTypeId = typeId;
		mThreads.clear();
		mAdapter.notifyDataSetChanged();
		fetchPageAt(1);
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		Bundle args = getArguments();

		mFid = args.getInt("fid", -1);
		mTypeId = args.getInt("typeId", -1);

		assert mFid != -1;
		assert mTypeId != -1;

		return rootView;
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

		if (mVisibleInPager && mThreads != null && mThreads.size() == 0) {
			fetch(1);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	void fetchPageAt(int page) {
		getCore().getHttpApi().getThreads(page, mFid, mTypeId, getOrder(), new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				new AsyncTask<String, Void, Threads>() {
					@Override
					protected Threads doInBackground(String... strings) {
						return getCore().getThreadsParser().parseThreads(strings[0], getSettings().getBoolean("show_fixed_threads", false));
					}

					@Override
					protected void onPostExecute(Threads threads) {
						FragmentNormalThreads.this.onThreads(threads);
					}
				}.execute(response);
			}

			@Override
			public void onFailure(String reason) {

			}
		});
	}

	@Override
	protected String keyOfThreadsToCache() {
		return "threads-normal-fid-" + mFid + "-typeId-" + mTypeId;
	}

	@Override
	public void onRefresh() {
		mThreads.clear();
		setRefreshSpinner(true);
		fetch(1);
	}
}
