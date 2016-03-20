package com.ladjzero.uzlee.model;

import android.databinding.ObservableArrayList;

import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;

/**
 * Created by chenzhuo on 2016/2/14.
 */
public class ObservablePosts extends ObservableArrayList<Post> {
	private Posts.Meta meta;

	public ObservablePosts() {
		super();
		meta = new Posts.Meta();
	}

	public Posts.Meta getMeta() {
		return meta;
	}

	public void setMeta(Posts.Meta meta) {
		this.meta = meta;
	}
}
