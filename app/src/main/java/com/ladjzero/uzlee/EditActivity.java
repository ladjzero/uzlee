package com.ladjzero.uzlee;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;

import java.io.File;
import java.util.ArrayList;


public class EditActivity extends SwipeActivity implements Core.OnRequestListener {
	public static final int EDIT_SUCCESS = 10;

	int tid;
	int pid;
	int fid;
	int uid;
	int no;
	boolean isNewThread, isReplyToOne, isReply, isEdit;
	String content;
	TextView subjectInput;
	TextView messageInput;
	Intent intent;
	private static final int SELECT_PHOTO = 100;
	ArrayList<Integer> attachIds = new ArrayList<Integer>();
	ArrayList<Integer> existedAttachIds = new ArrayList<Integer>();
	ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		enableBackAction();

		intent = getIntent();

		tid = intent.getIntExtra("tid", 0);
		pid = intent.getIntExtra("pid", 0);
		fid = intent.getIntExtra("fid", 0);
		uid = intent.getIntExtra("uid", 0);
		no = intent.getIntExtra("no", 0);
		isNewThread = (tid == 0 && pid == 0);
		isReplyToOne = (tid !=0 && pid != 0 && fid == 0);
		isReply = (tid != 0 && pid == 0 && fid == 0);
		isEdit = (tid != 0 && pid != 0 && fid != 0);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		getActionBar().setTitle(getIntent().getStringExtra("title"));
		setContentView(R.layout.edit);
		progress = new ProgressDialog(this);

		Core.getExistedAttach(new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				String[] ids = html.split(",");

				for (String id : ids) {
					if (id.length() > 0) existedAttachIds.add(Integer.valueOf(id));
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		subjectInput = (TextView) findViewById(R.id.edit_title);
		messageInput = (TextView) findViewById(R.id.edit_body);

		if (isNewThread) {
			subjectInput.setVisibility(View.VISIBLE);
		} else {
			if (uid != Core.getUid()) {
				//reply
				subjectInput.setVisibility(View.GONE);
			} else if (no != 1) {
				//edit non-top
				subjectInput.setVisibility(View.GONE);
			} else {
				//edit top
				subjectInput.setVisibility(View.VISIBLE);
			}
		}

		if (isEdit) {
			progress.setTitle("载入中");
			progress.show();

			Core.getEditBody(fid, tid, pid, new Core.OnRequestListener() {
				@Override
				public void onError(String error) {
					progress.dismiss();
				}

				@Override
				public void onSuccess(String html) {
					subjectInput.setText(html);
					progress.dismiss();
				}
			}, new Core.OnRequestListener() {
				@Override
				public void onError(String error) {
					progress.dismiss();
				}

				@Override
				public void onSuccess(String html) {
					messageInput.setText(html);
					progress.dismiss();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_reply, menu);

		menu.findItem(R.id.reply_send).setIcon(new IconDrawable(this, Iconify.IconValue.fa_send).colorRes(android.R.color.white).actionBarSize());
		menu.findItem(R.id.reply_add_image).setIcon(new IconDrawable(this, Iconify.IconValue.fa_image).colorRes(android.R.color.white).actionBarSize());

		if (fid != 0 && tid != 0 && pid != 0 && no != 1)
			menu.findItem(R.id.delete_post).setIcon(new IconDrawable(this, Iconify.IconValue.fa_trash).colorRes(android.R.color.white).actionBarSize());
		else
			menu.findItem(R.id.delete_post).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.reply_send) {
			String subject = subjectInput.getText().toString();
			String message = messageInput.getText().toString();

			if (isNewThread) {
				if (subject.length() == 0) {
					showToast("标题不能为空");
				} else if (message.length() == 0) {
					showToast("内容不能少于5字");
				} else {
					progress.setTitle("发送");
					progress.show();

					message += "\t\t\t[size=1][color=Gray]有只梨[/color][/size]";

					Core.newThread(fid, subject, message, attachIds, this);
				}
			} else if (isEdit) {
				if (no == 1 && subject.length() == 0) {
					showToast("标题不能为空");
				} else if (message.length() == 0) {
					showToast("内容不能少于5字");
				} else {
					progress.setTitle("发送");
					progress.show();

					Core.editPost(fid, tid, pid, subject, message, attachIds, this);
				}
			} else if (isReplyToOne) {
				progress.setTitle("发送");
				progress.show();
				message = "[b]回复 [url=http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=" + pid + "&ptid=" + tid + "]" + intent.getIntExtra("no", 0) + "#[/url] [i]" + intent.getStringExtra("userName") + "[/i] [/b]\n\n" + message;
				message += "\t\t\t[size=1][color=Gray]有只梨[/color][/size]";
				Core.sendReply(tid, message, attachIds, existedAttachIds, this);
			} else if (isReply) {
				progress.setTitle("发送");
				progress.show();

				message += "\t\t\t[size=1][color=Gray]有只梨[/color][/size]";
				Core.sendReply(tid, message, attachIds, existedAttachIds, this);
			}
//			if (fid != 0) {
//				// new thread or edit
//				String subject = subjectInput.getText().toString();
//				String message = messageInput.getText().toString();
//
//				if (isNewThread && subject.length() == 0) {
//					showToast("标题不能为空");
//				} else if (message.length() == 0) {
//					showToast("内容不能少于5字");
//				} else {
//					progress.setTitle("发送");
//					progress.show();
//
//					if (uid == Core.getUid()) {
//						Core.editPost(fid, tid, pid, subject, message, attachIds, this);
//					} else {
//						Core.newThread(fid, subject, message, attachIds, this);
//					}
//				}
//			} else {
//				// reply
//				progress.setTitle("发送");
//				progress.show();
//
//				String content = messageInput.getText().toString();
//
//				if (pid != 0) {
//					Intent intent = getIntent();
//					content = "[b]回复 [url=http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=" + pid + "&ptid=" + tid + "]" + intent.getIntExtra("no", 0) + "#[/url] [i]" + intent.getStringExtra("userName") + "[/i] [/b]\n\n" + content;
//				}
//
//				Core.sendReply(tid, content, attachIds, this);
//			}
		} else if (id == R.id.reply_add_image) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		} else if (id == R.id.delete_post) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("删除该回复？(实验性)");


			alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					progress.setTitle(getString(R.string.delete));
					progress.show();
					Core.deletePost(fid, tid, pid, EditActivity.this);
				}

			});

			alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}

			});

			alert.show();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		if (imageReturnedIntent != null) {
			Uri uri = imageReturnedIntent.getData();
			File imageFile = new File(getRealPathFromURI(this, uri));

			progress.setTitle("图片压缩");
			progress.show();

			Core.compressImage(imageFile, new Core.OnImageCompressed() {
				@Override
				public void onImage(File imageFile) {

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
									messageInput.setText(messageInput.getText() + "[attachimg]" + attachId + "[/attachimg]");
								}
							}
						}
					});
				}
			});
		}
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

	@Override
	public void onError(String error) {
		progress.dismiss();
		Toast.makeText(EditActivity.this, error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onSuccess(String html) {
		progress.dismiss();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("html", html);
		setResult(EDIT_SUCCESS, returnIntent);
		finish();
	}
}
