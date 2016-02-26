package com.ladjzero.uzlee.utils;

import android.content.Context;

/**
 * Created by R9NKCC3 on 2016/2/18.
 */
public class Timeline {
	private long now = 0;
	public static final String TAG = "TIMELINE";

	public long timeLine() {
		long next = System.currentTimeMillis();
		long span = now == 0 ? now : next - now;
		now = next;
		return span;
	}

	public Timeline reset() {
		now = 0;
		return this;
	}
}
