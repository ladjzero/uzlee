package com.ladjzero.hipda;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.ladjzero.uzlee.BaseActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.orhanobut.logger.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.AbstractMap;
import java.util.regex.*;

import de.greenrobot.event.EventBus;

public class Core {
	public final static int MAX_UPLOAD_LENGTH = 299 * 1024;
	public final static int UGLEE_ID = 1261;
	public static final Set<Integer> bans = new HashSet<Integer>();
	public static final HashMap<String, String> icons = new HashMap<String, String>();
	public static final Set<String> iconKeys;
	public static final Collection<String> iconValues;
	public static final String BASE_URL = "http://www.hi-pda.com/forum";
	public static final String DIVIDER = "123~#~321";

	static {
		icons.put("images/smilies/default/smile.gif", ":)");
		icons.put("images/smilies/default/sweat.gif", ":sweat:");
		icons.put("images/smilies/default/huffy.gif", ":huffy:");
		icons.put("images/smilies/default/cry.gif", ":cry:");
		icons.put("images/smilies/default/titter.gif", ":titter:");
		icons.put("images/smilies/default/handshake.gif", ":handshake:");
		icons.put("images/smilies/default/victory.gif", ":victory:");
		icons.put("images/smilies/default/curse.gif", ":curse:");
		icons.put("images/smilies/default/dizzy.gif", ":dizzy:");
		icons.put("images/smilies/default/shutup.gif", ":shutup:");
		icons.put("images/smilies/default/funk.gif", ":funk:");
		icons.put("images/smilies/default/loveliness.gif", ":loveliness:");
		icons.put("images/smilies/default/sad.gif", ":(");
		icons.put("images/smilies/default/biggrin.gif", ":D");
		icons.put("images/smilies/default/cool.gif", ":cool:");
		icons.put("images/smilies/default/mad.gif", ":mad:");
		icons.put("images/smilies/default/shocked.gif", ":o");
		icons.put("images/smilies/default/tongue.gif", ":P");
		icons.put("images/smilies/default/lol.gif", ":lol:");
		icons.put("images/smilies/default/shy.gif", ":shy:");
		icons.put("images/smilies/default/sleepy.gif", ":sleepy:");

		icons.put("images/smilies/coolmonkey/01.gif", "{:2_41:}");
		icons.put("images/smilies/coolmonkey/02.gif", "{:2_42:}");
		icons.put("images/smilies/coolmonkey/03.gif", "{:2_43:}");
		icons.put("images/smilies/coolmonkey/04.gif", "{:2_44:}");
		icons.put("images/smilies/coolmonkey/05.gif", "{:2_45:}");
		icons.put("images/smilies/coolmonkey/06.gif", "{:2_46:}");
		icons.put("images/smilies/coolmonkey/07.gif", "{:2_47:}");
		icons.put("images/smilies/coolmonkey/08.gif", "{:2_48:}");
		icons.put("images/smilies/coolmonkey/09.gif", "{:2_49:}");
		icons.put("images/smilies/coolmonkey/10.gif", "{:2_50:}");
		icons.put("images/smilies/coolmonkey/11.gif", "{:2_51:}");
		icons.put("images/smilies/coolmonkey/12.gif", "{:2_52:}");
		icons.put("images/smilies/coolmonkey/13.gif", "{:2_53:}");
		icons.put("images/smilies/coolmonkey/14.gif", "{:2_54:}");
		icons.put("images/smilies/coolmonkey/15.gif", "{:2_55:}");
		icons.put("images/smilies/coolmonkey/16.gif", "{:2_56:}");

		icons.put("images/smilies/grapeman/01.gif", "{:3_57:}");
		icons.put("images/smilies/grapeman/02.gif", "{:3_58:}");
		icons.put("images/smilies/grapeman/03.gif", "{:3_59:}");
		icons.put("images/smilies/grapeman/04.gif", "{:3_60:}");
		icons.put("images/smilies/grapeman/05.gif", "{:3_61:}");
		icons.put("images/smilies/grapeman/06.gif", "{:3_62:}");
		icons.put("images/smilies/grapeman/07.gif", "{:3_63:}");
		icons.put("images/smilies/grapeman/08.gif", "{:3_64:}");
		icons.put("images/smilies/grapeman/09.gif", "{:3_65:}");
		icons.put("images/smilies/grapeman/10.gif", "{:3_66:}");
		icons.put("images/smilies/grapeman/11.gif", "{:3_67:}");
		icons.put("images/smilies/grapeman/12.gif", "{:3_68:}");
		icons.put("images/smilies/grapeman/13.gif", "{:3_69:}");
		icons.put("images/smilies/grapeman/14.gif", "{:3_70:}");
		icons.put("images/smilies/grapeman/15.gif", "{:3_71:}");
		icons.put("images/smilies/grapeman/16.gif", "{:3_72:}");
		icons.put("images/smilies/grapeman/17.gif", "{:3_73:}");
		icons.put("images/smilies/grapeman/18.gif", "{:3_74:}");
		icons.put("images/smilies/grapeman/19.gif", "{:3_75:}");
		icons.put("images/smilies/grapeman/20.gif", "{:3_76:}");
		icons.put("images/smilies/grapeman/21.gif", "{:3_77:}");
		icons.put("images/smilies/grapeman/22.gif", "{:3_78:}");
		icons.put("images/smilies/grapeman/23.gif", "{:3_79:}");
		icons.put("images/smilies/grapeman/24.gif", "{:3_80:}");

		iconKeys = icons.keySet();
		iconValues = icons.values();
	}

	private static final String TAG = "Core";
	private static AsyncHttpClient httpClient = new AsyncHttpClient();
	private static String formhash;
	private static String hash;
	private static Context context;
	private static User user;
	private static PersistentCookieStore cookieStore;
	private static SharedPreferences pref;
	private static String code = "GBK";
	private static final String STATS = "论坛统计";
	private static boolean DEBUG;

	private static final Pattern colorReg = Pattern.compile("#(\\d|[A-F])+");

	public static void setup(Context context, boolean debug) {
		if (Core.context == null) {
			Core.context = context;
			cookieStore = new PersistentCookieStore(context);
			httpClient.setCookieStore(cookieStore);
			pref = PreferenceManager.getDefaultSharedPreferences(context);
			bans.addAll(getBanList());

			DEBUG = debug;
		}
	}

	public static User getUser() {
		return user;
	}

	public static void requestUpdate() {
		httpClient.get(
				context,
				"https://raw.githubusercontent.com/ladjzero/uzlee/master/release/update.json",
				new TextHttpResponseHandler() {
					@Override
					public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
					}

					@Override
					public void onSuccess(int i, Header[] headers, String s) {
						UpdateInfo updateInfo = JSON.parseObject(s, UpdateInfo.class);
						EventBus.getDefault().post(updateInfo);
					}
				});
	}

	public static void uploadImage(final File imageFile, final OnUploadListener onUploadListener) {
		httpClient.get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new TextHttpResponseHandler(code) {
			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

			}

			@Override
			public void onSuccess(int i, Header[] headers, String s) {
				getDoc(s);

				try {
					RequestParams params = new RequestParams();
					params.setContentEncoding(code);
					params.put("uid", user.getId());
					params.put("hash", hash);
					params.put("Filedata", imageFile);
					params.put("filename", imageFile.getName());

					httpClient.post("http://www.hi-pda.com/forum/misc.php?action=swfupload&operation=upload&simple=1&type=image", params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int i, Header[] headers, byte[] bytes) {
							onUploadListener.onUpload(new String(bytes));
						}

						@Override
						public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

						}
					});
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void getHtml(String url, final OnRequestListener onRequestListener) {
		httpClient.get(url, new RequestParams(), new TextHttpResponseHandler(code) {

			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
				onRequestListener.onError(throwable == null ? "error" : throwable.toString());
			}

			@Override
			public void onSuccess(int i, Header[] headers, String s) {
				onRequestListener.onSuccess(s);
			}
		});
	}

	public static Document getDoc(String html) {
		long time = System.currentTimeMillis();

		Document doc = Jsoup.parse(html);

		try {
			int msgCount = 0;

			for (Element a : doc.select("#prompt_pm, #prompt_announcepm, #prompt_systempm, #prompt_friend, #prompt_threads")) {
				String msgText = a.text();

				int _index = msgText.indexOf("("),
						index_ = msgText.indexOf(")");

				if (index_ > _index) {
					msgCount += Integer.valueOf(msgText.substring(_index + 1, index_));
				}
			}

			EventBus.getDefault().post(new MessageEvent(msgCount));

			Elements eUser = doc.select("#umenu > cite > a");
			String uidHref = eUser.attr("href");
			Uri uri = Uri.parse(uidHref);
			String uid = uri.getQueryParameter("uid");

			if (uid != null && uid.length() > 0) {
				int id = Integer.valueOf(uid);
				String name = eUser.text().trim();

				user = new User().setId(id).setName(name);

				Logger.i("EventBus.StatusChangeEvent %d %s", user.getId(), user.getName());
			} else {
				user = null;
				Logger.i("EventBus.StatusChangeEvent %d %s", 0, "null");
			}

			EventBus.getDefault().post(new UserEvent(user));
		} catch (Error e) {
			Logger.e(TAG, e.toString());
		}

		Elements formHashInput = doc.select("input[name=formhash]");

		if (formHashInput.size() > 0) {
			formhash = formHashInput.val();
		}

		Elements hashInput = doc.select("input[name=hash]");

		if (hashInput.size() > 0) {
			hash = hashInput.val();
		}

		String stats = doc.select("#footlink a[href=stats.php]").text();

		if (pref.getBoolean("detect_code", false)) {
			if (!stats.equals(STATS)) code = code.equals("GBK") ? "UTF-8" : "GBK";
		} else {
			code = "GBK";
		}

		Logger.i("%d ms", System.currentTimeMillis() - time);
		return doc;
	}

	public static void login(String username, String password, int questionId, String answer, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.setContentEncoding(code);
		params.put("sid", "fa6m4o");
		params.put("formhash", "ad793a3f");
		params.put("loginfield", "username");
		params.put("username", username);
		params.put("password", password);
		params.put("questionid", questionId);
		params.put("answer", answer);
		params.put("loginsubmit", "true");

		httpClient
				.post("http://www.hi-pda.com/forum/logging.php?action=login&loginsubmit=yes",
						params, new AsyncHttpResponseHandler() {

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
								onRequestListener.onError(error.toString());
							}

							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								Logger.i("login succeed");

								String html;
								try {
									html = new String(responseBody, code);
									if (html.contains("欢迎您回来")) {
										getDoc(html);
										onRequestListener.onSuccess("");
									} else if (html.contains("密码错误次数过多，请 15 分钟后重新登录")) {
										onRequestListener.onError("密码错误次数过多，请 15 分钟后重新登录");
									} else {
										onRequestListener.onError("登录错误");
									}
								} catch (UnsupportedEncodingException e) {
									onRequestListener.onError(e.toString());
								}
							}
						});

	}

	public static void logout(final OnRequestListener onRequestListener) {
		getHtml("http://www.hi-pda.com/forum/logging.php?action=logout&formhash=" + formhash, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onRequestListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				Logger.i("logout succeed");

				user = null;
				EventBus.getDefault().post(new UserEvent(user));
				EventBus.getDefault().post(new MessageEvent(0));

				onRequestListener.onSuccess(html);

				if (cookieStore != null && cookieStore.getCookies() != null) {
					for (Cookie cookie : cookieStore.getCookies()) {
						cookieStore.deleteCookie(cookie);
					}
				}
			}
		});
	}

	public static Posts parsePosts(String html) {
		return parsePosts(html, null);
	}

	public static Posts parsePosts(String html, OnProgress onProgress) {
		Posts posts = new Posts();
		Document doc = getDoc(html);

		Element eFid = doc.select("#nav a").last();
		String fidStr = eFid.attr("href");

//		Logger.i("html %s, eFid %s, fidStr %s", html, eFid.html(), fidStr);

		int fid = Integer.valueOf(Uri.parse(fidStr).getQueryParameter("fid"));
		String title = eFid.nextSibling().toString().replaceAll(" » ", "");

		Elements pages = doc.select("div.pages");
		int totalPage = 1;

		if (pages.size() == 2) {
			Elements lastPage = pages.select("a.last");
			if (lastPage.size() > 0) {
				try {
					totalPage = Integer.valueOf(Uri.parse(lastPage.attr("href")).getQueryParameter("page"));
				} catch (Exception e) {

				}
			} else {
				lastPage = pages.select("a:not(.next)");

				if (lastPage.size() > 0) {
					try {
						totalPage = Integer.valueOf(Uri.parse(lastPage.last().attr("href")).getQueryParameter("page"));
					} catch (Exception e) {

					}
				}
			}
		}

		Elements ePosts = doc.select("table[id^=pid]");

		int i = 0;

		for (Element ePost : ePosts) {
			posts.add(toPostObj(ePost));

			if (onProgress != null) onProgress.progress(++i, ePosts.size());
		}

		int currPage = 1;
		Elements page = doc.select("div.pages > strong");

		if (page.size() > 0) {
			currPage = Integer.valueOf(page.first().text());
		}

		boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

		posts.setHasNextPage(hasNextPage);
		posts.setPage(currPage);
		posts.setTotalPage(Math.max(totalPage, currPage));
		posts.setFid(fid);
		posts.setTitle(title);

		return posts;
	}

	private static Post toPostObj(Element ePost) {
//		Logger.d("raw post %s", ePost.html());

		int idPrefixLength = "pid".length();

		Post post = new Post();

		String id = ePost.attr("id").substring(idPrefixLength);

		Post quote = findQuote(ePost);

		if (quote != null) {
			post.setQuote(quote);
		}

		Elements eBody = ePost.select("td.t_msgfont");
		Post.NiceBody niceBody = new Post.NiceBody();

		if (eBody.size() == 0) {
			niceBody.add(new AbstractMap.SimpleEntry<Post.BodyType, String>(Post.BodyType.TXT, "blocked!"));
		} else {
			ePost.select(".imgtitle a[href^=attachment.php?]").remove();
			ePost.select(".t_attach a[href^=attachment.php?]").remove();

			Elements eAttachments = ePost.select("a[href^=attachment.php?]");
			Elements eAttachImages = ePost.select(".postattachlist img[file^=attachments/]");

			niceBody = postprocessPostBody(
					preprocessPostBody(eBody.get(0)),
					eAttachments,
					eAttachImages);
		}

		String timeStr = ePost.select(".authorinfo > em").text();

		if (timeStr.startsWith("发表于")) {
			timeStr = timeStr.substring(3);
			timeStr = timeStr.trim();
		}

		String postIndex = ePost.select("a[id^=postnum] > em").text();
		postIndex = postIndex.trim();

		Element eUinfo = ePost.select("a[href^=space.php?uid=]").get(0);
		String url = eUinfo.attr("href");
		String userId = Uri.parse(url).getQueryParameter("uid");
		String userName = eUinfo.text();

		User user = new User().setId(Integer.valueOf(userId)).setName(userName);

		post.setId(Integer.valueOf(id)).setNiceBody(niceBody)
				.setAuthor(user).setTimeStr(timeStr).setPostIndex(Integer.valueOf(postIndex));

		for (Map.Entry<Post.BodyType, String> body : niceBody) {
			if (body.getKey() == Post.BodyType.SIG) {
				post.setSig(body.getValue());
				break;
			}
		}

		boolean isTxt = false;
		int i = 0;
		Post.BodyType type;

		while (i < niceBody.size()) {
			type = niceBody.get(i).getKey();

			if (isTxt) {
				isTxt = type == Post.BodyType.TXT;

				if (isTxt) {
					AbstractMap.SimpleEntry mergedEntry = new AbstractMap.SimpleEntry(Post.BodyType.TXT,
							niceBody.get(i - 1).getValue() + niceBody.get(i).getValue());

					niceBody.set(i - 1, mergedEntry);
					niceBody.remove(i);
				} else {
					i++;
				}
			} else {
				isTxt = type == Post.BodyType.TXT;
				i++;
			}
		}

		compress(niceBody);

		return post;
	}

	private static void compress(Post.NiceBody niceBody) {
		for (int i = 0; i < niceBody.size(); ) {
			Map.Entry<Post.BodyType, String> body = niceBody.get(i);

			if (body.getKey() == Post.BodyType.TXT) {
				String trimBody = body.getValue().trim();

				if (trimBody.length() == 0) {
					niceBody.remove(i);
				} else {
					body.setValue(trimBody);
					i++;
				}
			} else {
				i++;
			}
		}
	}

	private static Post findQuote(Element ePost) {
		Elements eBlockQuote = ePost.select(".quote > blockquote"),
				eSimpleReply = ePost.select("strong > a[href^=http://www.hi-pda.com/forum/redirect.php?goto=findpost]");

		if (eBlockQuote.size() > 0) {
			Elements eId = eBlockQuote.select("font[size=2] > a[href^=http://www.hi-pda.com/forum/redirect.php?goto=findpost]");

			if (eId.size() > 0) {
				Uri uri = Uri.parse(eId.attr("href"));
				String id = uri.getQueryParameter("pid");

				if (id != null && id.length() > 0) {
					Post quote = new Post().setId(Integer.valueOf(id));

					String userName = eBlockQuote.select("font[size=2] > font").text();
					int _ = userName.indexOf("发表于");

					if (_ > 0) {
						userName = userName.substring(0, _).trim();
						quote.setAuthor(new User().setName(userName));
					} else {
						quote.setAuthor(new User().setName(""));
					}

//					eBlockQuote.get(0).child(0).remove();
//					eBlockQuote.select("font[size=2]").remove();

					String content = eBlockQuote.text().trim();
					quote.setBody(content);

					return quote;
				}
			}
		} else if (eSimpleReply.size() > 0) {
			Uri uri = Uri.parse(eSimpleReply.attr("href"));
			String id = uri.getQueryParameter("pid");

			if (id != null && id.length() > 0) {
				Post quote = new Post().setId(Integer.valueOf(id));

				String quoteIndex = eSimpleReply.text().trim();

				if (quoteIndex.endsWith("#")) {
					quoteIndex = quoteIndex.substring(0, quoteIndex.length() - 1);
					try {
						quote.setPostIndex(Integer.valueOf(quoteIndex));
					} catch (Exception e) {

					}
				}

				String userName = eSimpleReply.get(0).parent().select("> i").text().trim();
				quote.setAuthor(new User().setName(userName))
						.setBody("");

				eSimpleReply.get(0).parent().remove();

				return quote;
			}
		}

		return null;
	}

	@SuppressLint("DefaultLocale")
	private static Element preprocessPostBody(Element eBody) {
		// remove edit status
		eBody.select("i.pstatus").remove();

		// remove quote
		Elements quote = eBody.select("div.quote");
		for (Element q : quote) {
			if (q.select("img[src=http://www.hi-pda.com/forum/images/common/back.gif]").size() > 0)
				q.remove();
		}

		// remove reply
		Element first = eBody.children().first();
		if (first != null && first.tagName().toLowerCase().equals("strong")) {
			first.remove();
		}

		// remove all image attachment links
		Elements eImageAttaches = eBody.select("div.t_attach");
		eImageAttaches.remove();

		// remove all attachment icons
		eBody.select("img[src^=images/attachicons]").remove();

		return eBody;
	}

	public static Posts parseMessages(String html) {
		Posts posts = new Posts();
		Document doc = getDoc(html);
		Elements ePosts = doc.select("#pmlist > ul > li.s_clear");

		for (Element ePost : ePosts) {
			posts.add(toMessageObj(ePost));
		}

		return posts;
	}

	public static void sendMessage(String name, String message, final OnRequestListener onRequestListener) {
		String url = "http://www.hi-pda.com/forum/pm.php?action=send&pmsubmit=yes&infloat=yes&sendnew=yes";

		RequestParams params = new RequestParams();
		params.setContentEncoding(code);
		params.put("formhash", formhash);
		params.put("msgto", name);
		params.put("message", message);
		params.put("pmsubmit", "true");

		httpClient
				.post(url,
						params, new TextHttpResponseHandler(code) {
							@Override
							public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
								onRequestListener.onError(s);
							}

							@Override
							public void onSuccess(int i, Header[] headers, String s) {
								onRequestListener.onSuccess(s);
							}
						});
	}

	private static Post toMessageObj(Element ePost) {
		Element eCite = ePost.select("> p.cite").first();
		String name = eCite.select("> cite").text();
		Element eUser = ePost.select("> a.avatar").get(0);
		String uid = Uri.parse(eUser.attr("href")).getQueryParameter("uid");
		String image = eUser.select("> img").attr("src");
		String date = eCite.textNodes().get(1).text().trim();
		Post.NiceBody niceBody = postprocessPostBody(ePost.select("> div.summary").get(0), null, null);

		compress(niceBody);

		return new Post().setNiceBody(niceBody)
				.setTimeStr(date)
				.setAuthor(
						new User()
								.setName(name)
								.setId(uid == null ? user.getId() : Integer.valueOf(uid))
								.setImage(image.replace("_small", "_middle"))
				);
	}

	/**
	 * Convert to String Array smartly. Prepend `txt:` to content string.
	 * Prepend `img:` to image source. Try to return absolute image path.
	 *
	 * @param eBody
	 * @param eAttachments
	 * @return
	 */
	private static Post.NiceBody postprocessPostBody(Element eBody, Elements eAttachments, Elements eAttachImages) {
		ArrayList<String> temps = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		Post.NiceBody bodies = new Post.NiceBody();
//		StringBuilder sb = new StringBuilder();
		String sbStr;

		for (Node node : eBody.childNodes()) {
			if (node instanceof TextNode) {
				// Jsoup maps &nbsp; to \u00a0
				String body = ((TextNode) node).text().replaceAll("\u00a0", " ").trim();
				bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.TXT, body));
			} else {
				Element e = (Element) node;
				String tag = e.tagName();
				String className = e.className();

				if (tag.equals("a") && e.attr("target").equals("_blank")) {
					String href = e.attr("href");
					if (StringUtils.endsWithAny(href, "tid=1579403", "tid=1408844")) {
						bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.SIG, "{fa-android} HiPDA"));
					} else {
						String text = e.text().trim();
						String link = e.attr("href").trim();
						String body = text.equals(link) || StringUtils.startsWithAny(text, "http://", "https://") ? link : text + "  " + link;
						body += "\r\n";

						bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.TXT, body));
					}
				} else if (tag.equals("br")) {
					bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.TXT, "\r\n"));
				} else if (tag.equals("font") && e.attr("size").equals("1")) {
					// Remove bad spaces of sig. (some bad people sign it)
					String body = e.text().replaceAll("\u00a0", " ").trim();
					bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.SIG, body));
				} else if (tag.equals("img") && e.attr("width").equals("16") && e.attr("height").equals("16")) {
					bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.SIG, "{fa-windows}"));
				} else if (tag.equals("img")) {
					String src = e.attr("src");

					final String finalSrc = src;
					String iconKey = (String) CollectionUtils.find(iconKeys, new Predicate() {
						@Override
						public boolean evaluate(Object o) {
							return StringUtils.endsWith(finalSrc, (String) o);
						}
					});

					if (iconKey != null) {
						bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.TXT, icons.get(iconKey)));
					} else {

						if (e.attr("file").length() != 0) {
							src = e.attr("file");
						}

						if (!src.startsWith("http")) {
							src = "http://www.hi-pda.com/forum/" + src;
						}

						bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.IMG, src));
					}
				} else if (tag.equals("blockquote")) {
					bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.QUOTE,
							e.text().replaceAll("\u00a0", " ").trim()));
				} else if (tag.equals("font") || tag.equals("p") || tag.equals("strong") || tag.equals("div")) {
					bodies.addAll(postprocessPostBody(e, null, null));
				} else {
					bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.TXT,
							e.text().replaceAll("\u00a0", " ").trim()));
				}
			}
		}

		if (eAttachImages != null) {
			for (Element eImg : eAttachImages) {
				String src = eImg.attr("file");

				bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.IMG,
						src.startsWith("http") ? src : "http://www.hi-pda.com/forum/" + src));
			}
		}

		if (eAttachments != null) {
			for (Element eAtt : eAttachments) {
				String url = BASE_URL + "/" + eAtt.attr("href");
				String filename = eAtt.text();
				Node eSize = eAtt.parent().nextSibling();
				String size = "(? KB)";
				if (eSize instanceof TextNode) {
					size = ((TextNode) eSize).text();
				} else {
					eSize = eAtt.nextSibling();
					if (eSize instanceof TextNode) size = ((TextNode) eSize).text();
				}

				bodies.add(new AbstractMap.SimpleEntry(Post.BodyType.ATT,
						url + DIVIDER + filename + DIVIDER + size));
			}
		}

//		boolean isTxt = false;
//		int i = 0;
//		String type;
//
//		while (i < temps.size()) {
//			type = types.get(i);
//
//			if (isTxt) {
//				isTxt = type.equals("txt");
//
//				if (isTxt) {
//					temps.set(i, temps.get(i - 1) + temps.get(i));
//					temps.remove(i);
//					types.remove(i);
//				} else {
//					i++;
//				}
//			} else {
//				isTxt = type.equals("txt");
//				i++;
//			}
//		}
//
//		for (i = 0; i < temps.size(); ++i) {
//			temps.set(i, types.get(i) + ":" + temps.get(i));
//		}

		return bodies;
	}

	public static void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.setContentEncoding(code);
		params.put("formhash", formhash);
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("wysiwyg", 1);
		params.put("iconid", "");
		params.put("tags", "");
		params.put("attention_add", 1);
		params.put("subject", subject);
		params.put("message", message);

		if (attachIds != null) {
			for (Integer attachId : attachIds) {
				params.put("attachnew[" + attachId + "][description]", "");
			}
		}

		httpClient
				.post("http://www.hi-pda.com/forum/post.php?action=newthread&fid=" + fid + "&extra=&topicsubmit=yes", params,
						new TextHttpResponseHandler(code) {
							@Override
							public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
								onRequestListener.onError(s);
							}

							@Override
							public void onSuccess(int i, Header[] headers, String s) {
								onRequestListener.onSuccess(s);
							}
						});
	}

	public static void editPost(int fid, int tid, int pid, String subject, String message, ArrayList<Integer> attachIds, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.setContentEncoding(code);
		params.put("formhash", formhash);
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("wysiwyg", 1);
		params.put("iconid", 0);
		params.put("fid", fid);
		params.put("tid", tid);
		params.put("pid", pid);
		params.put("page", 1);
		params.put("tags", "");
		params.put("editsubmit", "true");
		params.put("subject", subject);
		params.put("message", message);

		if (attachIds != null) {
			for (Integer attachId : attachIds) {
				params.put("attachnew[" + attachId + "][description]", "");
			}
		}

		httpClient
				.post("http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=", params,
						new TextHttpResponseHandler(code) {
							@Override
							public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
								onRequestListener.onError(throwable.toString());
							}

							@Override
							public void onSuccess(int i, Header[] headers, String s) {
								onRequestListener.onSuccess(s);
							}
						});
	}

	public static void deletePost(int fid, int tid, int pid, final OnRequestListener onRequestListener) {
		Logger.i("fid %d, tid %d, pid %d", fid, tid, pid);

		RequestParams params = new RequestParams();
		params.setContentEncoding(code);
		params.put("formhash", formhash);
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("wysiwyg", 1);
		params.put("page", 1);
		params.put("delete", 1);
		params.put("editsubmit", "true");
		params.put("subject", "");
		params.put("message", "");
		params.put("fid", fid);
		params.put("tid", tid);
		params.put("pid", pid);

		httpClient
				.post("http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=",
						params, new TextHttpResponseHandler(code) {
							@Override
							public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
								Logger.i("fail");

								onRequestListener.onError(s);
							}

							@Override
							public void onSuccess(int i, Header[] headers, String s) {
								if (s.contains("未定义操作，请返回。")) {
									onFailure(i, headers, "未定义操作", null);
								} else {
									Logger.i("succeed");

									onRequestListener.onSuccess(s);
								}
							}
						});
	}

	public static void getExistedAttach(final OnRequestListener onRequestListener) {
		getHtml("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new OnRequestListener() {

			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements tds = doc.select("td[id^=image_td_]");
				ArrayList<Integer> attachIds = new ArrayList<Integer>();

				for (Element td : tds) {
					String id = td.id();
					id = id.substring("image_td_".length());

					try {
						attachIds.add(Integer.valueOf(id));
					} catch (Exception e) {

					}
				}

				onRequestListener.onSuccess(StringUtils.join(attachIds, ","));
			}
		});
	}

	public static void sendReply(int tid, String content, ArrayList<Integer> attachIds, ArrayList<Integer> existedAttchIds, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.setContentEncoding(code);
		params.put("formhash", formhash);
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("subject", "");
		params.put("wysiwyg", 1);
		params.put("noticeauthor", "");
		params.put("noticetrimstr", "");
		params.put("noticeauthormsg", "");
		params.put("subject", "");
		params.put("message", content);

		if (attachIds != null) {
			for (Integer attachId : attachIds) {
				params.put("attachnew[" + attachId + "][description]", "");
			}
		}

		if (existedAttchIds != null) {
			for (Integer id : existedAttchIds) {
				params.put("attachdel[]", id);
			}
		}

		httpClient
				.post("http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes",
						params, new TextHttpResponseHandler(code) {
							@Override
							public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
								onRequestListener.onError(s);
							}

							@Override
							public void onSuccess(int i, Header[] headers, String s) {
								onRequestListener.onSuccess(s);
							}
						});
	}

	public static void search(String query, int page, final OnThreadsListener onThreadsListener) {
		try {
			getHtml("http://www.hi-pda.com/forum/search.php?srchtxt=" + URLEncoder.encode(query, code)
					+ "&srchtype=title&"
					+ "searchsubmit=true&"
					+ "st=on&"
					+ "srchuname=&"
					+ "srchfilter=all&"
					+ "srchfrom=0&"
					+ "before=&"
					+ "orderby=lastpost&"
					+ "ascdesc=desc&"
					+ "srchfid%5B0%5D=all&"
					+ "page=" + page, new OnRequestListener() {

				@Override
				public void onError(String error) {
					onThreadsListener.onError(error);
				}

				@Override
				public void onSuccess(String html) {
					new AsyncTask<String, Void, Core.ThreadsRet>() {
						@Override
						protected Core.ThreadsRet doInBackground(String... strings) {
							return Core.parseThreads(strings[0]);
						}

						@Override
						protected void onPostExecute(Core.ThreadsRet ret) {
							onThreadsListener.onThreads(ret.threads, ret.page, ret.hasNextPage);
						}
					}.execute(html);
				}

			});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static ThreadsRet parseThreads(String html) {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		Document doc = getDoc(html);

		String selectStr = pref.getBoolean("show_fixed_threads", false) ? "tbody[id^=normalthread_],tbody[id^=stickthread_" : "tbody[id^=normalthread_]";

		Elements eThreads = doc.select("body#search").size() == 0 ? doc.select(selectStr) : doc.select("div.searchlist tbody");

		for (Element eThread : eThreads) {
			Thread thread = toThreadObj(eThread);
			if (thread != null) threads.add(toThreadObj(eThread));
		}

		int currPage = 1;

		Elements page = doc.select("div.pages > strong");

		if (page.size() > 0) {
			currPage = Integer.valueOf(page.first().text());
		}

		Elements nextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]");

		ThreadsRet ret = new ThreadsRet();
		ret.hasNextPage = nextPage.size() > 0;
		ret.page = currPage;
		ret.threads = threads;

		return ret;
	}

	private static Thread toThreadObj(Element eThread) {
		Elements eSubject = eThread.select("th.subject");
		Elements eLastPost = eThread.select("td.lastpost em a");
		String lastHref = eLastPost.attr("href");
		String style;
		String type = eSubject.select("em > a[href^=forumdisplay.php]").text();


		if (lastHref != null && lastHref.length() > 0) {
			String id = Uri.parse(lastHref).getQueryParameter("tid");

			Element _title = eSubject.select("span a[href^=viewthread.php], a[href^=viewthread.php]").first();
			String title = _title.text();
			style = _title.attr("style");
			boolean isNew = eThread.select("th.subject").hasClass("new");
			Elements eAuthor = eThread.select("td.author");
			Elements eUser = eAuthor.select("a");
			String userName = eUser.text();
			String dateStr = eAuthor.select("em").text().trim();
			// if userHref.length() == 0, this thread is closed for some reason.
			String userHref = eUser.attr("href");
			if (userHref.length() == 0) {
				return null;
			}

			String userId = Uri.parse(userHref).getQueryParameter("uid");
			String commentNum = eThread.select("td.nums > strong").text().trim();

			String forumLink = eThread.select(".forum a").attr("href");

			String fid = null;

			int uid = Integer.valueOf(userId);

			User user = new User().setId(uid).setName(userName);
			Thread ret = new Thread();


			ret.setId(Integer.valueOf(id)).setTitle(title).setNew(isNew)
					.setCommentCount(Integer.valueOf(commentNum)).setAuthor(user).setDateStr(dateStr)
					.setStick(eThread.id().startsWith("stickthread_"))
					.setBold(style.contains("bold"))
					.setType(type);

			Matcher matcher = colorReg.matcher(style);
			if (matcher.find()) ret.setColor(matcher.group());

			if (forumLink.length() > 0) {
				fid = forumLink.substring(forumLink.indexOf("fid=") + 4);
				ret.setFid(Integer.valueOf(fid));
			}

			return ret;
		} else {
			return null;
		}
	}

	public static void getUser(int uid, final OnUserListener onUserListener) {
		getHtml("http://www.hi-pda.com/forum/space.php?uid=" + uid, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onUserListener.onUser(null);
			}

			@Override
			public void onSuccess(String html) {
				onUserListener.onUser(parseUser(html));
			}
		});
	}

	public static User parseUser(String html) {
		Document doc = getDoc(html);

		String img = doc.select("div.avatar>img").attr("src");
		String uidLink = doc.select("li.searchpost a").attr("href");
		String uid = Uri.parse(uidLink).getQueryParameter("srchuid");
		String name = doc.select("div.itemtitle.s_clear h1").text().trim();

		Element eProfile = doc.select("#profilecontent").first();

		Elements eBaseProfile = eProfile.select("> #baseprofile > table");
		String sex = eBaseProfile.first().select("td[width=70]").text().trim();
		String qq = eBaseProfile.get(1).select("a[href^=http://wpa.qq.com]").text().trim();
		String point = eProfile.select("> h3").get(1).text().trim().substring(4);

		eProfile = eProfile.select("> div.s_clear").get(1);

		String registerDate = eProfile.select("> .right > li").first().text().trim().substring(6);

		Elements eSubProfiles = eProfile.select("> ul").get(1).select("> li");

		String level = eSubProfiles.get(0).text().substring(7);
		String totalThreads = eSubProfiles.get(2).text().trim().substring(4);
		totalThreads = totalThreads.substring(0, totalThreads.indexOf("篇") - 1);

		return new User()
				.setId(Integer.valueOf(uid))
				.setImage(img)
				.setName(name)
				.setRegisterDateStr(registerDate)
				.setQq(qq)
				.setSex(sex)
				.setPoints(point)
				.setLevel(level)
				.setTotalThreads(totalThreads);
	}

	public static void getFavorites(int page, final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=favorites&type=thread&page=" + page, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements eThreads = doc.select("form[method=post] tbody tr");
				ArrayList<Thread> threads = new ArrayList<Thread>();

				for (Element eThread : eThreads) {
					Elements eTitle = eThread.select("th a");

					if (eTitle.size() > 0) {
						String href = eTitle.attr("href");
						String id = href.substring(href.indexOf("tid=") + 4, href.indexOf("&from"));
						String title = eTitle.text();
						String forumStr = eThread.select("td.forum > a").attr("href");
						String fid = Uri.parse(forumStr).getQueryParameter("fid");
						Thread thread = new Thread().setTitle(title).setId(Integer.valueOf(id)).setFid(Integer.valueOf(fid));
						threads.add(thread);
					}
				}

				int currPage = 1;
				Elements page = doc.select("div.pages > strong");

				if (page.size() > 0) {
					currPage = Integer.valueOf(page.first().text());
				}

				boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

				onThreadsListener.onThreads(threads, currPage, hasNextPage);
			}
		});
	}

	public static void getEditBody(int fid, int tid, int pid, final OnRequestListener onRequestListener, final OnRequestListener onRequestListener2) {
		getHtml("http://www.hi-pda.com/forum/post.php?action=edit&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1", new OnRequestListener() {
			@Override
			public void onError(String error) {
				onRequestListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);
				String title = doc.select("#subject").val();
				String editBody = doc.select("#e_textarea").text();

				onRequestListener.onSuccess(title);
				onRequestListener2.onSuccess(editBody);
			}
		});
	}

	public static void getMentions(final OnPostsListener onPostsListener) {
		getHtml("http://www.hi-pda.com/forum/notice.php", new OnRequestListener() {
			@Override
			public void onError(String error) {
				onPostsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				try {
					Document doc = getDoc(html);

					Elements eNotices = doc.select("ul.feed > li.s_clear > div");
					Posts posts = new Posts();

					for (Element eNotice : eNotices) {
						String title;
						Elements eSummary = eNotice.select(">dl.summary");
						String body;
						String tid;
						String pid;
						String fid = "0";
						String findPostLink;
						if (eSummary.size() > 0) {
							findPostLink = eNotice.select(">p>a").last().attr("href");
							title = eNotice.select(">a").last().text();
							body = eSummary.select("dt").last().text() + eSummary.select("dd").last().text();
							String viewPostLink = eNotice.select(">p>a").first().attr("href");

							fid = Uri.parse(viewPostLink).getQueryParameter("tid");
//					} else if (findPostLink == null || findPostLink.length() == 0) {
//						// other alerts, like thread being highlighted
//						title = eNotice.text();
//						body = "";
						} else {
							// thread watched on
							Element lastA = eNotice.select(">a").last();
							findPostLink = lastA.attr("href");
							lastA.remove();
							lastA = eNotice.select(">a").last();
							title = lastA.text();
							lastA.remove();

							eNotice.select(">em").last().remove();
							eNotice.select("dfn").remove();
							body = eNotice.text();
						}

						Uri findPostUri = Uri.parse(findPostLink);

						tid = findPostUri.getQueryParameter("ptid");
						pid = findPostUri.getQueryParameter("pid");
						if (tid == null) tid = "0";
						if (pid == null) pid = "0";

						Post post = new Post().setId(Integer.valueOf(pid))
								.setTid(Integer.valueOf(tid))
								.setFid(Integer.valueOf(fid))
								.setTitle(title).setBody(body);
						posts.add(post);
					}

					int currPage = 1;
					Elements page = doc.select("div.pages > strong");

					if (page.size() > 0) {
						currPage = Integer.valueOf(page.first().text());
					}

					boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

					// TO-DO
					posts.setHasNextPage(hasNextPage);
					posts.setPage(currPage);
					onPostsListener.onPosts(posts);
				} catch (Exception e) {
					onError(e.toString());
				}
			}
		});
	}

	public static void getMessages(final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/pm.php?filter=privatepm", new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements pms = doc.select("ul.pm_list li.s_clear");
				ArrayList<Thread> threads = new ArrayList<Thread>();

				for (Element pm : pms) {
					Elements eUser = pm.select("p.cite a");
					String userName = eUser.text();
					String userLink = eUser.attr("href");
					String uid = userLink.substring(userLink.indexOf("uid=") + 4);
					String uimg = pm.select("a.avatar img").attr("src");

					User u = new User().setId(Integer.valueOf(uid)).setName(userName);

					String title = pm.select("div.summary").text();
					boolean isNew = pm.select("img[alt=NEW]").size() != 0;
					String dateStr = ((TextNode) pm.select("p.cite").get(0).childNode(2)).text().replaceAll("\u00a0", "");

					Thread thread = new Thread().setTitle(title).setAuthor(u).setNew(isNew).setDateStr(dateStr);
					threads.add(thread);
				}

				int currPage = 1;
				Elements page = doc.select("div.pages > strong");

				if (page.size() > 0) {
					currPage = Integer.valueOf(page.first().text());
				}

				boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

				onThreadsListener.onThreads(threads, currPage, hasNextPage);
			}
		});
	}

	public static void getMyThreads(int page, final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=threads&page" + page, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements eThreads = doc.select("div.threadlist tbody tr");
				ArrayList<Thread> threads = new ArrayList<Thread>();

				for (Element eThread : eThreads) {
					Elements eTitle = eThread.select("th a");
					Elements eForum = eThread.select(".forum a");

					if (eTitle.size() > 0) {
						String href = eTitle.attr("href");
						String id = href.substring(href.indexOf("tid=") + 4);
						String title = eTitle.text();
						String forumLink = eForum.attr("href");
						String fid = null;
						if (forumLink.length() > 0) {
							fid = forumLink.substring(forumLink.indexOf("fid=") + 4);
						}
						Thread thread = new Thread().setTitle(title).setId(Integer.valueOf(id));
						if (fid != null) {
							thread.setFid(Integer.valueOf(fid));
						}
						threads.add(thread);
					}
				}

				int currPage = 1;
				Elements page = doc.select("div.pages > strong");

				if (page.size() > 0) {
					currPage = Integer.valueOf(page.first().text());
				}

				boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

				onThreadsListener.onThreads(threads, currPage, hasNextPage);
			}
		});
	}

	public static void getUserThreadsAtPage(String userName, int page, OnThreadsListener onThreadsListener) {
		String url = null;
		try {
			url = "http://www.hi-pda.com/forum/search.php?srchtype=title&srchtxt=&searchsubmit=true&st=on&srchuname=" + URLEncoder.encode(userName, code) + "&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&page=" + page;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		getThreadsByUrl(url, onThreadsListener);
	}

	public static void getMyPosts(int page, final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=posts&page=" + page, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements eThreads = doc.select("div.threadlist tbody tr");
				ArrayList<Thread> threads = new ArrayList<Thread>();

				for (int i = 0; i < eThreads.size(); i += 2) {
					Elements eTitle = eThreads.get(i).select("th a");

					if (eTitle.size() > 0) {
						String href = eTitle.attr("href");
						Uri uri = Uri.parse(href);
						String id = uri.getQueryParameter("ptid");
						String pid = uri.getQueryParameter("pid");
						String title = eTitle.text();
						String body = eThreads.get(i + 1).select("th.lighttxt").text().trim();
						String forumStr = eThreads.get(i).select("td.forum > a").attr("href");
						String fid = Uri.parse(forumStr).getQueryParameter("fid");

						Thread thread = new Thread()
								.setTitle(title)
								.setId(Integer.valueOf(id))
								.setBody(body)
								.setFid(Integer.valueOf(fid))
								.setToFind(Integer.valueOf(pid));

						threads.add(thread);
					}
				}

				int currPage = 1;
				Elements page = doc.select("div.pages > strong");

				if (page.size() > 0) {
					currPage = Integer.valueOf(page.first().text());
				}

				boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

				onThreadsListener.onThreads(threads, currPage, hasNextPage);
			}
		});
	}

	public static void getThreadsByUrl(String url, final OnThreadsListener onThreadsListener) {
		getHtml(url, new OnRequestListener() {

			@Override
			public void onError(String error) {
				onThreadsListener.onError(error);
			}

			@Override
			public void onSuccess(String html) {
				new AsyncTask<String, Void, Core.ThreadsRet>() {
					@Override
					protected Core.ThreadsRet doInBackground(String... strings) {
						return Core.parseThreads(strings[0]);
					}

					@Override
					protected void onPostExecute(Core.ThreadsRet ret) {
						onThreadsListener.onThreads(ret.threads, ret.page, ret.hasNextPage);
					}
				}.execute(html);
			}
		});
	}

	public static Set<Integer> getBanList() {
		String banList = pref.getString("ban", "");
		String bans[] = banList.split(",");

		ArrayList<String> banlist = new ArrayList<String>();
		CollectionUtils.addAll(banlist, bans);

		return new HashSet(CollectionUtils.collect(banlist, new Transformer() {
			@Override
			public Object transform(Object o) {
				String ban = (String) o;
				try {
					return Integer.valueOf(ban);
				} catch (Exception e) {
					return Integer.valueOf(0);
				}
			}
		}));
	}

	public static void addToBanList(int uid) {
		bans.add(uid);
		String bansStr = StringUtils.join(bans, ",");

		SharedPreferences.Editor editor = pref.edit();
		editor.putString("ban", bansStr);
		editor.commit();

	}

	public static void removeFromBanList(int uid) {
		bans.remove(uid);
		String bansStr = StringUtils.join(bans, ",");

		SharedPreferences.Editor editor = pref.edit();
		editor.putString("ban", bansStr);
		editor.commit();
	}

	/**
	 *
	 * If the length of image file is less than maxSize, quality will not be applied, and
	 * image file will be returned directly.
	 *
	 * @param imageFile
	 * @param maxSize
	 * @param quality
	 * @return File
	 */
	private static File findBestQuality(final File imageFile, int maxSize, int quality) {
		long fileLength = imageFile.length();

		if (fileLength > maxSize) {
			try {
				Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
				File tempDir = context.getCacheDir();
				File tempFile = File.createTempFile("uzlee-compress", ".jpg", tempDir);
				OutputStream os = new FileOutputStream(tempFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
				os.close();
				fileLength = tempFile.length();

				Logger.i("length: %d, quality: %d", fileLength, quality);

				return fileLength > maxSize ? null : tempFile;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return imageFile;
		}
	}

	private static File findBestQuality(final File imageFile, int maxSize) {
		// Test the worst case.
		File tempFile = findBestQuality(imageFile, maxSize, 30);

		// Find a better one.
		if (tempFile != null) {
			int quality = 90;

			do {
				tempFile = findBestQuality(imageFile, maxSize, quality);
				quality -= 15;
			} while (tempFile == null && quality >= 30);
		}

		return tempFile;
	}

	private static File compressBySize(final File imageFile, int maxSize, float rate) {
		long fileLength = imageFile.length();

		if (fileLength > maxSize) {
			try {
				Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				width = (int) (width * rate);
				height = (int) (height * rate);

				File tempDir = context.getCacheDir();
				File tempFile = File.createTempFile("uzlee-compress", ".jpg", tempDir);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width , height, true);
				OutputStream os = new FileOutputStream(tempFile);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
				os.close();
				fileLength = tempFile.length();

				Logger.i("length: %d, height: %d, width: %d", fileLength, height, width);

				return fileLength > maxSize ? null : tempFile;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return imageFile;
		}
	}

	/**
	 *
	 * @param imageFile
	 * @param maxSize
	 * @param currentRate, 1.0f as the initial value.
	 * @return
	 */
	private static File compressImage(File imageFile, int maxSize, float currentRate) {
		File tempFile = findBestQuality(imageFile, maxSize);

		if (tempFile != null) {
			return tempFile;
		} else {
			currentRate = currentRate * 0.8f;
			tempFile = compressBySize(imageFile, maxSize, currentRate);

			if (tempFile == null) {
				return compressImage(imageFile, maxSize, currentRate);
			} else {
				return tempFile;
			}
		}
	}

	public static File compressImage(File imageFile, int maxSize) {
		return compressImage(imageFile, maxSize, 1.0f);
	}

	public static void addToFavorite(int tid, final OnRequestListener onRequestListener) {
		httpClient.get("http://www.hi-pda.com/forum/my.php?item=favorites&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg",
				new TextHttpResponseHandler(code) {
					@Override
					public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
						onRequestListener.onError("收藏失败");
					}

					@Override
					public void onSuccess(int i, Header[] headers, String s) {
						onRequestListener.onSuccess(s);
					}
				});
	}

	public static void removeFromFavoriate(int tid, final OnRequestListener onRequestListener) {
		httpClient.get("http://www.hi-pda.com/forum/my.php?item=favorites&action=remove&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg",
				new TextHttpResponseHandler(code) {
					@Override
					public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
						onRequestListener.onError("删除失败");
					}

					@Override
					public void onSuccess(int i, Header[] headers, String s) {
						onRequestListener.onSuccess(s);
					}
				});
	}

	public static class Attachment {
		public String url;
		public String filename;
		public String size;
	}

	public interface OnRequestListener {
		void onError(String error);

		void onSuccess(String html);
	}

	public interface OnUploadListener {
		void onUpload(String response);
	}

	public interface OnUserListener {
		void onUser(User u);
	}

	public interface OnPostsListener {
		void onPosts(Posts posts);

		void onError(String error);
	}

	public interface OnThreadsListener {
		void onThreads(ArrayList<Thread> threads, int currPage, boolean hasNextPage);

		void onError(String error);
	}

	public interface OnImageCompressed {
		void onImage(File imageFile);
	}

	public static class UserEvent{
		public User user;

		public UserEvent(User user) {
			this.user = user;
		}
	}

	public static class MessageEvent {
		public int count;

		public MessageEvent(int count) {
			this.count = count;
		}
	}

//	public static class PostsRet {
//		public ArrayList<Post> posts;
//		public boolean hasNextPage;
//		public int page;
//		public int totalPage;
//	}

	public static class UpdateInfo {
		private String version;
		private String uri;
		private String info;

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getInfo() {
			return info;
		}

		public void setInfo(String info) {
			this.info = info;
		}
	}

	public interface OnProgress {
		void progress(int current, int total);
	}

	public static class ThreadsRet {
		public ArrayList<Thread> threads;
		public boolean hasNextPage;
		public int page;
	}
}
