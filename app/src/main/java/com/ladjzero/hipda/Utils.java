package com.ladjzero.hipda;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by chenzhuo on 15-9-19.
 */
public class Utils {
	public static String readAssetFile(Context context, String file) {
		BufferedReader reader = null;
		StringBuilder ret = new StringBuilder();

		try {
			reader = new BufferedReader(new InputStreamReader(context.getAssets().open(file), "UTF-8"));

			String mLine = reader.readLine();

			while (mLine != null) {
				ret.append(mLine);
				mLine = reader.readLine();
			}

			return ret.toString();
		} catch (IOException e) {
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
