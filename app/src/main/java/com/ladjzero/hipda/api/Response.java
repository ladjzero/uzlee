package com.ladjzero.hipda.api;

import com.google.gson.Gson;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.hipda.parsers.Parser;

import java.io.Serializable;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public class Response {
	private static Gson gson = new Gson();
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
		@Override
		public String toString() {
			return gson.toJson(this);
		}

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

		public Integer getUnread() {
			return unread;
		}

		public void setUnread(Integer unread) {
			this.unread = unread;
		}

		public Integer getUid() {
			return uid;
		}

		public void setUid(Integer uid) {
			this.uid = uid;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		private String userName;
		private Integer uid;
		private String formhash;
		private String hash;
		private String code = Parser.CODE_GBK;
		private Integer unread;
	}
}
