package com.ladjzero.uzlee;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import uk.co.senab.photoview.PhotoViewAttacher;

import java.util.ArrayList;
import java.util.HashMap;


public class ImageActivity extends BaseActivity {

	String mUrl;
	SlidrInterface slidrInterface;
	ArrayList<String> mUrls;
	HashMap<Integer, Fragment> mFragments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_pager);
		mActionbar.setDisplayHomeAsUpEnabled(true);

		slidrInterface = Slidr.attach(this);


		Intent intent = getIntent();

		mUrls = intent.getStringArrayListExtra("urls");
		mUrl = getIntent().getStringExtra("url");

		mFragments = new HashMap<>();

		int index = mUrls.indexOf(mUrl);
		if (index == -1) index = 0;

		if (index == 0) slidrInterface.unlock();
		else slidrInterface.lock();

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new PagerAdapter(getFragmentManager()));
		pager.setCurrentItem(index, false);
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mUrl = mUrls.get(position);
				mActionbar.setTitle(String.format("%d/%d", position + 1, mUrls.size()));

				if (position == 0) slidrInterface.unlock();
				else slidrInterface.lock();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mActionbar.setTitle(String.format("%d/%d", index + 1, mUrls.size()));
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.image, menu);
		menu.findItem(R.id.image_save).setIcon(new IconDrawable(this, Iconify.IconValue.fa_save).colorRes(android.R.color.white).actionBarSize());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.image_save) {
			ImageLoader.getInstance().loadImage(mUrl, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String url, View v, Bitmap bitmap) {
					String localUrl = MediaStore.Images.Media.insertImage(
							getContentResolver(),
							bitmap,
							"" + System.currentTimeMillis(),
							mUrl.substring(mUrl.lastIndexOf("/") + 1)
					);

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse(localUrl), "image/*");
					startActivity(intent);
				}
			});
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public class ImageFragment extends Fragment {

		private static final String TAG = "ImageActivity";
		private String mUrl;
		private ImageView mImageView;
		PhotoViewAttacher mAttacher;
		private int position;

		public ImageFragment(String url) {
			position = mUrls.indexOf(url);
			Log.i(TAG, String.format("new ImageFragment %d %s", position, url));
			mUrl = url;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.activity_image, null);
			mImageView = (ImageView) rootView.findViewById(R.id.image_view);
			return rootView;
		}

		@Override
		public void onResume() {
			ImageLoader.getInstance().displayImage(mUrl, mImageView, BesetQualityForSingleImage, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					mAttacher = new PhotoViewAttacher(mImageView);
					mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
						@Override
						public void onViewTap(View view, float x, float y) {
							if (mActionbar.isShowing()) mActionbar.hide();
							else mActionbar.show();
						}
					});
				}
			});

			super.onResume();
		}
	}

	public class PagerAdapter extends FragmentPagerAdapter {

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mUrls.size();
		}

		@Override
		public Fragment getItem(int position) {
			return new ImageFragment(mUrls.get(position));
		}
	}
}
