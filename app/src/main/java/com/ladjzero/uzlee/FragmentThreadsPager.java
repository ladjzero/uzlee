package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Forum;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentThreadsPager extends Fragment implements FragmentThreadsAbs.OnScrollUpOrDown, ActivityBase.OnToolbarClickListener {
	@Bind(R.id.pager)
	ViewPager mViewPager;

	private MyFragmentPagerAdapter mPagerAdapter;
	private ActivityMain mActivity;
	private boolean mTabsContainerVisible = true;
	private boolean mLockAnimation = false;
	private List<ViewPager.OnPageChangeListener> mOnPageChangeListeners;
	private OnCreatedListener mOnCreatedListener;
	private List<Forum> mForums;

	public static FragmentThreadsPager newInstance(Bundle bundle) {
		FragmentThreadsPager fragment = new FragmentThreadsPager();
		fragment.mOnPageChangeListeners = new ArrayList<>();
		return fragment;
	}

	public void setOnCreatedListener(OnCreatedListener l) {
		mOnCreatedListener = l;
	}

	public void setOnPageChangeListener(OnPageChangeListener l) {
		mPagerAdapter.setOnPageChangeListener(l);
	}

	public FragmentThreadsAbs getCurrentFragment() {
		return mPagerAdapter.getCurrentFragment();
	}

	public Forum getCurrentForum() {
		return mPagerAdapter.getCurrentForum();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.full_view_pager, container, false);
		ButterKnife.bind(this, rootView);

		mActivity = (ActivityMain) getActivity();
		mForums = App.getInstance().getSelectedForums();
		mPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), mForums);
		mViewPager.setAdapter(mPagerAdapter);

		mActivity.getPageIndicator().setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

			}
		});

		if (mOnCreatedListener != null) mOnCreatedListener.onCreated(mViewPager);

		return rootView;
	}

	@Override
	public void onUp(int ms) {
		if (mTabsContainerVisible || mLockAnimation) return;
	}

	@Override
	public void onDown(int ms) {
		if (!mTabsContainerVisible || mLockAnimation) return;
	}

	@Override
	public void toolbarClick() {
		mPagerAdapter.getCurrentFragment().toolbarClick();
	}

	public interface OnCreatedListener {
		void onCreated(ViewPager viewPager);
	}

	public interface OnPageChangeListener {
		void onPageChange(FragmentThreadsAbs f);
	}

	public static class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		private FragmentThreadsAbs mFragment;
		private List<Forum> mForums;
		private OnPageChangeListener mOnPageChangeListener;

		public MyFragmentPagerAdapter(FragmentManager fm, List<Forum> forums) {
			super(fm);
			mForums = forums;
		}

		public void setOnPageChangeListener(OnPageChangeListener l) {
			mOnPageChangeListener = l;
		}

		@Override
		public int getCount() {
			int count = mForums.size();
			return count;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int fid = mForums.get(position).getFid();
			String title = Forum.findById(mForums, fid).toString();
			return title;
		}

		@Override
		public Fragment getItem(int position) {
			int fid = mForums.get(position).getFid();

			Bundle args = new Bundle();
			args.putInt("fid", fid);
			args.putBoolean("enablePullToRefresh", true);

			FragmentThreadsAbs fragment = FragmentNormalThreads.newInstance();
			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			if (getCurrentFragment() != object) {
				mFragment = ((FragmentThreadsAbs) object);
				if (mOnPageChangeListener != null) mOnPageChangeListener.onPageChange(mFragment);
			}
			super.setPrimaryItem(container, position, object);
		}

		public FragmentThreadsAbs getCurrentFragment() {
			return mFragment;
		}

		public Forum getCurrentForum() {
			if (mFragment != null) {
				return (Forum) CollectionUtils.find(mForums, new Predicate() {
					int fid = mFragment.getArguments().getInt("fid");

					@Override
					public boolean evaluate(Object o) {
						return fid == ((Forum) o).getFid();
					}
				});
			} else {
				return null;
			}
		}
	}
}
