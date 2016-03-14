package com.ladjzero.uzlee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.uzlee.utils.EmojiUtils;
import com.ladjzero.uzlee.utils.Utils;
import com.ladjzero.uzlee.utils.VersionComparator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;
import com.orhanobut.logger.Logger;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.tencent.stat.StatService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public abstract class ActivityBase extends ActionBarActivity {
	public static final int IMAGE_MEM_CACHE_SIZE = 16 * 1024 * 1024;
	public static final String DefaultTheme = "dark";
	private static final String TAG = "ActivityBase";
	private static final int mTransparenty = android.R.color.transparent;
	private static List<Forum> mForums = null;



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

	static {
		L.writeLogs(false);
	}

	SharedPreferences setting;
	EmojiUtils emojiUtils;
	private int mThemeId;
	SharedPreferences.OnSharedPreferenceChangeListener prefListener;

	public Core getCore() {
		return getApp().getCore();
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
	}

	public void checkUpdate(boolean force) {
		Long lastCheck = setting.getLong("last_update_check", 0);
		Long now = System.currentTimeMillis();

		if (force || now - lastCheck > 12 * 3600 * 1000) {
			getApp().getHttpClient().get("https://raw.githubusercontent.com/ladjzero/uzlee/master/release/update.json", new HttpClientCallback() {
				@Override
				public void onSuccess(String response) {
					RemotePackageInfo info = null;

					try {
						info = JSON.parseObject(response, RemotePackageInfo.class);
					} catch (Exception e) {

					}

					if (info != null) {
						String version = getVersion();
						String newVersion = info.getVersion();

						if (new VersionComparator().compare(version, newVersion) < 0) {
							final RemotePackageInfo finalInfo = info;
							SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light) {

								@Override
								public void onPositiveActionClicked(DialogFragment fragment) {
									Uri uri = Uri.parse(finalInfo.getUri());
									Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
									startActivity(downloadIntent);
								}
							};

							builder.message(info.getInfo())
									.positiveAction(getString(R.string.download));

							DialogFragment dialog = DialogFragment.newInstance(builder);

							dialog.show(getSupportFragmentManager(), null);
						} else {
							showToast("已是最新版");
						}
					}
				}

				@Override
				public void onFailure(String reason) {
					showToast("检查更新失败");
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


	public interface OnToolbarClickListener {
		void toolbarClick();
	}

	public Application2 getApp() {
		return (Application2) getApplication();
	}

	public List<Forum> getSelectedForums(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		Collection<Integer> selected = CollectionUtils.collect(Arrays.asList(pref.getString("selected_forums", "").split(",")), new Transformer() {
			@Override
			public Object transform(Object o) {
				try {
					return Integer.valueOf((String) o);
				} catch (Exception e) {
					return -1;
				}
			}
		});

		if (selected.size() == 0 || selected.contains(-1)) {
			List<String> selectedStrs = Arrays.asList(
					context.getResources().getStringArray(R.array.default_forums));

			selected = CollectionUtils.collect(selectedStrs, new Transformer() {
				@Override
				public Object transform(Object o) {
					return Integer.valueOf((String) o);
				}
			});

			pref.edit().putString("selected_forums", StringUtils.join(selectedStrs, ','));
		}

		return Forum.findByIds(getForums(context), selected);
	}

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
	public static List<Forum> getFlattenForums(Context context) {
		return Forum.flatten(getForums(context));
	}
}
