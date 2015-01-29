package com.ladjzero.uzlee;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuIcon;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class EditActivity extends BaseActivity {

	int tid;
	int pid;
	int fid;
	String content;
	TextView titleInput;
	TextView contentInput;
	private static final int SELECT_PHOTO = 100;
	ArrayList<Integer> attachIds = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enableBackAction();


		Intent intent = getIntent();

		tid = intent.getIntExtra("tid", 0);
		pid = intent.getIntExtra("pid", 0);
		fid = intent.getIntExtra("fid", 0);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		getActionBar().setTitle(getIntent().getStringExtra("title"));
		setContentView(R.layout.edit);

	}

	@Override
	public void onStart() {
		super.onStart();

		titleInput = (TextView) findViewById(R.id.edit_title);
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
			if (fid != 0) {

				String subject = titleInput.getText().toString();
				String content = contentInput.getText().toString();

				if (subject.length() == 0) {
					Toast.makeText(EditActivity.this, "标题不能为空", Toast.LENGTH_LONG).show();
				} else if (content.length() == 0) {
					Toast.makeText(EditActivity.this, "内容不能少于5字", Toast.LENGTH_LONG).show();
				} else {
					final ProgressDialog progress = new ProgressDialog(this);
					progress.setTitle("发送");
					progress.show();

					Core.newThread(fid, subject, content, attachIds, new Core.OnRequestListener() {
						@Override
						public void onError(String error) {
							Toast.makeText(EditActivity.this, error, Toast.LENGTH_LONG).show();
						}

						@Override
						public void onSuccess(String html) {
							progress.dismiss();
							finish();
						}
					});
				}
			} else {
				final ProgressDialog progress = new ProgressDialog(this);
				progress.setTitle("发送");
				progress.show();

				String content = contentInput.getText().toString();

				if (pid != 0) {
					Intent intent = getIntent();
					content = "[b]回复 [url=http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=" + pid + "&ptid=" + tid + "]" + intent.getIntExtra("no", 0) + "#[/url] [i]" + intent.getStringExtra("userName") + "[/i] [/b]\n\n" + content;
				}

				Core.sendReply(tid, content, attachIds, new Core.OnRequestListener() {
					@Override
					public void onError(String error) {
						Toast.makeText(EditActivity.this, error, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onSuccess(String html) {
						progress.dismiss();
						finish();
					}
				});
			}
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

		final ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("图片压缩");
		progress.show();

		Core.compressImage(imageFile, new Core.OnImageCompressed() {
			@Override
			public void onImage(File imageFile) {
//				progress.dismiss();

				progress.setTitle("图片上传");

				Core.uploadImage(imageFile, new Core.OnUploadListener() {
					@Override
					public void onUpload(String response) {
						progress.dismiss();

						if (response.startsWith("DISCUZUPLOAD")) {
							int attachId = -1;

							try {
								attachId = Integer.valueOf(response.split("\\|")[2]);
							} catch (Exception e) {

							}

							if (attachId != -1) {
								attachIds.add(attachId);
								contentInput.setText(contentInput.getText() + "[attachimg]" + attachId + "[/attachimg]");
							}
						}
					}
				});
			}
		});
	}

	private String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = {MediaStore.Images.Media.DATA};
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
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
