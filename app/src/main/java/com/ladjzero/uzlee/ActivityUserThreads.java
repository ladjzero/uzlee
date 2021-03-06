package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ActivityUserThreads extends ActivityHardSlide {

	private FragmentThreadsAbs mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		mFragment = new FragmentUserThreads();
		Bundle args = new Bundle();
//		bundle.putInt("dataSource", FragmentThreadsAbs.DATA_SOURCE_USER);
		args.putBoolean("enablePullToRefresh", false);
		String userName = intent.getStringExtra("name");
		args.putString("userName", userName);
		args.putString("title", userName + "的主题");
		mFragment.setArguments(args);

		setContentView(R.layout.activity_threads);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFragment.toolbarClick();
			}
		});
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(args.getString("title"));

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();
	}
}
