package com.ladjzero.uzlee.utils;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;

import java.io.File;

/**
 * Created by R9NKCC3 on 2016/2/22.
 */
public class UilUtils {
	private DiskCache defaultCache;

	public UilUtils(Context context) {
		defaultCache = DefaultConfigurationFactory.createDiskCache(context, DefaultConfigurationFactory.createFileNameGenerator(), 0, 0);
	}

	public File getFile(String uri) {
		return defaultCache.get(uri);
	}
}
