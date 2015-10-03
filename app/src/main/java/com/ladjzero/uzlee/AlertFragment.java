package com.ladjzero.uzlee;

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

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;

/**
 * Created by ladjzero on 2015/1/1.
 */
public class AlertFragment extends Fragment implements AbsListView.OnItemClickListener {
	Core core;
	private int tabIndex = -1;
	private AbsListView mListView;
	private ArrayAdapter mAdapter;

	public static AlertFragment newInstance(int position) {
		AlertFragment fragment = new AlertFragment();
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
				mAdapter = new MessageSummaryAdapter(getActivity(), new ArrayList<Thread>());
				break;
			case 1:
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
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_threads, container, false);

		mListView = (AbsListView) view.findViewById(R.id.simple_thread_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setEmptyView(view.findViewById(R.id.empty_view));

		switch (tabIndex) {
			case 0:
				Core.getMessages(new Core.OnThreadsListener() {
					@Override
					public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
						mAdapter.addAll(threads);
					}

					@Override
					public void onError(String error) {
						((BaseActivity) getActivity()).showToast(error);
					}
				});
				break;
			case 1:
				Core.getMentions(new Core.OnPostsListener() {
					@Override
					public void onPosts(Posts posts) {
						mAdapter.addAll(posts);
					}

					@Override
					public void onError(String error) {
						((BaseActivity) getActivity()).showToast(error);
					}
				});
				break;
		}

		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (tabIndex == 0) {
			Thread chat = (Thread) parent.getAdapter().getItem(position);
			Intent intent = new Intent(getActivity(), ActivityChat.class);
			intent.putExtra("name", chat.getAuthor().getName());
			intent.putExtra("uid", chat.getAuthor().getId());
			startActivity(intent);

		} else {
			Post post = (Post) parent.getAdapter().getItem(position);
			Intent intent = new Intent(getActivity(), ActivityPosts.class);
			intent.putExtra("tid", post.getTid());
			intent.putExtra("fid", post.getFid());
			intent.putExtra("title", post.getTitle());
			intent.putExtra("pid", post.getId());
			startActivity(intent);
		}
	}
}
