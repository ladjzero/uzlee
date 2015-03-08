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
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ladjzero on 2015/1/1.
 */
public class MsgFragment extends Fragment implements AbsListView.OnItemClickListener {
	Core core;
	private OnFragmentInteractionListener mListener;
	int tabIndex = -1;
	int pageToLoad = 1;

	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;


	/**
	 * The Adapter which will be used to populate the ListView/GridView with
	 * Views.
	 */
	private ArrayAdapter mAdapter;

	// TODO: Rename and change types of parameters
	public static MsgFragment newInstance(int position) {
		MsgFragment fragment = new MsgFragment();
		Bundle args = new Bundle();
		args.putInt("tab_index", position);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MsgFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tabIndex = getArguments().getInt("tab_index");

		switch (tabIndex) {
			case 0:
				mAdapter = new ArrayAdapter<Post>(getActivity(), R.layout.simple_post, R.id.simple_thread_text) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View row = super.getView(position, convertView, parent);

						TextView title = (TextView) row.findViewById(R.id.simple_thread_text);
						TextView content = (TextView) row.findViewById(R.id.simple_post_text);
						Post post = getItem(position);
						title.setText(post.getTitle());

						String body = post.getBody();

						if (body.length() > 0) {
							content.setText(post.getBody());
							content.setVisibility(View.VISIBLE);
						} else {
							content.setVisibility(View.GONE);
						}

						return row;
					}
				};

				break;

			case 1:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View row = super.getView(position, convertView, parent);

						TextView title = (TextView) row.findViewById(R.id.simple_thread_text);
						Thread thread = getItem(position);
						String msg = thread.getDateStr() + ", " + thread.getAuthor().getName() + ": " + thread.getTitle();


						title.setText(msg);
						if (!thread.isNew()) {
							title.setTextColor(getResources().getColor(R.color.dark_light));
						}


						return row;
					}
				};

				break;
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_threads, container, false);

		// Set the adapter
		mListView = (AbsListView) view.findViewById(R.id.simple_thread_list);
		((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(this);

		view.findViewById(R.id.hint).setVisibility(View.GONE);

		switch (tabIndex) {
			case 0:
				Core.getAlerts(new Core.OnPostsListener() {
					@Override
					public void onPosts(ArrayList<Post> posts, int page, int totalPage) {
						mAdapter.addAll(posts);
					}

					@Override
					public void onError() {
						((MainActivity) getActivity()).showToast("请求错误");
					}
				});
				break;

			case 1:
				Core.getMessages(new Core.OnThreadsListener() {
					@Override
					public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
						mAdapter.addAll(threads);
					}

					@Override
					public void onError() {
						((MainActivity) getActivity()).showToast("请求错误");
					}
				});

				break;
		}

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
		if (tabIndex == 0) {
			Post post = (Post) parent.getAdapter().getItem(position);
			Intent intent = new Intent(getActivity(), PostsActivity.class);
			intent.putExtra("tid", post.getTid());
			intent.putExtra("fid", post.getFid());
			intent.putExtra("title", post.getTitle());

			startActivity(intent);
		}
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
