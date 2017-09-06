package com.ladjzero.uzlee;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.webkit.WebView;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.uzlee.api.Interceptor;
import com.ladjzero.uzlee.model.Forum;
import com.ladjzero.uzlee.api.Api;
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
public class App extends Application {
	private static App app;
	private LruCache<String, String> mMemCache;
	private SharedPreferences mPref;
	private List<OnEventListener> mListeners;
	private Api mApi;

	public Integer getUid() {
		return mUid;
	}

	public String getUserName() {
		return mUserName;
	}

	private String mUserName;
	private Integer mUid;

	public Integer getUnread() {
		return mUnread;
	}

	private Integer mUnread;

	public static App getInstance() {
		return app;
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

		mApi = Api.getApi(this);
		mApi.intercept(new Interceptor() {
			@Override
			public Response intercept(Response res) {
				Response.Meta meta = res.getMeta();

				if (meta != null) {
					if (meta.getUid() != null) {
						mUid = meta.getUid();
					}

					if (meta.getUserName() != null) {
						mUserName = meta.getUserName();
					}

					if (meta.getUnread() != null) {
						mUnread = meta.getUnread();
					}
				}

				return res;
			}
		});
		mApi.setMode(Api.Mode.LOCAL);
		UilUtils.init(this);
		mPref = PreferenceManager.getDefaultSharedPreferences(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		app = this;
		mListeners = new ArrayList<>();

		Logger.init();
	}

	public LruCache<String, String> getMemCache() {
		return mMemCache;
	}

	public List<Forum> getFlattenForums() {
		return Forum.flatten(Utils.getAllForums(this));
	}

	public List<Forum> getUserFlattenForums() {
		List<Forum> forums = getFlattenForums();

		CollectionUtils.filter(forums, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				Forum f = (Forum) o;
				return (mUid != null && mUid != 0) || !f.isSecurity();
			}
		});

		return forums;
	}

	public SharedPreferences getSharedPreferences() {
		return mPref;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ImageLoader.getInstance().clearMemoryCache();
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
