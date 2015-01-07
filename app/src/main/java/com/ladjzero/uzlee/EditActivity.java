package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;

import java.io.File;


public class EditActivity extends BaseActivity {

	int tid;
	String content;
	TextView contentInput;
	private static final int SELECT_PHOTO = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tid = getIntent().getIntExtra("thread_id", 0);

		getActionBar().setTitle(getIntent().getStringExtra("title"));
		setContentView(R.layout.edit);
	}

	@Override
	public void onStart() {
		super.onStart();

		View titleInput = findViewById(R.id.edit_title);
		contentInput = (TextView) findViewById(R.id.edit_body);
		if (getIntent().getBooleanExtra("hideTitleInput", false)) {
			titleInput.setVisibility(View.GONE);
		} else {
			titleInput.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_reply, menu);

		menu.findItem(R.id.reply_send).setIcon(new IconDrawable(this, Iconify.IconValue.fa_send).colorRes(android.R.color.white).actionBarSize());
		menu.findItem(R.id.reply_add_image).setIcon(new IconDrawable(this, Iconify.IconValue.fa_image).colorRes(android.R.color.white).actionBarSize());
		menu.findItem(R.id.reply_add_emoji).setIcon(new IconDrawable(this, Iconify.IconValue.fa_smile_o).colorRes(android.R.color.white).actionBarSize());

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.reply_send) {
//			Core.sendReply(tid, contentInput.getText().toString());
			Core.uploadImage(new File("/mnt/sdcard/6.png"), new Core.OnUploadListener() {

				@Override
				public void onUpload(String response) {

				}
			});
		} else if (id == R.id.reply_add_image) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		Uri uri = imageReturnedIntent.getData();
		File imageFile = new File(getRealPathFromURI(this, uri));
		Core.uploadImage(imageFile, new Core.OnUploadListener() {
			@Override
			public void onUpload(String response) {
				if (response.startsWith("DISCUZUPLOAD")) {
					int attachId = -1;

					try {
						attachId = Integer.valueOf(response.split("\\|")[2]);
					} catch (Exception e) {

					}

					if (attachId != -1) {
						Core.sendReply(tid, "[attachimg]" + attachId + "[/attachimg]", "attachnew[" + attachId + "][description]");
					}
				}
			}
		});
	}

	private String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
