package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.sql.SQLException;

public class UserActivity extends SwipeActivity {

	private Dao<User, Integer> userDao;
	DiscreteSeekBar seekbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		enableBackAction();

		setContentView(R.layout.activity_user);

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
					ImageLoader.getInstance().displayImage(img, imageView);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Core.getUser(uid, new Core.OnUserListener() {
			@Override
			public void onUser(User u) {
				ImageLoader.getInstance().displayImage(u.getImage(), imageView);

				try {
					userDao.createOrUpdate(u);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		final String userName = getIntent().getStringExtra("name");
		setTitle(userName + "的资料");

		Button searchPosts = (Button) findViewById(R.id.user_btn_1);
		searchPosts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(UserActivity.this, ThreadsActivity.class);
				intent.putExtra("name", userName);
				startActivity(intent);
			}
		});
		searchPosts.setBackgroundResource(R.color.dark_primary);

		final Button ban = (Button) findViewById(R.id.user_btn_2);

		if (Core.bans.contains(uid)) {
			ban.setText("移除黑名单");
			ban.setBackgroundResource(R.color.grass_primary);
		} else {
			ban.setText("加入黑名单");
			ban.setBackgroundResource(R.color.blood_primary);
		}

		ban.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(Core.bans.contains(uid)) {
					Core.removeFromBanList(uid);
					ban.setText("加入黑名单");
					ban.setBackgroundResource(R.color.blood_primary);
				} else {
					Core.addToBanList(uid);
					ban.setText("移除黑名单");
					ban.setBackgroundResource(R.color.grass_primary);
				}
			}
		});

		seekbar = (DiscreteSeekBar) findViewById(R.id.seekbar);
		seekbar.setMin(2);
		seekbar.setMax(10);
	}

	public boolean onTouchEvent(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
