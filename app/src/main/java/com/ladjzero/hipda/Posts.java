package com.ladjzero.hipda;

import java.util.ArrayList;

/**
 * Created by ladjzero on 2015/4/6.
 */

public class Posts extends ArrayList<Post> {
	private int fid;
	private int page;
	private int totalPage;
	private boolean hasNextPage;
	private int orderType;
	private String title;

	public void replaceMeta(Posts posts) {
		this.setFid(posts.getFid());
		this.setPage(posts.getPage());
		this.setTotalPage(posts.getTotalPage());
		this.setOrderType(posts.getOrderType());
		this.setTitle(posts.getTitle());
	}

	public boolean replace(Posts posts) {
		replaceMeta(posts);
		this.clear();
		return this.addAll(posts);
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
}
