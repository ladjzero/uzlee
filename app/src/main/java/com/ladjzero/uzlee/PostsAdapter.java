package com.ladjzero.uzlee;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
	HashMap<Integer, ArrayList<View>> niceBodyCache = new HashMap<Integer, ArrayList<View>>();

	public PostsAdapter(Context context, ArrayList<Post> posts) {
		super(context, R.layout.post, posts);
		this.context = (PostsActivity) context;
		this.posts = posts;
	}

	public void clearViewCache() {
		niceBodyCache.clear();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder() : (PostHolder) row.getTag();

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.post, parent, false);

			holder.quoteLayout = row.findViewById(R.id.post_quote);
			holder.img = (ImageView) row.findViewById(R.id.user_mini_image);
			holder.quoteImg = (ImageView) holder.quoteLayout.findViewById(R.id.user_mini_image);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.quoteName = (TextView) holder.quoteLayout.findViewById(R.id.user_mini_name);
			holder.body = (LinearLayout) row.findViewById(R.id.post_body_layout);
			holder.quoteBody = (TextView) holder.quoteLayout.findViewById(R.id.post_quote_text);
			holder.postNo = (TextView) row.findViewById(R.id.post_no);
			holder.quotePostNo = (TextView) holder.quoteLayout.findViewById(R.id.post_no);

			row.setTag(holder);
		}

		final Post post = getItem(position);
		final User author = post.getAuthor();
		int uid = author.getId();

		holder.name.setText(author.getName());
		holder.name.setTag(author);
		holder.name.setOnClickListener(this);
		holder.img.setTag(author);
		holder.img.setOnClickListener(this);

		if (author.getImage() == null) {
			ImageLoader.getInstance().displayImage("", holder.img);
		} else {
			ImageLoader.getInstance().displayImage(author.getImage(), holder.img, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
					author.setImage(imageUri);
				}

				@Override
				public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
					author.setImage("");
				}
			});
		}

		ArrayList<View> niceBody = niceBodyCache.get(position);

		if (niceBody == null) {
			niceBody = buildBody(post);
			niceBodyCache.put(position, niceBody);
		}

		holder.body.removeAllViews();

		if (Core.bans.contains(uid)) {
			LinearLayout postTextLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_fa_text_segment, null);
			TextView tv = (TextView) postTextLayout.findViewById(R.id.post_body_fa_text_segment);
			tv.setText(context.getString(R.string.blocked));
			postTextLayout.removeAllViews();
			holder.body.addView(tv);
		} else {
			for (View view : niceBody) {
				holder.body.addView(view);
			}
		}

		holder.postNo.setText("#" + (position + 1));

		final int quoteId = post.getReplyTo();

		Post quote = null;

		if (quoteId > 0) {
			quote = (Post) CollectionUtils.find(posts, new Predicate() {

				@Override
				public boolean evaluate(Object post) {
					return ((Post) post).getId() == quoteId;
				}

			});
		}

		if (quote != null) {
			if (quote.getAuthor().getId() == Core.UGLEE_ID) {
				holder.quoteLayout.setBackgroundResource(R.color.uglee);
			}

			holder.quoteLayout.setVisibility(View.VISIBLE);
			holder.quoteName.setText(quote.getAuthor().getName());
			ImageLoader.getInstance().displayImage(quote.getAuthor().getImage(), holder.quoteImg);
			String bodySnippet0 = quote.getNiceBody()[0];

			int quid = quote.getAuthor().getId();

			if (Core.bans.contains(quid)) {
				holder.quoteBody.setText(context.getString(R.string.blocked));
			} else {
				holder.quoteBody
						.setText(bodySnippet0.indexOf("txt:") == 0 ? bodySnippet0
								.substring(4) : "[image]");
			}
			holder.quotePostNo.setText("#" + (posts.indexOf(quote) + 1));
		} else {
			holder.quoteLayout.setVisibility(View.GONE);
		}


		if (author.getId() == Core.UGLEE_ID) {
			row.setBackgroundResource(R.color.uglee);
			holder.quoteLayout.setBackgroundResource(R.color.ugleeQuote);
		} else {
			row.setBackgroundResource(android.R.color.white);
			holder.quoteLayout.setBackgroundResource(R.color.backgroud);
		}

		viewCache.put(position, row);

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

	private ArrayList<View> buildBody(final Post post) {
		ArrayList<View> views = new ArrayList<View>();

		for (String bodySnippet : post.getNiceBody()) {
			if (bodySnippet.startsWith("txt:")) {
				LinearLayout postTextLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_text_segment, null);
				TextView textView = (TextView) postTextLayout.findViewById(R.id.post_body_text_segment);
				textView.setText(context.emojiUtils.getSmiledText(context, bodySnippet.substring(4)));
				postTextLayout.removeAllViews();

				views.add(textView);
			} else if (bodySnippet.startsWith("sig:")) {
				LinearLayout postTextLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_sig, null);
				TextView textView = (TextView) postTextLayout.findViewById(R.id.post_body_sig);
				textView.setText(bodySnippet.substring(4));
				postTextLayout.removeAllViews();

				views.add(textView);
			} else {
				LinearLayout postImageLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_image_segment, null);
				final PostImageView imageView = (PostImageView) postImageLayout.findViewById(R.id.post_img);
				postImageLayout.removeAllViews();

				views.add(imageView);

				final String url = bodySnippet.substring(4);

				ImageLoader.getInstance().displayImage(url, imageView);

				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(context, ImageActivity.class);
						intent.putExtra("url", url);
						intent.putExtra("tid", post.getTid());
						context.startActivity(intent);
					}
				});
			}
		}

		return views;
	}
}
