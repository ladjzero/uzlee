package com.ladjzero.uzlee.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.ladjzero.uzlee.R;

/**
 * Created by chenzhuo on 16/5/22.
 */
public class HorizontalTagsView extends HorizontalScrollView {
	private Object[] mTags;
	private LinearLayout mContainer;
	private Context mContext;
	private TagActiveListener mListener;

	public HorizontalTagsView(Context context) {
		super(context);
		init(context);
	}

	public HorizontalTagsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HorizontalTagsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HorizontalTagsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		mContainer = (LinearLayout) inflate(context, R.layout.horizontal_tags_view_lieanerlayout, null);
		addView(mContainer);
	}

	public void setTags(Object[] tags, Object init) {
		mTags = tags;

		if (mTags == null) return;

		for (int i = 0; i < mTags.length; ++i) {
			inflate(mContext, R.layout.horizontal_tags_view_tagview, mContainer);
			TagView tagView = ((TagView) mContainer.getChildAt(i));
			tagView.setText(mTags[i].toString());
			tagView.setTag(mTags[i]);

			if (mTags[i] == init) tagView.setActive(true);

			tagView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					TagView v = (TagView) view;

					if (v.isActive()) return;

					for (int j = 0; j < mContainer.getChildCount(); ++j) {
						((TagView) mContainer.getChildAt(j)).setActive(false);
					}

					v.setActive(true);

					if (mListener != null) mListener.onTagActive(v.getTag());
				}
			});
		}
	}

	public void setTagActiveListener(TagActiveListener l) {
		mListener = l;
	}

	public interface TagActiveListener {
		void onTagActive(Object tag);
	}
}

