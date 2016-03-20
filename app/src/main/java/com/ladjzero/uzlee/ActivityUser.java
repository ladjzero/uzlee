package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.LocalApi;
import com.ladjzero.hipda.User;
import com.ladjzero.hipda.UserParser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class ActivityUser extends ActivityEasySlide {

	LinearLayout mInfo;
	View chat;
	Button block;
	User user;
	int uid;
	private LocalApi mLocalApi;
	private AsyncTask mParseTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mInfo = (LinearLayout) findViewById(R.id.user_info_list);
		chat = findViewById(R.id.chat);
		block = (Button) findViewById(R.id.block);

		chat.setVisibility(View.GONE);
		block.setVisibility(View.GONE);

		chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (user != null) {
					Intent intent = new Intent(ActivityUser.this, ActivityChat.class);
					intent.putExtra("uid", user.getId());
					intent.putExtra("name", user.getName());
					startActivity(intent);
				}
			}
		});

		uid = getIntent().getIntExtra("uid", 0);

		final ImageView imageView = (ImageView) findViewById(R.id.user_info_img);

		setProgressBarIndeterminateVisibility(true);

		mLocalApi = getCore().getLocalApi();

		getApp().getHttpClient().get("http://www.hi-pda.com/forum/space.php?uid=" + uid, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				mParseTask = new AsyncTask<String, Object, User>() {
					@Override
					protected User doInBackground(String... strings) {
						return getCore().getUserParser().parseUser(strings[0]);
					}

					@Override
					protected void onPostExecute(final User user) {
						ActivityUser.this.user = user;

						if (user.getId() != mLocalApi.getUser().getId()) {
							chat.setVisibility(View.VISIBLE);
							block.setVisibility(View.VISIBLE);
						}

						setProgressBarIndeterminateVisibility(false);

						ImageLoader.getInstance().displayImage(user.getImage(), imageView);
						TextView name = (TextView) findViewById(R.id.name);
						TextView level = (TextView) findViewById(R.id.level);
						TextView uid = (TextView) findViewById(R.id.uid);

						name.setText(user.getName());
						setTitle(user.getName());
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
										Intent intent = new Intent(ActivityUser.this, ActivityUserThreads.class);
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
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
			}

			@Override
			public void onFailure(String reason) {
				showToast(reason);
			}
		});

		final String userName = getIntent().getStringExtra("name");
		setTitle(userName);

		updateBlockButton();

		block.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				User banned = new User().setId(uid);

				if (mLocalApi.getBanned().contains(banned)) {
					mLocalApi.deleteBanned(banned);
				} else {
					mLocalApi.insertBanned(banned);
				}

				updateBlockButton();
			}
		});
	}

	private void updateBlockButton() {
		if (mLocalApi.getBanned().contains(new User().setId(uid))) {
			block.setText("移除黑名单");
			block.setBackgroundResource(R.color.greenPrimary);
		} else {
			block.setText("加入黑名单");
			block.setBackgroundResource(R.color.redPrimary);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mParseTask != null && !mParseTask.isCancelled()) {
			mParseTask.cancel(true);
		}
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
