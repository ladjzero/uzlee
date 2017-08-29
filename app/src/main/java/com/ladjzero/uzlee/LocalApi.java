package com.ladjzero.uzlee;

import com.ladjzero.hipda.entities.User;
import com.ladjzero.uzlee.api.ApiStore;

/**
 * Created by chenzhuo on 16-2-13.
 */
public class LocalApi {
	private ApiStore mStore;

	public LocalApi() {
		mStore = ApiStore.getStore();
	}

	public User getUser() {
		return mStore.getUser();
	}

	public int getUnread() {
		return mStore.getUnread();
	}
}
