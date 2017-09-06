package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ladjzero.hipda.entities.Thread;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

public class AdapterThreads extends ArrayAdapter<Thread> implements View.OnClickListener {

	ActivityBase context;
	private float mFontSize;
	private int mColorRead, mColorUnread;
	private boolean mShowProfileImage;


	public AdapterThreads(Context context, ArrayList<Thread> threads) {
		super(context, R.layout.list_item_thread, threads);

		this.context = (ActivityBase) context;

		mColorRead = Utils.getThemeColor(context, R.attr.colorTextLighter);
		mColorUnread = Utils.getThemeColor(context, R.attr.colorUnread);

		initalPreferences();
	}

	private void initalPreferences() {
		SharedPreferences setting = context.getSettings();
		String fontsize = setting.getString("font_size", "normal");

		if (fontsize.equals("normal")) {
			mFontSize = 16f;
		} else if (fontsize.equals("big")) {
			mFontSize = 20f;
		} else {
			mFontSize = 24f;
		}

		mShowProfileImage = setting.getBoolean(Constants.PREF_KEY_SHOW_PROFILE_IMAGE, true);
	}

	@Override
	public void notifyDataSetChanged() {
		initalPreferences();
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder() : (PostHolder) row.getTag();

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.list_item_thread, parent, false);

			holder.userWrapper = row.findViewById(R.id.user_wrapper);
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

		if (mShowProfileImage) {
			holder.userWrapper.setVisibility(View.VISIBLE);

			holder.imageMask.setText(Utils.getFirstChar(userName));

			ImageLoader.getInstance().displayImage(imageUrl, holder.image, ActivityBase.LowQualityDisplay, new SimpleImageLoadingListener() {
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
			holder.image.setOnClickListener(this);
		} else {
			holder.userWrapper.setVisibility(View.GONE);
			holder.name.setTag(author);
			holder.name.setOnClickListener(this);
		}

		holder.date.setText(Utils.prettyTime(thread.getDateStr()));
		holder.name.setText(thread.getAuthor().getName());
		holder.title.setText(thread.getTitle());
		holder.title.getPaint().setFakeBoldText(thread.getBold());

		if (color != null && color.length() > 0) {
			holder.title.setTextColor(lowerSaturation(Color.parseColor(color)));
		} else {
			holder.title.setTextColor(Utils.getThemeColor(context, R.attr.colorText));
		}

		holder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSize);

		holder.commentCount.setBackgroundColor(isNew ? mColorUnread : mColorRead);
		holder.commentCount.setText(String.valueOf(count));

		return row;
	}

	@Override
	public void onClick(View view) {
		Integer myId = App.getInstance().getUid();

		if (myId == null) {
			context.showToast(context.getResources().getString(R.string.error_login_required));
		} else {
			User user = (User) view.getTag();
			Intent intent = new Intent(context, ActivityUser.class);
			intent.putExtra("uid", user.getId());
			intent.putExtra("name", user.getName());
			context.startActivity(intent);
		}
	}

	private int lowerSaturation(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] = 0.48f;
		return Color.HSVToColor(hsv);
	}

	static class PostHolder {
		View userWrapper;
		ImageView image;
		TextView imageMask;
		TextView name;
		TextView date;
		TextView title;
		TextView commentCount;
	}
}
