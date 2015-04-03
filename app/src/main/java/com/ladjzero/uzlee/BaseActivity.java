package com.ladjzero.uzlee;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import de.greenrobot.event.EventBus;


public class BaseActivity extends OrmLiteBaseActivity<DBHelper> {

	SharedPreferences setting;
	EmojiUtils emojiUtils;
	AlertDialog alert;
	static DisplayImageOptions displayImageOptions_no_scale = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.none)
			.showImageOnLoading(R.drawable.none)
			.showImageOnFail(R.drawable.none)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.NONE)
			.build();


	private boolean enableSwipe() {
		return true;
	}

	;

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

		setting = PreferenceManager.getDefaultSharedPreferences(this);

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
				.cacheOnDisk(true)
				.build();


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
	}


	public AlertDialog buildLoginDialog() {
		final View alertView = getLayoutInflater().inflate(R.layout.login_dialog, null);
		final Spinner question = (Spinner) alertView.findViewById(R.id.question);
		final EditText answer = (EditText) alertView.findViewById(R.id.answer);

		final CheckBox checkBox = (CheckBox) alertView.findViewById(R.id.checkBox);
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				int visibility = isChecked ? View.VISIBLE : View.GONE;

				question.setVisibility(visibility);
				answer.setVisibility(visibility);
			}
		});

		question.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				answer.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				answer.setText("");
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.login_hipda));
		builder.setView(alertView);
		builder.setPositiveButton(getString(R.string.login), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				String username = ((EditText) alertView.findViewById(R.id.user_name))
						.getText().toString();
				String password = ((EditText) alertView
						.findViewById(R.id.user_password)).getText().toString();
				final ProgressDialog progress = ProgressDialog.show(
						BaseActivity.this, "", getString(R.string.login) + "...", true);

				int questionId = checkBox.isChecked() ? question.getSelectedItemPosition() : 0;
				String answerStr = checkBox.isChecked() ? answer.getText().toString() : "";

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
			}

		});

		return builder.create();
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

	public void showLogin() {
		if (alert != null) alert.dismiss();
		alert = buildLoginDialog();
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