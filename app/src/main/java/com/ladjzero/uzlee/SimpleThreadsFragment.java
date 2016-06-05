package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.Threads;
import com.ladjzero.hipda.ThreadsParser;

import java.util.ArrayList;
import java.util.List;

public class SimpleThreadsFragment extends FragmentBase implements AbsListView.OnItemClickListener {

	Core core;
	int tabIndex;
	Threads mThreads;
	private HttpApi mApi;
	private ThreadsParser mThreadsParser;
	private ArrayList<AsyncTask> mTasks;
	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;

	/**
	 * The Adapter which will be used to populate the ListView/GridView with
	 * Views.
	 */
	private ArrayAdapter mAdapter;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public SimpleThreadsFragment() {
	}

	// TODO: Rename and change types of parameters
	public static SimpleThreadsFragment newInstance(int position) {
		SimpleThreadsFragment fragment = new SimpleThreadsFragment();
		Bundle args = new Bundle();
		args.putInt("tab_index", position);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tabIndex = getArguments().getInt("tab_index");

		Core core = App.getInstance().getCore();
		mApi = core.getHttpApi();
		mThreadsParser = core.getThreadsParser();
		mTasks = new ArrayList<>();
		mThreads = new Threads();

		try {
			String cached = App.getInstance().getMemCache().get("simple_threads_tab_" + tabIndex);
			List<Thread> ts = JSON.parseArray(cached, Thread.class);
			mThreads.addAll(ts);
		} catch (Exception e) {

		}

		switch (tabIndex) {
			case 0:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text, mThreads);
				break;

			case 1:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_post, R.id.simple_thread_text, mThreads) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View row = super.getView(position, convertView, parent);

						TextView title = (TextView) row.findViewById(R.id.simple_thread_text);
						TextView content = (TextView) row.findViewById(R.id.simple_post_text);
						Thread thread = getItem(position);
						title.setText(thread.getTitle());

						String body = thread.getBody();

						if (body.length() > 0) {
							content.setText(thread.getBody());
							content.setVisibility(View.VISIBLE);
						} else {
							content.setVisibility(View.GONE);
						}

						return row;
					}
				};

				break;

			case 2:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text, mThreads);
				break;
		}
	}

	private void load(int tabIndex) {
		switch (tabIndex) {
			case 0:
				mApi.getOwnThreads(mThreads.getMeta().getPage() + 1, new HttpClientCallback() {
					@Override
					public void onSuccess(String response) {
						mTasks.add(new AsyncTask<String, Object, Threads>() {
							@Override
							protected Threads doInBackground(String... strings) {
								return mThreadsParser.parseOwnThreads(strings[0]);
							}

							@Override
							protected void onPostExecute(Threads threads) {
								onThreads(threads);
							}
						}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response));
					}

					@Override
					public void onFailure(String reason) {
						((ActivityBase) getActivity()).showToast(reason);
					}
				});
				break;
			case 1:
				mApi.getOwnPosts(mThreads.getMeta().getPage() + 1, new HttpClientCallback() {
					@Override
					public void onSuccess(String response) {
						mTasks.add(new AsyncTask<String, Object, Threads>() {
							@Override
							protected Threads doInBackground(String... strings) {
								return mThreadsParser.parseOwnPosts(strings[0]);
							}

							@Override
							protected void onPostExecute(Threads threads) {
								onThreads(threads);
							}
						}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response));
					}

					@Override
					public void onFailure(String reason) {
						((ActivityBase) getActivity()).showToast(reason);
					}
				});
				break;
			case 2:
				mApi.getMarkedThreads(mThreads.getMeta().getPage() + 1, new HttpClientCallback() {
					@Override
					public void onSuccess(String response) {
						mTasks.add(new AsyncTask<String, Object, Threads>() {
							@Override
							protected Threads doInBackground(String... strings) {
								return mThreadsParser.parseMarkedThreads(strings[0]);
							}

							@Override
							protected void onPostExecute(Threads threads) {
								onThreads(threads);
							}
						}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response));
					}

					@Override
					public void onFailure(String reason) {
						((ActivityBase) getActivity()).showToast(reason);
					}
				});
				break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_threads, container, false);


		// Set the adapter
		mListView = (AbsListView) view.findViewById(R.id.simple_thread_list);
		mListView.setEmptyView(view.findViewById(R.id.empty_view));

		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (mThreads.getMeta().hasNextPage()) {
					((ActivityBase) getActivity()).showToast("载入下一页");
					load(tabIndex);
				}
			}
		});

		if (mThreads.size() == 0) {
			load(tabIndex);
		}

		mListView.setAdapter(mAdapter);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Thread thread = (Thread) parent.getAdapter().getItem(position);
		Intent intent = new Intent(getActivity(), ActivityPosts.class);
		intent.putExtra("tid", thread.getId());
		intent.putExtra("title", thread.getTitle());
		intent.putExtra("fid", thread.getFid());
		intent.putExtra("pid", thread.getToFind());

		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		for (AsyncTask task : mTasks) {
			if (task != null && !task.isCancelled()) task.cancel(true);
		}

		mTasks.clear();

		App.getInstance().getMemCache().put("simple_threads_tab_" + tabIndex, JSON.toJSONString(mThreads));
	}

	/**
	 * The default content for this Fragment has a TextView that is shown when
	 * the list is empty. If you would like to change the text, call this method
	 * to supply the text it should use.
	 */
	public void setEmptyText(CharSequence emptyText) {
		View emptyView = mListView.getEmptyView();

		if (emptyView instanceof TextView) {
			((TextView) emptyView).setText(emptyText);
		}
	}

	public void onThreads(Threads threads) {
		mThreads.addAll(threads);
		mThreads.getMeta().setHasNextPage(threads.getMeta().hasNextPage());
		mThreads.getMeta().setPage(threads.getMeta().getPage());
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(String id);
	}

}
