package me.ladjzero.uzlee;

import android.content.Context;

/**
 * Created by ladjzero on 2015/2/28.
 */
public class Utils {
	public static String getFirstChar(String input) {
		if (input.length() > 0) {
			String first = input.substring(0, 1);
			char f = first.charAt(0);

			if ('a' <= f && f <= 'z') {
				first = first.toUpperCase();
			}

			return first;
		} else {
			return "";
		}
	}

	public static int getColor(Context context, int resId) {
		return context.getResources().getColor(resId);
	}
}
