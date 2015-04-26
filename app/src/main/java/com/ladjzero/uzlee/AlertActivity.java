package com.ladjzero.uzlee;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

/**
 * Created by ladjzero on 2015/1/1.
 */
public class AlertActivity extends SwipeActivity implements SimpleThreadsFragment.OnFragmentInteractionListener, ViewPager.OnPageChangeListener {
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	SparseArray<Fragment> mFragmentCache;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_my_posts);

		mFragmentCache = new SparseArray<>();
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

		TypedValue tv = new TypedValue();

		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
			ViewGroup.LayoutParams params = tabs.getLayoutParams();
			params.height = actionBarHeight;
			tabs.setLayoutParams(params);
			tabs.setViewPager(mViewPager);
			tabs.setOnPageChangeListener(this);
		}
	}

	@Override
	protected void onDestroy() {
		mFragmentCache.clear();
		super.onDestroy();
	}


	@Override
	public void onFragmentInteraction(String id) {
	}

	@Override
	public void onPageScrolled(int i, float v, int i2) {
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
			Fragment ret = mFragmentCache.get(position);

			if (ret == null) {
				ret = AlertFragment.newInstance(position);
				mFragmentCache.put(position, ret);
			}

			return ret;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "短信";
				case 1:
					return "提醒";
			}
			return null;
		}
	}
}
