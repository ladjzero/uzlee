package com.ladjzero.uzlee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.uzlee.model.Forum;
import com.ladjzero.uzlee.utils.Utils;
import com.rey.material.app.Dialog;
import com.rey.material.widget.TabPageIndicator;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityMain extends ActivityBase implements SharedPreferences.OnSharedPreferenceChangeListener, AdapterView.OnItemClickListener {

	String title = "";
	Toolbar toolbar;
	boolean doubleBackToExitPressedOnce = false;
	AdapterMenuItem actionsAdapter;
	private FragmentNav mFragmentNav;
	private FragmentThreadsPager mFragment;
	private int mCurrentPagePosition = -1;
	private boolean mIsRunning = false;
	private boolean mNeedReload = false;
	private View mCustomToolbarView;
	private TabPageIndicator mPageIndicator;
	private ViewPager mViewPager;
	private Dialog mMenuDialog;
	private View mMenuView;

	public TabPageIndicator getPageIndicator() {
		return mPageIndicator;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		setTitle(null);
		mFragmentNav = (FragmentNav) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
		mFragmentNav.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), (Toolbar) findViewById(R.id.toolbar));
		mFragmentNav.closeDrawer();

		FragmentManager fragmentManager = getSupportFragmentManager();

		if (Utils.getUserSelectedForums(this).size() == 0) {
			fragmentManager
					.beginTransaction()
					.replace(R.id.container, FragmentToPickForums.newInstance(null))
					.commit();

			return;
		}

		Bundle bundle = new Bundle();
		mFragment = FragmentThreadsPager.newInstance(bundle);

		fragmentManager
				.beginTransaction()
				.replace(R.id.container, mFragment)
				.commit();

		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFragment.toolbarClick();
			}
		});

		LayoutInflater mInflater = LayoutInflater.from(this);
		mCustomToolbarView = mInflater.inflate(R.layout.tab_page_indicator_scroll, null);

		mPageIndicator = (TabPageIndicator) mCustomToolbarView.findViewById(R.id.tabs);


		getSupportActionBar().setDisplayShowCustomEnabled(true);

		mFragment.setOnCreatedListener(new FragmentThreadsPager.OnCreatedListener() {
			@Override
			public void onCreated(ViewPager viewPager) {
				mViewPager = viewPager;
				// Delay because TabPageIndicator can not be re-rendered after menu icon were inserted.
				// The underline of the default tab will be wider as it should be.
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Toolbar.LayoutParams params = new Toolbar.LayoutParams(
								Toolbar.LayoutParams.WRAP_CONTENT,
								Toolbar.LayoutParams.MATCH_PARENT,
								Gravity.CENTER_HORIZONTAL
						);
						mCustomToolbarView.setLayoutParams(params);
						toolbar.addView(mCustomToolbarView);
						mPageIndicator.setViewPager(mViewPager, 0);
						mPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
							int formerPosition = 0;

							@Override
							public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
								if (mCurrentPagePosition != position) {
									mCurrentPagePosition = position;
								}
							}

							@Override
							public void onPageSelected(int position) {
								if (formerPosition == position) {
									mFragment.toolbarClick();
								}

								formerPosition = position;
							}

							@Override
							public void onPageScrollStateChanged(int state) {

							}
						});
					}
				}, 700);

				mFragment.setOnPageChangeListener(new FragmentThreadsPager.OnPageChangeListener() {
					@Override
					public void onPageChange(FragmentThreadsAbs f) {
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								invalidateOptionsMenu();
							}
						}, 300);
					}
				});
			}
		});

		mMenuView = getLayoutInflater().inflate(R.layout.actions_dialog, null);


		mMenuDialog = new Dialog(this);
		ListView menuList = (ListView) mMenuView.findViewById(R.id.actions);

		if (App.getInstance().getApi().getStore().getMeta().getUser().getId() == 0) {
			actionsAdapter = new AdapterMenuItem(this, new String[]{
					"刷新"
			}, new String[]{
					"{md-refresh}"
			});
		} else {
			actionsAdapter = new AdapterMenuItem(this, new String[]{
					"新主题",
					"刷新"
			}, new String[]{
					"{md-add}",
					"{md-refresh}"
			});
		}

		menuList.setAdapter(actionsAdapter);
		menuList.setOnItemClickListener(this);

		mMenuDialog.title("")
				.titleColor(Utils.getThemeColor(this, R.attr.colorText))
				.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
				.negativeAction("取消")
				.negativeActionClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mMenuDialog.dismiss();
					}
				})
				.contentView(mMenuView)
				.canceledOnTouchOutside(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			return false;
		} else if (id == R.id.more) {
			mMenuDialog.show();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsRunning = true;

		if (mNeedReload) {
			mNeedReload = false;
			reload();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsRunning = false;
	}

	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		showToast("再次后退将会退出");
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, 2000);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mFragmentNav.toggleDrawer();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.threads, menu);

		menu.findItem(R.id.more)
				.setIcon(new IconDrawable(this, MaterialIcons.md_more_vert)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize());

		return super.onCreateOptionsMenu(menu);
	}

	@OnClick(R.id.message)
	void onMessageClick() {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivityAlerts.class);
		startActivity(intent);
	}

	@OnClick(R.id.my_posts)
	void onMyPostsClick() {
		mFragmentNav.closeDrawer();

		if (App.getInstance().getApi().getStore().getMeta().getUser() != null) {
			Intent intent = new Intent(this, ActivityMyPosts.class);
			startActivity(intent);
		}
	}

	@OnClick(R.id.search)
	void onSearchClick() {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivitySearch.class);
		startActivity(intent);
	}

	@OnClick(R.id.settings)
	void onSettingsClick() {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivitySettings.class);
		startActivityForResult(intent, 0);
	}

	@OnClick(R.id.themeSwitch)
	void onThemeSwitch() {
		SharedPreferences settings = getSettings();
		if (Utils.getTheme("night") == getThemeId()) {
			settings.edit().putString("theme", settings.getString("lastDayTheme", DefaultTheme)).commit();
		} else {
			settings.edit().putString("theme", "night").commit();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && data.getBooleanExtra("reload", false)) {
			reload();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		getSettings().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		getSettings().unregisterOnSharedPreferenceChangeListener(this);

		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if ("theme".equals(key) || "selected_forums".equals(key)) {
			if (mIsRunning) {
				reload();
			} else {
				mNeedReload = true;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		mMenuDialog.dismiss();

		if (App.getInstance().getApi().getStore().getMeta().getUser().getId() == 0) {
			App.getInstance().dispatchEvent(new FragmentThreadsAbs.EventRefresh());
		} else {
			switch (i) {
				case 0:
					Forum f = mFragment.getCurrentForum();
					int fid = f.getFid();
					Intent intent = new Intent(this, ActivityEdit.class);
					intent.putExtra("title", Forum.findById(App.getInstance().getFlattenForums(), fid).getName());
					intent.putExtra("fid", fid);
					startActivity(intent);
					return;
				case 1:
					App.getInstance().dispatchEvent(new FragmentThreadsAbs.EventRefresh());
			}
		}


	}
}
