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

import com.google.gson.reflect.TypeToken;
import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.entities.Thread;
import com.ladjzero.hipda.entities.Threads;
import com.ladjzero.uzlee.utils.Json;
import com.ladjzero.uzlee.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SimpleThreadsFragment extends FragmentBase implements AbsListView.OnItemClickListener {

	int tabIndex;
	Threads mThreads;
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

		mTasks = new ArrayList<>();
		mThreads = new Threads();

		try {
			String cached = App.getInstance().getMemCache().get("simple_threads_tab_" + tabIndex);
			List<Thread> ts = Json.fromJson(cached, new TypeToken<ArrayList<Thread>>(){}.getType());
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
				App.getInstance().getApi().getOwnThreads(mThreads.getMeta().getPage() + 1, new OnRespondCallback() {
					@Override
					public void onRespond(Response res) {
						if (res.isSuccess()) {
							onThreads((Threads) res.getData());
						} else {
							Utils.showToast(getActivity(), res.getData().toString());
						}
					}
				});
				break;
			case 1:
				App.getInstance().getApi().getOwnPosts(mThreads.getMeta().getPage() + 1, new OnRespondCallback() {
					@Override
					public void onRespond(Response res) {
						if (res.isSuccess()) {
							onThreads((Threads) res.getData());
						} else {
							Utils.showToast(getActivity(), res.getData().toString());
						}
					}
				});
				break;
			case 2:
				App.getInstance().getApi().getMarkedThreads(mThreads.getMeta().getPage() + 1, new OnRespondCallback() {
					@Override
					public void onRespond(Response res) {
						if (res.isSuccess()) {
							onThreads((Threads) res.getData());
						} else {
							Utils.showToast(getActivity(), res.getData().toString());
						}
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

		App.getInstance().getMemCache().put("simple_threads_tab_" + tabIndex, Json.toJson(mThreads));
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
