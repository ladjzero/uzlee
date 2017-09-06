package com.ladjzero.uzlee.api;

import android.content.Context;

import com.ladjzero.hipda.api.Response;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by chenzhuo on 16-2-11.
 */
abstract class HttpClient {
	private AsyncHttpClient mAsyncHttpClient;
	private PersistentCookieStore mCookieStore;

	abstract Response.Meta getMeta();

	abstract String getCode();

	public HttpClient(Context context) {
		mAsyncHttpClient = new AsyncHttpClient();
		mCookieStore = new PersistentCookieStore(context);
		mAsyncHttpClient.setCookieStore(mCookieStore);
	}

	public void clearCookie() {
		mCookieStore.clear();
	}

	public void get(String url, final HttpClientCallback callback) {
		get(url, getCode(), callback);
	}

	public void get(String url, String code, final HttpClientCallback callback) {
		mAsyncHttpClient.get(url, new RequestParams(), new TextHttpResponseHandler(code) {

			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
				callback.onFailure(throwable == null ? "error" : throwable.toString());
			}

			@Override
			public void onSuccess(int i, Header[] headers, String s) {
				callback.onSuccess(s);
			}
		});
	}

	public void post(String url, Map<String, String> form, Map<String, File> files, final HttpClientCallback callback) {
		RequestParams params = new RequestParams();
		params.setContentEncoding(getCode());

		if (form != null) {
			for (Map.Entry<String, String> entry : form.entrySet()) {
				params.put(entry.getKey(), entry.getValue());
			}
		}

		if (files != null) {
			for (Map.Entry<String, File> entry : files.entrySet()) {
				try {
					params.put(entry.getKey(), entry.getValue());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		mAsyncHttpClient.post(url, params, new TextHttpResponseHandler(getCode()) {
			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
				callback.onFailure(s);
			}

			@Override
			public void onSuccess(int i, Header[] headers, String s) {
				callback.onSuccess(s);
			}
		});
	}
}
