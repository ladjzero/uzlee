package com.ladjzero.uzlee;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		Core.addOnMsgListener(mNavigationDrawerFragment);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		FragmentManager fragmentManager = getFragmentManager();
		ActionBar actionBar = getActionBar();
		Intent intent;
		final int D_ID = 2;
		final int BS_ID = 6;
		final int EINK_ID = 59;

		switch (position) {
			case 1:
				actionBar.setTitle("Buy & Sell");
				fragmentManager.beginTransaction().replace(R.id.container, ThreadsFragment.newInstance(BS_ID)).commit();
				break;
			case 2:
				actionBar.setTitle("E-INK");
				fragmentManager.beginTransaction().replace(R.id.container, ThreadsFragment.newInstance(EINK_ID)).commit();
				break;
			case 3:
				intent = new Intent(this, MsgActivity.class);
				startActivity(intent);
				break;
			case 4:
				intent = new Intent(this, MyPostsActivity.class);
				startActivity(intent);
				break;
			case 5:
				intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				break;
			default:
				actionBar.setTitle("Discovery");
				fragmentManager.beginTransaction().replace(R.id.container, ThreadsFragment.newInstance(D_ID)).commit();
		}
	}

	public void onSectionAttached(int number) {
		switch (number) {
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.threads, menu);
			restoreActionBar();
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
		if (id == R.id.action_settings) {
			return true;
		}

		if (id == R.id.thread_publish) {
			Intent editIntent = new Intent(this, EditActivity.class);
			editIntent.putExtra("title", "新主题");
			startActivity(editIntent);
			return true;
		}

		if (id == android.R.id.home) {
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
				doubleBackToExitPressedOnce=false;
			}
		}, 2000);
	}
}
