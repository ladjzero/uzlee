package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ladjzero.uzlee.utils.UilUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by chenzhuo on 16-1-31.
 */
public class WebView2 extends WebView {
	private static final String TAG = "WebView2";
	private final String JS_INTERFACE_NAME = "WebView2";
	private boolean isEverScrolled;
	private boolean isScrolling;
	private ActionMode mActionMode;
	private long ms;

	public WebView2(Context context) {
		super(context);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public WebView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public WebView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	@TargetApi(21)
	public WebView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public WebView2(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
		super(context, attrs, defStyleAttr, privateBrowsing);
		this.addJavascriptInterface(this, JS_INTERFACE_NAME);
	}

	public boolean isScrolling() {
		return isScrolling;
	}

	public void setScrolling(boolean scrolling) {
		isScrolling = scrolling;
	}

	public boolean finishActionMode() {
		if (mActionMode != null) {
			mActionMode.finish();
			mActionMode = null;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ActionMode startActionMode(ActionMode.Callback callback) {
		mActionMode = super.startActionMode(callback);
		return mActionMode;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean toReturn = super.onTouchEvent(event);
		int action = event.getAction();

		if (action == MotionEvent.ACTION_MOVE) {
			if (isEverScrolled) {
				requestDisallowInterceptTouchEvent(true);
				toReturn = true;
			}
		} else {
			isEverScrolled = false;
		}

		return toReturn;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		long now = System.currentTimeMillis();

		setScrolling(true);

		postDelayed(new Runnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() - ms > 100) setScrolling(false);
			}
		}, 100);

		ms = now;

		super.onScrollChanged(l, t, oldl, oldt);
		isEverScrolled = true;
	}

	public Bitmap toBitmap() {
		this.setDrawingCacheEnabled(true);
		this.buildDrawingCache();

		return toBitmap(1);
	}

	public Bitmap toBitmap(float radio) {
		int width = (int) (this.getWidth() * radio);
		int height = (int) (this.getContentHeight() * this.getScale() * radio);

		if (width < 100) {
			return null;
		}

		Bitmap bitmap;

		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.scale(radio, radio);
			this.draw(canvas);
		} catch (OutOfMemoryError error) {
			bitmap = toBitmap(radio * 0.9f);
		}

		return bitmap;
	}

	public static abstract class ImageCacheClient extends WebViewClient {
		// Close streams once webview is detached.
		// Or webview would hung up next time.
		protected abstract boolean isCancelled();

		protected boolean shouldInterceptRequest(String uri) {
			return uri.startsWith("http") && (uri.contains(".jpg") || uri.contains(".jpeg") || uri.contains(".png") || uri.contains(".gif"));
		}

		public abstract boolean shouldDownloadImage();

		@Override
		public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
			Logger.i("shouldInterceptRequest");

			WebResourceResponse res = null;

			if (shouldInterceptRequest(url)) {
				UilUtils uil = UilUtils.getInstance();
				final File cache = uil.getFile(url);

				if (cache == null) {
					Logger.t(TAG).e("cache file is null.");
				} else if (cache.exists()) {
					try {
						res = new WebResourceResponse(getMimeType(url), "binary", new FileInputStream(cache));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				} else if (shouldDownloadImage()) {
					try {
						final PipedInputStream pipeIn = new PipedInputStream();
						final PipedOutputStream pipeOs = new PipedOutputStream(pipeIn);

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									if (isCancelled()) {
										pipeOs.close();
										return;
									}

									ImageLoader.getInstance().loadImageSync(url);
									InputStream imgIn = new FileInputStream(cache);

									byte[] buffer = new byte[1024];

									try {
										int len = imgIn.read(buffer);

										while (len != -1 && !isCancelled()) {
											pipeOs.write(buffer, 0, len);

											len = imgIn.read(buffer);
										}
									} catch (IOException e) {
										e.printStackTrace();
									} finally {
										try {
											imgIn.close();
										} catch (IOException e) {
											e.printStackTrace();
										}

										try {
											pipeOs.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									try {
										pipeOs.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}).start();

						res = new WebResourceResponse(getMimeType(url), "binary", pipeIn);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			return res;
		}

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			return shouldInterceptRequest(view, request.getUrl().toString());
		}

		protected String getMimeType(String uri) {
			if (uri.endsWith(".jpg") && uri.endsWith(".jpeg")) {
				return "image/jpeg";
			} else if (uri.endsWith(".png")) {
				return "image/png";
			} else if (uri.endsWith(".gif")) {
				return "image/gif";
			} else {
				return "image/*";
			}
		}
	}
}
