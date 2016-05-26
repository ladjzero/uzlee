package com.ladjzero.uzlee.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.ladjzero.uzlee.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenzhuo on 16/5/22.
 */
public class HorizontalTagsView extends HorizontalScrollView {
	private Object[] mTags;
	private LinearLayout mContainer;
	private Context mContext;
	private TagStateChangeListener mListener;

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

	public void toggle(int index, boolean on) {
		((TagView) mContainer.getChildAt(index)).setActive(on);
	}

	public void toggle(boolean on) {
		for (int i = 0; i < mContainer.getChildCount(); ++i) {
			toggle(i, on);
		}
	}

	public Object[] getTags() {
		return mTags;
	}

	public Object[] getTags(boolean active) {
		List<Object> tags = new ArrayList<>();

		for (int i = 0; i < mTags.length; ++i) {
			if (((TagView) mContainer.getChildAt(i)).isActive() == active) {
				tags.add(mTags[i]);
			}
		}

		return tags.toArray();
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
					Object tag = v.getTag();
					int index = Arrays.asList(mTags).indexOf(tag);
					v.setActive(!v.isActive());

					if (mListener != null) {
						if (v.isActive()) {
							mListener.onTagActive(v.getTag(), index);
						} else {
							mListener.onTagInactive(v.getTag(), index);
						}
					}

				}
			});
		}
	}

	public void setTagActiveListener(TagStateChangeListener l) {
		mListener = l;
	}

	public interface TagStateChangeListener {
		void onTagActive(Object tag, int i);

		void onTagInactive(Object tag, int i);
	}
}

