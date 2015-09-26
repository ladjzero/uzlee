package com.ladjzero.uzlee;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.rey.material.widget.TabPageIndicator;

import java.util.HashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import de.greenrobot.event.EventBus;

/**
 * Created by chenzhuo on 15-8-13.
 */
public class FragmentThreadsPager extends Fragment implements ActivityMain.OnTypeSelect, SharedPreferences.OnSharedPreferenceChangeListener {
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private TabPageIndicator mTabs;
	private ActivityMain mActivity;

	public static FragmentThreadsPager newInstance(Bundle bundle) {
		FragmentThreadsPager fragment = new FragmentThreadsPager();

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.full_view_pager, container, false);
		mActivity = (ActivityMain) getActivity();


		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
//		mTabs = (TabPageIndicator) ((ActivityMain)getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.tabs);
		mTabs = (TabPageIndicator) rootView.findViewById(R.id.tabs);
		mViewPager = (ViewPager) rootView.findViewById(R.id.pager);

		mViewPager.setAdapter(mSectionsPagerAdapter);
		mTabs.setViewPager(mViewPager);
		mTabs.setOnPageChangeListener(mActivity);

		mActivity.setOnTypeSelect(this);


		return rootView;
	}

	@Override
	public void onTypeSelect(int fid, int typeId) {
		((FragmentThreads) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).setTypeId(typeId);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(ActivitySettings.SelectedForumsChangeEvent e) {
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		HashMap<Integer, Fragment> cache = new HashMap<>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = cache.get(position);

			if (fragment == null) {
				Bundle bundle = new Bundle();
				int fid = Core.getSelectedForums(getActivity()).get(position).getFid();
				bundle.putInt("fid", fid);
				fragment = FragmentThreads.newInstance(bundle);
				cache.put(fid, fragment);
			}

			return fragment;
		}

		@Override
		public int getCount() {
			int count = Core.getSelectedForums(getActivity()).size();
			return count;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int fid = Core.getSelectedForums(getActivity()).get(position).getFid();
			String title = Forum.findById(Core.getForums(getActivity()), fid).getName();
			return title;
		}

		@Override
		public void notifyDataSetChanged() {
			cache.clear();
			super.notifyDataSetChanged();
		}
	}

}
