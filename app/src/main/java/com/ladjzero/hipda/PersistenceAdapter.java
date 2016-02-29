package com.ladjzero.hipda;

/**
 * Created by chenzhuo on 16-2-11.
 */
public interface PersistenceAdapter {
	<T> T getValue(String key, Class<T> t, T defaultValue);

	void putValue(String key, Object value);
}
