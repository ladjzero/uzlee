package com.ladjzero.uzlee;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.uzlee.service.Api;
import com.ladjzero.uzlee.utils.UilUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityUser extends ActivityEasySlide {

	LinearLayout mInfo;
	View chat;
	User mUser;
	int uid;
	@Bind(R.id.user_info_img)
	ImageView mImageView;
	@Bind(R.id.name)
	TextView mNameView;
	@Bind(R.id.level)
	TextView mLevelView;
	@Bind(R.id.uid)
	TextView mUid;
	private LocalApi mLocalApi;
	private AsyncTask mParseTask;
	@OnClick(R.id.user_info_img)
	public void onImageClick() {
		if (mUser != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(UilUtils.getInstance().getFile(mUser.getImage())), "image/*");
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		ButterKnife.bind(this);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mInfo = (LinearLayout) findViewById(R.id.user_info_list);
		chat = findViewById(R.id.chat);

		chat.setVisibility(View.GONE);

		chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUser != null) {
					Intent intent = new Intent(ActivityUser.this, ActivityChat.class);
					intent.putExtra("uid", mUser.getId());
					intent.putExtra("name", mUser.getName());
					startActivity(intent);
				}
			}
		});

		uid = getIntent().getIntExtra("uid", 0);

		setProgressBarIndeterminateVisibility(true);

		mLocalApi = App.getInstance().getCore().getLocalApi();

		App.getInstance().getApi().getUser(uid, new Api.OnRespond() {
			@Override
			public void onRespond(Response res) {
				if (res.isSuccess()) {
					final User user = (User) res.getData();
					ActivityUser.this.mUser = user;

					if (user.getId() != mLocalApi.getUser().getId()) {
						chat.setVisibility(View.VISIBLE);
					}

					ImageLoader.getInstance().displayImage(user.getImage(), mImageView);

					mNameView.setText(user.getName());
					setTitle(user.getName());
					mNameView.getPaint().setFakeBoldText(true);
					mLevelView.setText(user.getLevel());
					mUid.setText("No." + user.getId());

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
				} else {
					showToast(res.getData().toString());
				}
			}
		});

		final String userName = getIntent().getStringExtra("name");
		setTitle(userName);
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

		if (qq != null && qq.length() > 0) strings.add("QQ," + qq);
		strings.add("发帖数量," + totalThreads);
		strings.add("积分," + points);
		strings.add("注册日期," + registerDate);

		return strings;
	}
}
