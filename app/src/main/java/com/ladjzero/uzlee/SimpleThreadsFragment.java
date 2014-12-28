package com.ladjzero.uzlee;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
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
import com.ladjzero.uzlee.dummy.DummyContent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SimpleThreadsFragment extends Fragment implements AbsListView.OnItemClickListener {

	Core core;
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

	// TODO: Rename and change types of parameters
	public static SimpleThreadsFragment newInstance(int position) {
		SimpleThreadsFragment fragment = new SimpleThreadsFragment();
		Bundle args = new Bundle();
		args.putInt("tab_index", position);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public SimpleThreadsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int tabIndex = getArguments().getInt("tab_index");

		switch (tabIndex) {
			case 0:
				mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text);

				Core.getMyThreads(new Core.OnThreads() {
					@Override
					public void onThreads(ArrayList<Thread> threads) {
						mAdapter.addAll(CollectionUtils.collect(threads, new Transformer() {
							@Override
							public Object transform(Object o) {
								return ((Thread) o).getTitle();
							}
						}));
					}
				});
				break;

			case 1:
				mAdapter = new ArrayAdapter<Thread>(getActivity(), R.layout.simple_post, R.id.simple_thread_text) {
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

				Core.getMyPosts(new Core.OnThreads() {
					@Override
					public void onThreads(ArrayList<Thread> threads) {
						mAdapter.addAll(threads);
					}
				});

				break;

			case 2:
				mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_thread, R.id.simple_thread_text);

				Core.getFavorites(new Core.OnThreads() {
					@Override
					public void onThreads(ArrayList<Thread> threads) {
						mAdapter.addAll(CollectionUtils.collect(threads, new Transformer() {
							@Override
							public Object transform(Object o) {
								return ((Thread) o).getTitle();
							}
						}));
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
		((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(this);

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
		if (null != mListener) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
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
