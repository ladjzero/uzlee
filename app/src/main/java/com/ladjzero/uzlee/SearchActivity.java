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

import com.r0adkll.slidr.Slidr;

public class SearchActivity extends BaseActivity implements View.OnKeyListener, ThreadsFragment.OnFetch {

	private ThreadsFragment mFragment;


	private EditText mSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		Slidr.attach(this);

		mActionbar.setIcon(null);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(R.layout.search_action_bar);

		mFragment = new ThreadsFragment();
		mFragment.setOnFetch(this);
		Bundle bundle = new Bundle();
		bundle.putInt("dataSource", ThreadsFragment.DATA_SOURCE_SEARCH);
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
	}

	@Override
	public void fetchEnd() {
	}
}
