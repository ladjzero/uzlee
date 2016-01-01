package com.ladjzero.uzlee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentNav extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
	/**
	 * Per the design guidelines, you should show the drawer on launch until the user manually
	 * expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
	@Bind(R.id.message)
	View message;
	@Bind(R.id.my_posts)
	View myPosts;

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	@Bind(R.id.nav_user_image)
	ImageView imageView;
	@Bind(R.id.nav_user_name)
	TextView userName;
	@Bind(R.id.nav_user)
	View userLayout;
	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mActionBarDrawerToggle;
	private DrawerLayout mDrawerLayout;
	//	private RecyclerView mDrawerList;
	private View mFragmentContainerView;
	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;
	private TextView mAlertIcon;
	private ActivityBase mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		mContext = (ActivityBase) getActivity();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.nav, container, false);
		ButterKnife.bind(this, view);

		mAlertIcon = (TextView) view.findViewById(R.id.alert_icon);

		mContext.getSettings().registerOnSharedPreferenceChangeListener(this);

		final User user = Core.getUser();

		if (user == null || user.getId() == 0) {
			message.setVisibility(View.GONE);
			myPosts.setVisibility(View.GONE);
		}

		userLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (user == null || user.getId() == 0) {
					mContext.toLoginPage();
				} else {
					Intent intent = new Intent(mContext, ActivityUser.class);
					intent.putExtra("uid", Core.getUser().getId());
					startActivity(intent);
				}
			}
		});

		return view;
	}

	@Override
	public void onDestroyView() {
		mContext.getSettings().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		User user = Core.getUser();

		super.onResume();

		if (user != null && user.getId() > 0) {
			Logger.i("set user layout visible, user %d %s", user.getId(), user.getName());

			userLayout.postDelayed(new Runnable() {
				@Override
				public void run() {
					userLayout.setVisibility(View.VISIBLE);
					ImageLoader.getInstance().displayImage(Core.getUser().getImage(), imageView);
					userName.setText(Core.getUser().getName());
				}
			}, 300);
		}

		int count = mContext.getSettings().getInt("unread", 0);
		mAlertIcon.setTextColor(count == 0 ?
				Utils.getThemeColor(mContext, R.attr.colorText) :
				Utils.getColor(mContext, R.color.commentNoBg));

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mActionBarDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	public ActionBarDrawerToggle getActionBarDrawerToggle() {
		return mActionBarDrawerToggle;
	}

	public DrawerLayout getDrawerLayout() {
		return mDrawerLayout;
	}

	/**
	 * Users of this fragment must call this method to set up the navigation drawer interactions.
	 *
	 * @param fragmentId   The android:id of this fragment in its activity's layout.
	 * @param drawerLayout The DrawerLayout containing this fragment's UI.
	 * @param toolbar      The Toolbar of the activity.
	 */
	public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
		mFragmentContainerView = (View) getActivity().findViewById(fragmentId).getParent();
		mDrawerLayout = drawerLayout;

//		mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.myPrimaryDarkColor));

		mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) return;
				if (!mUserLearnedDrawer) {
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				}
				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) return;

				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mActionBarDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
	}

	public void openDrawer() {
		mDrawerLayout.openDrawer(mFragmentContainerView);
	}

	public void closeDrawer() {
		mDrawerLayout.closeDrawer(mFragmentContainerView);
	}

	// menu key triggers this
	public void toggleDrawer() {
		boolean isOpen = isDrawerOpen();
		if (isOpen) mDrawerLayout.closeDrawer(Gravity.LEFT);
		else mDrawerLayout.openDrawer(Gravity.LEFT);
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("unread")) {
			int count = sharedPreferences.getInt("unread", 0);

			mAlertIcon.setTextColor(count == 0 ?
					Utils.getThemeColor(mContext, R.attr.colorText) :
					Utils.getColor(mContext, R.color.commentNoBg));
		}
	}
}
