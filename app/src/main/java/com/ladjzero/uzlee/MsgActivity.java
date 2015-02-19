package com.ladjzero.uzlee;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ladjzero on 2015/1/1.
 */
public class MsgActivity extends SwipeActivity implements MsgFragment.OnFragmentInteractionListener, ViewPager.OnPageChangeListener {
	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	int page = 0;
	float x = -1;
	float xDelta = 0;
	HashMap<Integer, Fragment> fragmentCache;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		enableBackAction();
		getActionBar().hide();
		setContentView(R.layout.activity_my_posts);
		fragmentCache = new HashMap<Integer, Fragment>();


		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
//        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//        });

		PagerTabStrip tabs = (PagerTabStrip) findViewById(R.id.tabs);

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
			ViewGroup.LayoutParams params = tabs.getLayoutParams();
			params.height = actionBarHeight;
			tabs.setLayoutParams(params);
		}
	}

	@Override
	protected void onDestroy() {
		fragmentCache.clear();
		super.onDestroy();
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent e) {
//		if (page > 0) {
//			setEnableSwipe(false);
//			return false;
//		} else {
//			if (x < 0) {
//				x = e.getX();
//				return true;
//			} else {
//				xDelta = e.getX() - x;
//				setEnableSwipe(xDelta > 0);
//				x = -1;
//				return false;
//			}
//		}
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_my_posts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onFragmentInteraction(String id) {

	}

	@Override
	public void onPageScrolled(int i, float v, int i2) {
//		setEnableSwipe(i == 0 && i2 <= 0);
	}

	@Override
	public void onPageSelected(int i) {
		setEnableSwipe(i == 0);
	}

	@Override
	public void onPageScrollStateChanged(int i) {

	}

	/**
	 * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment ret = fragmentCache.get(position);

			if (ret == null) {
				ret = MsgFragment.newInstance(position);
				fragmentCache.put(position, ret);
			}

			return ret;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 0:
					return "提醒";
				case 1:
					return "短信";
			}
			return null;
		}
	}
}
