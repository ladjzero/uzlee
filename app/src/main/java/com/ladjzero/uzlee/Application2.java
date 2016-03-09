package com.ladjzero.uzlee;

import android.app.Application;
import android.util.LruCache;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.PersistenceAdapter;
import com.ladjzero.uzlee.utils.UilUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by ladjzero on 2015/1/2.
 */
public class Application2 extends Application {

	private LruCache<String, String> mMemCache;
	private HttpClient2 mHttpClient;
	private Core mCore;

	public HttpClient2 getHttpClient() {
		return mHttpClient;
	}

	public Core getCore() {
		return mCore;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Iconify.with(new FontAwesomeModule()).with(new MaterialModule());

		// 10MB.
		mMemCache = new LruCache<String, String>(1024 * 1024 * 10) {

			@Override
			protected int sizeOf(String key, String value) {
				return value.length();
			}
		};

		mHttpClient = new HttpClient2(this);
		PersistenceAdapter adapter = new AndroidAdapter(this);
		mCore = Core.initialize(adapter, mHttpClient);
		UilUtils.init(this);

		Logger.init();
	}

	public LruCache<String, String> getMemCache() {
		return mMemCache;
	}
}
