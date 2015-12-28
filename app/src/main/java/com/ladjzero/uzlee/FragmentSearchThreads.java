package com.ladjzero.uzlee;

import com.ladjzero.hipda.Core;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentSearchThreads extends FragmentThreadsAbs {

    private String mQuery;

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
            Core.search(mQuery, page, this);
        }
    }

    @Override
    protected String keyOfThreadsToCache() {
        return "threads-search-query-" + mQuery;
    }
}
