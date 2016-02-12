package com.ladjzero.hipda;

/**
 * Created by chenzhuo on 16-2-11.
 */
public interface HttpClientCallback {
	void onSuccess(String response);

	void onFailure(String reason);
}
