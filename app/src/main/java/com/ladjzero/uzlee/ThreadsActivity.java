package com.ladjzero.uzlee;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class ThreadsActivity extends SwipeActivity {

	private ThreadsFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		mFragment = new ThreadsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", ThreadsFragment.DATA_SOURCE_SEARCH);
		bundle.putBoolean("enablePullToRefresh", false);
		String userName = intent.getStringExtra("name");
		bundle.putString("userName", userName);
		bundle.putString("title", userName + "的主题");
		mFragment.setArguments(bundle);

		setContentView(R.layout.threads);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();
	}
}
