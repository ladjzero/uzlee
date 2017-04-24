package com.ladjzero.uzlee;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.webkit.WebView;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.UilUtils;
import com.ladjzero.uzlee.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.orhanobut.logger.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ladjzero on 2015/1/2.
 */
public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static App app;

	private LruCache<String, String> mMemCache;
	private HttpClient2 mHttpClient;
	private Core mCore;
	private boolean mShouldDownloadImage;
	private SharedPreferences mPref;
	private List<OnEventListener> mListeners;
	private Api mApi;

	private List<Forum> mFlattenForums;

	public static App getInstance() {
		return app;
	}

	public HttpClient2 getHttpClient() {
		return mHttpClient;
	}

	public Core getCore() {
		return mCore;
	}

	public Api getApi() { return mApi; }

	@Override
	public void onCreate() {
		super.onCreate();

		Iconify.with(new MaterialModule());

		// 10MB.
		mMemCache = new LruCache<String, String>(1024 * 1024 * 10) {

			@Override
			protected int sizeOf(String key, String value) {
				return value.length();
			}
		};

		ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(this)
				.memoryCacheSizePercentage(50)
				.defaultDisplayImageOptions(Constants.DIO_USER_IMAGE).build();
		ImageLoader.getInstance().init(ilConfig);

		mHttpClient = new HttpClient2(this);
		mApi = new Api();
		PersistenceAdapter adapter = new AndroidAdapter(this);
		mCore = Core.initialize(adapter, mHttpClient);
		UilUtils.init(this);
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mPref.registerOnSharedPreferenceChangeListener(this);
		setImageNetwork();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		app = this;
		mListeners = new ArrayList<>();

		Logger.init();
	}

	@Override
	public void onTerminate() {
		mPref.unregisterOnSharedPreferenceChangeListener(this);
		super.onTerminate();
	}

	public LruCache<String, String> getMemCache() {
		return mMemCache;
	}

	public List<Forum> getFlattenForums() {
		return Forum.flatten(Utils.getAllForums(this));
	}

	public List<Forum> getUserFlattenForums() {
		final User me = getInstance().getCore().getApiStore().getUser();
		List<Forum> forums = getFlattenForums();

		CollectionUtils.filter(forums, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				Forum f = (Forum) o;
				return me.getId() != 0 || !f.isSecurity();
			}
		});

		return forums;
	}

	public boolean shouldDownloadImage() {
		return mShouldDownloadImage;
	}

	public SharedPreferences getSharedPreferences() {
		return mPref;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		if (s.equals(Constants.PREF_KEY_ENABLE_DOWNLOAD_IMAGE)) {
			setImageNetwork();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ImageLoader.getInstance().clearMemoryCache();
	}

	public void setImageNetwork() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

		if (activeNetInfo != null) {
			boolean isWifi = activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
			boolean loadWifiOnly = mPref.getBoolean(Constants.PREF_KEY_ENABLE_DOWNLOAD_IMAGE, false);

			mShouldDownloadImage = isWifi || !loadWifiOnly;

			ImageLoader.getInstance().denyNetworkDownloads(!mShouldDownloadImage);
			Logger.i("wifi %b load when wifi only %b", isWifi, loadWifiOnly);
		}
	}

	public interface OnEventListener {
		void onEvent(Object o);
	}

	public void addEventListener(OnEventListener l) {
		mListeners.add(l);
	}

	public void removeEventListener(OnEventListener l){
		mListeners.remove(l);
	}

	public void dispatchEvent(Object o) {
		for (OnEventListener l : mListeners) {
			l.onEvent(o);
		}
	}
}