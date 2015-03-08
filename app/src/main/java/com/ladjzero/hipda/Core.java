package com.ladjzero.hipda;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

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

import de.greenrobot.event.EventBus;

public class Core {
	public final static int UGLEE_ID = 1261;
	public static final Set<Integer> bans = new HashSet<Integer>();
	public static final HashMap<String, String> icons = new HashMap<String, String>();
	public static final Set<String> iconKeys;
	public static final Collection<String> iconValues;

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
	private final static int maxImageLength = 299 * 1024;
	private static AsyncHttpClient httpClient = new AsyncHttpClient();
	private static String formhash;
	private static String hash;
	private static Context context;
	private static int uid;
	private static boolean isOnline = false;
	private static PersistentCookieStore cookieStore;

	public static void setup(Context context) {
		if (Core.context == null) {
			Core.context = context;
			cookieStore = new PersistentCookieStore(context);
			httpClient.setCookieStore(cookieStore);
			bans.addAll(getBanList());
		}
	}

	public static boolean isOnline() {
		return isOnline;
	}

	public static int getUid() {
		return uid;
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
		httpClient.get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new TextHttpResponseHandler("GBK") {
			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

			}

			@Override
			public void onSuccess(int i, Header[] headers, String s) {
				getDoc(s);

				try {
					RequestParams params = new RequestParams();
					params.setContentEncoding("GBK");
					params.put("uid", uid);
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
		httpClient.get(url, new RequestParams(), new TextHttpResponseHandler("GBK") {

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

			if (msgCount > 0) {
				EventBus.getDefault().post(new MessageEvent(msgCount));
			}

			String uidHref = doc.select("#umenu > cite > a").attr("href");
			Uri uri = Uri.parse(uidHref);
			String uid = uri.getQueryParameter("uid");

			if (uid != null && uid.length() > 0) {
				isOnline = true;
				Core.uid = Integer.valueOf(uid);
			} else {
				isOnline = false;

				EventBus.getDefault().post(new StatusChangeEvent(false));
			}
		} catch (Error e) {

		}

		Elements formHashInput = doc.select("input[name=formhash]");

		if (formHashInput.size() > 0) {
			formhash = formHashInput.val();
		}

		Elements hashInput = doc.select("input[name=hash]");

		if (hashInput.size() > 0) {
			hash = hashInput.val();
		}

		Log.i(TAG, "getDoc " + (System.currentTimeMillis() - time));
		return doc;
	}

	public static void login(String username, String password, int questionId, String answer, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.setContentEncoding("GBK");
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
								isOnline = true;

								String html;
								try {
									html = new String(responseBody, "GBK");
									if (html.contains("欢迎您回来")) {
										getDoc(html);
										EventBus.getDefault().post(new StatusChangeEvent(true));
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
				isOnline = false;
				uid = -1;
				EventBus.getDefault().post(new StatusChangeEvent(false));
				EventBus.getDefault().post(new MessageEvent(0));

				onRequestListener.onSuccess(html);

				for (Cookie cookie : cookieStore.getCookies()) {
					cookieStore.deleteCookie(cookie);
				}
			}
		});
	}

	public static PostsRet parsePosts(String html) {
		ArrayList<Post> posts = new ArrayList<Post>();
		Document doc = getDoc(html);

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

		for (Element ePost : ePosts) {
			posts.add(toPostObj(ePost));
		}

		int currPage = 1;
		Elements page = doc.select("div.pages > strong");

		if (page.size() > 0) {
			currPage = Integer.valueOf(page.first().text());
		}

		boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

		PostsRet ret = new PostsRet();
		ret.posts = posts;
		ret.hasNextPage = hasNextPage;
		ret.page = currPage;
		ret.totalPage = totalPage;

		return ret;
	}

	private static Post toPostObj(Element ePost) {
		int idPrefixLength = "pid".length();

		Post post = new Post();

		String id = ePost.attr("id").substring(idPrefixLength);

		Post quote = findQuote(ePost);

		if (quote != null) {
			post.setQuote(quote);
		}

		Elements eBody = ePost.select("td.t_msgfont");
		String[] niceBody;

		if (eBody.size() == 0) {
			niceBody = new String[]{"txt:blocked!"};
		} else {
			niceBody = postprocessPostBody(preprocessPostBody(eBody.get(0)), ePost.select("div.postattachlist"));
		}

		String timeStr = ePost.select(".authorinfo > em").text();

		if (timeStr.startsWith("发表于")) {
			timeStr = timeStr.substring(3);
			timeStr = timeStr.trim();
		}

		String postIndex = ePost.select("a[id^=postnum] > em").text();
		postIndex = postIndex.trim();

		Element eUser = ePost.select("td.postauthor").get(0);
		Elements eUinfo = eUser.select("div.postinfo a");
		String userId = eUinfo.attr("href").substring("space.php?uid=".length());
		String userName = eUinfo.text();
		String userImg = eUser.select("div.avatar img").attr("src");

		User user = new User().setId(Integer.valueOf(userId)).setName(userName)
				.setImage(userImg);

		post.setId(Integer.valueOf(id)).setNiceBody(niceBody)
				.setAuthor(user).setTimeStr(timeStr).setPostIndex(Integer.valueOf(postIndex));

		return post;
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

					eBlockQuote.get(0).child(0).remove();
					eBlockQuote.select("font[size=2]").remove();

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
		eBody.select("div.quote").remove();

		// remove reply
		Element first = eBody.children().first();
		if (first != null && first.tagName().toLowerCase().equals("strong")) {
			first.remove();
		}

		// remove all image attachment links
		Elements eImageAttaches = eBody.select("div.t_attach");
		eImageAttaches.remove();

		return eBody;
	}

	/**
	 * Convert to String Array smartly. Prepend `txt:` to content string.
	 * Prepend `img:` to image source. Try to return absolute image path.
	 *
	 * @param eBody
	 * @param eAttachments
	 * @return
	 */
	private static String[] postprocessPostBody(Element eBody, Elements eAttachments) {
		ArrayList<String> temps = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String sbStr;

		for (Node node : eBody.childNodes()) {
			if (node instanceof TextNode) {
				// Jsoup maps &nbsp; to \u00a0
				sb.append(((TextNode) node).text().replaceAll("\u00a0", " ")
						.trim());
			} else {
				Element e = (Element) node;
				String tag = e.tagName();

				if (tag.equals("a") && e.attr("target").equals("_blank")) {
					String href = e.attr("href");
					if (StringUtils.endsWith(href, "tid=1408844")) {
						if ((sbStr = sb.toString().trim()).length() != 0) {
							temps.add("txt:" + sbStr);
						}

						temps.add("sig:{fa-android} HiPDA");
						sb.delete(0, sb.length());
					} else {
						sb.append(e.attr("href"));
					}
				} else if (tag.equals("br")) {
					sb.append("\r\n");
				} else if (tag.equals("font") && e.attr("size").equals("1")) {
					if ((sbStr = sb.toString().trim()).length() != 0) {
						temps.add("txt:" + sbStr);
					}

					// Remove bad spaces of sig. (some bad people sign it)
					temps.add("sig:" + e.text().replaceAll("\u00a0", " ").trim());
					sb.delete(0, sb.length());
				} else if (tag.equals("img") && e.attr("width").equals("16") && e.attr("height").equals("16")) {
					if ((sbStr = sb.toString().trim()).length() != 0) {
						temps.add("txt:" + sbStr);
					}

					temps.add("sig:{fa-windows}");
					sb.delete(0, sb.length());
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
						sb.append(icons.get(iconKey));
					} else {
						if ((sbStr = sb.toString().trim()).length() != 0) {
							temps.add("txt:" + sbStr);
						}

						if (e.attr("file").length() != 0) {
							src = e.attr("file");
						}

						if (!src.startsWith("http")) {
							src = "http://www.hi-pda.com/forum/" + src;
						}

						temps.add("img:" + src);

						sb.delete(0, sb.length());
					}
				} else {
					sb.append(e.text().replaceAll("\u00a0", " ").trim());
				}
			}
		}

		if ((sbStr = sb.toString().trim()).length() != 0) {
			temps.add("txt:" + sbStr);
		}

		for (Element eImg : eAttachments.select("img")) {
			String src = eImg.attr("file");
			temps.add("img:" + (src.startsWith("http") ? src : "http://www.hi-pda.com/forum/" + src));
		}

		if (temps.size() == 0) {
			temps.add("txt:(正文格式很奇怪，解析不了)");
		}

		return temps.toArray(new String[0]);
	}

	public static void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.setContentEncoding("GBK");
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
						new TextHttpResponseHandler("GBK") {
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
		params.setContentEncoding("GBK");
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
						new TextHttpResponseHandler("GBK") {
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
		RequestParams params = new RequestParams();
		params.setContentEncoding("GBK");
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
						params, new TextHttpResponseHandler("GBK") {
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
		params.setContentEncoding("GBK");
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
						params, new TextHttpResponseHandler("GBK") {
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
			getHtml("http://www.hi-pda.com/forum/search.php?srchtxt=" + URLEncoder.encode(query, "GBK")
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
					onThreadsListener.onError();
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

		Elements eThreads = doc.select("body#search").size() == 0 ? doc.select("tbody[id^=normalthread_]") : doc.select("div.searchlist tbody");

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
		Elements eLastPost = eThread.select("td.lastpost em a");
		String lastHref = eLastPost.attr("href");

		if (lastHref != null && lastHref.length() > 0) {
			String id = lastHref.substring(lastHref.indexOf("tid=") + 4, lastHref.indexOf("&goto"));

			String title = eThread.select("th.subject span a, th.subject a").first().text();
			boolean isNew = eThread.select("th.subject").hasClass("new");
			Elements eUser = eThread.select("td.author a");
			String userName = eUser.text();
			// if userHref.length() == 0, this thread is closed for some reason.
			String userHref = eUser.attr("href");

			if (userHref.length() == 0) {
				return null;
			}

			String userId = userHref.substring("space.php?uid=".length());
			String commentNum = eThread.select("td.nums > strong").text().trim();

			String forumLink = eThread.select(".forum a").attr("href");

			String fid = null;

			int uid = Integer.valueOf(userId);

			int avatar0 = uid / 10000;
			int avatar1 = (uid % 10000) / 100;
			int avatar2 = uid % 100;

			User user = new User().setId(uid).setName(userName)
					.setImage("http://www.hi-pda.com/forum/uc_server/data/avatar/000/" + String.format("%02d", avatar0) + "/" + String.format("%02d", avatar1) + "/" + String.format("%02d", avatar2) + "_avatar_middle.jpg");
			Thread ret = new Thread();
			ret.setId(Integer.valueOf(id)).setTitle(title).setNew(isNew)
					.setCommentCount(Integer.valueOf(commentNum)).setAuthor(user);

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
		String uid = uidLink.substring(uidLink.indexOf("uid=") + 4, uidLink.indexOf("&srchfid="));
		String name = doc.select("div.itemtitle.s_clear h1").text().trim();

		return new User().setId(Integer.valueOf(uid)).setImage(img).setName(name);
	}

	public static void getFavorites(int page, final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=favorites&type=thread&page=" + page, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError();
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
						Thread thread = new Thread().setTitle(title).setId(Integer.valueOf(id));
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

	public static void getAlerts(final OnPostsListener onPostsListener) {
		getHtml("http://www.hi-pda.com/forum/notice.php", new OnRequestListener() {
			@Override
			public void onError(String error) {
				onPostsListener.onError();
			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements eNotices = doc.select("ul.feed > li.s_clear > div");
				ArrayList<Post> posts = new ArrayList<Post>();

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

						int fidIndex = viewPostLink.indexOf("&fid=");
						fid = viewPostLink.substring(fidIndex + 5, viewPostLink.indexOf("&tid="));
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
				onPostsListener.onPosts(posts, currPage, 10);
			}
		});
	}

	public static void getMessages(final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/pm.php?filter=privatepm", new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError();
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

					User u = new User().setId(Integer.valueOf(uid)).setImage(uimg).setName(userName);

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
				onThreadsListener.onError();
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
			url = "http://www.hi-pda.com/forum/search.php?srchtype=title&srchtxt=&searchsubmit=true&st=on&srchuname=" + URLEncoder.encode(userName, "GBK") + "&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&page=" + page;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		getThreadsByUrl(url, onThreadsListener);
	}

	public static void getMyPosts(int page, final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=posts&page=" + page, new OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError();
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
						String id = href.substring(href.indexOf("ptid=") + 5);
						String title = eTitle.text();
						String body = eThreads.get(i + 1).select("th.lighttxt").text().trim();
						Thread thread = new Thread().setTitle(title).setId(Integer.valueOf(id)).setBody(body);
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
				onThreadsListener.onError();
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
		SharedPreferences setting = context.getSharedPreferences("uzlee.setting", 0);
		String banList = setting.getString("ban", "");
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

		SharedPreferences setting = context.getSharedPreferences("uzlee.setting", 0);
		SharedPreferences.Editor editor = setting.edit();
		editor.putString("ban", bansStr);
		editor.commit();

	}

	public static void removeFromBanList(int uid) {
		bans.remove(uid);
		String bansStr = StringUtils.join(bans, ",");

		SharedPreferences setting = context.getSharedPreferences("uzlee.setting", 0);
		SharedPreferences.Editor editor = setting.edit();
		editor.putString("ban", bansStr);
		editor.commit();
	}

	public static void compressImage(final File imgFile, final OnImageCompressed onImageCompressed) {
//		new AsyncTask<Void, Void, Void>() {
//			@Override
//			protected Void doInBackground(Void... params) {
		Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		File tempDir = context.getCacheDir();
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		int newWidth = width, newHeight = height;

		if (width > 800 && height > 800) {
			if (width > height) {
				newWidth = 800;
				newHeight = 800 * height / width;
			} else {
				newHeight = 800;
				newWidth = 800 * width / height;
			}
		}

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

		long fileLength = imgFile.length();

		File tempFile = imgFile;

		while (fileLength > maxImageLength) {
			OutputStream os = null;

			try {
				tempFile = File.createTempFile("uzlee-compress", ".jpg", tempDir);
				os = new FileOutputStream(tempFile);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
				os.close();

				fileLength = tempFile.length();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		onImageCompressed.onImage(tempFile);

//				return null;
//			}
//		}.execute();
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
		void onPosts(ArrayList<Post> posts, int currPage, int totalPage);
		void onError();
	}

	public interface OnThreadsListener {
		void onThreads(ArrayList<Thread> threads, int currPage, boolean hasNextPage);

		void onError();
	}

	public interface OnImageCompressed {
		void onImage(File imageFile);
	}

	public static class MessageEvent {
		public int count;

		public MessageEvent(int count) {
			this.count = count;
		}
	}

	public static class StatusChangeEvent {
		public boolean online;

		public StatusChangeEvent(boolean online) {
			this.online = online;
		}
	}

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

	public static class PostsRet {
		public ArrayList<Post> posts;
		public boolean hasNextPage;
		public int page;
		public int totalPage;
	}

	public static class ThreadsRet {
		public ArrayList<Thread> threads;
		public boolean hasNextPage;
		public int page;
	}

	public static void addToFavorite(int tid, final OnRequestListener onRequestListener) {
		httpClient.get("http://www.hi-pda.com/forum/my.php?item=favorites&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg",
				new TextHttpResponseHandler("GBK") {
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
				new TextHttpResponseHandler("GBK") {
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
}
