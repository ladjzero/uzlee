package com.ladjzero.uzlee;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.HttpApi;
import com.ladjzero.hipda.HttpClientCallback;
import com.ladjzero.hipda.LocalApi;
import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.Utils;
import com.rey.material.app.Dialog;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class ActivitySettings extends ActivityEasySlide implements SharedPreferences.OnSharedPreferenceChangeListener {
	private HttpApi mHttpApi;
	private LocalApi mLocalApi;
	private HttpClient2 mHttpClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHttpApi = getCore().getHttpApi();
		mLocalApi = getCore().getLocalApi();
		mHttpClient = getApp().getHttpClient();

		setTitle("设置");
		setContentView(R.layout.activity_settings);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(R.id.content, new SettingFragment()).commit();

		this.setResult(0, new Intent());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("enable_image_only_wifi")) {
			this.setImageNetwork();
		}

		if (key.equals("selected_forums")) {
			Intent intent = new Intent();
			intent.putExtra("reload", true);
			this.setResult(0, intent);
		}

		if (key.equals("theme")) {
			String newTheme = sharedPreferences.getString("theme", DefaultTheme);

			if (!newTheme.equals("night")) {
				sharedPreferences.edit().putString("lastDayTheme", newTheme).commit();
			}

			Intent intent = new Intent();
			this.setResult(0, intent);
			this.reload();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		getSettings().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		getSettings().unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	public static class SettingFragment extends PreferenceFragment {
		private ActivityBase mActivity;


		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);
			mActivity = (ActivityBase) getActivity();

			final SharedPreferences pref = mActivity.getSettings();

			findPreference("appinfo").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					try {
						Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						intent.setData(Uri.parse("package:com.ladjzero.uzlee"));
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
						startActivity(intent);
					}
					return true;
				}
			});

			Preference logout = findPreference("logout");
			final ActivitySettings activity = (ActivitySettings) getActivity();
			User me = activity.mLocalApi.getUser();
			logout.setTitle(me == null || me.getId() == 0 ? "登入" : "登出");

			logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					activity.mHttpApi.logout(new HttpClientCallback() {
						@Override
						public void onSuccess(String response) {
							activity.mHttpClient.getCookieStore().clear();

							Utils.gotoActivity(mActivity, ActivityLogin.class,
									Intent.FLAG_ACTIVITY_TASK_ON_HOME |
											Intent.FLAG_ACTIVITY_NEW_TASK |
											Intent.FLAG_ACTIVITY_CLEAR_TASK);
						}

						@Override
						public void onFailure(String reason) {
							mActivity.showToast(reason);
						}
					});

					return false;
				}
			});

			Preference forumsBtn = findPreference("selected_forums");
			forumsBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(getActivity(), ActivityForumPicker.class);
					getActivity().startActivity(intent);
					return false;
				}
			});

			final Preference theme = findPreference("theme");
			String themeStr = pref.getString("theme", DefaultTheme);
			theme.setSummary(Utils.getThemeName(getActivity(), themeStr));
			theme.setOnPreferenceClickListener(new ThemeClickListener((ActivityBase) getActivity(), "配色", "theme", R.layout.picker_theme) {

				@Override
				public void onSelect(String summary) {
					// No need to reset summary. For that this activity will be reloaded.
				}

				@Override
				public boolean onPreferenceClick(Preference preference) {
					dialog.show();
					return false;
				}
			});


			String KEY_FONT_SIZE = "font_size";
			final Preference fontSize = findPreference(KEY_FONT_SIZE);
			String fontSizeStr = pref.getString(KEY_FONT_SIZE, "normal");
			fontSize.setSummary(Utils.getFontSizeName(fontSizeStr));
			fontSize.setOnPreferenceClickListener(new FontSizeClickListener((ActivityBase) getActivity(), "正文字体大小", KEY_FONT_SIZE, R.layout.picker_font_size) {

				@Override
				public void onSelect(String summary) {
					fontSize.setSummary(summary);
				}

				@Override
				public boolean onPreferenceClick(Preference preference) {
					show(true);
					return false;
				}
			});

			String KEY_SORT_THREAD = "sort_thread";
			final Preference threadSort = findPreference(KEY_SORT_THREAD);
			String sortVal = pref.getString(KEY_SORT_THREAD, "2");
			threadSort.setSummary("按照" + Utils.getSortName(sortVal) + "排序");
			threadSort.setOnPreferenceClickListener(new ThreadSortClickListener((ActivityBase) getActivity(), "主题排序", KEY_SORT_THREAD, R.layout.picker_thread_sort) {
				@Override
				public void onSelect(String summary) {
					threadSort.setSummary(summary);
				}

				@Override
				public boolean onPreferenceClick(Preference preference) {
					show(true);
					return false;
				}
			});

			int imgSize = pref.getInt(Constants.PREF_KEY_IMG_SIZE, Constants.DEFAULT_IMAGE_UPLOAD_SIZE);
			final Preference imgSizePref = findPreference(Constants.PREF_KEY_IMG_SIZE);
			switch (imgSize) {
				case 300:
					imgSizePref.setSummary("300KB");
					break;
				case 800:
					imgSizePref.setSummary("800KB");
					break;
				case 1500:
					imgSizePref.setSummary("1.5MB");
					break;
			}
			imgSizePref.setOnPreferenceClickListener(new ImgSizeClickListener((ActivityBase) getActivity(), "图片大小", Constants.PREF_KEY_IMG_SIZE, R.layout.picker_image_size) {
				@Override
				public void onSelect(String summary) {
					imgSizePref.setSummary(summary);
				}

				@Override
				public boolean onPreferenceClick(Preference preference) {
					show(true);
					return false;
				}
			});

			Preference draftPref = findPreference("draft");
			draftPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					String json = pref.getString("draft", "");

					if (json.length() > 0) {
						ActivityEdit.Draft draft = null;

						try {
							draft = JSON.parseObject(json, ActivityEdit.Draft.class);
						} catch (Exception e) {
						}

						if (draft != null) {
							Intent intent = new Intent(mActivity, ActivityEdit.class);
							intent.putExtra("tid", draft.tid);
							intent.putExtra("fid", draft.fid);
							intent.putExtra("pid", draft.pid);
							intent.putExtra("uid", draft.uid);
							intent.putExtra("no", draft.no);
							intent.putExtra("subject", draft.subject);
							intent.putExtra("message", draft.message);
							intent.putExtra("title", draft.activityTitle);

							startActivity(intent);
						}
					}

					return true;
				}
			});


			Preference about = findPreference("about");
			about.setTitle("关于 " + mActivity.getVersion());
			about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Utils.gotoActivity(mActivity, ActivityAbout.class);
					return true;
				}
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(Utils.getThemeColor(getActivity(), android.R.attr.colorBackground));
			return view;
		}
	}

	abstract static class BaseClickListener implements Preference.OnPreferenceClickListener {
		Dialog dialog;
		ActivityBase context;
		String key;

		public BaseClickListener(ActivityBase context, String title, String key, int layoutId) {
			this.key = key;
			this.context = context;
			dialog = new Dialog(context);
			View contentView = context.getLayoutInflater().inflate(layoutId, null);
			ButterKnife.bind(this, contentView);

			dialog.negativeActionClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.title(title)
					.titleColor(Utils.getThemeColor(context, R.attr.colorText))
					.backgroundColor(Utils.getThemeColor(context, android.R.attr.colorBackground))
					.negativeAction("取消")
					.contentView(contentView);
		}

		public void show(boolean show) {
			if (show) {
				dialog.show();
			} else {
				dialog.dismiss();
			}
		}

		public abstract void onSelect(String summary);
	}

	abstract static class ImgSizeClickListener extends BaseClickListener {

		public ImgSizeClickListener(ActivityBase context, String title, String key, int layoutId) {
			super(context, title, key, layoutId);
			this.key = key;
		}

		@OnClick(R.id.img_small_size)
		void setImageSmallSize() {
			setImageSize(300);
		}

		@OnClick(R.id.img_middle_size)
		void setImageMiddleSize() {
			setImageSize(800);
		}

		@OnClick(R.id.img_big_size)
		void setImageBigSize() {
			setImageSize(1500);
		}

		void setImageSize(int size) {
			switch (size) {
				case 300:
					onSelect("300KB");
					break;
				case 800:
					onSelect("800KB");
					break;
				case 1500:
					onSelect("1.5MB");
					break;
			}

			context.getSettings().edit().putInt("img_size", size).commit();
			show(false);
		}
	}

	abstract static class ThreadSortClickListener extends BaseClickListener {

		public ThreadSortClickListener(ActivityBase context, String title, String key, int layoutId) {
			super(context, title, key, layoutId);
			this.key = key;
		}

		@OnClick(R.id.publish_time)
		void publishTime() {
			setSort("发表时间");
		}

		@OnClick(R.id.reply_time)
		void replyTime() {
			setSort("回复时间");
		}

		private void setSort(String sort) {
			String value;

			if ("发表时间".equals(sort)) {
				value = "1";
			} else {
				value = "2";
			}

			context.getSettings().edit().putString(key, value).commit();
			onSelect("按照" + sort + "排序");
			show(false);
		}
	}

	abstract static class FontSizeClickListener extends BaseClickListener {
		public FontSizeClickListener(ActivityBase context, String title, String key, int layoutId) {
			super(context, title, key, layoutId);
			this.key = key;
		}

		@OnClick(R.id.normal)
		void normal() {
			setFontSize("normal");
		}

		@OnClick(R.id.big)
		void big() {
			setFontSize("big");
		}

		@OnClick(R.id.bigger)
		void bigger() {
			setFontSize("bigger");
		}

		public void setFontSize(String size) {
			context.getSettings().edit().putString("font_size", size).commit();
			onSelect(Utils.getFontSizeName(size));
			show(false);
		}

		public abstract void onSelect(String summary);
	}

	abstract static class ThemeClickListener extends BaseClickListener {
		public ThemeClickListener(ActivityBase context, String title, String key, int layoutId) {
			super(context, title, key, layoutId);
			this.key = key;
		}

		@OnClick(R.id.red)
		void red() {
			setTheme("red");
		}

		@OnClick(R.id.carrot)
		void carrot() {
			setTheme("carrot");
		}

		@OnClick(R.id.orange)
		void orange() {
			setTheme("orange");
		}

		@OnClick(R.id.green)
		void green() {
			setTheme("green");
		}

		@OnClick(R.id.blueGrey)
		void blueGrey() {
			setTheme("blueGrey");
		}

		@OnClick(R.id.blue)
		void blue() {
			setTheme("blue");
		}

		@OnClick(R.id.purple)
		void purple() {
			setTheme("purple");
		}

		@OnClick(R.id.dark)
		void dark() {
			setTheme("dark");
		}

		@OnClick(R.id.night)
		void night() {
			setTheme("night");
		}

		private void setTheme(String themeStr) {
			context.getSettings().edit().putString(key, themeStr).commit();
			show(false);
		}
	}
}
