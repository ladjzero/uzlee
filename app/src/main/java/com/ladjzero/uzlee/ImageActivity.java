package com.ladjzero.uzlee;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;
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
	ViewPager pager;
	int mWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_pager);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66454545")));
		slidrInterface = Slidr.attach(this);


		Intent intent = getIntent();

		mUrls = intent.getStringArrayListExtra("urls");
		mUrl = getIntent().getStringExtra("url");

		mFragments = new HashMap<>();

		int index = mUrls.indexOf(mUrl);
		if (index == -1) index = 0;

		if (index == 0) slidrInterface.unlock();
		else slidrInterface.lock();

		pager = (ViewPager) findViewById(R.id.pager);
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

					if (localUrl == null) {
						showToast("保存失败，图像地址已复制到剪切板中");

						ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clipData = ClipData.newPlainText("image url", mUrl);
						clipboardManager.setPrimaryClip(clipData);
					} else {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(localUrl), "image/*");
						startActivity(intent);
					}
				}
			});
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("ValidFragment")
	public static class ImageFragment extends Fragment {

		private static final String TAG = "ImageActivity";
		private String mUrl;
		private ArrayList<String> mUrls;
		private ImageView mImageView;
		PhotoViewAttacher mAttacher;
		private int position;
		private ActionBar mActionbar;
		int mWidth;

		@SuppressLint("ValidFragment")
		public ImageFragment(Context context, String url, ArrayList<String> urls) {
			mUrls = urls;
			position = mUrls.indexOf(url);
			Logger.i("new ImageFragment %d %s", position, url);
			mUrl = url;
			mActionbar = ((ImageActivity) context).mActionbar;

			Display display = ((ImageActivity)context).getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			mWidth = size.x;
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
					mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
						@Override
						public void onPhotoTap(View view, float v, float v1) {
							if (mActionbar.isShowing()) {
								mActionbar.hide();
							} else {
								mActionbar.show();
							}
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
			return new ImageFragment(ImageActivity.this, mUrls.get(position), mUrls);
		}
	}
}
