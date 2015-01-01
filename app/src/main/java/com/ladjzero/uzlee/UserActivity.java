package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;


public class UserActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		final int uid = getIntent().getIntExtra("uid", -1);

		Core.getHtml("http://www.hi-pda.com/forum/space.php?uid=" + uid, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				User user = Core.parseUser(html);

				ImageView imageView = (ImageView) findViewById(R.id.user_info_img);
				ImageLoader.getInstance().displayImage(user.getImage(), imageView);
			}
		});

		String userName = getIntent().getStringExtra("name");
		setTitle(userName + " 的资料");

		Button searchPosts = (Button) findViewById(R.id.user_btn_1);
		searchPosts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(UserActivity.this, ThreadsActivity.class);
				intent.putExtra("url", "http://www.hi-pda.com/forum/search.php?srchuid=" + uid + "&srchfid=all&srchfrom=0&searchsubmit=yes");
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
