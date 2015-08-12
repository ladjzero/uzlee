package com.ladjzero.uzlee;

import android.widget.AbsListView;

import com.r0adkll.slidr.model.SlidrInterface;

/**
 * Created by chenzhuo on 15-7-28.
 */
public class LockOnScrollListener implements AbsListView.OnScrollListener{
	private SlidrInterface slidr;

	public LockOnScrollListener(SlidrInterface slidr) {
		this.slidr = slidr;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE) {
			slidr.unlock();
		} else {
			slidr.lock();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}
}
