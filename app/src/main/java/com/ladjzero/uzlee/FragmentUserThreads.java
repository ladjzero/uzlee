package com.ladjzero.uzlee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;

/**
 * Created by chenzhuo on 15-12-14.
 */
public class FragmentUserThreads extends FragmentThreadsAbs {

    private String userName;

    public static FragmentThreadsAbs newInstance() {
        return new FragmentUserThreads();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        Bundle args = getArguments();

        userName = args.getString("userName");

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
        Core.getUserThreadsAtPage(userName, page, this);
    }

    @Override
    protected String keyOfThreadsToCache() {
        return "threads-user-name-" + userName;
    }
}
