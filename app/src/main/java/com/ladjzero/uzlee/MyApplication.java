package com.ladjzero.uzlee;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by ladjzero on 2015/1/2.
 */
@ReportsCrashes(
		formKey = "", // This is required for backward compatibility but not used
		mailTo = "ladjzero@163.com",
		mode = ReportingInteractionMode.TOAST
)
public class MyApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}
