package com.ladjzero.uzlee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import com.ladjzero.hipda.Core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ladjzero on 2015/1/25.
 */
public class EmojiUtils {

	public static final HashMap<String, String> icons = new HashMap<String, String>();
	public static final Set<String> iconKeys;
	public static final Collection<String> iconValues;
	static Context context;

	static {
		icons.put("images/smilies/default/smile.gif", ":)");
		icons.put("images/smilies/default/sweat.gif", ":sweat:");
		icons.put("images/smilies/default/huffy.gif", ":huffy:");
		icons.put("images/smilies/default/cry.gif", ":cry:");
		icons.put("images/smilies/default/titter.gif", ":titter:");
		icons.put("images/smilies/default/handshake.gif", ":handshake:");
		icons.put("images/smilies/default/victory.gif", ":victory:");
		icons.put("images/smilies/default/curse.gif", ":curse:");
		icons.put("images/smilies/default/dizzy.gif", ":dizzy:");
		icons.put("images/smilies/default/shutup.gif", ":shutup:");
		icons.put("images/smilies/default/funk.gif", ":funk:");
		icons.put("images/smilies/default/loveliness.gif", ":loveliness:");
		icons.put("images/smilies/default/sad.gif", ":(");
		icons.put("images/smilies/default/biggrin.gif", ":D");
		icons.put("images/smilies/default/cool.gif", ":cool:");
		icons.put("images/smilies/default/mad.gif", ":mad:");
		icons.put("images/smilies/default/shocked.gif", ":o");
		icons.put("images/smilies/default/tongue.gif", ":P");
		icons.put("images/smilies/default/lol.gif", ":lol:");
		icons.put("images/smilies/default/shy.gif", ":shy:");
		icons.put("images/smilies/default/sleepy.gif", ":sleepy:");

		icons.put("images/smilies/coolmonkey/01.gif", "{:2_41:}");
		icons.put("images/smilies/coolmonkey/02.gif", "{:2_42:}");
		icons.put("images/smilies/coolmonkey/03.gif", "{:2_43:}");
		icons.put("images/smilies/coolmonkey/04.gif", "{:2_44:}");
		icons.put("images/smilies/coolmonkey/05.gif", "{:2_45:}");
		icons.put("images/smilies/coolmonkey/06.gif", "{:2_46:}");
		icons.put("images/smilies/coolmonkey/07.gif", "{:2_47:}");
		icons.put("images/smilies/coolmonkey/08.gif", "{:2_48:}");
		icons.put("images/smilies/coolmonkey/09.gif", "{:2_49:}");
		icons.put("images/smilies/coolmonkey/10.gif", "{:2_50:}");
		icons.put("images/smilies/coolmonkey/11.gif", "{:2_51:}");
		icons.put("images/smilies/coolmonkey/12.gif", "{:2_52:}");
		icons.put("images/smilies/coolmonkey/13.gif", "{:2_53:}");
		icons.put("images/smilies/coolmonkey/14.gif", "{:2_54:}");
		icons.put("images/smilies/coolmonkey/15.gif", "{:2_55:}");
		icons.put("images/smilies/coolmonkey/16.gif", "{:2_56:}");

		icons.put("images/smilies/grapeman/01.gif", "{:3_57:}");
		icons.put("images/smilies/grapeman/02.gif", "{:3_58:}");
		icons.put("images/smilies/grapeman/03.gif", "{:3_59:}");
		icons.put("images/smilies/grapeman/04.gif", "{:3_60:}");
		icons.put("images/smilies/grapeman/05.gif", "{:3_61:}");
		icons.put("images/smilies/grapeman/06.gif", "{:3_62:}");
		icons.put("images/smilies/grapeman/07.gif", "{:3_63:}");
		icons.put("images/smilies/grapeman/08.gif", "{:3_64:}");
		icons.put("images/smilies/grapeman/09.gif", "{:3_65:}");
		icons.put("images/smilies/grapeman/10.gif", "{:3_66:}");
		icons.put("images/smilies/grapeman/11.gif", "{:3_67:}");
		icons.put("images/smilies/grapeman/12.gif", "{:3_68:}");
		icons.put("images/smilies/grapeman/13.gif", "{:3_69:}");
		icons.put("images/smilies/grapeman/14.gif", "{:3_70:}");
		icons.put("images/smilies/grapeman/15.gif", "{:3_71:}");
		icons.put("images/smilies/grapeman/16.gif", "{:3_72:}");
		icons.put("images/smilies/grapeman/17.gif", "{:3_73:}");
		icons.put("images/smilies/grapeman/18.gif", "{:3_74:}");
		icons.put("images/smilies/grapeman/19.gif", "{:3_75:}");
		icons.put("images/smilies/grapeman/20.gif", "{:3_76:}");
		icons.put("images/smilies/grapeman/21.gif", "{:3_77:}");
		icons.put("images/smilies/grapeman/22.gif", "{:3_78:}");
		icons.put("images/smilies/grapeman/23.gif", "{:3_79:}");
		icons.put("images/smilies/grapeman/24.gif", "{:3_80:}");

		iconKeys = icons.keySet();
		iconValues = icons.values();
	}

	private final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();
	private final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();
	int px;

	public EmojiUtils(Context context) {
		EmojiUtils.context = context;
		px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 26, context.getResources().getDisplayMetrics());

		for (String iconKey : iconKeys) {
			addPattern(emoticons, icons.get(iconKey), getEmojiResId(iconKey));
		}
	}

	public static int getResId(Context context, String variableName, Class<?> c) {
		return context.getResources().getIdentifier(variableName, "drawable", "com.ladjzero.uzlee");
	}

	private static void addPattern(Map<Pattern, Integer> map, String smile, int resource) {
		map.put(Pattern.compile(Pattern.quote(smile)), resource);
	}

	public int getEmojiResId(String iconKey) {
		String[] splits = iconKey.split("/");
		int len = splits.length;
		String s_2 = splits[len - 2],
				s_1 = splits[len - 1];

		if (s_2.equals("default")) {
			return getResId(context, s_1.substring(0, s_1.length() - 4), Drawable.class);
		} else {
			String s = s_2 + s_1;
			return getResId(context, s.substring(0, s.length() - 4), Drawable.class);
		}
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
