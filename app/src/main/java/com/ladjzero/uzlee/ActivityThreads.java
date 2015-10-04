package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class ActivityThreads extends ActivityBase {

	private FragmentThreads mFragment;
	protected SlidrInterface slidrInterface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		mFragment = new FragmentThreads();
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", FragmentThreads.DATA_SOURCE_USER);
		bundle.putBoolean("enablePullToRefresh", false);
		String userName = intent.getStringExtra("name");
		bundle.putString("userName", userName);
		bundle.putString("title", userName + "的主题");
		mFragment.setArguments(bundle);

		setContentView(R.layout.activity_threads);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(bundle.getString("title"));

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();

		slidrInterface = Slidr.attach(this);
	}
}
