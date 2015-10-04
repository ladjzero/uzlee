package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.content.Intent;
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

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.orhanobut.logger.Logger;
import com.rey.material.widget.FloatingActionButton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class ActivityMain extends ActivityBase implements ViewPager.OnPageChangeListener {

	private FragmentNav mFragmentNav;
	int fid;
	String title = "";

	public static final int D_ID = 2;
	public static final int BS_ID = 6;
	public static final int EINK_ID = 59;

	private MenuItem bsType;
	private int bsTypeId;
	private Icon bsTypeIcon;
	private FragmentThreads bsFragment;
	public int navPosition = 0;
	private int uid = 0;
	Toolbar toolbar;
	FloatingActionButton mEditButton;
	protected List<Forum> mForums;
	private List<Forum.Type> mTypes;
	private ArrayList<Integer> mFids = new ArrayList<>();
	private int mFid;
	private HashMap<Integer, Integer> mLastSelectedType = new HashMap<>();
	private OnTypeSelect onTypeSelect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		ActionBar mActionbar = getSupportActionBar();
		setTitle(null);
		mFragmentNav = (FragmentNav) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
		mFragmentNav.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), (Toolbar) findViewById(R.id.toolbar));
		mForums = Core.getSelectedForums(this);
		mFid = mForums.get(0).getFid();

		Bundle bundle = new Bundle();
		getSupportFragmentManager().beginTransaction().replace(R.id.container, FragmentThreadsPager.newInstance(bundle)).commit();
	}

	@Override
	public void onEventMainThread(Core.UserEvent userEvent) {
		Logger.i("EventBus.onEventMainThread.statusChangeEvent : user is null ? %b", userEvent.user == null);

		if (userEvent.user != null && uid != userEvent.user.getId()) {
			super.onEventMainThread(userEvent);

			switch (fid) {
				case D_ID:
					navPosition = 0;
					break;
				case BS_ID:
					navPosition = 1;
					break;
				default:
					navPosition = 2;
			}

			uid = userEvent.user.getId();

			Bundle bundle = new Bundle();
			bundle.putInt("fid", fid);
//			getFragmentManager().beginTransaction().replace(R.id.container, FragmentThreadsPager.newInstance(bundle)).commit();
		}

		if (userEvent.user == null) {
			super.onEventMainThread(userEvent);
			uid = 0;
		}
	}

	public void restoreActionBar() {
//		setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.threads, menu);

		boolean sortVisible = mTypes != null && mTypes.size() > 0;
		Forum.Type type = null;

		if (sortVisible) {
			Integer typeId = mLastSelectedType.get(mFid);
			if (typeId == null) typeId = -1;

			final Integer finalTypeId = typeId;

			type = (Forum.Type) CollectionUtils.find(mTypes, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ((Forum.Type) o).getId() == finalTypeId;
				}
			});
		}

		menu.findItem(R.id.thread_sort).setVisible(sortVisible).setTitle(sortVisible ? type.getName() : "");
		menu.findItem(R.id.thread_publish).setIcon(new IconDrawable(this, MaterialIcons.md_edit).colorRes(android.R.color.white).actionBarSize());
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.thread_publish) {
			Intent intent = new Intent(this, ActivityEdit.class);
			intent.putExtra("title", "新主题");
			intent.putExtra("fid", fid);

			startActivity(intent);

			return true;
		} else if (id == R.id.thread_sort) {
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
						if (onTypeSelect != null) onTypeSelect.onTypeSelect(mFid, type.getId());
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


	boolean doubleBackToExitPressedOnce = false;

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
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList("fids", new ArrayList<Integer>(mLastSelectedType.keySet()));
		outState.putIntegerArrayList("types", new ArrayList<Integer>(mLastSelectedType.values()));

		super.onSaveInstanceState(outState);
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

	public void onMessageClick(View view) {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivityAlerts.class);
		startActivity(intent);
	}


	public void onMyPostsClick(View view) {
		mFragmentNav.closeDrawer();

		if (Core.getUser() != null) {
			Intent intent = new Intent(this, ActivityMyPosts.class);
			startActivity(intent);
		}
	}

	public void onSearchClick(View view) {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivitySearch.class);
		startActivity(intent);
	}

	public void onSettingsClick(View view) {
		mFragmentNav.closeDrawer();
		Intent intent = new Intent(this, ActivitySettings.class);
		startActivity(intent);
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

	public void onExitClick(View view) {
		mFragmentNav.closeDrawer();

		Core.logout(new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				showToast(error);
			}

			@Override
			public void onSuccess(String html) {
				showToast("登出成功");
			}
		});

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		mFid = Core.getSelectedForums(this).get(position).getFid();
		mTypes = Forum.findById(Core.getForums(this), mFid).getTypes();
		invalidateOptionsMenu();
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public void setOnTypeSelect(OnTypeSelect onTypeSelect) {
		this.onTypeSelect = onTypeSelect;
	}

	interface OnTypeSelect {
		void onTypeSelect(int fid, int typeId);
	}
}
