package com.ladjzero.uzlee;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ladjzero.hipda.Post;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class PostsAdapter extends ArrayAdapter<Post> {
	PostsActivity context;
	ArrayList<Post> posts;

	public PostsAdapter(Context context, ArrayList<Post> posts) {
		super(context, R.layout.post, posts);
		this.context = (PostsActivity) context;
		this.posts = posts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder()
				: (PostHolder) row.getTag();

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();

			row = inflater.inflate(R.layout.post, parent, false);

			holder.quoteLayout = row.findViewById(R.id.post_quote);

			holder.img = (ImageView) row.findViewById(R.id.user_mini_image);
			holder.quoteImg = (ImageView) holder.quoteLayout
					.findViewById(R.id.user_mini_image);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.quoteName = (TextView) holder.quoteLayout
					.findViewById(R.id.user_mini_name);
			holder.body = (LinearLayout) row
					.findViewById(R.id.post_body_layout);
			holder.quoteBody = (TextView) holder.quoteLayout
					.findViewById(R.id.post_quote_text);
			holder.postNo = (TextView) row.findViewById(R.id.post_no);
			holder.quotePostNo = (TextView) holder.quoteLayout
					.findViewById(R.id.post_no);

			row.setTag(holder);
		}

		final Post p = getItem(position);
		String img = p.getAuthor().getImage();
		if (img == null) {

		} else {
			ImageLoader.getInstance().displayImage(p.getAuthor().getImage(),
					holder.img);
		}
		holder.name.setText(p.getAuthor().getName());

		holder.body.removeAllViews();
		
		StringBuilder sb = new StringBuilder();
		for (String bodySnippet : p.getNiceBody()) {
			if (bodySnippet.startsWith("txt:")) {
				LinearLayout postTextLayout = (LinearLayout) context
						.getLayoutInflater().inflate(
								R.layout.post_body_text_segment, null);
				TextView tv = (TextView) postTextLayout
						.findViewById(R.id.post_body_text_segment);
				tv.setText(bodySnippet.substring(4));
				// tv.setText(Html.fromHtml(bodySnippet.substring(4), new
				// Html.ImageGetter() {
				//
				// @Override
				// public Drawable getDrawable(String source) {
				// Bitmap bitmap =
				// ImageLoader.getInstance().loadImageSync("assets://emoticons/"
				// + source + ".gif");
				// Drawable d = new BitmapDrawable(bitmap);
				//
				// d.setBounds(0,0,40,40);
				//
				// return d;
				// }
				// }, null));
				postTextLayout.removeAllViews();
				holder.body.addView(tv);
			} else if (bodySnippet.startsWith("emo:")) {
				
			} else {
				LinearLayout postImageLayout = (LinearLayout) context
						.getLayoutInflater().inflate(R.layout.post_body_image_segment,
								null);
				final PostImageView iv = (PostImageView) postImageLayout
						.findViewById(R.id.post_img);
				postImageLayout.removeAllViews();
				holder.body.addView(iv);

				ImageLoader.getInstance().displayImage(
						bodySnippet.substring(4), iv);
			}
		}

		holder.postNo.setText("#" + (position + 1));

		final int quoteId = p.getReplyTo();
		if (quoteId > 0) {
			Post quote = (Post) CollectionUtils.find(posts, new Predicate() {

				@Override
				public boolean evaluate(Object post) {
					return ((Post) post).getId() == quoteId;
				}

			});

			holder.quoteLayout.setVisibility(View.VISIBLE);
			holder.quoteName.setText(quote.getAuthor().getName());
			ImageLoader.getInstance().displayImage(
					quote.getAuthor().getImage(), holder.quoteImg);
			String bodySnippet0 = quote.getNiceBody()[0];
			holder.quoteBody
					.setText(bodySnippet0.indexOf("txt:") == 0 ? bodySnippet0
							.substring(4) : "[image]");
			holder.quotePostNo.setText("#" + (posts.indexOf(quote) + 1));
		} else {
			holder.quoteLayout.setVisibility(View.GONE);
		}

		return row;
	}

	static class PostHolder {
		ImageView img;
		ImageView quoteImg;
		TextView name;
		TextView quoteName;
		LinearLayout body;
		TextView quoteBody;
		TextView postNo;
		TextView quotePostNo;
		View quoteLayout;
	}
}
