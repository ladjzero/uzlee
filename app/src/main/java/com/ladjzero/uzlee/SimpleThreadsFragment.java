package com.ladjzero.uzlee;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;

public class SimpleThreadsFragment extends Fragment implements AbsListView.OnItemClickListener, Core.OnThreadsListener {

	Core core;
	int tabIndex;
	boolean hasNextPage = false;
	ArrayList<Thread> threads = new ArrayList<Thread>();
	int pageToLoad = 1;
	private OnFragmentInteractionListener mListener;
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

		switch (tabIndex) {
			case 0:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text, threads);
				break;

			case 1:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_post, R.id.simple_thread_text, threads) {
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
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text, threads);
				break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_threads, container, false);


		// Set the adapter
		mListView = (AbsListView) view.findViewById(R.id.simple_thread_list);

		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {

					switch (tabIndex) {
						case 0:
							Core.getMyThreads(pageToLoad, SimpleThreadsFragment.this);
							break;
						case 1:
							Core.getMyPosts(pageToLoad, SimpleThreadsFragment.this);
							break;
						case 2:
							Core.getFavorites(pageToLoad, SimpleThreadsFragment.this);
							break;
					}
				}
			}
		});

		if (pageToLoad == 1) {
			switch (tabIndex) {
				case 0:
					Core.getMyThreads(pageToLoad, this);
					break;
				case 1:
					Core.getMyPosts(pageToLoad, this);
					break;
				case 2:
					Core.getFavorites(pageToLoad, this);
					break;
			}
		}

		mListView.setAdapter(mAdapter);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Thread thread = (Thread) parent.getAdapter().getItem(position);
		Intent intent = new Intent(getActivity(), PostsActivity.class);
		intent.putExtra("tid", thread.getId());
		intent.putExtra("title", thread.getTitle());
		intent.putExtra("fid", thread.getFid());

		startActivity(intent);
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

	@Override
	public void onThreads(ArrayList<Thread> threads, int currPage, boolean hasNextPage) {
		pageToLoad = currPage + 1;
		this.threads.addAll(threads);
		mAdapter.notifyDataSetChanged();
		this.hasNextPage = hasNextPage;
	}

	@Override
	public void onError() {
		((MainActivity) getActivity()).showToast("请求错误");
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
		public void onFragmentInteraction(String id);
	}

}
