package com.ladjzero.uzlee;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;
import com.rey.material.widget.ProgressView;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pt.fjss.TextViewWithLinks.TextViewWithLinks;

public class PostsAdapter extends ArrayAdapter<Post> implements OnClickListener {
	private BaseActivity context;
	private Posts mPosts;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private Date mNow;
	private int mWhite;
	private int mHoloBlue;
	private int mTransparent;
	private int mDefaultTextColor;
	private int mTextColor;
	private int mLinkColor;
	private int layout = R.layout.post;
	private HashMap<Post, Integer> mItemHeights = new HashMap<Post, Integer>();
	private HashMap<Post, ArrayList<View>> mBodyCache = new HashMap<Post, ArrayList<View>>();
	private ListView mListView;
	private int[] layoutXY = new int[2];
	private TYPE type;
	private DisplayImageOptions mDisplayOption;
	private ArrayList<String> mUrls;
	private boolean mShowSig;

	public static enum TYPE {
		POST,
		CHAT
	}

	public PostsAdapter(Context context, Posts posts, TYPE type) {
		super(context, R.layout.post, posts);
		this.context = (BaseActivity) context;
		mPosts = posts;
		mNow = new Date();
		mShowSig = this.context.setting.getBoolean("show_sig", false);
		this.type = type;

		Resources res = context.getResources();
		mLinkColor = mHoloBlue = res.getColor(android.R.color.holo_blue_dark);
		mTransparent = res.getColor(android.R.color.transparent);
		mWhite = res.getColor(android.R.color.white);
		mTextColor = mDefaultTextColor = res.getColor(R.color.smallFont);

		mDisplayOption = getImageLoader();
		mUrls = new ArrayList<>();
	}

	public PostsAdapter(Context context, Posts posts) {
		this(context, posts, TYPE.POST);
	}

	private DisplayImageOptions getImageLoader() {
		int imageCount = 0;

		for (Post post : mPosts.getLastMerged()) {
			for (Map.Entry<Post.BodyType, String> entry : post.getNiceBody()) {
				if (entry.getKey() == Post.BodyType.IMG) imageCount++;
			}
		}

		return imageCount > 3 ? BaseActivity.LowQualityDisplay : BaseActivity.BestQualityDisplay;
	}

	public void clearViewCache() {
		for (ArrayList<View> views : mBodyCache.values()) {
			for (View view : views) {
				Object tag = view.getTag();
				if (tag instanceof PostImageView) {
					PostImageView imageView = (PostImageView) tag;
					//http://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
					imageView.setImageResource(R.drawable.none);
					imageView.setTag(R.id.img_has_bitmap, false);
				}
			}
		}
	}

	protected void setLayout(int layout, int textColor, int linkColor) {
		this.layout = layout;
		mTextColor = textColor;
		mLinkColor = linkColor;
	}

	@Override
	public int getCount() {
		return mPosts.getLastMerged().size();
	}

	@Override
	public Post getItem(int position) {
		return mPosts.getLastMerged().get(position);
	}

	@Override
	public int getPosition(Post post) {
		return mPosts.getLastMerged().indexOf(post);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mDisplayOption = getImageLoader();
		clearViewCache();

		mUrls.clear();

		for (Post post : mPosts) {
			for (Map.Entry<Post.BodyType, String> entry : post.getNiceBody()) {
				if (entry.getKey() == Post.BodyType.IMG) mUrls.add(entry.getValue());
			}
		}
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		final Post post = getItem(position);
		Post quote = post.getQuote();

		if (mListView == null) {
			mListView = (ListView) parent;
			mListView.getLocationOnScreen(layoutXY);
		}

		View row = convertView;
		final PostHolder holder = row == null ? new PostHolder() : (PostHolder) row.getTag();

		if (row == null) {
			row = context.getLayoutInflater().inflate(layout, parent, false);

			holder.img = (ImageView) row.findViewById(R.id.user_image);
			holder.imageMask = (TextView) row.findViewById(R.id.user_image_mask);
			holder.name = (TextView) row.findViewById(R.id.user_mini_name);
			holder.body = (LinearLayout) row.findViewById(R.id.post_body_layout);
			holder.sig = (TextView) row.findViewById(R.id.post_body_sig);
			holder.postNo = (TextView) row.findViewById(R.id.post_no);
			holder.postDate = (TextView) row.findViewById(R.id.post_date);

			if (type == TYPE.POST) {
				holder.quoteLayout = row.findViewById(R.id.post_quote);
				holder.quoteImg = (ImageView) holder.quoteLayout.findViewById(R.id.user_image);
				holder.quoteName = (TextView) holder.quoteLayout.findViewById(R.id.user_mini_name);
				holder.quoteBody = (TextView) holder.quoteLayout.findViewById(R.id.post_quote_text);
				holder.quotePostNo = (TextView) holder.quoteLayout.findViewById(R.id.post_no);
			}
		}

		holder.position = position;
		row.setTag(holder);

		final User author = post.getAuthor();
		final String userName = author.getName();
		final String imageUrl = author.getImage();
		String sig = post.getSig();
		final int uid = author.getId();
		int index = post.getPostIndex();

		holder.body.removeAllViews();
		holder.name.setText(userName);
		holder.name.setTag(author);
		holder.name.setOnClickListener(this);
		holder.img.setTag(author);
		holder.img.setOnClickListener(this);
		holder.imageMask.setText(Utils.getFirstChar(userName));
		holder.postDate.setText(prettyTime(post.getTimeStr()));
		holder.postNo.setText(index == 1 ? "楼主" : index + "楼");
		row.setBackgroundResource(uid == Core.UGLEE_ID ? R.color.uglee : android.R.color.transparent);

		if (mShowSig && sig != null && sig.length() > 0) {
			holder.sig.setVisibility(View.VISIBLE);
			holder.sig.setText(sig);
		} else {
			holder.sig.setVisibility(View.GONE);
		}

		ImageLoader.getInstance().displayImage(imageUrl, holder.img, BaseActivity.LowQualityDisplay, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
				author.setImage(imageUri);
			}

			@Override
			public void onLoadingFailed(String imageUri, android.view.View view, FailReason failReason) {
				author.setImage(null);
				((ImageView) view).setImageResource(android.R.color.transparent);
				holder.imageMask.setText(Utils.getFirstChar(userName));
			}
		});

		ArrayList<View> views = buildBody(post);

		if (Core.bans.contains(uid)) {
			TextView view = (TextView) context.getLayoutInflater().inflate(R.layout.post_body_fa_text_segment, null);
			view.setText(context.getString(R.string.blocked));
			holder.body.addView(view);
		} else {
			for (View view : views) {
				ViewParent vParent = view.getParent();
				if (vParent != null) ((ViewGroup) vParent).removeView(view);
				holder.body.addView(view);

				Object childView = null;

				try {
					childView = view.getTag();
				} catch (Exception e) {
					Logger.e(e, "fail to get child view");
				}

				if (childView != null && childView instanceof PostImageView) {
					PostImageView imageView = (PostImageView) childView;
					final String url = (String) imageView.getTag(R.id.img_url);
					boolean hasBitmap = (Boolean) imageView.getTag(R.id.img_has_bitmap);

					Double widthHeight = (Double) imageView.getTag(R.id.img_width_height);

					if (widthHeight != null) imageView.setWidthHeight(widthHeight);

					if (!hasBitmap) {
						ProgressView bar = (ProgressView) imageView.getTag(R.id.img_progress);
						bar.start();

						ImageLoader.getInstance().displayImage(url, imageView, mDisplayOption, new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri, View view, android.graphics.Bitmap loadedImage) {
								if (view != null) {
									ProgressView bar = (ProgressView) view.getTag(R.id.img_progress);

									bar.stop();
									view.setTag(R.id.img_has_bitmap, true);
									view.setTag(R.id.img_width_height, 1.0 * loadedImage.getWidth() / loadedImage.getHeight());
								}
							}

							@Override
							public void onLoadingFailed(String url, View view, FailReason reason) {
								if (view != null) {

									ProgressView bar = (ProgressView) view.getTag(R.id.img_progress);

									bar.stop();
								}
							}
						}, new ImageLoadingProgressListener() {
							@Override
							public void onProgressUpdate(String s, View view, int current, int total) {
								if (view != null) {
									ProgressView bar = (ProgressView) view.getTag(R.id.img_progress);

									bar.setProgress(current * 1.0f / total);
								}
							}
						});
					}
				}
			}
		}

		if (type == TYPE.POST) {
			if (quote != null) {
				holder.quoteLayout.setBackgroundResource(uid == Core.UGLEE_ID ? R.color.ugleeQuote : R.color.snow_light);
				holder.quoteLayout.setVisibility(View.VISIBLE);

				final int quoteId = quote.getId();

				Post betterQuote = (Post) CollectionUtils.find(mPosts, new Predicate() {
					@Override
					public boolean evaluate(Object post) {
						return ((Post) post).getId() == quoteId;
					}
				});

				if (betterQuote != null && betterQuote.getNiceBody().size() > 0) {
					User _user = betterQuote.getAuthor();
					int _index = betterQuote.getPostIndex();
					Map.Entry<Post.BodyType, String> _body = betterQuote.getNiceBody().get(0);

					holder.quoteName.setText(_user.getName());
					holder.quotePostNo.setText(_index == 1 ? "楼主" : _index + "楼");

					if (Core.bans.contains(_user.getId())) {
						holder.quoteBody.setText(context.getString(R.string.blocked));
					} else {
						holder.quoteBody.setText(
								_body.getKey() == Post.BodyType.TXT ? _body.getValue() : "[图像]");
					}
				} else {
					holder.quoteName.setText(quote.getAuthor().getName());
					holder.quoteBody.setText(quote.getBody());

					if (quote.getPostIndex() > 0) {
						holder.quotePostNo.setText(quote.getPostIndex() + "楼");
					} else {
						holder.quotePostNo.setText("");
					}
				}
			} else {
				if (holder.quoteLayout != null) holder.quoteLayout.setVisibility(View.GONE);
			}
		}

		Integer height = mItemHeights.get(post);
		if (height != null && height > 0) {
			ViewGroup.LayoutParams params = row.getLayoutParams();
			params.height = height;
			row.setLayoutParams(params);
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

	protected String prettyTime(String timeStr) {
		try {
			Date thatDate = dateFormat.parse(timeStr);

			if (DateUtils.isSameDay(thatDate, mNow)) {
				return DateFormatUtils.format(thatDate, "HH:mm");
			} else if (DateUtils.isSameDay(DateUtils.addDays(thatDate, 1), mNow)) {
				return DateFormatUtils.format(thatDate, "昨天 HH:mm");
			} else if (mNow.getYear() == thatDate.getYear()) {
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

		for (Map.Entry<Post.BodyType, String> bodySnippet : post.getNiceBody()) {
			switch (bodySnippet.getKey()) {
				case TXT:
					String body = bodySnippet.getValue();

					if (body.equals("blocked!")) {
						TextView textView = (TextView) context.getLayoutInflater()
								.inflate(R.layout.post_body_fa_text_segment, null);
						textView.setText(context.getString(R.string.blocked));
						textView.setTag(Integer.valueOf(1));
						views.add(textView);
					} else {
						final TextViewWithLinks textView = (TextViewWithLinks) context
								.getLayoutInflater().inflate(R.layout.post_body_text_segment, null);
						textView.setText(context.emojiUtils.getSmiledText(context, body));
						textView.linkify(new TextViewWithLinks.OnClickLinksListener() {
							@Override
							public void onLinkClick(String url) {
								if (url.startsWith("http://www.hi-pda.com/forum/")) {
									Uri uri = Uri.parse(url);
									String tid = uri.getQueryParameter("tid");
									String page = uri.getQueryParameter("page");
									if (page == null || page.length() == 0) page = "1";

									if (tid != null && tid.length() > 0) {
										Intent intent = new Intent(context, PostsActivity.class);
										intent.putExtra("tid", Integer.valueOf(tid));
										intent.putExtra("page", Integer.valueOf(page));
										context.startActivity(intent);
									} else {
										Intent intent = new Intent(Intent.ACTION_VIEW);
										intent.setData(Uri.parse(url));
										context.startActivity(intent);
									}
								} else {
									Intent intent = new Intent(Intent.ACTION_VIEW);
									intent.setData(Uri.parse(url));
									context.startActivity(intent);
								}
							}

							@Override
							public void onTextViewClick(MotionEvent event) {
								int position = mListView
										.pointToPosition(
												(int) event.getRawX(),
												(int) event.getRawY() - layoutXY[1]
										);

								mListView.performItemClick(mListView.getChildAt(position), position, 0);
							}
						});

						textView.setLinkColors(mLinkColor, mTransparent);
						textView.setTextColor(mTextColor);

						textView.setTag(Integer.valueOf(1));
						views.add(textView);
					}

					break;
				case IMG:
					View imageContainer = context.getLayoutInflater().inflate(R.layout.post_body_image_segment, null);
					PostImageView imageView = (PostImageView) imageContainer.findViewById(R.id.post_img);
					imageContainer.setTag(imageView);
					views.add(imageContainer);

					final String url = bodySnippet.getValue();

					imageView.setTag(R.id.img_url, url);
					imageView.setTag(R.id.img_has_bitmap, false);
					imageView.setTag(R.id.img_progress, imageContainer.findViewById(R.id.progress_bar));

					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent intent = new Intent(context, ImageActivity.class);
							intent.putExtra("url", url);
							intent.putExtra("tid", post.getTid());
							intent.putStringArrayListExtra("urls", mUrls);
							context.startActivity(intent);
						}
					});

					imageView.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View view) {
							return false;
						}
					});

					break;
				case ATT:
					View view = context.getLayoutInflater().inflate(R.layout.attachment, null);
					final String[] url_name_size = bodySnippet.getValue().split(Core.DIVIDER);
					String name = url_name_size[1];
					int _index = name.lastIndexOf(".");
					String ext = _index < 0 ? "" : name.substring(_index + 1);

					TextView extView = (TextView) view.findViewById(R.id.file_type);
					extView.setText(ext);
					extView.setBackgroundResource(getBackgroundResByExt(ext));

					TextView filenameView = (TextView) view.findViewById(R.id.file_name);

					String _text = url_name_size.length == 2 ? name : name + "  " + url_name_size[2];

					filenameView.setText(_text);

					view.setTag(Integer.valueOf(0));

					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(url_name_size[0]));
							context.startActivity(intent);
						}
					});

					views.add(view);

					break;
				case QUOTE:
					View quoteContainer = context.getLayoutInflater().inflate(R.layout.quote, null);
					TextView textView = (TextView) quoteContainer.findViewById(R.id.post_quote_text);
					textView.setText(bodySnippet.getValue());
					quoteContainer.setTag(Integer.valueOf(1));
					views.add(quoteContainer);

					break;
			}
		}

		return views;
	}

	private int getBackgroundResByExt(String ext) {
		String resName = "filetype";
		int randomIndex = 0;

		for (int i = 0; i < ext.length(); ++i) {
			randomIndex += Character.getNumericValue(ext.charAt(i));
		}

		randomIndex = randomIndex % 6;

		resName += randomIndex;

		return EmojiUtils.getResId(context, resName, Drawable.class);
	}

	interface OnBuildBody {
		void buildBody(ArrayList<View> view);
	}

	static class PostHolder {
		int position = -1;
		ImageView img;
		TextView imageMask;
		ImageView quoteImg;
		TextView name;
		TextView quoteName;
		LinearLayout body;
		TextView sig;
		TextView quoteBody;
		TextView postNo;
		TextView postDate;
		TextView quotePostNo;
		View quoteLayout;
	}
}
