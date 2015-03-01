package com.ladjzero.uzlee;

import java.lang.reflect.Array;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

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
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PostsAdapter extends ArrayAdapter<Post> implements OnClickListener {
	PostsActivity context;
	ArrayList<Post> posts;
	HashMap<Integer, View> viewCache = new HashMap<Integer, View>();
	HashMap<Integer, ArrayList<View>> niceBodyCache = new HashMap<Integer, ArrayList<View>>();
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	TextDrawable.IShapeBuilder textBuilder = TextDrawable.builder()
			.beginConfig()
				.bold()
			.endConfig();
	Date now = new Date();
	String title;

	public PostsAdapter(Context context, ArrayList<Post> posts, String title) {
		super(context, R.layout.post, posts);
		this.context = (PostsActivity) context;
		this.posts = posts;
		this.title = title;
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
			holder.title = (TextView) row.findViewById(R.id.post_title);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.quoteName = (TextView) holder.quoteLayout.findViewById(R.id.user_mini_name);
			holder.body = (LinearLayout) row.findViewById(R.id.post_body_layout);
			holder.quoteBody = (TextView) holder.quoteLayout.findViewById(R.id.post_quote_text);
			holder.postNo = (TextView) row.findViewById(R.id.post_no);
			holder.postDate = (TextView) row.findViewById(R.id.post_date);
			holder.quotePostNo = (TextView) holder.quoteLayout.findViewById(R.id.post_no);

			row.setTag(holder);
		}

		final Post post = getItem(position);
		final User author = post.getAuthor();
		int uid = author.getId();

		holder.title.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
		if (position == 0) {
			holder.title.setText(title);
//			holder.title.getPaint().setFakeBoldText(true);
		}
		holder.name.setText(author.getName());
		holder.name.getPaint().setFakeBoldText(true);
		holder.name.setTag(author);
		holder.name.setOnClickListener(this);
		holder.img.setTag(author);
		holder.img.setOnClickListener(this);

		if (author.getImage() == null || author.getImage().length() == 0) {
			holder.img.setImageDrawable(textBuilder.buildRect(Utils.getFirstChar(author.getName()), Utils.getColor(context, R.color.snow_dark)));
		} else {
			ImageLoader.getInstance().displayImage(author.getImage(), holder.img, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
					author.setImage(imageUri);
				}

				@Override
				public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
					author.setImage("");
					((ImageView) view).setImageDrawable(textBuilder.buildRect(Utils.getFirstChar(author.getName()), Utils.getColor(context, R.color.snow_dark)));
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
				ViewParent vParent = view.getParent();
				if (vParent != null) ((ViewGroup)vParent).removeView(view);
				holder.body.addView(view);
			}
		}

		holder.postNo.setText("#" + post.getPostIndex());

		Post quote = post.getQuote();

		if (author.getId() == Core.UGLEE_ID) {
			row.setBackgroundResource(R.color.uglee);
			holder.quoteLayout.setBackgroundResource(R.color.ugleeQuote);
		} else {
			row.setBackgroundResource(android.R.color.white);
			holder.quoteLayout.setBackgroundResource(R.color.snow_light);
		}

		if (quote != null) {
			final int quoteId = quote.getId();

			Post betterQuote = (Post) CollectionUtils.find(posts, new Predicate() {

				@Override
				public boolean evaluate(Object post) {
					return ((Post) post).getId() == quoteId;
				}

			});

			if (betterQuote != null) {
				User quoteUser = betterQuote.getAuthor();

				if (quoteUser.getId() == Core.UGLEE_ID) {
					holder.quoteLayout.setBackgroundResource(R.color.uglee);
				}

				holder.quoteLayout.setVisibility(View.VISIBLE);
				holder.quoteName.setText(quoteUser.getName());
				ImageLoader.getInstance().displayImage(quoteUser.getImage(), holder.quoteImg);
				String bodySnippet0 = betterQuote.getNiceBody()[0];

				int quid = quoteUser.getId();

				if (Core.bans.contains(quid)) {
					holder.quoteBody.setText(context.getString(R.string.blocked));
				} else {
					holder.quoteBody
							.setText(bodySnippet0.indexOf("txt:") == 0 ? bodySnippet0
									.substring(4) : "[image]");
				}
				holder.quotePostNo.setText("#" + betterQuote.getPostIndex());
			} else {
				holder.quoteLayout.setVisibility(View.VISIBLE);
				holder.quoteName.setText(quote.getAuthor().getName());
				holder.quoteBody.setText(quote.getBody());

				if (quote.getPostIndex() > 0) {
					holder.quotePostNo.setText("#" + quote.getPostIndex());
				} else {
					holder.quotePostNo.setText("");
				}
			}
		} else {
			holder.quoteLayout.setVisibility(View.GONE);
		}

		holder.postDate.setText(prettyTime(post.getTimeStr()));
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
		TextView title;
		TextView name;
		TextView quoteName;
		LinearLayout body;
		TextView quoteBody;
		TextView postNo;
		TextView postDate;
		TextView quotePostNo;
		View quoteLayout;
	}

	private String prettyTime(String timeStr) {
		try {
			Date thatDate = dateFormat.parse(timeStr);

			if (DateUtils.isSameDay(thatDate, now)) {
				return DateFormatUtils.format(thatDate, "HH:mm");
			} else if (now.getTime() - thatDate.getTime() < 24 * 3600 *1000) {
				return DateFormatUtils.format(thatDate, "M月d日 HH:mm");
			} else if (now.getYear() == thatDate.getYear()) {
				return DateFormatUtils.format(thatDate, "M月d日");
			} else {
				return DateFormatUtils.format(thatDate, "yyyy年M月d日");
			}
		} catch (ParseException e) {
			return timeStr;
		}
	}

	private ArrayList<View> buildBody(final Post post) {
		ArrayList<View> views = new ArrayList<View>();

		for (String bodySnippet : post.getNiceBody()) {
			if (bodySnippet.startsWith("txt:")) {
				String content = bodySnippet.substring(4);
				TextView textView;

				if (content.equals("blocked!")) {
					LinearLayout postTextLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_fa_text_segment, null);
					textView = (TextView) postTextLayout.findViewById(R.id.post_body_fa_text_segment);
					textView.setText(context.getString(R.string.blocked));
					postTextLayout.removeAllViews();
				} else {
					LinearLayout postTextLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_text_segment, null);
					textView = (TextView) postTextLayout.findViewById(R.id.post_body_text_segment);
					textView.setText(context.emojiUtils.getSmiledText(context, bodySnippet.substring(4)));
					postTextLayout.removeAllViews();
				}

				views.add(textView);
			} else if (bodySnippet.startsWith("sig:") && bodySnippet.length() > 4) {
				LinearLayout postTextLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.post_body_sig, null);
				TextView textView = (TextView) postTextLayout.findViewById(R.id.post_body_sig);
				textView.setText(bodySnippet.substring(4));
				postTextLayout.removeAllViews();

				views.add(textView);
			} else if (bodySnippet.startsWith("img:")) {
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

				imageView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						return false;
					}
				});
			}
		}

		return views;
	}
}
