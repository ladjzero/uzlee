package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.ladjzero.hipda.Core;

/**
 * Created by chenzhuo on 16-2-12.
 */
public class FragmentBase extends Fragment {

	private ActivityBase mActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (ActivityBase) activity;
	}

	public Application2 getApp() {
		return mActivity.getApp();
	}

	public Core getCore() {
		return getApp().getCore();
	}

	public SharedPreferences getSettings() {
		return mActivity.getSettings();
	}
}
