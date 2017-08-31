package com.ladjzero.uzlee.api;

import com.ladjzero.hipda.api.Response;

/**
 * Created by chenzhuo on 16-2-12.
 */
public class ApiStore {
	private static ApiStore singleton;

	public Response.Meta getMeta() {
		return mMeta == null ? new Response.Meta() : mMeta;
	}

	public void setMeta(Response.Meta meta) {
		this.mMeta = meta;
	}

	private Response.Meta mMeta;

	public static ApiStore getStore() {
		if (singleton == null) singleton = new ApiStore();
		return singleton;
	}
}
