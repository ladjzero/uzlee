package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.r0adkll.slidr.Slidr;
import com.rey.material.widget.ProgressView;

public class SearchActivity extends BaseActivity implements View.OnKeyListener, ThreadsFragment.OnFetch {

	private ThreadsFragment mFragment;
	private ProgressView proressbar;


	private EditText mSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		Slidr.attach(this);

		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(R.layout.search_action_bar);

		proressbar = (ProgressView) findViewById(R.id.progress_bar);
		proressbar.setVisibility(View.GONE);

		mFragment = new ThreadsFragment();
		mFragment.setOnFetch(this);
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", ThreadsFragment.DATA_SOURCE_SEARCH);
		bundle.putBoolean("enablePullToRefresh", false);
		mFragment.setArguments(bundle);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();


		mSearch = (EditText) findViewById(R.id.search_input);

		mSearch.setOnKeyListener(this);
	}

	@Override
	public boolean onKey(View view, int i, KeyEvent keyEvent) {
		if (keyEvent.getAction() == KeyEvent.ACTION_UP && (i == KeyEvent.KEYCODE_SEARCH || i == KeyEvent.KEYCODE_ENTER)) {
			String query = mSearch.getText().toString();
			query = query.trim();

			if (query.length() != 0) {
				mFragment.updateSearch(query);
			}
		}

		return false;
	}

	@Override
	public void fetchStart() {
		proressbar.setVisibility(View.VISIBLE);
	}

	@Override
	public void fetchEnd() {
		proressbar.setVisibility(View.GONE);
	}
}
