package com.ladjzero.uzlee;

import android.content.Context;
import android.os.AsyncTask;

import com.ladjzero.hipda.Parse;
import com.ladjzero.hipda.PostsParser;
import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.ThreadsParser;
import com.ladjzero.hipda.UserParser;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public class Api {
	private ThreadsParser mThreadsParser = new ThreadsParser();

	public PostsParser getPostsParser() {
		return mPostsParser;
	}

	private PostsParser mPostsParser = new PostsParser();
	private UserParser mUserParser = new UserParser();

	public void getPosts(String url, OnRespond onRespond) {
		App.getInstance().getHttpClient().get(url, new ApiHttpClientCallback(mPostsParser, onRespond));
	}

	public void getThreads(int page, int fid, int typeid, String order, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getThreads(page, fid, typeid, order, new ApiHttpClientCallback(mThreadsParser, onRespond));
	}

	public void searchThreads(String query, int page, int[] fids, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().searchThreads(query, page, fids, new ApiHttpClientCallback(mThreadsParser, onRespond));
	}

	public void searchUserThreads(String name, int page, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().searchUserThreads(name, page, new ApiHttpClientCallback(mThreadsParser, onRespond));
	}

	public void getUser(int uid, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getUser(uid, new ApiHttpClientCallback(mUserParser, onRespond));
	}

	interface OnRespond {
		void onRespond(Response res);
	}

	protected class ApiHttpClientCallback implements HttpClientCallback {
		private Parse p;
		private OnRespond onRespond;

		public ApiHttpClientCallback(Parse p, OnRespond onRespond) {
			this.p = p;
			this.onRespond = onRespond;
		}

		@Override
		public void onSuccess(String response) {
			new AsyncTask<String, Void, Object>() {
				@Override
				protected Object doInBackground(String... strings) {
					return p.parse(strings[0]);
				}

				@Override
				protected void onPostExecute(Object o) {
					Response res = (Response) o;

					ApiStore store = App.getInstance().getCore().getApiStore();
					Response.Meta meta = res.getMeta();
					store.setUser(meta.getUser());
					store.setCode(meta.getCode());
					store.setFormhash(meta.getFormhash());
					store.setHash(meta.getHash());
					store.setUnread(meta.getUnread());

					onRespond.onRespond(res);
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
		}

		@Override
		public void onFailure(String reason) {
			Response res = new Response();
			res.setSuccess(false);
			res.setData(reason);
			onRespond.onRespond(res);
		}
	}
}
