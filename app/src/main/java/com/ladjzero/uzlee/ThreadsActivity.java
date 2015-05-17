package com.ladjzero.uzlee;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;

public class ThreadsActivity extends BaseActivity {

	private ThreadsFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		mFragment = new ThreadsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", ThreadsFragment.DATA_SOURCE_USER);
		bundle.putBoolean("enablePullToRefresh", false);
		String userName = intent.getStringExtra("name");
		bundle.putString("userName", userName);
		bundle.putString("title", userName + "的主题");
		mFragment.setArguments(bundle);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView =  mInflater.inflate(R.layout.toolbar_title_for_post, null);

		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(customView);

		mTitleView = (TextView) customView.findViewById(R.id.title);

		setContentView(R.layout.threads);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();

		Slidr.attach(this);
	}
}
