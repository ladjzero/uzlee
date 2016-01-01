package com.ladjzero.uzlee;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;

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
		Core.getHtml("http://www.hi-pda.com/forum/forumdisplay.php?fid=" + mFid
				+ "&page=" + page
				+ "&filter=type&typeid=" + mTypeId
				+ "&orderby=" + getOrder(), new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				FragmentNormalThreads.this.onError(error);
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
						FragmentNormalThreads.this.onThreads(ret.threads, ret.page, ret.hasNextPage);
					}
				}.execute(html);
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
