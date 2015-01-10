package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Core.OnThreadsListener;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ThreadsFragment extends Fragment implements OnRefreshListener, AdapterView.OnItemClickListener, OnThreadsListener {

	private SwipeRefreshLayout swipe;
	private DBHelper db;
	private Dao<Thread, Integer> threadDao;
	private Dao<User, Integer> userDao;
	private final ArrayList<Thread> threads = new ArrayList<Thread>();
	private ListView listView;
	private ThreadsAdapter adapter;
	private static HashMap<Integer, ThreadsFragment> fragmentsCache = new HashMap<Integer, ThreadsFragment>();
	private boolean hasNextPage = false;

	public static ThreadsFragment newInstance(int fid) {
		ThreadsFragment fragment = fragmentsCache.get(Integer.valueOf(fid));

		if (fragment == null) {
			fragment = new ThreadsFragment();
			Bundle args = new Bundle();
			args.putInt("fid", fid);
			fragment.setArguments(args);
			fragmentsCache.put(fid, fragment);
		}

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (db == null) db = ((BaseActivity) getActivity()).getHelper();

		try {
			threadDao = db.getThreadDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		View rootView = inflater.inflate(R.layout.threads_can_refresh, container, false);

		swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.thread_swipe);
		swipe.setOnRefreshListener(this);
		swipe.setColorSchemeResources(R.color.deep_darker, R.color.deep_dark, R.color.deep_light, android.R.color.white);

		listView = (ListView) rootView.findViewById(R.id.threads);
		adapter = new ThreadsAdapter(getActivity(), threads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {
					fetch(page, ThreadsFragment.this);
				}
			}
		});

		return rootView;
	}

	private void fetch(int page, final OnThreadsListener onThreadsListener) {
		Core.getHtml("http://www.hi-pda.com/forum/forumdisplay.php?fid=" + getArguments().getInt("fid") + "&page=" + page, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Core.parseThreads(html, onThreadsListener);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		if (threads.size() == 0) {
			fetch(1, this);
		}

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (threads != null) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (Thread t : threads) {
				ids.add(t.getId());
			}
			outState.putIntegerArrayList("ids", ids);
		}
		outState.putInt("index", listView.getFirstVisiblePosition());
		View v = listView.getChildAt(0);
		outState.putInt("top", v == null ? 0 : v.getTop());
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Thread t = (Thread) adapterView.getAdapter().getItem(i);
		t.setNew(false);

		Intent intent = new Intent(getActivity(), PostsActivity.class);
		intent.putExtra("thread_id", t.getId());
		intent.putExtra("title", t.getTitle());

		startActivity(intent);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}

	class SaveData extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@Override
	public void onRefresh() {
		fetch(1, new OnThreadsListener() {
			@Override
			public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
				ThreadsFragment.this.hasNextPage = hasNextPage;
				ThreadsFragment.this.threads.clear();
				ThreadsFragment.this.threads.addAll(threads);
				adapter.notifyDataSetChanged();
				swipe.setRefreshing(false);
			}
		});
	}
}
