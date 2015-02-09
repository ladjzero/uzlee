package com.ladjzero.uzlee;

import java.util.Comparator;
import java.util.Collection;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuIcon;
import com.cengalabs.flatui.FlatUI;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.DBHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.greenrobot.event.EventBus;


public class BaseActivity extends OrmLiteBaseActivity<DBHelper> {

	SharedPreferences setting;
	EmojiUtils emojiUtils;
	AlertDialog alert;

	public void enableBackAction() {
		MaterialMenuIcon materialMenu = new MaterialMenuIcon(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
		materialMenu.setState(MaterialMenuDrawable.IconState.ARROW);
	}

	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Core.setup(this);
		emojiUtils = new EmojiUtils(this);

		setting = getSharedPreferences("uzlee.setting", 0);

		FlatUI.initDefaultValues(this);
		FlatUI.setDefaultTheme(FlatUI.DARK);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(FlatUI.getActionBarDrawable(this, FlatUI.DARK, false));
		actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

		DisplayImageOptions ilOptions = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.none)
				.showImageOnLoading(R.drawable.none)
				.showImageOnFail(R.drawable.none)
				.cacheInMemory(true)
				.cacheOnDisk(true).build();
		ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(ilOptions).build();
		ImageLoader.getInstance().init(ilConfig);

		Long lastCheck = setting.getLong("last_update_check", 0);
		Long now = System.currentTimeMillis();

		if (now - lastCheck > 12 * 3600 * 1000) {
			Core.requestUpdate();

			SharedPreferences.Editor editor = setting.edit();
			editor.putLong("last_update_check", now);
			editor.commit();
		}

		final View alertView = getLayoutInflater().inflate(R.layout.login_dialog, null);
		alert = new AlertDialog.Builder(this)
				.setTitle(getString(R.string.login_hipda))
				.setView(alertView)
				.setPositiveButton(getString(R.string.login), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						String username = ((EditText) alertView.findViewById(R.id.user_name))
								.getText().toString();
						String password = ((EditText) alertView
								.findViewById(R.id.user_password)).getText().toString();
						final ProgressDialog progress = ProgressDialog.show(
								BaseActivity.this, "", getString(R.string.login) + "...", true);
						Core.login(username, password, new Core.OnRequestListener() {

							@Override
							public void onError(String error) {
								progress.dismiss();
								Toast.makeText(BaseActivity.this, error, Toast.LENGTH_LONG).show();
							}

							@Override
							public void onSuccess(String html) {
								progress.dismiss();
								Toast.makeText(BaseActivity.this, getString(R.string.login_succeed), Toast.LENGTH_LONG).show();
							}
						});
					}

				}).create();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void showLogin() {
		alert.dismiss();
		alert.show();
	}

	public void onEventMainThread(Core.StatusChangeEvent statusChangeEvent) {
		if (!statusChangeEvent.online) {
			showLogin();
		}
	}

	public void onEventMainThread(final Core.UpdateInfo updateInfo) {
		if (updateInfo != null) {
			String version = getVersion();
			String newVersion = updateInfo.getVersion();

			if (new VersionComparator().compare(version, newVersion) < 0) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(getString(R.string.update_available) + " " + newVersion);
				alert.setMessage(updateInfo.getInfo());
				alert.setNegativeButton(getString(R.string.cancel), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}

				});
				alert.setPositiveButton(getString(R.string.download), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Uri uri = Uri.parse(updateInfo.getUri());
						Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(downloadIntent);
					}
				});
				alert.show();
			} else {
				showToast("已是最新版");
			}
		}
	}



	public String getVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "0.1";
		}
	}

	public static class VersionComparator implements Comparator<String> {

		@Override
		public int compare(String str1, String str2) {
			String[] vals1 = str1.split("\\.");
			String[] vals2 = str2.split("\\.");
			int i = 0;
			// set index to first non-equal ordinal or length of shortest version string
			while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
				i++;
			}
			// compare first non-equal ordinal number
			if (i < vals1.length && i < vals2.length) {
				int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
				return Integer.signum(diff);
			}
			// the strings are equal or one string is a substring of the other
			// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
			else {
				return Integer.signum(vals1.length - vals2.length);
			}
		}
	}
}
