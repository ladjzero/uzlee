package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.orhanobut.logger.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

public class ActivityMain extends ActivityBase implements ViewPager.OnPageChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

	String title = "";
	Toolbar toolbar;
	boolean doubleBackToExitPressedOnce = false;
	private FragmentNav mFragmentNav;
	private List<Forum.Type> mTypes;
	private HashMap<Integer, Integer> mLastSelectedType = new HashMap<>();
	private OnTypeChange mOnTypeChange;
	private FragmentThreadsPager mFragment;
	private int mCurrentPagePosition = -1;
	private int mFid = -1;
	private boolean mIsRunning = false;
	private boolean mNeedReload = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar mActionbar = getSupportActionBar();
		setTitle(null);
		mFragmentNav = (FragmentNav) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
		mFragmentNav.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), (Toolbar) findViewById(R.id.toolbar));

		Bundle bundle = new Bundle();

		mFragment =  FragmentThreadsPager.newInstance(bundle);
		setOnTypeChangeListener(mFragment);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, mFragment)
				.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.thread_publish) {
			Intent intent = new Intent(this, ActivityEdit.class);
			intent.putExtra("title", "新主题");
			intent.putExtra("fid", mFid);

			startActivity(intent);

			return true;
		} else if (id == R.id.thread_types) {
			if (mTypes != null) {
				ListView listView = new ListView(this);
				listView.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_of_dialog, R.id.text, mTypes));

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setView(listView);
				final AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.show();

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						Forum.Type type = (Forum.Type) adapterView.getItemAtPosition(i);
						mLastSelectedType.put(mFid, type.getId());
						if (mOnTypeChange != null) mOnTypeChange.onTypeSelect(mFid, type.getId());
						invalidateOptionsMenu();
						alertDialog.dismiss();
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
		mFragment.registerPageChangeListener(this);
		mIsRunning = true;

		if (mNeedReload) {
			mNeedReload = false;
			reload();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mFragment.unregisterPageChangeListener(this);
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
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		ArrayList<Integer> fids = savedInstanceState.getIntegerArrayList("fids");
		ArrayList<Integer> types = savedInstanceState.getIntegerArrayList("types");

		if (mLastSelectedType == null) mLastSelectedType = new HashMap<>();

		if (fids != null && types != null && fids.size() == types.size())
			for (int i = 0, len = fids.size(); i < len; ++i) {
				mLastSelectedType.put(fids.get(i), types.get(i));
			}
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

		Integer typeId = mLastSelectedType.get(mFid);

		if (typeId != null) {
			final Integer finalTypeId = typeId;

			Forum.Type type = (Forum.Type) CollectionUtils.find(mTypes, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ((Forum.Type) o).getId() == finalTypeId;
				}
			});

			menu.findItem(R.id.thread_types)
					.setTitle(type.getName());
		}

		menu.findItem(R.id.thread_publish)
				.setIcon(new IconDrawable(this, MaterialIcons.md_edit)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize());

		return super.onCreateOptionsMenu(menu);
	}

	@OnClick(R.id.message) void onMessageClick() {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivityAlerts.class);
		startActivity(intent);
	}

	@OnClick(R.id.my_posts) void onMyPostsClick() {
		mFragmentNav.closeDrawer();

		if (Core.getUser() != null) {
			Intent intent = new Intent(this, ActivityMyPosts.class);
			startActivity(intent);
		}
	}

	@OnClick(R.id.search) void onSearchClick() {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivitySearch.class);
		startActivity(intent);
	}

	@OnClick(R.id.settings) void onSettingsClick() {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivitySettings.class);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && data.getBooleanExtra("reload", false)) {
			reload();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList("fids", new ArrayList<Integer>(mLastSelectedType.keySet()));
		outState.putIntegerArrayList("types", new ArrayList<Integer>(mLastSelectedType.values()));

		super.onSaveInstanceState(outState);
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

	public void onInfoClick(View view) {
		final MaterialDialog mMaterialDialog = new MaterialDialog(this);
		mMaterialDialog.setCanceledOnTouchOutside(true);

		final View v = getLayoutInflater().inflate(R.layout.about, null);
		WebView webView = (WebView) v.findViewById(R.id.about_webView);
		webView.loadUrl("https://cdn.rawgit.com/ladjzero/uzlee/master/release/readme.html");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		mMaterialDialog.setContentView(v);
		mMaterialDialog.setPositiveButton("检查更新", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMaterialDialog.dismiss();
				Core.requestUpdate();
			}
		});
		mMaterialDialog.show();

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		Logger.d("ActivityMain onPageScrolled position %d", position);
		// onPageScrolled is always called after initializing.
		// However onPageSelected will not be called until dragging.
		if (mCurrentPagePosition != position) {
			mCurrentPagePosition = position;
			mFid = Core.getSelectedForums(this).get(position).getFid();
			mTypes = Forum.findById(Core.getForums(this), mFid).getTypes();
			invalidateOptionsMenu();
		}
	}

	@Override
	public void onPageSelected(int position) {
		Logger.d("ActivityMain onPageSelected position %d", position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		Logger.d("ActivityMain onPageScrollStateChanged state %d", state);
	}

	public void setOnTypeChangeListener(OnTypeChange onTypeChange) {
		this.mOnTypeChange = onTypeChange;
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

	interface OnTypeChange {
		void onTypeSelect(int fid, int typeId);
	}
}
