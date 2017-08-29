package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.uzlee.model.Forum;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.uzlee.model.Version;
import com.ladjzero.uzlee.utils.EmojiUtils;
import com.ladjzero.uzlee.utils.Utils;
import com.ladjzero.uzlee.utils.VersionComparator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;
import com.rey.material.app.Dialog;
import com.tencent.stat.StatService;

import org.apache.commons.lang3.StringUtils;

import java.util.List;


public abstract class ActivityBase extends ActionBarActivity {
	public static final int IMAGE_MEM_CACHE_SIZE = 16 * 1024 * 1024;
	public static final String DefaultTheme = "dark";
	private static final String TAG = "ActivityBase";
	private static final int mTransparenty = android.R.color.transparent;
	public static final DisplayImageOptions LowQualityDisplay = new DisplayImageOptions.Builder()
			.delayBeforeLoading(800)
			.showImageForEmptyUri(mTransparenty)
			.showImageOnLoading(mTransparenty)
			.showImageOnFail(mTransparenty)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.displayer(new FadeInBitmapDisplayer(300, true, true, false))
			.build();
	private static List<Forum> mForums = null;

	static {
		L.writeLogs(false);
	}

	SharedPreferences setting;
	EmojiUtils emojiUtils;
	SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	private int mThemeId;

	public static List<Forum> getForums(Context context) {
		if (mForums == null) {
			mForums = buildFromJSON(Utils.readAssetFile(context, "hipda.json"));
		}

		return mForums;
	}

	public static List<Forum> buildFromJSON(String json) {
		List<Forum> forums = JSON.parseArray(json, Forum.class);
		addALLType(forums);

		return forums;
	}

	private static void addALLType(List<Forum> forums) {
		Forum.Type all = new Forum.Type();
		all.setId(-1);
		all.setName("所有类别");

		for (Forum f : forums) {
			List<Forum.Type> types = f.getTypes();
			List<Forum> children = f.getChildren();

			if (types != null) types.add(0, all);
			if (children != null) addALLType(children);
		}
	}

	public void showToast(String message) {
		Utils.showToast(this, message);
	}

	public void reload() {
		finish();
		Intent intent = new Intent(this, this.getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public SharedPreferences getSettings() {
		return setting;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setting = PreferenceManager.getDefaultSharedPreferences(this);
		String themeColor = setting.getString("theme", DefaultTheme);

		setTheme(mThemeId = Utils.getTheme(themeColor));

		super.onCreate(savedInstanceState);

		emojiUtils = new EmojiUtils(this);


		ActionBar mActionbar = getSupportActionBar();
//		mActionbarHeight = mActionbar.getHeight();

		checkUpdate(false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				window.setStatusBarColor(Utils.getThemeColor(this, R.attr.colorPrimary));
			} catch (Throwable t) {

			}
		}
	}

	public void checkUpdate(boolean force) {
		Long lastCheck = setting.getLong("last_update_check", 0);
		Long now = System.currentTimeMillis();

		if (force || now - lastCheck > 12 * 3600 * 1000) {
			App.getInstance().getApi().getVersions(new OnRespondCallback() {
				@Override
				public void onRespond(Response res) {
					if (res.isSuccess()) {
						List<Version> info = (List<Version>) res.getData();

						if (info != null) {
							String version = getVersion();
							String newVersion = String.valueOf(info.get(0).getV());

							if (new VersionComparator().compare(version, newVersion) < 0) {
								final List<Version> finalInfo = info;

								final Dialog dialog = new Dialog(ActivityBase.this);
								View dialogView = getLayoutInflater().inflate(R.layout.update_info, null);
								TextView infoText = (TextView) dialogView.findViewById(R.id.text);
								infoText.setText(StringUtils.join(info.get(0).getLogs(), '\n'));

								dialog.title("新的版本 v" + info.get(0).getV())
										.titleColor(Utils.getThemeColor(ActivityBase.this, R.attr.colorText))
										.backgroundColor(Utils.getThemeColor(ActivityBase.this, android.R.attr.colorBackground))
										.negativeAction("取消")
										.negativeActionClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										})
										.positiveAction(getString(R.string.download))
										.positiveActionClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												Uri uri = Uri.parse(finalInfo.get(0).getUrl());
												Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
												startActivity(downloadIntent);
											}
										})
										.contentView(dialogView)
										.show();
							} else {
								showToast("已是最新版");
							}
						}
					}
				}
			});

			SharedPreferences.Editor editor = setting.edit();
			editor.putLong("last_update_check", now);
			editor.commit();
		}
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

	public ActionBar setCustomView(int toolbarId, int customViewLayoutId) {
		Toolbar toolbar = (Toolbar) findViewById(toolbarId);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();

		if (customViewLayoutId > 0) {
			LayoutInflater mInflater = LayoutInflater.from(this);
			View customView = mInflater.inflate(customViewLayoutId, null);

			actionBar.setTitle(null);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setCustomView(customView);
		}

		return getSupportActionBar();
	}

	public String getVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "0.1";
		}
	}

	public int getThemeId() {
		return mThemeId;
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.push_right_out);
	}

	public void toLoginPage() {
		Utils.replaceActivity(this, ActivityLogin.class);
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
		setting.registerOnSharedPreferenceChangeListener(prefListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
		setting.unregisterOnSharedPreferenceChangeListener(prefListener);
	}

	public App getApp() {
		return (App) getApplication();
	}

	public interface OnToolbarClickListener {
		void toolbarClick();
	}
}
