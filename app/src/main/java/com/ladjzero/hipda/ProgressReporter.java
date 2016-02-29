package com.ladjzero.hipda;

/**
 * Created by chenzhuo on 16-2-11.
 */
public interface ProgressReporter {
	void onProgress(int i, int size, Object o);
}
