package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ThreadsAdapter extends ArrayAdapter<Thread> implements View.OnClickListener {

	private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final Date NOW = new Date();
	BaseActivity context;
	Core core;
	private int mCommentBgColor;
	private int mWhite;
	private int mUserNameColor;
	private boolean mHighlightUnread = true;


	public ThreadsAdapter(Context context, ArrayList<Thread> threads) {
		super(context, R.layout.thread, threads);
		this.context = (BaseActivity) context;

		mHighlightUnread = this.context.setting.getBoolean("highlight_unread", true);

		Resources res = context.getResources();
		mCommentBgColor = res.getColor(R.color.commentNoBg);
		mWhite = res.getColor(android.R.color.white);
		mUserNameColor = res.getColor(R.color.snow_darker);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mHighlightUnread = this.context.setting.getBoolean("highlight_unread", true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder() : (PostHolder) row.getTag();

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.thread, parent, false);

			holder.image = (ImageView) row.findViewById(R.id.user_image);
			holder.imageMask = (TextView) row.findViewById(R.id.user_image_mask);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.title = (TextView) row.findViewById(R.id.thread_title);
			holder.date = (TextView) row.findViewById(R.id.thread_date);
			holder.commentCount = (TextView) row.findViewById(R.id.thread_comment_count);

			row.setTag(holder);
		}

		final Thread thread = getItem(position);
		final User author = thread.getAuthor();
		String imageUrl = author.getImage();
		final int uid = author.getId();
		final String userName = author.getName();
		String color = thread.getColor();
		int count = thread.getCommentCount();
		boolean isNew = thread.isNew();

		row.setBackgroundResource(uid == Core.UGLEE_ID ? R.color.uglee : android.R.color.white);
		holder.imageMask.setText(Utils.getFirstChar(userName));

		ImageLoader.getInstance().displayImage(imageUrl, holder.image, BaseActivity.LowQualityDisplay, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
				author.setImage(imageUri);
			}

			@Override
			public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
				((ImageView) view).setImageResource(android.R.color.transparent);
				author.setImage(null);
				holder.imageMask.setText(Utils.getFirstChar(userName));
			}
		});

		holder.image.setTag(author);
		holder.name.setTag(author);
//		holder.name.getPaint().setFakeBoldText(true);
		holder.image.setOnClickListener(this);
		holder.name.setOnClickListener(this);
		holder.date.setText(prettyTime(thread.getDateStr()));
		holder.name.setText(thread.getAuthor().getName());

		if (Core.bans.contains(uid)) {
			holder.title.setText(context.getString(R.string.blocked));
		} else {
			holder.title.setText(thread.getTitle());
			holder.title.getPaint().setFakeBoldText(thread.getBold());
		}

		if (color != null && color.length() > 0) {
			holder.title.setTextColor(lowerSaturation(Color.parseColor(color)));
		} else {
			holder.title.setTextColor(Color.BLACK);
		}

		if (mHighlightUnread) {
			holder.commentCount.setBackgroundResource(isNew ? R.color.commentNoBg : R.color.border);
			holder.commentCount.setTextColor(mWhite);
			holder.commentCount.setText(String.valueOf(count));
			holder.commentCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		} else {
			holder.commentCount.setBackgroundResource(android.R.color.transparent);
			holder.commentCount.setTextColor(mUserNameColor);
			holder.commentCount.setText(count + " " + (isNew ? "{fa-comments}" : "{fa-comments-o}"));
			holder.commentCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		}

		return row;
	}

	@Override
	public void onClick(View view) {
		User user = (User) view.getTag();
		Intent intent = new Intent(context, ActivityUser.class);
		intent.putExtra("uid", user.getId());
		intent.putExtra("name", user.getName());
		context.startActivity(intent);
	}

	private String prettyTime(String timeStr) {
		try {
			Date thatDate = DATE_FORMAT.parse(timeStr);

			if (DateUtils.isSameDay(thatDate, NOW)) {
				return "今天";
			} else if (DateUtils.isSameDay(DateUtils.addDays(thatDate, 1), NOW)) {
				return "昨天";
			} else if (NOW.getYear() == thatDate.getYear()) {
				return DateFormatUtils.format(thatDate, "M月d日");
			} else {
				return DateFormatUtils.format(thatDate, "yyyy年M月d日");
			}
		} catch (ParseException e) {
			return timeStr;
		}
	}

	private int lowerSaturation(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
//		hsv[1] = hsv[1] * 0.43f;
		hsv[1] = 0.48f;
		return Color.HSVToColor(hsv);
	}

	private int lowerSaturation(int color, float rate) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] = hsv[1] * rate;
		return Color.HSVToColor(hsv);
	}

	static class PostHolder {
		ImageView image;
		TextView imageMask;
		TextView name;
		TextView date;
		TextView title;
		TextView commentCount;
	}
}
