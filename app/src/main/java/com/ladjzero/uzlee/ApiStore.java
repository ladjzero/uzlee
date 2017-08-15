package com.ladjzero.uzlee;

import com.ladjzero.hipda.Parser;
import com.ladjzero.hipda.User;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * Created by chenzhuo on 16-2-12.
 */
public class ApiStore extends Observable {
	private static ApiStore singleton;
	private static PersistenceAdapter mAdapter;
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

	public static void initialize(PersistenceAdapter adapter) {
		mAdapter = adapter;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if (!code.equals(this.code)) {
			notifyObservers("code");
			this.code = code;
		}
	}

	public User getUser() {
		if (user == null) {
			user = mAdapter.getValue("user", User.class, null);
		}

		return user == null ? new User() : user;
	}

	public void setUser(User user) {
		setChanged();
		this.user = user;
		mAdapter.putValue("user", user);
		notifyObservers("user");
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
		setChanged();
		this.unread = unread;
		notifyObservers("unread");
	}
}
