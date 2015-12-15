package com.ladjzero.uzlee;

import android.app.Application;
import android.util.LruCache;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by ladjzero on 2015/1/2.
 */
@ReportsCrashes(
		formKey = "", // This is required for backward compatibility but not used
		mailTo = "ladjzero@163.com",
		customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT},
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_report
)
public class MyApplication extends Application {

	private LruCache<String, String> mMemCache;

	@Override
	public void onCreate() {
		super.onCreate();
//		ACRA.init(this);


		Iconify.with(new FontAwesomeModule()).with(new MaterialModule());

		// 10MB.
		mMemCache = new LruCache<String, String>(1024 * 1024 * 10) {

			@Override
			protected int sizeOf(String key, String value) {
				return value.length();
			}
		};

		Logger.init();
	}

	public LruCache<String, String> getMemCache() {
		return mMemCache;
	}
}
