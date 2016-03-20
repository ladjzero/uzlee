package com.ladjzero.uzlee;

import android.app.Activity;
import android.os.AsyncTask;

import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Threads;
import com.ladjzero.hipda.ThreadsParser;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentSearchThreads extends FragmentThreadsAbs {

	private String mQuery;
	private AsyncTask mParseTask;

	public static FragmentThreadsAbs newInstance() {
		return new FragmentSearchThreads();
	}

	public void updateSearch(String query) {
		mThreads.clear();
		mQuery = query;
		fetch(1);
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh_no_padding_top;
	}

	@Override
	void fetchPageAt(int page) {
		if (mQuery != null && mQuery.length() > 0) {
			getCore().getHttpApi().searchThreads(mQuery, page, new HttpClientCallback() {
				@Override
				public void onSuccess(String response) {
					mParseTask = new AsyncTask<String, Object, Threads>() {
						@Override
						protected Threads doInBackground(String... strings) {
							return getCore().getThreadsParser().parseThreads(strings[0], getSettings().getBoolean("show_fixed_threads", false));
						}

						@Override
						protected void onPostExecute(Threads threads) {
							onThreads(threads);
						}
					}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
				}

				@Override
				public void onFailure(String reason) {

				}
			});
		}
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
		return "threads-search-query-" + mQuery;
	}
}
