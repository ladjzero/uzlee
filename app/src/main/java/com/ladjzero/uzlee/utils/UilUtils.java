package com.ladjzero.uzlee.utils;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;

import java.io.File;

/**
 * Created by R9NKCC3 on 2016/2/22.
 */
public class UilUtils {
	private static boolean initialized;
	private static UilUtils instance;

	private DiskCache defaultCache;

	public static void init(Context context) {
		instance = new UilUtils();
		instance.defaultCache = DefaultConfigurationFactory.createDiskCache(context, DefaultConfigurationFactory.createFileNameGenerator(), 0, 0);
		initialized = true;
	}

	public static UilUtils getInstance() {
		throwErrorIfNotInitialized();
		return instance;
	}

	public File getFile(String uri) {
		return defaultCache.get(uri);
	}

	private static void throwErrorIfNotInitialized() {
		if (!initialized) {
			throw new Error("Call init first.");
		}
	}
}