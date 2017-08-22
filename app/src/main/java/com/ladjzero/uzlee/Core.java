package com.ladjzero.uzlee;

import com.ladjzero.hipda.PostsParser;
import com.ladjzero.hipda.ThreadsParser;
import com.ladjzero.hipda.UserParser;
import com.ladjzero.uzlee.service.ApiStore;
import com.ladjzero.uzlee.service.HttpApi;
import com.ladjzero.uzlee.service.HttpClient;

public class Core {

	private ApiStore mApiStore;
	private PostsParser mPostsParser;
	private ThreadsParser mThreadsParser;
	private UserParser mUserParser;
	private HttpApi mHttpApi;
	private LocalApi mLocalApi;

	/**
	 * Use singleton utils out of box.
	 * @param adapter
	 * @param client
	 * @return
	 */
	public static Core initialize(PersistenceAdapter adapter, HttpClient client) {
		Core core = new Core();
		ApiStore.initialize(adapter);
		core.mApiStore = ApiStore.getStore();
		core.mPostsParser = new PostsParser();
		core.mThreadsParser = new ThreadsParser();
		core.mUserParser = new UserParser();
		core.mHttpApi = new HttpApi(client);
		core.mLocalApi = new LocalApi();

		return core;
	}

	public ApiStore getApiStore() {
		return mApiStore;
	}

	public PostsParser getPostsParser() {
		return mPostsParser;
	}

	public ThreadsParser getThreadsParser() {
		return mThreadsParser;
	}

	public UserParser getUserParser() {
		return mUserParser;
	}

	public LocalApi getLocalApi() {
		return mLocalApi;
	}

	public HttpApi getHttpApi() {
		return mHttpApi;
	}
}
