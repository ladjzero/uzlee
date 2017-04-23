package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

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

	public SharedPreferences getSettings() {
		return mActivity.getSettings();
	}
}
