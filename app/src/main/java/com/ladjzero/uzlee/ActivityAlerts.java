package com.ladjzero.uzlee;


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ActivityAlerts extends ActivityPagerBase {
	@Override
	public Fragment getItem(int position) {
		Fragment ret = mFragmentCache.get(position);

		if (ret == null) {
			ret = FragmentAlerts.newInstance(position);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getInstance().getMemCache().remove("alerts_tab_0");
		App.getInstance().getMemCache().remove("alerts_tab_1");
	}
}
