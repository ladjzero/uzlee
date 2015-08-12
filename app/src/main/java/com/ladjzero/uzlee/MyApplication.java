package com.ladjzero.uzlee;

import android.app.Application;

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

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);

		Logger.init().setLogLevel(LogLevel.NONE);
	}
}
