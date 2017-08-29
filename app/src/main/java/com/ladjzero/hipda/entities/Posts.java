package com.ladjzero.hipda.entities;

import java.util.ArrayList;

/**
 * Created by ladjzero on 2015/4/6.
 */

public class Posts extends ArrayList<Post> {
	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	private Meta meta = new Meta();

	public static class Meta {
		private int fid;
		private int page;
		private int totalPage;
		private boolean hasNextPage;
		private int orderType;
		private String title;

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
}
