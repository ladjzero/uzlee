package com.ladjzero.uzlee.api;

import com.ladjzero.hipda.parsers.Parser;
import com.ladjzero.hipda.entities.User;

import java.util.Observable;

/**
 * Created by chenzhuo on 16-2-12.
 */
public class ApiStore extends Observable {
	private static ApiStore singleton;
	private User user;
	private String formhash;
	private String hash;
	private String code;
	private int unread;

	public static ApiStore getStore() {
		if (singleton == null) singleton = new ApiStore();
		singleton.code = Parser.CODE_GBK;
		return singleton;
	}


	protected String getCode() {
		return code;
	}

	protected void setCode(String code) {
		if (!code.equals(this.code)) {
			notifyObservers("code");
			this.code = code;
		}
	}

	public User getUser() {
		return user == null ? new User() : user;
	}

	protected void setUser(User user) {
		this.user = user;
	}

	protected String getFormhash() {
		return formhash;
	}

	protected void setFormhash(String formhash) {
		this.formhash = formhash;
	}

	protected String getHash() {
		return hash;
	}

	protected void setHash(String hash) {
		this.hash = hash;
	}

	public int getUnread() {
		return unread;
	}

	protected void setUnread(int unread) {
		setChanged();
		this.unread = unread;
		notifyObservers("unread");
	}
}
