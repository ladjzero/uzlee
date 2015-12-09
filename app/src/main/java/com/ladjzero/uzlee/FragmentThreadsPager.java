package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.rey.material.widget.TabPageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentThreadsPager extends Fragment implements ActivityMain.OnTypeChange, FragmentThreads.OnScrollUpOrDown {
	@Bind(R.id.pager)
	ViewPager mViewPager;
	@Bind(R.id.tabs)
	TabPageIndicator mTabs;
	@Bind(R.id.tabsContainer)
	View tabsContainer;

	private MyFragmentPagerAdapter mPagerAdapter;
	private ActivityMain mActivity;
	private boolean mTabsContainerVisible = true;
	private boolean mLockAnimation = false;

	public static FragmentThreadsPager newInstance(Bundle bundle) {
		FragmentThreadsPager fragment = new FragmentThreadsPager();

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.full_view_pager, container, false);
		ButterKnife.bind(this, rootView);

		mActivity = (ActivityMain) getActivity();
		mActivity.setOnTypeChange(this);
		mPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mTabs.setViewPager(mViewPager);

		mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			private final int POSITION_NOT_READY = -10;
			private int mCurrentPosition = POSITION_NOT_READY;
			private int mState = ViewPager.SCROLL_STATE_IDLE;
			private boolean mChanged = false;
			private int mLastPosition;

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mActivity.onPageScrolled(position, positionOffset, positionOffsetPixels);

				// Skip initial state.
				if (mState == ViewPager.SCROLL_STATE_IDLE) return;

				// Record the position when begin to drag.
				if (mState == ViewPager.SCROLL_STATE_DRAGGING && mCurrentPosition == POSITION_NOT_READY) {
					mCurrentPosition = mLastPosition;
				}

				if (mState != ViewPager.SCROLL_STATE_IDLE && !mTabsContainerVisible) {
					tabsContainer.setAlpha(mCurrentPosition == position ? positionOffset : 1 - positionOffset);
				}
			}

			@Override
			public void onPageSelected(int position) {
				mActivity.onPageSelected(position);

				mChanged = true;
				mLastPosition = position;
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				mActivity.onPageScrollStateChanged(state);

				if (state == ViewPager.SCROLL_STATE_DRAGGING) {
					if (!mTabsContainerVisible) YoYo.with(Techniques.SlideInDown).duration(0).playOn(tabsContainer);
				} else if (state == ViewPager.SCROLL_STATE_IDLE) {
					mCurrentPosition = POSITION_NOT_READY;
				}

				// If position has been changed successfully, show tabs.
				if (mChanged) {
					onUp(0);
					mChanged = false;
				}

				mState = state;
			}
		});

		return rootView;
	}

	@Override
	public void onUp(int ms) {
		if (mTabsContainerVisible || mLockAnimation) return;

		YoYo.with(Techniques.SlideInDown).withListener(new Utils.OnAnimatorStartEndListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mLockAnimation = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mTabsContainerVisible = true;
				mLockAnimation = false;
			}
		}).duration(ms).playOn(tabsContainer);
	}

	@Override
	public void onDown(int ms) {
		if (!mTabsContainerVisible || mLockAnimation) return;

		YoYo.with(Techniques.SlideOutUp).withListener(new Utils.OnAnimatorStartEndListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mLockAnimation = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mLockAnimation = mTabsContainerVisible = false;
			}
		}).duration(ms).playOn(tabsContainer);
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
			FragmentThreads fragment = FragmentThreads.newInstance(bundle);
			fragment.setScrollUpOrDownListener(FragmentThreadsPager.this);

			return fragment;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			if (getCurrentFragment() != object) {
				mFragment = ((Fragment) object);
			}
			super.setPrimaryItem(container, position, object);
		}

		public Fragment getCurrentFragment() {
			return mFragment;
		}
	}
}
