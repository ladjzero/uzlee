package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;


import de.greenrobot.event.EventBus;

public class NavFragment extends Fragment {
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
	private NavigationDrawerCallbacks mCallbacks;
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private View mFragmentContainerView;

	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;

	private MainActivity activity;
	NavAdapter adapter;
	ImageView imageView;
	TextView userName;
	View userLayout;
	String title;

	public NavFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = (MainActivity) getActivity();

//		materialMenu = new MaterialMenuIconCompat(activity, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
		// Read in the flag indicating whether or not the user has demonstrated awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);
		// Select either the default item (0) or the last selected item.
		selectItem(activity.navPosition);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logger.i("start");

		View layout = inflater.inflate(R.layout.nav, container, false);
		mDrawerListView = (ListView) layout.findViewById(R.id.nav_list);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Logger.i("position %d", position);

				selectItem(position);
			}
		});
		adapter = new NavAdapter(
				getActivity(),
				R.layout.nav_item,
				new String[]{
						getString(R.string.title_section1),
						getString(R.string.title_section2),
						getString(R.string.title_section3),
						getString(R.string.nav_alert),
						getString(R.string.nav_my_posts),
						getString(R.string.nav_search),
						getString(R.string.nav_setting),
						getString(R.string.nav_logout),
						getString(R.string.nav_about)
				});

		mDrawerListView.setAdapter(adapter);
		mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

		userLayout = layout.findViewById(R.id.nav_user);
		imageView = (ImageView) layout.findViewById(R.id.nav_user_image);
		userName = (TextView) layout.findViewById(R.id.nav_user_name);

		userLayout.setVisibility(View.GONE);
		userLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Core.getUser() != null) {
					Intent intent = new Intent(activity, UserActivity.class);
					intent.putExtra("uid", Core.getUser().getId());
					startActivity(intent);
				}
			}
		});

		return layout;
	}

	@Override
	public void onResume() {
		User user = Core.getUser();

		Logger.i("EventBus.register, Core.user %b", user != null);

		super.onResume();

		if (user != null) {
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

		EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		Logger.i("EventBus.unregister");

		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(Core.MessageEvent messageEvent) {
		Logger.i("EventBus.onEventMainThread -> set message count %d", messageEvent.count);

		adapter.setAlert(messageEvent.count);
	}

	public void onEventMainThread(Core.UserEvent userEvent) {
		Logger.i("EventBus.onEventMainThread -> userLayout, user %b", userEvent.user != null);

		adapter.notifyDataSetChanged();
		getView().invalidate();

		if (userEvent.user != null) {
			Logger.i("EventBase.onEventMainThread -> userLayout -> visible, current visible %b", userLayout.getVisibility() == View.VISIBLE);

			if (userLayout.getVisibility() == View.GONE) {
				Logger.i("EventBase.onEventMainThread -> userLayout -> visible, uid %d, name %s", userEvent.user.getId(), userEvent.user.getName());

				userLayout.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage(Core.getUser().getImage(), imageView);
				userName.setText(Core.getUser().getName());
			}
		} else {
			Logger.i("EventBus.onEventMainThread -> userLayout -> gone");

			userLayout.setVisibility(View.GONE);
		}
	}

	public void onEventMainThread(final Core.UpdateInfo updateInfo) {
		Logger.i("EventBus.onEventMainThread -> update info");

		if (updateInfo != null) {
			String version = ((MainActivity) getActivity()).getVersion();
			String newVersion = updateInfo.getVersion();

			if (new BaseActivity.VersionComparator().compare(version, newVersion) < 0) {
				adapter.setUpdate(true);
			}
		}
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

	/**
	 * Users of this fragment must call this method to set up the navigation drawer interactions.
	 *
	 * @param fragmentId   The android:id of this fragment in its activity's layout.
	 * @param drawerLayout The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(
				getActivity(),                    /* host Activity */
				mDrawerLayout,                    /* DrawerLayout object */
				R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
				R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				}

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
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Logger.i("onConfigurationChanged");

		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar. See also
		// showGlobalContextActionBar, which controls the top-left area of the action bar.
		if (mDrawerLayout != null && isDrawerOpen()) {
			inflater.inflate(R.menu.global, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void selectItem(int position) {
		Logger.i("selectItem, position: %d", position);

		mCurrentSelectedPosition = position;
		if (mDrawerListView != null) {
			mDrawerListView.setItemChecked(position, true);
		}
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if (mCallbacks != null) {
			mCallbacks.onNavigationDrawerItemSelected(position);
		}
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to show the global app
	 * 'context', rather than just what's in the current screen.
	 */
	private void showGlobalContextActionBar() {
		activity.setTitle(R.string.app_name);
	}

	private ActionBar getActionBar() {
		return ((BaseActivity) getActivity()).getSupportActionBar();
	}

	public static interface NavigationDrawerCallbacks {
		void onNavigationDrawerItemSelected(int position);
	}
}
