package com.ladjzero.uzlee;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Guide;
import com.rey.material.widget.TabPageIndicator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by chenzhuo on 15-8-13.
 */
public class ThreadsPagerFragment extends Fragment implements ViewPager.OnPageChangeListener{
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private TabPageIndicator mTabs;
	private HashMap<Integer, Fragment> fragmentCache;
	private static ArrayList<Guide.Topic> selectedTopics = (ArrayList<Guide.Topic>) CollectionUtils.select(Guide.AllTopics, new Predicate() {
		@Override
		public boolean evaluate(Object o) {
			int fid = ((Guide.Topic) o).fid;

			return fid == 2 || fid == 6 || fid == 59;
		}
	});


	public static ThreadsPagerFragment newInstance(Bundle bundle) {
		ThreadsPagerFragment fragment = new ThreadsPagerFragment();
		fragment.fragmentCache = new HashMap<>();

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.full_view_pager, container, false);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
		mTabs = (TabPageIndicator) rootView.findViewById(R.id.tabs);
		mViewPager = (ViewPager) rootView.findViewById(R.id.pager);

		mViewPager.setAdapter(mSectionsPagerAdapter);
		mTabs.setViewPager(mViewPager);
//		mTabs.setOnPageChangeListener(this);

		rootView.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), GuidePicker.class);
				startActivity(intent);
			}
		});

		return rootView;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		mViewPager.setCurrentItem(position, true);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment ret = fragmentCache.get(position);

			if (ret == null) {
				Bundle bundle = new Bundle();
				bundle.putInt("fid", selectedTopics.get(position).fid);

				ret = ThreadsFragment.newInstance(bundle);
				fragmentCache.put(position, ret);
			}

			return ret;
		}

		@Override
		public int getCount() {
			return selectedTopics.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return selectedTopics.get(position).title;
		}
	}

}
