package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
	int mIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUrls = getIntent().getStringArrayExtra("srcs");
		mIndex = getIntent().getIntExtra("index", 0);
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
		@Bind(R.id.gallery_image)
		WebView2 mImage;
		private String mUrl;

		public static FragmentGalleryImage newInstance(Bundle args) {
			FragmentGalleryImage f = new FragmentGalleryImage();
			f.mUrl = args.getString("url");
			return f;
		}

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_gallery_image, null);
			ButterKnife.bind(this, root);
			mImage.getSettings().setJavaScriptEnabled(true);
			mImage.loadUrl("file:///android_asset/gallery_image.html");
			mImage.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					view.loadUrl("javascript:_showImage(\"" + mUrl + "\")");
				}
			});
			return root;
		}
	}
}
