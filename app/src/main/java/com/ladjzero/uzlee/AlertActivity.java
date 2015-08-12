package com.ladjzero.uzlee;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;
import com.rey.material.widget.TabPageIndicator;

/**
 * Created by ladjzero on 2015/1/1.
 */
public class AlertActivity extends BaseActivity implements SimpleThreadsFragment.OnFragmentInteractionListener, ViewPager.OnPageChangeListener {
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	SparseArray<Fragment> mFragmentCache;
	SlidrInterface slidrInterface;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_pager);

		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(R.layout.view_page_bar);

		mFragmentCache = new SparseArray<>();
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

//		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		TabPageIndicator tabs = (TabPageIndicator) findViewById(R.id.tabs);

		tabs.setViewPager(mViewPager);
		tabs.setOnPageChangeListener(this);

		slidrInterface = Slidr.attach(this, new SlidrConfig.Builder()
				.position(SlidrPosition.LEFT)
				.sensitivity(1f)
				.build());
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
//		setEnableSwipe(i == 0);
		if (i == 0) slidrInterface.unlock();
		else slidrInterface.lock();
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
