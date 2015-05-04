package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import java.sql.SQLException;

public class UserActivity extends SwipeActivity {

	private Dao<User, Integer> userDao;
	LinearLayout mInfo;
	Button chat;
	Button block;
	User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
//		enableBackAction();
		setContentView(R.layout.activity_user);
		mInfo = (LinearLayout) findViewById(R.id.user_info_list);
		chat = (Button) findViewById(R.id.chat);
		block = (Button) findViewById(R.id.block);

		chat.setVisibility(View.GONE);
		block.setVisibility(View.GONE);

		chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (user != null) {
					Intent intent = new Intent(UserActivity.this, ChatActivity.class);
					intent.putExtra("uid", user.getId());
					intent.putExtra("name", user.getName());
					startActivity(intent);
				}
			}
		});

		try {
			userDao = getHelper().getUserDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		final int uid = getIntent().getIntExtra("uid", -1);

		final ImageView imageView = (ImageView) findViewById(R.id.user_info_img);

		try {
			User user = userDao.queryForId(uid);

			if (user != null) {
				String img = user.getImage();

				if (img != null) {
					ImageLoader.getInstance().displayImage(img, imageView, DisplayImageOptions.createSimple());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		setProgressBarIndeterminateVisibility(true);

		Core.getUser(uid, new Core.OnUserListener() {
			@Override
			public void onUser(final User u) {
				user = u;

				if (user.getId() != Core.getUid()) {
					chat.setVisibility(View.VISIBLE);
					block.setVisibility(View.VISIBLE);
				}

				setProgressBarIndeterminateVisibility(false);

				ImageLoader.getInstance().displayImage(user.getImage(), imageView);
				TextView name = (TextView) findViewById(R.id.name);
				TextView level = (TextView) findViewById(R.id.level);
				TextView uid = (TextView) findViewById(R.id.uid);

				name.setText(user.getName());
				name.getPaint().setFakeBoldText(true);
				level.setText(user.getLevel());
				uid.setText("No." + user.getId());

				for (String kv : propertyToString(user)) {
					View view = getLayoutInflater().inflate(R.layout.user_info_row, null, false);
					TextView key = (TextView) view.findViewById(R.id.key);
					TextView value = (TextView) view.findViewById(R.id.value);
					View more = view.findViewById(R.id.more);

					String[] strings = kv.split(",");
					key.setText(strings[0]);
					value.setText(strings[1]);

					if (strings[0].equals("发帖数量")) {
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(UserActivity.this, ThreadsActivity.class);
								intent.putExtra("name", user.getName());
								startActivity(intent);
							}
						});

						more.setVisibility(View.VISIBLE);
					} else {
						more.setVisibility(View.INVISIBLE);
					}

					mInfo.addView(view);
				}

				try {
					userDao.createOrUpdate(user);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		final String userName = getIntent().getStringExtra("name");

//		searchPosts.setBackgroundResource(R.color.dark_primary);


		if (Core.bans.contains(uid)) {
			block.setText("移除黑名单");
//			ban.setBackgroundResource(R.color.grass_primary);
		} else {
			block.setText("加入黑名单");
//			ban.setBackgroundResource(R.color.blood_primary);
		}

		block.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Core.bans.contains(uid)) {
					Core.removeFromBanList(uid);
					block.setText("加入黑名单");
//					ban.setBackgroundResource(R.color.blood_primary);
				} else {
					Core.addToBanList(uid);
					block.setText("移除黑名单");
//					ban.setBackgroundResource(R.color.grass_primary);
				}
			}
		});
	}

	public ArrayList<String> propertyToString(User user) {
		ArrayList<String> strings = new ArrayList<>();

		String qq = user.getQq();
		String registerDate = user.getRegisterDateStr();
		String totalThreads = user.getTotalThreads();
		String points = user.getPoints();

		if (qq != null && qq.length() > 0) strings.add("{fa-qq}," + qq);
		strings.add("发帖数量," + totalThreads);
		strings.add("积分," + points);
		strings.add("注册日期," + registerDate);

		return strings;
	}
}
