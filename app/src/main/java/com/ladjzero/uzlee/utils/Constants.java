package com.ladjzero.uzlee.utils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by chenzhuo on 16-3-12.
 */
public class Constants {
	public static int DEFAULT_IMAGE_UPLOAD_SIZE = 3000; // KB
	public static String PREF_KEY_IMG_SIZE = "img_size";
	public static String PREF_KEY_SHOW_PROFILE_IMAGE = "show_profile_image";
	public static String PREF_KEY_SHOW_TYPES = "show_types";
	public static String DEFAULT_SHOW_TYPES = "6";
	public static String PREF_KEY_SELECTED_FORUMS = "selected_forums";
	public static String DEFAULT_SELECTED_FORUMS = "2,6,59";

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
