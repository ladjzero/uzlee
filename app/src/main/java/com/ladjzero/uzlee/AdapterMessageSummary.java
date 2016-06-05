package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by ladjzero on 2015/4/25.
 */
public class AdapterMessageSummary extends ArrayAdapter<Thread> implements View.OnClickListener{
	ActivityBase context;
	Core core;
	private LocalApi mLocalApi;


	public AdapterMessageSummary(Context context, ArrayList<Thread> threads) {
		super(context, R.layout.message_row, threads);
		this.context = (ActivityBase) context;
		mLocalApi = App.getInstance().getCore().getLocalApi();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder() : (PostHolder) row.getTag();

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.message_row, parent, false);

			holder.image = (ImageView) row.findViewById(R.id.user_image);
			holder.imageMask = (TextView) row.findViewById(R.id.user_image_mask);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.title = (TextView) row.findViewById(R.id.message);
			holder.date = (TextView) row.findViewById(R.id.thread_date);

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

		holder.imageMask.setText(com.ladjzero.uzlee.utils.Utils.getFirstChar(userName));

		ImageLoader.getInstance().displayImage(imageUrl, holder.image, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
				author.setImage(imageUri);
			}

			@Override
			public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
				((ImageView) view).setImageResource(android.R.color.transparent);
				author.setImage(null);
				holder.imageMask.setText(com.ladjzero.uzlee.utils.Utils.getFirstChar(userName));
			}
		});

		holder.image.setTag(author);
		holder.name.setTag(author);
//		holder.name.getPaint().setFakeBoldText(true);
		holder.image.setOnClickListener(this);
		holder.name.setOnClickListener(this);
		holder.date.setText(com.ladjzero.uzlee.utils.Utils.prettyTime(thread.getDateStr()));
		holder.name.setText(thread.getAuthor().getName());

		if (mLocalApi.getBanned().contains(new User().setId(uid))) {
			holder.title.setText(context.getString(R.string.blocked));
		} else {
			holder.title.setText(thread.getTitle());
			holder.title.getPaint().setFakeBoldText(thread.getBold());
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
		TextView title;
		TextView name;
		TextView date;
		ImageView image;
		TextView imageMask;
	}
}
