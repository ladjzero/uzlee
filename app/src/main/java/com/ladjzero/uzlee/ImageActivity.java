package com.ladjzero.uzlee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


public class ImageActivity extends SwipeActivity implements SwipeActivity.OnSwipeToggle{

	String url;
	int tid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
//		enableBackAction();
		final MyImageView imageView = (MyImageView) findViewById(R.id.image_view);
		imageView.setSwipeToggle(this);
//		imageView.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent motionEvent) {
//				if (imageView.canScroll(-1)) {
//					setEnableSwipe(false);
//					return false;
//				} else {
//					setEnableSwipe(true);
//					return true;
//				}
//			}
//		});

		url = getIntent().getStringExtra("url");
		tid = getIntent().getIntExtra("tid", 0);
		ImageLoader.getInstance().displayImage(url, imageView, imageStandAlone);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		menu.findItem(R.id.image_save).setIcon(new IconDrawable(this, Iconify.IconValue.fa_save).colorRes(android.R.color.white).actionBarSize());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.image_save) {
			ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String url, View v, Bitmap bitmap) {
					String localUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "tid_" + tid + "_" + System.currentTimeMillis(), "tid_" + tid);

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
}
