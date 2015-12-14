package com.ladjzero.uzlee;

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
import com.ladjzero.hipda.User;
import com.r0adkll.slidr.Slidr;
import com.rey.material.app.Dialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class ActivitySettings extends ActivityBase implements SharedPreferences.OnSharedPreferenceChangeListener {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("enable_image_only_wifi")) {
            this.setImageNetwork();
        }

        if (key.equals("selected_forums")) {
            Intent intent = new Intent();
            intent.putExtra("reload",  true);
            this.setResult(0, intent);
        }

        if (key.equals("theme")) {
            Intent intent = new Intent();
            intent.putExtra("reload", true);
            this.setResult(0, intent);
            this.reload();
        }
    }

    public static class SelectedForumsChangeEvent {
        private Set<String> values;

        public SelectedForumsChangeEvent(Set<String> values) {
            this.values = values;
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

    public static class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ActivityBase mActivity;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
            mActivity = (ActivityBase) getActivity();
            SharedPreferences pref = mActivity.getSettings();

//			MultiSelectListPreference selectedForums = (MultiSelectListPreference) findPreference("selected_forums");
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

//			selectedForums.setEntries(forumNames);
//			selectedForums.setEntryValues(forumIds);

            Preference logout = findPreference("logout");
            User me = Core.getUser();
            logout.setTitle(me == null || me.getId() == 0 ? "登入" : "登出");

            logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Core.logout(new Core.OnRequestListener() {
                        @Override
                        public void onError(String error) {
                            mActivity.showToast(error);
                        }

                        @Override
                        public void onSuccess(String html) {
                            Utils.gotoActivity(mActivity, ActivityLogin.class,
                                    Intent.FLAG_ACTIVITY_TASK_ON_HOME |
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(Utils.getThemeColor(getActivity(), android.R.attr.colorBackground));
            return view;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);

            if (key.equals("enable_image_only_wifi")) {
                ((ActivityBase) getActivity()).setImageNetwork();
            }

            if (key.equals("selected_forums")) {
                Intent intent = new Intent();
                intent.putExtra("reload",  true);
                getActivity().setResult(0, intent);
            }

            if (key.equals("theme")) {
                Intent intent = new Intent();
                intent.putExtra("reload", true);
                mActivity.setResult(0, intent);
                mActivity.reload();

//				preference.setSummary(((ListPreference) preference).getEntry());
            }
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

            dialog.title(title)
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

    abstract static class ThreadSortClickListener extends BaseClickListener {

        @OnClick(R.id.publish_time)
        void publishTime() {
            setSort("发表时间");
        }

        @OnClick(R.id.reply_time)
        void replyTime() {
            setSort("回复时间");
        }

        public ThreadSortClickListener(ActivityBase context, String title, String key, int layoutId) {
            super(context, title, key, layoutId);
            this.key = key;
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

        public FontSizeClickListener(ActivityBase context, String title, String key, int layoutId) {
            super(context, title, key, layoutId);
            this.key = key;
        }

        public void setFontSize(String size) {
            context.getSettings().edit().putString("font_size", size).commit();
            onSelect(Utils.getFontSizeName(size));
            show(false);
        }

        public abstract void onSelect(String summary);
    }

    abstract static class ThemeClickListener extends BaseClickListener {
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

        public ThemeClickListener(ActivityBase context, String title, String key, int layoutId) {
            super(context, title, key, layoutId);
            this.key = key;
        }


        private void setTheme(String themeStr) {
            context.getSettings().edit().putString(key, themeStr).commit();
            show(false);
        }
    }
}
