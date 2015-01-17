package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.sql.SQLException;

public class UserActivity extends BaseActivity {

	private Dao<User, Integer> userDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableBackAction();

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
			String img = user.getImage();

			if (img != null) {
				ImageLoader.getInstance().displayImage(img, imageView);
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
				intent.putExtra("uid", uid);
				startActivity(intent);
			}
		});
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
