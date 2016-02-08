package com.ladjzero.uzlee;

import android.os.Bundle;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

/**
 * Created by chenzhuo on 16-2-7.
 */
public class ActivityEasySlide extends ActivityBase {
	private SlidrInterface mSlidrInterface;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mSlidrInterface = Slidr.attach(this, new SlidrConfig.Builder()
				.position(SlidrPosition.LEFT)
				.distanceThreshold(0.25f)
				.velocityThreshold(2400)
				.sensitivity(1f)
				.build());

	}

	protected SlidrInterface getSlidrInterface() {
		return mSlidrInterface;
	}
}
