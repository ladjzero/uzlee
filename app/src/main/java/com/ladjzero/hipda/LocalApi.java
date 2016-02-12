package com.ladjzero.hipda;

import java.util.Set;

/**
 * Created by chenzhuo on 16-2-13.
 */
public class LocalApi {
	private ApiStore mStore;

	public LocalApi() {
		mStore = ApiStore.getStore();
	}

	public void insertBanned(User user) {
		Set<User> banned = mStore.getBanned();

		banned.add(user);

		mStore.setBanned(banned);
	}

	public void deleteBanned(User user) {
		Set<User> banned = mStore.getBanned();

		banned.remove(user);

		mStore.setBanned(banned);
	}

	public Set<User> getBanned() {
		return mStore.getBanned();
	}

	public User getUser() {
		return mStore.getUser();
	}

	public int getUnread() {
		return mStore.getUnread();
	}
}
