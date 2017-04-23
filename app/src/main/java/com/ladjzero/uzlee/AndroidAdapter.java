package com.ladjzero.uzlee;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * Created by chenzhuo on 16-2-11.
 */
public class AndroidAdapter implements PersistenceAdapter {
	private SharedPreferences mPref;

	public AndroidAdapter(Context context) {
		mPref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public <T> T getValue(String key, Class<T> t, T defaultValue) {
		Object ret;

		if (t.equals(Integer.class)) {
			ret = mPref.getInt(key, (Integer) defaultValue);
		} else if (t.equals(Long.class)) {
			ret = mPref.getLong(key, (Long) defaultValue);
		} else if (t.equals(Float.class)) {
			ret = mPref.getFloat(key, (Float) defaultValue);
		} else if (t.equals(Boolean.class)) {
			ret = mPref.getBoolean(key, (Boolean) defaultValue);
		} else if (t.equals(String.class)) {
			ret = mPref.getString(key, (String) defaultValue);
		} else {
			String value = mPref.getString(key, null);

			if (value == null) {
				return defaultValue;
			} else {
				if (List.class.isAssignableFrom(t)) {
					ret = JSON.parseArray(value);
				} else {
					ret = JSON.parse(value);
				}
			}
		}

		return (T) ret;
	}

	@Override
	public void putValue(String key, Object value) {
		SharedPreferences.Editor editor = mPref.edit();

		if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Long) {
			editor.putLong(key, (Long) value);
		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		} else if (value instanceof String) {
			editor.putString(key, (String) value);
		} else {
			editor.putString(key, JSON.toJSONString(value, SerializerFeature.WriteClassName));
		}

		editor.commit();
	}
}
