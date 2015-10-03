package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.rey.material.widget.TabPageIndicator;

public class FragmentThreadsPager extends Fragment implements ActivityMain.OnTypeSelect {
	private MyFragmentPagerAdapter mPagerAdapter;
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
		mActivity.setOnTypeSelect(this);

		mPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager()) {

		};
		mTabs = (TabPageIndicator) rootView.findViewById(R.id.tabs);
		mViewPager = (ViewPager) rootView.findViewById(R.id.pager);

		mViewPager.setAdapter(mPagerAdapter);
		mTabs.setViewPager(mViewPager);
		mTabs.setOnPageChangeListener(mActivity);

		return rootView;
	}

	@Override
	public void onTypeSelect(int fid, int typeId) {
		Fragment fragment = mPagerAdapter.getCurrentFragment();
		((FragmentThreads) fragment).setTypeId(typeId);
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		Fragment mFragment;

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public Fragment getCurrentFragment() {
			return mFragment;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			if (getCurrentFragment() != object) {
				mFragment = ((Fragment) object);
			}
			super.setPrimaryItem(container, position, object);
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
		public Fragment getItem(int position) {
			Bundle bundle = new Bundle();
			int fid = Core.getSelectedForums(getActivity()).get(position).getFid();
			bundle.putInt("fid", fid);
			return FragmentThreads.newInstance(bundle);
		}
	}
}
