package com.ladjzero.uzlee;

import java.sql.SQLException;
import java.util.ArrayList;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;
import com.ladjzero.hipda.Core.GetUserCB;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.app.Activity;
import android.content.Context;
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
	GetUserCB userCb;
	DBHelper db;
	Dao<User, Integer> userDao;

	public ThreadsAdapter(Context context, ArrayList<Thread> threads) {
		super(context, R.layout.thread, threads);
		this.context = context;
		core = Core.getInstance(context);
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
		if (img == null) {
			int uid = thread.getAuthor().getId();
			User u;
			try {
				u = userDao.queryForId(uid);
				if (u != null && u.getImage() != null) {
					ImageLoader.getInstance().displayImage(u.getImage(),
							holder.img);
				} else {
					// core.getUserFromServer(thread.getAuthor().getId(),
					// new GetUserCB() {
					//
					// @Override
					// public void onGet(User u) {
					// try {
					// userDao.createOrUpdate(u);
					// } catch (SQLException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					// ImageLoader.getInstance().displayImage(
					// u.getImage(), holder.img);
					// }
					//
					// });
					ImageLoader.getInstance().displayImage("", holder.img);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			ImageLoader.getInstance().displayImage(
					thread.getAuthor().getImage(), holder.img);
		}
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
