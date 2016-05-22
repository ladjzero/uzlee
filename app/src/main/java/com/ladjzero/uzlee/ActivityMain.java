package com.ladjzero.uzlee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.Forum;
import com.ladjzero.uzlee.utils.Utils;
import com.rey.material.app.Dialog;
import com.rey.material.widget.TabPageIndicator;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityMain extends ActivityBase implements SharedPreferences.OnSharedPreferenceChangeListener {

	String title = "";
	Toolbar toolbar;
	boolean doubleBackToExitPressedOnce = false;
	private FragmentNav mFragmentNav;
	private FragmentThreadsPager mFragment;
	private int mCurrentPagePosition = -1;
	private boolean mIsRunning = false;
	private boolean mNeedReload = false;
	private View mCustomToolbarView;
	private TabPageIndicator mPageIndicator;
	private ViewPager mViewPager;

	public TabPageIndicator getPageIndicator() {
		return mPageIndicator;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFragment.toolbarClick();
			}
		});

		setTitle(null);
		mFragmentNav = (FragmentNav) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
		mFragmentNav.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), (Toolbar) findViewById(R.id.toolbar));

		Bundle bundle = new Bundle();

		mFragment = FragmentThreadsPager.newInstance(bundle);

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container, mFragment)
				.commit();

		LayoutInflater mInflater = LayoutInflater.from(this);
		mCustomToolbarView = mInflater.inflate(
				getSelectedForums(this).size() > 3 ?
						R.layout.tab_page_indicator_scroll :
						R.layout.tab_page_indicator_fixed, null);

		mPageIndicator = (TabPageIndicator) mCustomToolbarView.findViewById(R.id.tabs);


		getSupportActionBar().setDisplayShowCustomEnabled(true);

		mFragment.setOnCreatedListener(new FragmentThreadsPager.OnCreatedListener() {
			@Override
			public void onCreated(ViewPager viewPager) {
				mViewPager = viewPager;
				// Delay because TabPageIndicator can not be re-rendered after menu icon were inserted.
				// The underline of the default tab will be wider as it should be.
				mCustomToolbarView.postDelayed(new Runnable() {
					@Override
					public void run() {
						getSupportActionBar().setCustomView(mCustomToolbarView);
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
						mViewPager.postDelayed(new Runnable() {
							@Override
							public void run() {
								invalidateOptionsMenu();
							}
						}, 300);
					}
				});
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Forum f = mFragment.getCurrentForum();
		int fid = f.getFid();

		if (id == R.id.thread_publish) {
			Intent intent = new Intent(this, ActivityEdit.class);
			intent.putExtra("title", Forum.findById(getFlattenForums(this), fid).getName());
			intent.putExtra("fid", fid);

			startActivity(intent);

			return true;
		} else if (id == R.id.thread_types) {
			List<Forum.Type> types = Forum.findById(getFlattenForums(this), fid).getTypes();

			if (types != null) {
				View typesMenuView = getLayoutInflater().inflate(R.layout.threads_actions_dialog, null);
				ListView listView = (ListView) typesMenuView.findViewById(R.id.actions);
				listView.setDivider(null);
				listView.setAdapter(new ArrayAdapter<Forum.Type>(this, R.layout.list_item_of_dialog, R.id.text, types) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View view = super.getView(position, convertView, parent);
						Forum.Type type = getItem(position);
						Forum.Type currentType = mFragment.getCurrentForum().getCurrentType();

						if (currentType != null && currentType.getId() > 0 && type.getId() == currentType.getId()) {
							view.setBackgroundColor(Utils.getThemeColor(ActivityMain.this, R.attr.colorRead));
						} else {
							view.setBackgroundColor(Utils.getColor(ActivityMain.this, android.R.color.transparent));
						}

						return view;
					}
				});

				final Dialog dialog = new Dialog(this);

				dialog.title(title)
						.titleColor(Utils.getThemeColor(this, R.attr.colorText))
						.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
						.negativeAction("取消")
						.negativeActionClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						})
						.contentView(typesMenuView)
						.show();

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						Forum f = mFragment.getCurrentForum();
						int fid = f.getFid();

						Forum.Type type = (Forum.Type) adapterView.getItemAtPosition(i);
						mFragment.getCurrentForum().setCurrentType(type);
						invalidateOptionsMenu();
						dialog.dismiss();
					}
				});
			}
		} else if (id == android.R.id.home) {
			return false;
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
		Forum.Type type = null;

		try {
			type = mFragment.getCurrentForum().getCurrentType();
		} catch (Throwable t) {
		}

		menu.findItem(R.id.thread_publish)
				.setIcon(new IconDrawable(this, MaterialIcons.md_add)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize());

		menu.findItem(R.id.thread_types)
				.setVisible(getSettings().getBoolean("show_types", false))
				.setIcon(new IconDrawable(this, type != null && type.getId() > 0 ? MaterialIcons.md_bookmark : MaterialIcons.md_bookmark_border)
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

		if (getCore().getLocalApi().getUser() != null) {
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
		} else if ("show_types".equals(key)) {
			mNeedReload = true;
		}
	}
}
