package com.ladjzero.hipda;


import java.io.File;
import java.util.Map;

/**
 * Created by chenzhuo on 16-2-11.
 */
public abstract class HttpClient {
	private ApiStore mStore;

	public HttpClient() {
		mStore = ApiStore.getStore();
	}

	public String getCode() {
		return mStore.getCode();
	}

	/**
	 * http get.
	 *
	 * @param url
	 * @param callback
	 */
	public abstract void get(String url, HttpClientCallback callback);

	/**
	 * http post.
	 *
	 * @param url
	 * @param form
	 * @param files
	 * @param callback
	 */
	public abstract void post(String url, Map<String, String> form, Map<String, File> files, HttpClientCallback callback);
}