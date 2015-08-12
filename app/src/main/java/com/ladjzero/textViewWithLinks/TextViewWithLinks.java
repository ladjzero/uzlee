package com.ladjzero.textViewWithLinks;

import android.content.Context;
import android.graphics.Color;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

public class TextViewWithLinks extends TextView {

	private BackgroundColorSpan color = new BackgroundColorSpan(Color.GREEN);

	static OnClickLinksListener l;

	private String link;

	private final String TAG = "TextViewWithLinks";

	public TextViewWithLinks(Context context) {
		super(context);

	}

	public TextViewWithLinks(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextViewWithLinks(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void linkify(OnClickLinksListener listener) {
		l = listener;

		Linkify.addLinks(this, Linkify.WEB_URLS);

		setMovementMethod(LinkMovementMethodExt.getInstance(new LinkMovementMethodExt.LinkMovementListener() {

			@Override
			public void onKeyUpOnLink(MessageSpan msg) {
				Logger.d(TAG, "onKeyUpOnLink");
				MessageSpan ms = msg;
				Object[] spans = (Object[]) ms.getObj();
				TextView view = ms.getView();

				for (Object span : spans) {
					if (span instanceof URLSpan) {
						int start = Selection.getSelectionStart(view.getText());
						int end = Selection.getSelectionEnd(view.getText());

						Spannable _span = (Spannable) view.getText();

						l.onLinkClick(((URLSpan) span).getURL());
					}

				}

				Spannable _span = (Spannable) view.getText();
				_span.removeSpan(color);
				view.setText(_span);

			}

			@Override
			public void onKeyDownOnTextView(MotionEvent event) {
				Logger.d(TAG, "onKeyDownOnTextView");
				l.onTextViewClick(event);
			}

			@Override
			public void onKeyDownOnLink(MessageSpan msg) {
				Logger.d(TAG, "onKeyDownOnLink");
				MessageSpan ms = msg;
				Object[] spans = (Object[]) ms.getObj();
				TextView view = ms.getView();

				for (Object span : spans) {
					if (span instanceof URLSpan) {
						int start = Selection.getSelectionStart(view.getText());
						int end = Selection.getSelectionEnd(view.getText());

						Spannable _span = (Spannable) view.getText();

						_span.setSpan(color, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						view.setText(_span);
					}
				}
			}

			@Override
			public void onScroll(TextView widget) {
				Logger.d(TAG, "onScroll");
				Spannable _span = (Spannable) widget.getText();
				_span.removeSpan(color);
				widget.setText(_span);

			}
		}, URLSpan.class));
	}

	public void setLinkColors(int linkColor, int backgroundLinkColor) {
		color = new BackgroundColorSpan(backgroundLinkColor);
		this.setLinkTextColor(linkColor);
	}

	public interface OnClickLinksListener {
		public void onLinkClick(String url);

		public void onTextViewClick(MotionEvent event);
	}
}
