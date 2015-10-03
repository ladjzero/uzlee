package com.ladjzero.uzlee;

import android.content.Context;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ladjzero on 2015/2/28.
 */
public class Utils {
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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

	public static void fadeOut(final View view) {
		YoYo.with(Techniques.FadeOut).duration(100).withListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		}).playOn(view);
	}

	public static void fadeIn(final View view) {
		YoYo.with(Techniques.FadeIn).duration(100).withListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		}).playOn(view);
	}

	public static int parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return 0;
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
