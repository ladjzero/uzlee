package com.ladjzero.uzlee;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.Threads;
import com.ladjzero.uzlee.service.Api;
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mSlidr = ((ActivitySearch) getActivity()).getSlidrInterface();
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
		App.getInstance().getMemCache().put("search_key", mQuery);
		fetch(1);
	}

	@Override
	public int layout() {
		return R.layout.threads_can_refresh;
	}

	@Override
	void fetchPageAt(int page) {
		if (mQuery != null && mQuery.length() > 0) {
			App.getInstance().getApi().searchThreads(mQuery, page, toFids(mTags.getTags(true)), new Api.OnRespond() {
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
	}

	@Override
	protected void onListViewReady(ListView listView, LayoutInflater inflater) {
		mTags = (HorizontalTagsView) inflater.inflate(R.layout.horizontal_tags_view, null);
		mTags.setOnInterceptTouchEvent(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int action = motionEvent.getAction();

				if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
					mSlidr.lock();
				}

				return model.isFetchingAndParsing();
			}
		});
		mTags.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int action = motionEvent.getAction();

				if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					mSlidr.unlock();
				}
				return false;
			}
		});

		List<Forum> forums = new ArrayList<Forum>();
		forums.add(new Forum().setFid(-1).setName("全部"));
		forums.addAll(App.getInstance().getUserFlattenForums());

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
		return "threads-search-query-" + App.getInstance().getMemCache().get("search_key");
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
