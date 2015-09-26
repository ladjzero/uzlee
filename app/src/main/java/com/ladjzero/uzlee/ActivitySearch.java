package com.ladjzero.uzlee;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.rey.material.widget.ProgressView;

public class ActivitySearch extends BaseActivity implements View.OnKeyListener, FragmentThreads.OnFetch {

	private FragmentThreads mFragment;
	private ProgressView proressbar;
	protected SlidrInterface slidrInterface;


	private EditText mSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		slidrInterface = Slidr.attach(this);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
		actionbar.setCustomView(LayoutInflater.from(this).inflate(R.layout.search_action_bar, null));

		proressbar = (ProgressView) findViewById(R.id.progress_bar);
		proressbar.setVisibility(View.GONE);

		mFragment = new FragmentThreads();
		mFragment.setOnFetch(this);
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", FragmentThreads.DATA_SOURCE_SEARCH);
		bundle.putBoolean("enablePullToRefresh", false);
		mFragment.setArguments(bundle);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
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
