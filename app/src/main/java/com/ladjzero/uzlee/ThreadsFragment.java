package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Core.OnThreads;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Core.GetHtmlCB;
import com.ladjzero.hipda.User;
import com.ladjzero.hipda.cb.UserStatsCB;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadsFragment extends Fragment implements OnRefreshListener {

	Core core;
	SwipeRefreshLayout swipe;
	DBHelper db;
	Dao<Thread, Integer> threadDao;
	Dao<User, Integer> userDao;
	final ArrayList<Thread> threads = new ArrayList<Thread>();
	final ArrayList<Thread> backup = new ArrayList<Thread>();
	ListView postList;
	ThreadsAdapter adapter;
	private int index = 0, top = 0;
	static HashMap<Integer, ThreadsFragment> cache = new HashMap<Integer, ThreadsFragment>();

	public static ThreadsFragment newInstance(int fid) {
		ThreadsFragment fragment = cache.get(Integer.valueOf(fid));

		if (fragment == null) {
			fragment = new ThreadsFragment();
			Bundle args = new Bundle();
			args.putInt("fid", fid);
			fragment.setArguments(args);
			cache.put(fid, fragment);
		}

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

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
			
			index = savedInstanceState.getInt("index", 0);
			top = savedInstanceState.getInt("top", 0);
		}

		View rootView = inflater.inflate(R.layout.threads, container, false);

		swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.thread_swipe);

		swipe.setOnRefreshListener(this);
        swipe.setColorSchemeResources(R.color.deep_darker, R.color.deep_dark, R.color.deep_light, android.R.color.white);

		postList = (ListView) rootView.findViewById(R.id.disvoery_post_list);

        String udata = "下一页";
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);

        ViewGroup loadNextPage = ((ViewGroup) inflater.inflate(
                R.layout.load_next_page, postList, false));
        TextView _tv = (TextView) loadNextPage.findViewById(R.id.next_page);
        _tv.setText(content);

        postList.addFooterView(loadNextPage, null, false);
		adapter = new ThreadsAdapter(getActivity(),
				threads);
		postList.setAdapter(adapter);
		
		return rootView;
	}

	public void showSearch(ArrayList<Thread> threads) {
		backup.clear();
		backup.addAll(this.threads);
		this.threads.clear();
		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}
	
	public void dismissSearch() {
		threads.clear();
		threads.addAll(backup);
		adapter.notifyDataSetChanged();
	}
	
	private void fetch(OnThreads onThreads) {
		core.getThreadsByUrl(
				"http://www.hi-pda.com/forum/forumdisplay.php?fid=" + getArguments().getInt("fid"),
				onThreads, new UserStatsCB() {

					@Override
					public void onMsg() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onOffline() {
						((MainActivity) getActivity()).showLoginDialog();
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		
		adapter.notifyDataSetChanged();

		MainActivity ma = (MainActivity) getActivity();

		if (ma.dListClickListener != null) {
			postList.setOnItemClickListener(ma.dListClickListener);
		}

		core = Core.getInstance(getActivity());

		if (threads.size() == 0) {
			fetch(new OnThreads() {
				@Override
				public void onThreads(final ArrayList<Thread> _threads) {
					if (_threads != null) {

						threads.addAll(_threads);
						adapter.notifyDataSetChanged();

						(new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								for (Thread t : _threads) {
									try {
										userDao.createIfNotExists(t.getAuthor());
										threadDao.createOrUpdate(t);
									} catch (SQLException e) {
										// TODO Auto-generated catch
										// block
										e.printStackTrace();
									}
								}
								return null;
							}

						}).execute();
					}
				}
			});
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
		outState.putInt("index", postList.getFirstVisiblePosition());
		View v = postList.getChildAt(0);
		outState.putInt("top", v == null ? 0 : v.getTop());
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
		fetch(new OnThreads() {
			@Override
			public void onThreads(final ArrayList<Thread> _threads) {
				if (_threads != null) {

					threads.clear();
					threads.addAll(_threads);
					swipe.setRefreshing(false);
					adapter.notifyDataSetChanged();
					
					(new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							for (Thread t : _threads) {
								try {
									userDao.createIfNotExists(t.getAuthor());
									threadDao.createOrUpdate(t);
								} catch (SQLException e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
								}
							}
							return null;
						}

					}).execute();
				}
			}
		});
	}
}
