package com.ladjzero.hipda;

/**
 * Created by ladjzero on 2015/4/6.
 */

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collection;

public class Posts extends ArrayList<Post> {
	private int fid;
	private int page;
	private int totalPage;
	private boolean hasNextPage;
	private int orderType;
	private String title;
	private Posts lastMerged;
	private boolean noPermission;

	public boolean merge(Posts posts) {
		this.setFid(posts.getFid());
		this.setPage(posts.getPage());
		this.setTotalPage(posts.getTotalPage());
		this.setOrderType(posts.getOrderType());
		this.setTitle(posts.getTitle());

		final Collection ids = CollectionUtils.collect(posts, new Transformer() {
			@Override
			public Object transform(Object o) {
				return ((Post) o).getId();
			}
		});

		CollectionUtils.filter(this, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return !ids.contains(((Post) o).getId());
			}
		});

		lastMerged = posts;

		return super.addAll(posts);
	}

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public boolean isHasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Posts getLastMerged() {
		if (lastMerged == null)
			return this;
		else
			return lastMerged;
	}

	public boolean isNoPermission() {
		return noPermission;
	}

	public void setNoPermission(boolean noPermission) {
		this.noPermission = noPermission;
	}
}
