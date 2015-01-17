package com.ladjzero.uzlee;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PostsAdapter extends ArrayAdapter<Post> implements OnClickListener {
	PostsActivity context;
	ArrayList<Post> posts;
	HashMap<Integer, View> viewCache = new HashMap<Integer, View>();

	public PostsAdapter(Context context, ArrayList<Post> posts) {
		super(context, R.layout.post, posts);
		this.context = (PostsActivity) context;
		this.posts = posts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = viewCache.get(position);
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


			final Post post = getItem(position);
			final User user = post.getAuthor();

			String img = post.getAuthor().getImage();
			if (img == null) {
				ImageLoader.getInstance().displayImage("", holder.img);
			} else {
				ImageLoader.getInstance().displayImage(user.getImage(), holder.img, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
						user.setImage(imageUri);
					}

					@Override
					public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
						user.setImage("");
					}
				});
			}
			holder.name.setText(post.getAuthor().getName());

			holder.img.setTag(user);
			holder.name.setTag(user);
			holder.img.setOnClickListener(this);
			holder.name.setOnClickListener(this);

			holder.body.removeAllViews();

			StringBuilder sb = new StringBuilder();
			for (String bodySnippet : post.getNiceBody()) {
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

					final String url = bodySnippet.substring(4);

					ImageLoader.getInstance().displayImage(
							url, iv);

					iv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent intent = new Intent(context, ImageActivity.class);
							intent.putExtra("url", url);
							context.startActivity(intent);
						}
					});
				}
			}

			holder.postNo.setText("#" + (position + 1));

			final int quoteId = post.getReplyTo();
			if (quoteId > 0) {
				Post quote = (Post) CollectionUtils.find(posts, new Predicate() {

					@Override
					public boolean evaluate(Object post) {
						return ((Post) post).getId() == quoteId;
					}

				});

				if (quote.getAuthor().getId() == Core.UGLEE_ID) {
					holder.quoteLayout.setBackgroundResource(R.color.uglee);
				}

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


			if (user.getId() == Core.UGLEE_ID) {
				row.setBackgroundResource(R.color.uglee);
				holder.quoteLayout.setBackgroundResource(R.color.ugleeQuote);
			}


			viewCache.put(position, row);
		}
		return row;
	}

	@Override
	public void onClick(View view) {
		User user = (User) view.getTag();
		Intent intent = new Intent(context, UserActivity.class);
		intent.putExtra("uid", user.getId());
		intent.putExtra("name", user.getName());
		context.startActivity(intent);
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
