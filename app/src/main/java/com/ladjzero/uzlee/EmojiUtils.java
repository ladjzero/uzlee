package com.ladjzero.uzlee;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;

import com.ladjzero.hipda.Core;

/**
 * Created by ladjzero on 2015/1/25.
 */
public class EmojiUtils {
	static Context context;

	public EmojiUtils(Context context) {
		this.context = context;

		for (String iconKey : Core.iconKeys) {
			addPattern(emoticons, Core.icons.get(iconKey), getEmojiResId(iconKey));
		}
	}

	private final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

	private final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	public int getResId(String variableName, Class<?> c) {

//		try {
//			Field idField = c.getDeclaredField(variableName);
//			return idField.getInt(idField);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return -1;
//		}

		int rid = context.getResources().getIdentifier(variableName, "drawable", "com.ladjzero.uzlee");
		return rid;
	}

	public int getEmojiResId(String iconKey) {
		String[] splits = iconKey.split("/");
		int len = splits.length;
		String s_2 = splits[len-2],
				s_1 = splits[len-1];

		if (s_2.equals("default")) {
			return getResId(s_1.substring(0, s_1.length() - 4), Drawable.class);
		} else {
			String s = s_2 + s_1;
			return getResId(s.substring(0, s.length() - 4), Drawable.class);
		}
	}

	private static void addPattern(Map<Pattern, Integer> map, String smile, int resource) {
		map.put(Pattern.compile(Pattern.quote(smile)), resource);
	}

	public boolean addSmiles(Context context, Spannable spannable) {
		boolean hasChanges = false;

		for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
			Matcher matcher = entry.getKey().matcher(spannable);

			while (matcher.find()) {
				boolean set = true;

				for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
					if (spannable.getSpanStart(span) >= matcher.start()
							&& spannable.getSpanEnd(span) <= matcher.end())
						spannable.removeSpan(span);
					else {
						set = false;
						break;
					}
				}

				if (set) {
					hasChanges = true;
					Bitmap icon = BitmapFactory.decodeResource(context.getResources(), entry.getValue());
					spannable
							.setSpan(new ImageSpan(context, Bitmap.createScaledBitmap(icon, 40, 40, true), ImageSpan.ALIGN_BASELINE),
							matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		return hasChanges;
	}

	public Spannable getSmiledText(Context context, CharSequence text) {
		Spannable spannable = spannableFactory.newSpannable(text);
		addSmiles(context, spannable);
		return spannable;
	}
}
