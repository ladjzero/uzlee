package com.ladjzero.uzlee;

import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.service.ApiStore;

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
