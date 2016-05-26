package com.ladjzero.uzlee;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Threads;
import com.ladjzero.uzlee.widget.HorizontalTagsView;
import com.r0adkll.slidr.model.SlidrInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentSearchThreads extends FragmentThreadsAbs implements HorizontalTagsView.TagStateChangeListener {

	private String mQuery;
	private AsyncTask mParseTask;
	private SlidrInterface mSlidr;
	private HorizontalTagsView mTags;

	public static FragmentThreadsAbs newInstance() {
		return new FragmentSearchThreads();
	}

	private static int[] toFids(Object[] forums) {
		if (forums.length == 1 && ((Forum) forums[0]).getFid() == -1) {
			return null;
		} else {
			int[] ret = new int[forums.length];

			for (int i = 0; i < ret.length; ++i) {
				ret[i] = ((Forum) forums[i]).getFid();
			}

			return ret;
		}
	}

	public void updateSearch(String query) {
		mQuery = query;
		getApp().getMemCache().put("search_key", mQuery);
		fetch(1);
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh;
	}

	@Override
	void fetchPageAt(int page) {
		if (mQuery != null && mQuery.length() > 0) {
			getCore().getHttpApi().searchThreads(mQuery, page, toFids(mTags.getTags(true)), new HttpClientCallback() {
				@Override
				public void onSuccess(String response) {
					mParseTask = new AsyncTask<String, Object, Threads>() {
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
					((ActivityBase) getActivity()).showToast(reason);
				}
			});
		}
	}

	@Override
	protected void onListViewReady(ListView listView, LayoutInflater inflater) {
		mTags = (HorizontalTagsView) inflater.inflate(R.layout.horizontal_tags_view, null);
		mTags.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (model.isFetchingAndParsing()) {
					return true;
				}

				int action = motionEvent.getAction();

				if (mSlidr == null) {
					mSlidr = ((ActivityHardSlide) getActivity()).getSlidrInterface();
				}

				if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
					mSlidr.lock();
				} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					mSlidr.unlock();
				}

				return false;
			}
		});

		List<Forum> forums = new ArrayList<Forum>();
		forums.add(new Forum().setFid(-1).setName("全部"));
		forums.addAll(Application2.getInstance().getFlattenForums());

		mTags.setTags(forums.toArray(), forums.get(0));
		listView.addHeaderView(mTags);
		mTags.setTagActiveListener(this);
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
		return "threads-search-query-" + getApp().getMemCache().get("search_key");
	}

	@Override
	public void onTagActive(Object tag, int i) {
		if (i == 0) {
			mTags.toggle(false);
			mTags.toggle(0, true);
		} else {
			mTags.toggle(0, false);
		}

		if (mQuery != null && mQuery.length() > 0) {
			mThreads.clear();
			mAdapter.notifyDataSetChanged();
			fetch(1);
		}
	}

	@Override
	public void onTagInactive(Object tag, int i) {
		if (mTags.getTags(true).length == 0) {
			mTags.toggle(0, true);
		}

		if (mQuery != null && mQuery.length() > 0) {
			mThreads.clear();
			mAdapter.notifyDataSetChanged();
			fetch(1);
		}
	}
}
