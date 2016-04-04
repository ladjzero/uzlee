package com.ladjzero.uzlee.utils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by chenzhuo on 16-3-12.
 */
public class Constants {
	public static int DEFAULT_IMAGE_UPLOAD_SIZE = 3000; // KB
	public static String PREF_KEY_IMG_SIZE = "img_size";
	public static String PREF_KEY_ENABLE_DOWNLOAD_IMAGE = "enable_image_only_wifi";

	public static final DisplayImageOptions DIO_USER_IMAGE = new DisplayImageOptions.Builder()
			.delayBeforeLoading(800)
			.showImageForEmptyUri(android.R.color.transparent)
			.showImageOnLoading(android.R.color.transparent)
			.showImageOnFail(android.R.color.transparent)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.displayer(new FadeInBitmapDisplayer(300, true, true, false))
			.build();

}
