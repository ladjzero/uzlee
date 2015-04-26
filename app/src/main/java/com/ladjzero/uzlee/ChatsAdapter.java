package com.ladjzero.uzlee;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;

/**
 * Created by ladjzero on 2015/4/25.
 */
public class ChatsAdapter extends PostsAdapter {
	private int uid = Core.getUid();

	public ChatsAdapter(Context context, Posts posts) {
		super(context, posts);
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

	public int getLayout(int position) {
		return getItemViewType(position) == 0 ? R.layout.chat_you : R.layout.chat_me;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		setLayout(getLayout(position));

		View view = super.getView(position, convertView, parent);

		if (position > 0) {
			Post chat = getItem(position);
			Post preChat = getItem(position - 1);
			View date = view.findViewById(R.id.post_date);

			if (prettyTime(chat.getTimeStr()).equals(prettyTime(preChat.getTimeStr()))) {
				date.setVisibility(View.GONE);
			} else {
				date.setVisibility(View.VISIBLE);
			}
		}

		return view;
	}
}
