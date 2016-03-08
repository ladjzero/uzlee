package com.ladjzero.uzlee.model;

import android.databinding.ObservableArrayList;

import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;

/**
 * Created by chenzhuo on 2016/2/14.
 */
public class ObservablePosts extends ObservableArrayList<Post> {
	com.ladjzero.hipda.Posts mPosts;

	public ObservablePosts(Posts posts) {
		super();
		this.addAll(posts);
		mPosts = posts;
	}

	public void replaceMeta(Posts posts) {
		mPosts.replaceMeta(posts);
	}

	public int getFid() {
		return mPosts.getFid();
	}

	public void setFid(int fid) {
		mPosts.setFid(fid);
	}

	public int getPage() {
		return mPosts.getPage();
	}

	public String getTitle() {
		return mPosts.getTitle();
	}

}
