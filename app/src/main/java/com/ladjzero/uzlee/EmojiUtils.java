package com.ladjzero.uzlee;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import com.ladjzero.hipda.Core;

/**
 * Created by ladjzero on 2015/1/25.
 */
public class EmojiUtils {
	static Context context;
	int px;

	public EmojiUtils(Context context) {
		this.context = context;
		px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 26, context.getResources().getDisplayMetrics());

		for (String iconKey : Core.iconKeys) {
			addPattern(emoticons, Core.icons.get(iconKey), getEmojiResId(iconKey));
		}
	}

	private final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

	private final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	public static int getResId(Context context, String variableName, Class<?> c) {
		return context.getResources().getIdentifier(variableName, "drawable", "com.ladjzero.uzlee");
	}

	public int getEmojiResId(String iconKey) {
		String[] splits = iconKey.split("/");
		int len = splits.length;
		String s_2 = splits[len-2],
				s_1 = splits[len-1];

		if (s_2.equals("default")) {
			return getResId(context, s_1.substring(0, s_1.length() - 4), Drawable.class);
		} else {
			String s = s_2 + s_1;
			return getResId(context, s.substring(0, s.length() - 4), Drawable.class);
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
							.setSpan(new ImageSpan(context, Bitmap.createScaledBitmap(icon, px, px, true), ImageSpan.ALIGN_BASELINE),
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
