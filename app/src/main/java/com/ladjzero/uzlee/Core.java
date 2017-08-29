package com.ladjzero.uzlee;
import com.ladjzero.uzlee.api.ApiStore;
// TODO. remove this class
public class Core {

	private LocalApi mLocalApi;

	/**
	 * Use singleton utils out of box.
	 * @param adapter
	 * @return
	 */
	public static Core initialize(PersistenceAdapter adapter) {
		Core core = new Core();
		ApiStore.initialize(adapter);
		core.mLocalApi = new LocalApi();
		return core;
	}

	public LocalApi getLocalApi() {
		return mLocalApi;
	}

}
