package com.ladjzero.uzlee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.Threads;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentUserThreads extends FragmentThreadsAbs {

	private String userName;

	public static FragmentThreadsAbs newInstance() {
		return new FragmentUserThreads();
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		Bundle args = getArguments();

		userName = args.getString("userName");
		App.getInstance().getMemCache().put("search_key", userName);

		assert userName != null;

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mThreads != null && mThreads.size() == 0) {
			fetch(1);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	void fetchPageAt(int page) {
		App.getInstance().getApi().searchUserThreads(userName, page, new Api.OnRespond() {
			@Override
			public void onRespond(Response res) {
				if (res.isSuccess()) {
					onThreads((Threads) res.getData());
				} else {
					((ActivityBase) getActivity()).showToast(res.getData().toString());
				}
			}
		});
	}

	@Override
	protected String keyOfThreadsToCache() {
		Bundle args = getArguments();
		userName = args.getString("userName");

		return "threads-user-name-" + userName;
	}
}
