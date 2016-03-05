package com.ladjzero.uzlee;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.uzlee.utils.Utils;
import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by R9NKCC3 on 2016/2/23.
 */
public class ActivityGallery extends ActivityEasySlide implements ViewPager.OnPageChangeListener {
	@Bind(R.id.gallery)
	ViewPager mViewPage;
	private AdapterGallery mAdapter;
	private String[] mUrls;
	private int mIndex;
	private TextView mTitleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUrls = getIntent().getStringArrayExtra("srcs");
		mIndex = getIntent().getIntExtra("index", 0);
		setContentView(R.layout.activity_gallery);
		ButterKnife.bind(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setBackgroundResource(android.R.color.transparent);

		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView = mInflater.inflate(R.layout.toolbar_title, null);
		mTitleView = (TextView) customView.findViewById(R.id.title);
		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(customView);

		mViewPage.addOnPageChangeListener(this);
		mAdapter = new AdapterGallery(getSupportFragmentManager(), mUrls);
		mViewPage.setAdapter(mAdapter);
		mViewPage.setCurrentItem(mIndex);
		mViewPage.setOffscreenPageLimit(3);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mTitleView.setText((position + 1) + "/" + mUrls.length);

	}

	@Override
	public void onPageSelected(int position) {
		if (position == 0) {
			getSlidrInterface().unlock();
		} else {
			getSlidrInterface().lock();
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.posts, menu);
		menu.findItem(R.id.more)
				.setIcon(new IconDrawable(this, MaterialIcons.md_file_download)
						.color(Utils.getThemeColor(this, R.attr.colorTextInverse))
						.actionBarSize());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
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
			WebSettings settings = mImage.getSettings();
			settings.setJavaScriptEnabled(true);

			// Magic. Properly zoom gallery image.
			settings.setSupportZoom(true);
			settings.setBuiltInZoomControls(true);
			settings.setDisplayZoomControls(false);
			settings.setLoadWithOverviewMode(true);
			settings.setUseWideViewPort(true);

			mImage.setBackgroundColor(Color.parseColor("#393939"));
			mImage.setWebViewClient(new WebView2.ImageCacheClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					view.loadUrl("javascript:_showImage(\"" + mUrl + "\")");
				}
			});
			mImage.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage cm) {
					Logger.t("WebView").d(cm.message());
					return true;
				}
			});

			return root;
		}

		@Override
		public void onResume() {
			super.onResume();
			mImage.loadUrl("file:///android_asset/gallery_image.html");
		}
	}
}
