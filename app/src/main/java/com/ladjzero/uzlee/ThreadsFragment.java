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
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadsFragment extends Fragment implements OnRefreshListener, AdapterView.OnItemClickListener, OnThreadsListener {

	SwipeRefreshLayout swipe;
	DBHelper db;
	Dao<Thread, Integer> threadDao;
	Dao<User, Integer> userDao;
	final ArrayList<Thread> threads = new ArrayList<Thread>();
	ListView listView;
	ThreadsAdapter adapter;
	static HashMap<Integer, ThreadsFragment> fragmentsCache = new HashMap<Integer, ThreadsFragment>();

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

		db = ((BaseActivity) getActivity()).getHelper();
		try {
			threadDao = db.getThreadDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (savedInstanceState != null) {
			ArrayList<Integer> ids = savedInstanceState
					.getIntegerArrayList("ids");
			if (ids != null) {
				try {
					threads.addAll((threadDao.query(threadDao.queryBuilder()
							.where().in("id", ids).prepare())));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		View rootView = inflater.inflate(R.layout.threads_can_refresh, container, false);

		swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.thread_swipe);

		swipe.setOnRefreshListener(this);
		swipe.setColorSchemeResources(R.color.deep_darker, R.color.deep_dark, R.color.deep_light, android.R.color.white);

		listView = (ListView) rootView.findViewById(R.id.threads);

		String udata = "下一页";
		SpannableString content = new SpannableString(udata);
		content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);

		ViewGroup loadNextPage = ((ViewGroup) inflater.inflate(
				R.layout.load_next_page, listView, false));
		TextView _tv = (TextView) loadNextPage.findViewById(R.id.next_page);
		_tv.setText(content);

		adapter = new ThreadsAdapter(getActivity(),
				threads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		return rootView;
	}

	private void fetch(final OnThreadsListener onThreadsListener) {
		Core.getHtml("http://www.hi-pda.com/forum/forumdisplay.php?fid=" + getArguments().getInt("fid"), new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				onThreadsListener.onThreads(Core.parseThreads(html));
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		if (threads.size() == 0) {
			fetch(this);
		}
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
	public void onThreads(ArrayList<Thread> threads) {
		this.threads.clear();
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
		fetch(this);
	}
}
