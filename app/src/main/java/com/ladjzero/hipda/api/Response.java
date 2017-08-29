package com.ladjzero.hipda.api;

import com.ladjzero.hipda.entities.User;
import com.ladjzero.hipda.parsers.Parser;

import java.io.Serializable;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public class Response implements Serializable {
	private boolean success = true;
	private Meta meta;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	private Object data;

	public static class Meta {
		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getFormhash() {
			return formhash;
		}

		public void setFormhash(String formhash) {
			this.formhash = formhash;
		}

		public String getHash() {
			return hash;
		}

		public void setHash(String hash) {
			this.hash = hash;
		}

		public int getUnread() {
			return unread;
		}

		public void setUnread(int unread) {
			this.unread = unread;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		private User user;
		private String formhash;
		private String hash;
		private String code = Parser.CODE_GBK;
		private int unread;
	}
}
