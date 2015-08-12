package me.ladjzero.uzlee;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


public class PostImageView extends ImageView {

	private double widthHeight = -1;

	public PostImageView(final Context context) {
		super(context);
	}

	public PostImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PostImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setWidthHeight(double widthHeight) {
		this.widthHeight = widthHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable d = getDrawable();

		if (d != null) {
			// ceil not round - avoid thin vertical gaps along the left/right
			// edges
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height;

			if (widthHeight < 0) {
				height = (int) Math.ceil((float) width
						* (float) d.getIntrinsicHeight()
						/ (float) d.getIntrinsicWidth());
			} else {
				height = (int) Math.ceil((float) width / widthHeight);
			}

			setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
