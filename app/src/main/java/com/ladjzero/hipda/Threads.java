package com.ladjzero.hipda;

import java.util.ArrayList;

/**
 * Created by chenzhuo on 16-2-11.
 */
public class Threads extends ArrayList<Thread> {
	private boolean mHasNextPage;
	private int mPage;

	public boolean hasNextPage() {
		return mHasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		mHasNextPage = hasNextPage;
	}

	public int getPage() {
		return mPage;
	}

	public void setPage(int page) {
		mPage = page;
	}
}
