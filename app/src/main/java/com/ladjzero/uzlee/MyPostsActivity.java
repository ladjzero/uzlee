package com.ladjzero.uzlee;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;



public class MyPostsActivity extends BaseActivity implements ActionBar.TabListener, SimpleThreadsFragment.OnFragmentInteractionListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_posts);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		actionBar.setDisplayShowHomeEnabled(false);
//		actionBar.setDisplayShowTitleEnabled(false);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

//		PagerTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
//		tabs.setViewPager(mViewPager);


		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
//		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//			@Override
//			public void onPageSelected(int position) {
//				actionBar.setSelectedNavigationItem(position);
//			}
//		});

		PagerTabStrip tabs = (PagerTabStrip) findViewById(R.id.tabs);

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
			ViewGroup.LayoutParams params = tabs.getLayoutParams();
			params.height = actionBarHeight;
			tabs.setLayoutParams(params);
		}
//		actionBar.hide();

		// For each of the sections in the app, add a tab to the action bar.
//		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//			// Create a tab with text corresponding to the page title defined by
//			// the adapter. Also specify this Activity object, which implements
//			// the TabListener interface, as the callback (listener) for when
//			// this tab is selected.
//			actionBar.addTab(
//					actionBar.newTab()
//							.setText(mSectionsPagerAdapter.getPageTitle(i))
//							.setTabListener(this));
//		}
	}


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
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onFragmentInteraction(String id) {

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return SimpleThreadsFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 0:
					return "帖子";
				case 1:
					return "回复";
				case 2:
					return "收藏";
			}
			return null;
		}
	}
}
