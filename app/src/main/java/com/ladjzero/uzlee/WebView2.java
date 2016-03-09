package com.ladjzero.uzlee;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ladjzero.uzlee.stream.TeePipe;
import com.ladjzero.uzlee.utils.UilUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

/**
 * Created by chenzhuo on 16-1-31.
 */
public class WebView2 extends WebView {
	private static final String TAG = "WebView2";
	private final String JS_INTERFACE_NAME = "WebView2";
	boolean isEverScrolled;
	private Canvas mCanvas;

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
		super.onScrollChanged(l, t, oldl, oldt);
		isEverScrolled = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mCanvas = canvas;
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

	public static class ImageCacheClient extends WebViewClient {
		protected boolean shouldInterceptRequest(String uri) {
			return uri.startsWith("http") && (uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".png") || uri.endsWith(".gif"));
		}

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
				} else {
					try {
						URL imgUrl = new URL(url);
						InputStream imgIn = imgUrl.openStream();
						FileOutputStream fileOs = new FileOutputStream(cache);
						PipedInputStream pipeIn = new PipedInputStream();
						PipedOutputStream pipeOs = new PipedOutputStream(pipeIn);

						new AsyncTask() {
							@Override
							protected Object doInBackground(Object[] params) {
								try {
									TeePipe.stream((InputStream) params[0], (OutputStream) params[1], (OutputStream) params[2]);
								} catch (IOException e) {
									Logger.t(TAG).e(e, "stream fail");
									e.printStackTrace();
									cache.deleteOnExit();
									onReceivedError(view, ERROR_IO, "", url);
								}

								return null;
							}
						}.execute(imgIn, fileOs, pipeOs);

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
