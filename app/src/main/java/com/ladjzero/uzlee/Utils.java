package com.ladjzero.uzlee;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.orhanobut.logger.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

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

	public static String toHtml(Posts posts) {
		Logger.d(JSON.toJSONString(posts));

		return StringUtils.join(CollectionUtils.collect(posts, new Transformer() {
			@Override
			public Object transform(Object o) {
				Post post = (Post) o;
				User user = post.getAuthor();

				return "<img src=\"" + user.getImage() + "\" onclick=\"ActivityPosts.onUserClick(2)\"><h3>" + user.getName() + "</h3>" + JSON.toJSONString(post);
			}
		}), "");
	}
}
