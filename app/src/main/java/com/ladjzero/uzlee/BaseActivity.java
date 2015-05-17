package com.ladjzero.uzlee;

import java.util.Comparator;

import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ladjzero.hipda.Core;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;

import de.greenrobot.event.EventBus;


public class BaseActivity extends ActionBarActivity implements Core.OnProgress {
	private static final String TAG = "BaseActivity";
	protected ActionBar mActionbar;
	protected int mActionbarHeight;
	public static final int IMAGE_MEM_CACHE_SIZE = 16 * 1024 * 1024;

	SharedPreferences setting;
	EmojiUtils emojiUtils;
	Dialog alert;
	protected TextView mTitleView;


	public static final DisplayImageOptions imageStandAlone = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.color.snow_primary)
			.showImageOnLoading(R.color.snow_primary)
			.showImageOnFail(R.color.snow_primary)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.NONE_SAFE)
			.build();

	public static final DisplayImageOptions userImageInList = new DisplayImageOptions.Builder()
			.delayBeforeLoading(800)
			.showImageForEmptyUri(android.R.color.transparent)
			.showImageOnLoading(android.R.color.transparent)
			.showImageOnFail(android.R.color.transparent)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.displayer(new FadeInBitmapDisplayer(300, true, true, false))
			.build();

	public static final DisplayImageOptions BestQualityDisplay = new DisplayImageOptions.Builder()
			.delayBeforeLoading(800)
			.showImageForEmptyUri(R.color.snow_primary)
			.showImageOnLoading(R.color.snow_primary)
			.showImageOnFail(R.color.snow_primary)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.NONE_SAFE)
			.displayer(new FadeInBitmapDisplayer(300, true, true, false))
			.build();

	public static final DisplayImageOptions LowQualityDisplay = new DisplayImageOptions.Builder()
			.delayBeforeLoading(800)
			.showImageForEmptyUri(R.color.snow_primary)
			.showImageOnLoading(R.color.snow_primary)
			.showImageOnFail(R.color.snow_primary)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.displayer(new FadeInBitmapDisplayer(300, true, true, false))
			.build();

	public static final DisplayImageOptions BesetQualityForSingleImage = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(android.R.color.transparent)
			.showImageOnLoading(android.R.color.transparent)
			.showImageOnFail(android.R.color.transparent)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.NONE_SAFE)
			.build();

	public static final SlidrConfig slidrConfig = new SlidrConfig.Builder()
							.position(SlidrPosition.LEFT)
							.sensitivity(0.47f)
							.build();

	static {
		L.writeLogs(false);
	}

	public BaseActivity() {
	}

	private boolean enableSwipe() {
		return true;
	}

	;

	public void enableBackAction() {
	}

	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	public void setTitle(CharSequence title) {
		if (mTitleView == null) {
			super.setTitle(title);
		} else {
			mTitleView.setText(title);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Core.setup(this, true);
		emojiUtils = new EmojiUtils(this);

		setting = PreferenceManager.getDefaultSharedPreferences(this);

		mActionbar = getSupportActionBar();
//		mActionbar.setBackgroundDrawable(FlatUI.getActionBarDrawable(this, FlatUI.DARK, false));
		mActionbarHeight = mActionbar.getHeight();

		ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(this)
				.memoryCacheSizePercentage(50)
				.defaultDisplayImageOptions(userImageInList).build();
		ImageLoader.getInstance().init(ilConfig);

		Long lastCheck = setting.getLong("last_update_check", 0);
		Long now = System.currentTimeMillis();

		if (now - lastCheck > 12 * 3600 * 1000) {
			Core.requestUpdate();

			SharedPreferences.Editor editor = setting.edit();
			editor.putLong("last_update_check", now);
			editor.commit();
		}

		setImageNetwork();
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
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(Core.StatusChangeEvent statusChangeEvent) {
		Log.i(TAG, String.format("onEventMainThread.statusChangeEvent : user is null ? %b", statusChangeEvent.user == null));

		if (statusChangeEvent.user == null) {
			SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light) {

				@Override
				protected Dialog onBuild(Context context, int styleId) {
					Dialog dialog = super.onBuild(context, styleId);
					dialog.canceledOnTouchOutside(false);
					dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					return dialog;
				}

				@Override
				public void onPositiveActionClicked(DialogFragment fragment) {
					android.app.Dialog dialog = fragment.getDialog();

					String username = ((EditText) dialog.findViewById(R.id.user_name)).getText().toString();
					String password = ((EditText) dialog.findViewById(R.id.user_password)).getText().toString();
					final ProgressDialog progress = ProgressDialog.show(BaseActivity.this, "", getString(R.string.login) + "...", true);
					Spinner question = (Spinner) dialog.findViewById(R.id.question);
					EditText answer = (EditText) dialog.findViewById(R.id.answer);

					boolean questionShow = question.getVisibility() == View.VISIBLE;
					int questionId = questionShow ? question.getSelectedItemPosition() : 0;
					String answerStr = questionShow ? answer.getText().toString() : "";

					Core.login(username, password, questionId, answerStr, new Core.OnRequestListener() {

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

					super.onPositiveActionClicked(fragment);
				}

				@Override
				public void onNegativeActionClicked(DialogFragment fragment) {
					android.app.Dialog dialog = fragment.getDialog();

					final Spinner question = (Spinner) dialog.findViewById(R.id.question);
					final EditText answer = (EditText) dialog.findViewById(R.id.answer);

					if (question.getVisibility() == View.GONE) {
						question.setVisibility(View.VISIBLE);
						answer.setVisibility(View.VISIBLE);
						answer.setText("");
						mDialog.negativeAction("关闭验证问题");
					} else {
						question.setVisibility(View.GONE);
						answer.setVisibility(View.GONE);
						answer.setText("");
						mDialog.negativeAction("显示验证问题");
					}
				}
			};

			builder
					.contentView(R.layout.login_dialog)
					.title(getString(R.string.login_hipda))
					.positiveAction("登录")
					.negativeAction("显示验证问题");

			DialogFragment dialog = DialogFragment.newInstance(builder);

			dialog.show(getSupportFragmentManager(), null);
			Log.i(TAG, "login show");
		}
	}

	public void onEventMainThread(final Core.UpdateInfo updateInfo) {
		if (updateInfo != null) {
			String version = getVersion();
			String newVersion = updateInfo.getVersion();

			if (new VersionComparator().compare(version, newVersion) < 0) {
				SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light) {

					@Override
					public void onPositiveActionClicked(DialogFragment fragment) {
						Uri uri = Uri.parse(updateInfo.getUri());
						Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(downloadIntent);
					}
				};

				builder.message(updateInfo.getInfo())
						.positiveAction(getString(R.string.download));

				DialogFragment dialog = DialogFragment.newInstance(builder);

				dialog.show(getSupportFragmentManager(), null);
				Log.i(TAG, "update show");
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

	@Override
	public void progress(int current, int total) {
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

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.push_right_out);
	}

	protected void setImageNetwork() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

		if (activeNetInfo != null) {
			boolean isWifi = activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
			boolean loadWifiOnly = setting.getBoolean("enable_image_only_wifi", false);

			ImageLoader.getInstance().denyNetworkDownloads(!isWifi && loadWifiOnly);
			Log.i(TAG, String.format("wifi %b load when wifi only %b", isWifi, loadWifiOnly));
		}
	}

	public class ConnectionChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setImageNetwork();
		}
	}
}