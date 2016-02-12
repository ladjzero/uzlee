package com.ladjzero.hipda;

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
		core.mApiStore = ApiStore.getStore();
		core.mApiStore.initialize(adapter);
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
