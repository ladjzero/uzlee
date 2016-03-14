package com.ladjzero.uzlee;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.LruCache;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.PersistenceAdapter;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.UilUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.orhanobut.logger.Logger;

/**
 * Created by ladjzero on 2015/1/2.
 */
public class Application2 extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

	private LruCache<String, String> mMemCache;
	private HttpClient2 mHttpClient;
	private Core mCore;
	private boolean mShouldDownloadImage;
	private SharedPreferences mPref;

	public HttpClient2 getHttpClient() {
		return mHttpClient;
	}

	public Core getCore() {
		return mCore;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Iconify.with(new FontAwesomeModule()).with(new MaterialModule());

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
		PersistenceAdapter adapter = new AndroidAdapter(this);
		mCore = Core.initialize(adapter, mHttpClient);
		UilUtils.init(this);
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mPref.registerOnSharedPreferenceChangeListener(this);
		setImageNetwork();

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

	public boolean shouldDownloadImage() {
		return mShouldDownloadImage;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		if (s.equals(Constants.PREF_KEY_ENABLE_DOWNLOAD_IMAGE)) {
			setImageNetwork();
		}
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
}
