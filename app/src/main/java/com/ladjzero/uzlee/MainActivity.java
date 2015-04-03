package com.ladjzero.uzlee;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.*;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	int fid;
	String title = "";

	public static final int D_ID = 2;
	public static final int BS_ID = 6;
	public static final int EINK_ID = 59;

	private MenuItem bsType;
	private int bsTypeId;
	private Iconify.IconValue bsTypeIcon;
	private ThreadsFragment bsFragment;
	public int navPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fid = setting.getInt("fid", D_ID);
		navPosition = computeNavPosition(fid);
		bsTypeId = setting.getInt("bs_type", 0);
		bsTypeIcon = BsTypeAdapter.ICON_VALUES[bsTypeId];

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		FragmentManager fragmentManager = getFragmentManager();
		ActionBar actionBar = getActionBar();
		Intent intent;
		Bundle bundle = new Bundle();
		navPosition = position;

		switch (position) {
			case 0:
				fid = D_ID;
				actionBar.setTitle(title = "Discovery");
				bundle.putInt("fid", fid);
				fragmentManager.beginTransaction().replace(R.id.container, ThreadsFragment.newInstance(bundle)).commit();
				break;
			case 1:
				fid = BS_ID;
//				fid = 57;
				actionBar.setTitle(title = "Buy & Sell");
				bundle = new Bundle();
				bundle.putInt("fid", fid);
				bundle.putInt("bs_type_id", bsTypeId);
				bsFragment = ThreadsFragment.newInstance(bundle);
				fragmentManager.beginTransaction().replace(R.id.container, bsFragment).commit();
				break;
			case 2:
				fid = EINK_ID;
				bundle = new Bundle();
				bundle.putInt("fid", fid);
				actionBar.setTitle(title = "E-INK");
				fragmentManager.beginTransaction().replace(R.id.container, ThreadsFragment.newInstance(bundle)).commit();
				break;
			case 3:
				intent = new Intent(this, MsgActivity.class);
				startActivity(intent);
				break;
			case 4:
				if (Core.isOnline()) {
					intent = new Intent(this, MyPostsActivity.class);
					startActivity(intent);
				} else {
					showLogin();
				}
				break;
			case 5:
				intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				break;
			case 6:
				intent = new Intent(this, SettingActivity.class);
				startActivity(intent);
				break;
			case 7:
				Core.logout(new Core.OnRequestListener() {


					@Override
					public void onError(String error) {
						Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onSuccess(String html) {
					}
				});
				break;
			case 8:
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("关于");

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
				alert.setView(v);
				alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}

				});

				alert.setNegativeButton(getString(R.string.check_update), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Core.requestUpdate();
						dialog.cancel();
					}

				});
				alert.show();
				break;
			default:
				showToast("暂不可用");
		}

		SharedPreferences.Editor editor = setting.edit();
		editor.putInt("fid", fid);
		editor.commit();
	}

	@Override
	public void onEventMainThread(Core.StatusChangeEvent statusChangeEvent) {
		super.onEventMainThread(statusChangeEvent);

		if (statusChangeEvent.online && !statusChangeEvent.checkedEachDoc) {
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

			onNavigationDrawerItemSelected(navPosition);
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.threads, menu);
			restoreActionBar();
			bsType = menu.findItem(R.id.thread_sort);
			if (fid == BS_ID) {
				bsType.setIcon(new IconDrawable(this, bsTypeIcon).colorRes(android.R.color.white).actionBarSize());
			}
			bsType.setVisible(fid == BS_ID);
			menu.findItem(R.id.thread_publish).setIcon(new IconDrawable(this, Iconify.IconValue.fa_comment_o).colorRes(android.R.color.white).actionBarSize());
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.thread_publish) {
			Intent intent = new Intent(this, EditActivity.class);
			intent.putExtra("title", "新主题");
			intent.putExtra("fid", fid);

			startActivity(intent);
			return true;
		} else if (id == R.id.thread_sort) {
			ListView listView = new ListView(this);
			BsTypeAdapter adapter = new BsTypeAdapter(this);
			listView.setAdapter(adapter);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setView(listView);
			final AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.show();

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
					bsFragment.setTypeId(i);
					alertDialog.dismiss();
					SharedPreferences.Editor editor = setting.edit();
					bsTypeId = i;
					bsTypeIcon = BsTypeAdapter.ICON_VALUES[i];
					editor.putInt("bs_type", i);
					editor.commit();
					invalidateOptionsMenu();
				}
			});
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
		Toast.makeText(this, "再次后退将会退出", Toast.LENGTH_SHORT).show();

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
			mNavigationDrawerFragment.toggleDrawer();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private int computeNavPosition(int fid) {
		switch (fid) {
			case D_ID:
				return navPosition = 0;
			case BS_ID:
				return navPosition = 1;
			default:
				return navPosition = 2;
		}
	}
}
