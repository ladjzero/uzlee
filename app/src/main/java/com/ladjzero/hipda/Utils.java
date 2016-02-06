package com.ladjzero.hipda;

import android.content.Context;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenzhuo on 15-9-19.
 */
public class Utils {
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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

	public static String prettyTime(String timeStr) {
		Date mNow = new Date();

		try {
			Date thatDate = dateFormat.parse(timeStr);

			if (DateUtils.isSameDay(thatDate, mNow)) {
				return DateFormatUtils.format(thatDate, "HH:mm");
			} else if (DateUtils.isSameDay(DateUtils.addDays(thatDate, 1), mNow)) {
				return DateFormatUtils.format(thatDate, "昨天 HH:mm");
			} else if (mNow.getYear() == thatDate.getYear()) {
				return DateFormatUtils.format(thatDate, "M月d日");
			} else {
				return DateFormatUtils.format(thatDate, "yyyy年M月d日");
			}
		} catch (ParseException e) {
			return timeStr;
		}
	}
}
