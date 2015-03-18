package com.ladjzero.uzlee;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Layout;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class SearchActivity extends SwipeActivity implements View.OnKeyListener, ThreadsFragment.OnFetch {

	private ThreadsFragment mFragment;


	private EditText mSearch;
	private TextView mClose;
	private View mProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();

		mFragment = new ThreadsFragment();
		mFragment.setOnFetch(this);
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", ThreadsFragment.DATA_SOURCE_SEARCH);
		bundle.putBoolean("enablePullToRefresh", false);
		mFragment.setArguments(bundle);

		setContentView(R.layout.search);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.place_holder, mFragment);
		ft.commit();

		mClose = (TextView) findViewById(R.id.search_close);
		mClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String q = mSearch.getText().toString();
				if (q.length() == 0) {
					finish();
				} else {
					mSearch.setText("");
				}
			}
		});

		mSearch = (EditText) findViewById(R.id.search_input);

		View view = findViewById(R.id.action_container);

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
			ViewGroup.LayoutParams params = view.getLayoutParams();
			params.height = actionBarHeight;
			view.setLayoutParams(params);
			mSearch.setHeight(actionBarHeight);
		}

		mSearch.setOnKeyListener(this);

		mProgress = findViewById(R.id.progress);
		mProgress.setVisibility(View.INVISIBLE);
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
		mClose.setVisibility(View.GONE);
		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void fetchEnd() {
		mClose.setVisibility(View.VISIBLE);
		mProgress.setVisibility(View.GONE);
	}
}
