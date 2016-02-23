package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by R9NKCC3 on 2016/2/23.
 */
public class ActivityGallery extends ActivityBase implements ViewPager.OnPageChangeListener {
	@Bind(R.id.gallery)
	ViewPager mViewPage;
	AdapterGallery mAdapter;
	String[] mUrls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUrls = getIntent().getStringArrayExtra("imageUrls");
		setContentView(R.layout.activity_gallery);
		ButterKnife.bind(this);
		mViewPage.addOnPageChangeListener(this);
		mAdapter = new AdapterGallery(getSupportFragmentManager(), mUrls);
		mViewPage.setAdapter(mAdapter);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


	}

	@Override
	public void onPageSelected(int position) {

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public static class AdapterGallery extends FragmentPagerAdapter {
		private String[] mUrls;

		public AdapterGallery(FragmentManager fm, String[] urls) {
			super(fm);

			if (urls == null) {
				mUrls = new String[0];
			} else {
				mUrls = urls;
			}
		}

		@Override
		public Fragment getItem(int position) {
			Bundle args = new Bundle();
			args.putString("url", mUrls[position]);
			return FragmentGalleryImage.newInstance(args);
		}

		@Override
		public int getCount() {
			return mUrls.length;
		}
	}

	public static class FragmentGalleryImage extends Fragment {
		private String url;

		public static FragmentGalleryImage newInstance(Bundle args) {
			FragmentGalleryImage f = new FragmentGalleryImage();
			f.url = args.getString("url");
			return f;
		}
	}
}
