package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ThreadsAdapter extends ArrayAdapter<Thread> {

	Context context;
	Core core;
	DBHelper db;
	Dao<User, Integer> userDao;

	public ThreadsAdapter(Context context, ArrayList<Thread> threads) {
		super(context, R.layout.thread, threads);
		this.context = context;
		try {
			userDao = ((BaseActivity) context).getHelper().getUserDao();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder()
				: (PostHolder) row.getTag();

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(R.layout.thread, parent, false);

			holder.img = (ImageView) row.findViewById(R.id.user_mini_image);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.title = (TextView) row.findViewById(R.id.thread_title);
			holder.commentCount = (TextView) row
					.findViewById(R.id.thread_comment_count);

			row.setTag(holder);
		}

		final Thread thread = getItem(position);
		String img = thread.getAuthor().getImage();
		final int uid = thread.getAuthor().getId();
		final String userName = thread.getAuthor().getName();

		if (img == null) {
			try {
				User u = userDao.queryForId(uid);

				if (u != null && u.getImage() != null) {
					ImageLoader.getInstance().displayImage(u.getImage(), holder.img, new SimpleImageLoadingListener() {

						@Override
						public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
							thread.getAuthor().setImage(imageUri);
						}

						@Override
						public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
							thread.getAuthor().setImage("");
						}
					});
				} else {
					// TODO replace default image
					ImageLoader.getInstance().displayImage("", holder.img);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			ImageLoader.getInstance().displayImage(img, holder.img);
		}

		holder.img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, UserActivity.class);
				intent.putExtra("uid", uid);
				intent.putExtra("name", userName);
				context.startActivity(intent);
			}
		});

		holder.name.setText(thread.getAuthor().getName());
		holder.title.setText(thread.getTitle());
		holder.commentCount.setText(String.valueOf(thread.getCommentCount()));

		if (!thread.isNew()) {
			holder.commentCount.setBackgroundResource(R.color.border);
		} else {
			holder.commentCount.setBackgroundResource(R.color.commentNoBg);
		}

		return row;
	}

	static class PostHolder {
		ImageView img;
		TextView name;
		TextView date;
		TextView title;
		TextView commentCount;
	}
}
