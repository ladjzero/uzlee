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
import com.ladjzero.hipda.Forum;
import com.ladjzero.uzlee.utils.Utils;
import com.nineoldandroids.animation.Animator;
import com.rey.material.widget.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentThreadsPager extends Fragment implements ActivityMain.OnTypeChange, FragmentThreadsAbs.OnScrollUpOrDown, ActivityBase.OnToolbarClickListener {
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
	private List<ViewPager.OnPageChangeListener> mOnPageChangeListeners;

	public static FragmentThreadsPager newInstance(Bundle bundle) {
		FragmentThreadsPager fragment = new FragmentThreadsPager();
		fragment.mOnPageChangeListeners = new ArrayList<>();

		return fragment;
	}

	public void registerPageChangeListener(ViewPager.OnPageChangeListener l) {
		mOnPageChangeListeners.add(l);
	}

	public void unregisterPageChangeListener(ViewPager.OnPageChangeListener l) {
		mOnPageChangeListeners.remove(l);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.full_view_pager, container, false);
		ButterKnife.bind(this, rootView);

		mActivity = (ActivityMain) getActivity();
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
				if (mOnPageChangeListeners != null) {
					for (ViewPager.OnPageChangeListener l : mOnPageChangeListeners) {
						l.onPageScrolled(position, positionOffset, positionOffsetPixels);
					}
				}

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
				if (mOnPageChangeListeners != null) {
					for (ViewPager.OnPageChangeListener l : mOnPageChangeListeners) {
						l.onPageSelected(position);
					}
				}

				mChanged = true;
				mLastPosition = position;
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (mOnPageChangeListeners != null) {
					for (ViewPager.OnPageChangeListener l : mOnPageChangeListeners) {
						l.onPageScrollStateChanged(state);
					}
				}

				if (state == ViewPager.SCROLL_STATE_DRAGGING) {
					if (!mTabsContainerVisible)
						YoYo.with(Techniques.SlideInDown).duration(0).playOn(tabsContainer);
				} else if (state == ViewPager.SCROLL_STATE_IDLE) {
					mCurrentPosition = POSITION_NOT_READY;

					// If position has been changed successfully, show tabs.
					if (mChanged) {
						onUp(0);
						mChanged = false;
					} else {
						YoYo.with(mTabsContainerVisible ? Techniques.SlideInDown: Techniques.SlideOutUp)
								.duration(0)
								.playOn(tabsContainer);
					}
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
		onUp(0);
		Fragment fragment = mPagerAdapter.getCurrentFragment();
		((FragmentNormalThreads) fragment).setTypeId(typeId);
	}

	@Override
	public void toolbarClick() {
		((FragmentThreadsAbs)mPagerAdapter.getCurrentFragment()).toolbarClick();
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		Fragment mFragment;

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			int count = mActivity.getSelectedForums(getActivity()).size();
			return count;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int fid = mActivity.getSelectedForums(getActivity()).get(position).getFid();
			String title = Forum.findById(mActivity.getForums(getActivity()), fid).getName();
			return title;
		}

		@Override
		public Fragment getItem(int position) {
			int fid = mActivity.getSelectedForums(getActivity()).get(position).getFid();

			Bundle args = new Bundle();
			args.putInt("fid", fid);
			args.putBoolean("enablePullToRefresh", true);

			FragmentThreadsAbs fragment = FragmentNormalThreads.newInstance();
			fragment.setArguments(args);
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
