package com.ladjzero.uzlee;

import android.app.Application;
import android.util.LruCache;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.orhanobut.logger.Logger;

/**
 * Created by ladjzero on 2015/1/2.
 */
public class MyApplication extends Application {

	private LruCache<String, String> mMemCache;

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

		Logger.init();
	}

	public LruCache<String, String> getMemCache() {
		return mMemCache;
	}
}
