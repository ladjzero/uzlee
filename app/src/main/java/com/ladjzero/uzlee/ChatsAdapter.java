package com.ladjzero.uzlee;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;

/**
 * Created by ladjzero on 2015/4/25.
 */
public class ChatsAdapter extends PostsAdapter {
	private int uid = Core.getUser().getId();
	private int textColorYou;
	private int linkColorYou;
	private int textColorMe;
	private int linkColorMe;
	private Posts mChats;

	public ChatsAdapter(Context context, Posts posts) {
		super(context, posts, TYPE.CHAT);

		mChats = posts;

		Resources res = context.getResources();
		textColorYou = res.getColor(R.color.smallFont);
		linkColorYou = res.getColor(android.R.color.holo_blue_dark);
		textColorMe = linkColorMe = res.getColor(android.R.color.white);
	}

	@Override
	public int getCount() {
		return mChats.size();
	}

	@Override
	public Post getItem(int position) {
		return mChats.get(position);
	}

	@Override
	public int getPosition(Post post) {
		return mChats.indexOf(post);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		Post chat = getItem(position);

		return chat.getAuthor().getId() != uid ? 0 : 1;
	}

	public int getLayout(int position, int[] colors) {
		if (getItemViewType(position) == 0) {
			colors[0] = textColorYou;
			colors[1] = linkColorYou;

			return R.layout.chat_you;
		} else {
			colors[0] = textColorMe;
			colors[1] = linkColorMe;

			return R.layout.chat_me;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int[] colors = new int[2];
		int layoutId = getLayout(position, colors);

		setLayout(layoutId, colors[0], colors[1]);

		View view = super.getView(position, convertView, parent);

		Post chat = getItem(position);

		if (position > 0) {
			Post preChat = getItem(position - 1);
			View date = view.findViewById(R.id.post_date);

			if (prettyTime(chat.getTimeStr()).equals(prettyTime(preChat.getTimeStr()))) {
				date.setVisibility(View.GONE);
			} else {
				date.setVisibility(View.VISIBLE);
			}
		}

		if (layoutId == R.layout.chat_me) {
			View pendingView = view.findViewById(R.id.pending);

			pendingView.setVisibility(chat.isPending() ? View.VISIBLE : View.GONE);
		}

		return view;
	}
}