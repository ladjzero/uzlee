package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.entities.Post;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.uzlee.api.HttpClientCallback;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.EmojiUtils;
import com.ladjzero.uzlee.utils.Utils;
import com.nineoldandroids.animation.Animator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.orhanobut.logger.Logger;
import com.rey.material.app.Dialog;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


public class ActivityEdit extends ActivityHardSlide implements HttpClientCallback, OnRespondCallback {
	public static final int EDIT_SUCCESS = 10;
	private static final int SELECT_PHOTO = 100;
	int tid;
	int pid;
	int fid;
	int uid;
	int no;
	boolean isNewThread, isReplyToOne, isReply, isEdit;
	String content;
	TextView subjectInput;
	EditText mMessageInput;
	Intent intent;
	ArrayList<Integer> attachIds = new ArrayList<Integer>();
	ArrayList<Integer> existedAttachIds = new ArrayList<Integer>();
	Dialog progress;
	boolean mSaveDraft = true;
	private View mEmojiSelector;
	private InputMethodManager mImeManager;
	private boolean mIsAnimating = false;
	private AsyncTask mImageTask, mParseTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		intent = getIntent();

		tid = intent.getIntExtra("tid", 0);
		pid = intent.getIntExtra("pid", 0);
		fid = intent.getIntExtra("fid", 0);
		uid = intent.getIntExtra("uid", 0);
		no = intent.getIntExtra("no", 0);

		isNewThread = (tid == 0 && pid == 0);
		isReplyToOne = (tid != 0 && pid != 0 && fid == 0);
		isReply = (tid != 0 && pid == 0 && fid == 0);
		isEdit = (tid != 0 && pid != 0 && fid != 0);

		String title = intent.getStringExtra("title");
		if (title != null) setTitle(title);

		progress = new Dialog(this)
				.cancelable(false)
				.contentView(R.layout.progress_circular)
				.titleColor(Utils.getThemeColor(this, R.attr.colorText))
				.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground));

		App.getInstance().getApi().getExistedAttach(new OnRespondCallback() {
			@Override
			public void onRespond(Response res) {
				if (res.isSuccess()) {
					String[] ids = (String[]) res.getData();

					for (String id : ids) {
						if (id.length() > 0) existedAttachIds.add(Integer.valueOf(id));
					}
				} else {
					showToast(res.getData().toString());
				}
			}
		});

		mImeManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		subjectInput = (TextView) findViewById(R.id.edit_title);
		mMessageInput = (EditText) findViewById(R.id.edit_body);

		String subject = intent.getStringExtra("subject");
		String message = intent.getStringExtra("message");

		if (subject != null) subjectInput.setText(subject);
		if (message != null) mMessageInput.setText(message);

		mMessageInput.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mEmojiSelector != null) mEmojiSelector.setVisibility(View.GONE);
			}
		});

		if (isNewThread) {
			subjectInput.setVisibility(View.VISIBLE);
		} else {
			if (uid != App.getInstance().getCore().getLocalApi().getUser().getId()) {
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

		// If subject and message have content.
		// This activity may be revived from a draft.
		if (isEdit && subjectInput.length() == 0 && mMessageInput.length() == 0) {
			progress.setTitle("载入中");
			progress.show();

			getApp().getApi().getEditBody(fid, tid, pid, new OnRespondCallback() {
				@Override
				public void onRespond(Response res) {
					if (res.isSuccess()) {
						Post post = (Post) res.getData();
						subjectInput.setText(post.getTitle());
						mMessageInput.setText(post.getBody());
					} else {
						showToast(res.getData().toString());
					}

					progress.dismiss();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_reply, menu);

		int color = Utils.getThemeColor(this, R.attr.colorTextInverse);
		menu.findItem(R.id.reply_send)
				.setIcon(new IconDrawable(this, MaterialIcons.md_send)
						.color(color)
						.actionBarSize());
		menu.findItem(R.id.reply_add_image)
				.setIcon(new IconDrawable(this, MaterialIcons.md_image)
						.color(color)
						.actionBarSize());
		menu.findItem(R.id.reply_add_emoji)
				.setIcon(new IconDrawable(this, MaterialIcons.md_tag_faces)
						.color(color)
						.actionBarSize());

		if (fid != 0 && tid != 0 && pid != 0 && no != 1)
			menu.findItem(R.id.delete_post)
					.setIcon(new IconDrawable(this, MaterialIcons.md_delete)
							.color(color)
							.actionBarSize());
		else
			menu.findItem(R.id.delete_post).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.reply_send) {
			String sig = setting.getBoolean("use_sig", false) ? "有只梨" : "";

			String subject = subjectInput.getText().toString();
			String message = mMessageInput.getText().toString();

			if (isNewThread) {
				if (subject.length() == 0) {
					showToast("标题不能为空");
				} else if (message.length() == 0) {
					showToast("内容不能少于5字");
				} else {
					progress.setTitle("发送");
					progress.show();

					if (sig.length() > 0)
						message += "\t\t\t[size=1][color=Gray]" + sig + "[/color][/size]";

					getApp().getApi().newThread(fid, subject, message, attachIds, this);
				}
			} else if (isEdit) {
				if (no == 1 && subject.length() == 0) {
					showToast("标题不能为空");
				} else if (message.length() == 0) {
					showToast("内容不能少于5字");
				} else {
					progress.setTitle("发送");
					progress.show();

					getApp().getApi().editPost(fid, tid, pid, subject, message, attachIds, this);
				}
			} else if (isReplyToOne) {
				progress.setTitle("发送");
				progress.show();

				message = "[b]回复 [url=http://www.hi-pda.com/forum/redirect.php?goto=findpost&pid=" + pid + "&ptid=" + tid + "]" + intent.getIntExtra("no", 0) + "#[/url] [i]" + intent.getStringExtra("userName") + "[/i] [/b]\n\n" + message;
				message += "\t\t\t[size=1][color=Gray]" + sig + "[/color][/size]";
				getApp().getApi().sendReply(tid, message, attachIds, existedAttachIds, this);
			} else if (isReply) {
				progress.setTitle("发送");
				progress.show();

				message += "\t\t\t[size=1][color=Gray]" + sig + "[/color][/size]";
				getApp().getApi().sendReply(tid, message, attachIds, existedAttachIds, this);
			}
		} else if (id == R.id.reply_add_image) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		} else if (id == R.id.delete_post) {
			final Dialog mDialog = new Dialog(this);

			mDialog
					.title("删除该回复？(实验性)")
					.canceledOnTouchOutside(true)
					.positiveActionClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
							getApp().getApi().deletePost(fid, tid, pid, ActivityEdit.this);
						}
					})
					.negativeActionClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
						}
					})
					.negativeAction("取消")
					.positiveAction("确认")
					.titleColor(Utils.getThemeColor(this, R.attr.colorText))
					.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
					.show();
		} else if (id == R.id.reply_add_emoji) {
			if (mEmojiSelector == null) {
				((ViewStub) findViewById(R.id.emoji_viewstub)).inflate();
				mEmojiSelector = findViewById(R.id.emoji);
			}

			if (!mIsAnimating) {
				if (mEmojiSelector.getVisibility() == View.GONE) {
					YoYo.with(Techniques.SlideInUp)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mEmojiSelector.setVisibility(View.VISIBLE);
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mIsAnimating = false;
									mImeManager.hideSoftInputFromWindow(mMessageInput.getWindowToken(), 0);
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mEmojiSelector);
				} else {
					YoYo.with(Techniques.SlideOutDown)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mEmojiSelector.setVisibility(View.GONE);
									mIsAnimating = false;
									mImeManager.showSoftInput(mMessageInput, 0);
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mEmojiSelector);
				}
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		if (imageReturnedIntent != null) {
			Uri uri = imageReturnedIntent.getData();
			File imageFile = new File(getRealPathFromURI(this, uri));

			final Dialog mDialog = new Dialog(this);

			mDialog.title("图片处理")
					.cancelable(false)
					.contentView(R.layout.progress_circular)
					.titleColor(Utils.getThemeColor(this, R.attr.colorText))
					.backgroundColor(Utils.getThemeColor(this, android.R.attr.colorBackground))
					.show();

			mImageTask = new AsyncTask<File, Void, File>() {

				@Override
				protected File doInBackground(File... params) {
					return compressImage(params[0], (getSettings().getInt(Constants.PREF_KEY_IMG_SIZE, Constants.DEFAULT_IMAGE_UPLOAD_SIZE) - 1) * 1024);
				}

				@Override
				protected void onPostExecute(File tempFile) {
					if (tempFile == null || !tempFile.exists()) {
						mDialog.dismiss();
						showToast("图片处理失败");
					} else {
						mDialog.title("图片上传").show();

						App.getInstance().getApi().uploadImage(tempFile, new OnRespondCallback() {
							@Override
							public void onRespond(Response res) {
								if (res.isSuccess()) {
									int attachId = (int) res.getData();
									attachIds.add(attachId);
									mMessageInput.setText(mMessageInput.getText() + "[attachimg]" + attachId + "[/attachimg]");
								} else {
									showToast("图片上传失败");
								}

								mDialog.dismiss();
							}
						});
					}
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageFile);
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
	public void onSuccess(String response) {
		mSaveDraft = false;
		Intent returnIntent = new Intent();
		returnIntent.putExtra("posts-html", response);
		setResult(EDIT_SUCCESS, returnIntent);
		finish();
	}

	@Override
	public void onFailure(String reason) {
		showToast(reason);
	}

	public void addEmoji(View v) {
		String emojiText = (String) v.getTag();
		String emoji;

		if (emojiText.startsWith("coolmonkey")) {
			emoji = EmojiUtils.icons.get("images/smilies/coolmonkey/" + emojiText.substring(10) + ".gif");
		} else if (emojiText.startsWith("grapeman")) {
			emoji = EmojiUtils.icons.get("images/smilies/grapeman/" + emojiText.substring(8) + ".gif");
		} else {
			emoji = EmojiUtils.icons.get("images/smilies/default/" + emojiText + ".gif");
		}

		String temp = mMessageInput.getText().toString();
		int start = mMessageInput.getSelectionStart();
		int end = mMessageInput.getSelectionEnd();
		temp = StringUtils.left(temp, start) + emoji + StringUtils.right(temp, temp.length() - end);

		mMessageInput.setText(temp);
		mMessageInput.setSelection(start + emoji.length());
	}

	@Override
	protected void onDestroy() {
		if (mSaveDraft && (subjectInput.length() > 0 || mMessageInput.length() > 0)) {
			Draft draft = new Draft();
			draft.activityTitle = getTitle().toString();
			draft.subject = subjectInput.getText().toString();
			draft.message = mMessageInput.getText().toString();
			draft.fid = fid;
			draft.tid = tid;
			draft.pid = pid;
			draft.uid = uid;
			draft.no = no;

			String json = new Gson().toJson(draft);
			getSettings().edit().putString("draft", json).commit();
		}

		if (mImageTask != null && !mImageTask.isCancelled()) {
			mImageTask.cancel(true);
		}

		if (mParseTask != null && !mParseTask.isCancelled()) {
			mParseTask.cancel(true);
		}

		super.onDestroy();
	}


	/**
	 * If the length of image file is less than maxSize, quality will not be applied, and
	 * image file will be returned directly.
	 *
	 * @param imageFile
	 * @param maxSize
	 * @param quality
	 * @return File
	 */
	private File lowerQuality(final File imageFile, int maxSize, int quality) {
		long fileLength = imageFile.length();
		File tempFile = null;
		OutputStream os = null;

		if (fileLength > maxSize) {
			try {
				Bitmap bitmap = ImageLoader.getInstance().loadImageSync(Uri.fromFile(imageFile).toString());
				tempFile = File.createTempFile("uzlee-compress", ".jpg", this.getCacheDir());
				os = new FileOutputStream(tempFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
				os.close();
				fileLength = tempFile.length();

				Logger.i("length: %d, quality: %d", fileLength, quality);

				return fileLength > maxSize ? null : tempFile;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (tempFile != null) {
					tempFile.deleteOnExit();
				}

				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			return imageFile;
		}

		return null;
	}

	private File lowerQuality(final File imageFile, int maxSize) {
		int worst = 60;
		// Test the worst case.
		File tempFile = lowerQuality(imageFile, maxSize, worst);

		// Find a better one.
		if (tempFile != null) {
			int quality = 90;

			do {
				tempFile = lowerQuality(imageFile, maxSize, quality);
				quality -= 15;
			} while (tempFile == null && quality >= worst);
		}

		return tempFile;
	}

	private File scaleImage(final File imageFile) {
		File tempFile = null;
		OutputStream os = null;

		try {
			Bitmap bitmap;

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
			int imageHeight = options.outHeight;
			int imageWidth = options.outWidth;
			ImageSize imageSize;

			if (imageWidth < 720 || imageHeight < 720) {
				imageSize = new ImageSize(imageHeight, imageWidth);
			} else {
				if (imageWidth > imageHeight) {
					imageSize = new ImageSize((int) (720f * imageWidth / imageHeight), 720);
				} else {
					imageSize = new ImageSize(720, (int) (720f * imageHeight / imageWidth));
				}
			}

			bitmap = ImageLoader.getInstance().loadImageSync(Uri.fromFile(imageFile).toString(), imageSize);

			tempFile = File.createTempFile("uzlee-compress", ".jpg", this.getCacheDir());
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize.getWidth(), imageSize.getHeight(), true);
			os = new FileOutputStream(tempFile);
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
			os.close();

			Logger.i("length: %d, height: %d, width: %d", tempFile.length(), imageSize.getWidth(), imageSize.getHeight());

			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (tempFile != null) {
				tempFile.deleteOnExit();
			}

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param imageFile
	 * @param maxFileLength
	 * @return
	 */
	private File compressImage(File imageFile, int maxFileLength) {
		File outImage;

		if (imageFile.length() < maxFileLength) {
			outImage = imageFile;
		} else {
			outImage = scaleImage(imageFile);

			if (outImage.length() >= maxFileLength) {
				outImage = lowerQuality(outImage, maxFileLength);
			}
		}

		return outImage;
	}

	@Override
	public void onRespond(Response res) {
		if (res.isSuccess()) {
			mSaveDraft = false;
			Intent returnIntent = new Intent();
			returnIntent.putExtra("posts-html", res);
			setResult(EDIT_SUCCESS, returnIntent);
			finish();
		} else {
			showToast(res.getData().toString());
		}
	}

	public static class Draft {
		public String activityTitle;
		public String subject;
		public String message;
		public int tid;
		public int fid;
		public int pid;
		public int uid;
		public int no;
	}
}
