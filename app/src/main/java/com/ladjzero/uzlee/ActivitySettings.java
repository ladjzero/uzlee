package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.r0adkll.slidr.Slidr;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

public class ActivitySettings extends ActivityBase {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("设置");
		setContentView(R.layout.activity_settings);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(R.id.content, new SettingFragment()).commit();

		Slidr.attach(this);

		this.setResult(0, new Intent());
	}

	public static class SelectedForumsChangeEvent {
		private Set<String> values;

		public SelectedForumsChangeEvent(Set<String> values) {
			this.values = values;
		}
	}

	public static class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
		private ActivityBase mActivity;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);
			mActivity = (ActivityBase) getActivity();

			MultiSelectListPreference selectedForums = (MultiSelectListPreference) findPreference("selected_forums");
			List<Forum> forums = Core.getFlattenForums(mActivity);

			String[] forumNames = (String[]) CollectionUtils.collect(forums, new Transformer() {
				@Override
				public Object transform(Object o) {
					return o.toString();
				}
			}).toArray(new String[0]);

			String[] forumIds = (String[]) CollectionUtils.collect(forums, new Transformer() {
				@Override
				public Object transform(Object o) {
					return String.valueOf(((Forum) o).getFid());
				}
			}).toArray(new String[0]);

			selectedForums.setEntries(forumNames);
			selectedForums.setEntryValues(forumIds);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(android.R.color.white));
			return view;
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Preference preference = findPreference(key);

			if (key.equals("sort_thread")) {
				preference.setSummary("按照" + ((ListPreference) preference).getEntry() + "排序");
			}

			if (key.equals("enable_image_only_wifi")) {
				((ActivityBase) getActivity()).setImageNetwork();
			}

			if (key.equals("selected_forums")) {
				MultiSelectListPreference p = (MultiSelectListPreference) preference;
				EventBus.getDefault().post(new SelectedForumsChangeEvent(p.getValues()));
			}

			if (key.equals("theme_color")) {
				Intent intent = new Intent();
				intent.putExtra("reload", true);
				mActivity.setResult(0, intent);

				mActivity.reload();
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}
	}
}
