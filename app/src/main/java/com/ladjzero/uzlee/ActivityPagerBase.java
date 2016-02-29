package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import com.r0adkll.slidr.model.SlidrInterface;
import com.rey.material.widget.TabPageIndicator;

/**
 * Created by chenzhuo on 15-10-2.
 */
public abstract class ActivityPagerBase extends ActivityEasySlide implements ViewPager.OnPageChangeListener {
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	SparseArray<Fragment> mFragmentCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pager_base);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionBar = getSupportActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView = mInflater.inflate(R.layout.tab_page_indicator_fixed, null);

		actionBar.setTitle(null);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(customView);

		mFragmentCache = new SparseArray<>();
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabPageIndicator tabs = (TabPageIndicator) customView.findViewById(R.id.tabs);

		tabs.setViewPager(mViewPager);
		tabs.setOnPageChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		mFragmentCache.clear();
		super.onDestroy();
	}


	@Override
	public void onPageScrolled(int i, float v, int i2) {
	}

	@Override
	public void onPageSelected(int i) {
		SlidrInterface it = getSlidrInterface();

		if (i == 0) it.unlock();
		else it.lock();
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
			return ActivityPagerBase.this.getItem(position);
		}

		@Override
		public int getCount() {
			return ActivityPagerBase.this.getCount();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return ActivityPagerBase.this.getPageTitle(position);
		}
	}


	public abstract Fragment getItem(int position);

	public abstract int getCount();

	public abstract CharSequence getPageTitle(int position);
}
