package com.ladjzero.uzlee;


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ActivityMyPosts extends ActivityPagerBase {
	@Override
	public Fragment getItem(int position) {
		Fragment ret = mFragmentCache.get(position);

		if (ret == null) {
			ret = SimpleThreadsFragment.newInstance(position);
			mFragmentCache.put(position, ret);
		}

		return ret;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return "主题";
			case 1:
				return "回复";
			case 2:
				return "收藏";
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getApp().getMemCache().remove("simple_threads_tab_0");
		getApp().getMemCache().remove("simple_threads_tab_1");
		getApp().getMemCache().remove("simple_threads_tab_2");
	}
}
