package com.ladjzero.hipda;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class Core {
	private static AsyncHttpClient httpClient = new AsyncHttpClient();
	private static ArrayList<OnMessageListener> onMessageListeners = new ArrayList<OnMessageListener>();
	private static String formhash;
	private static Context context;
	private static int uid;
	private final static int maxImageLength = 299 * 1024;
	public final static int UGLEE_ID = 1261;

	public static void setup(Context context) {
		if (Core.context == null) {
			Core.context = context;
			httpClient.setCookieStore(new PersistentCookieStore(context));
		}
	}

	public interface OnMessageListener {
		public void onMsg(int count);
	}

	public static void addOnMsgListener(OnMessageListener onMessageListener) {
		onMessageListeners.add(onMessageListener);
	}

	public interface OnRequestListener {
		void onError(String error);

		void onSuccess(String html);
	}

	public interface OnUploadListener {
		void onUpload(String response);
	}

	public static void uploadImage(File imageFile, final OnUploadListener onUploadListener) {
		try {
			RequestParams params = new RequestParams();
			params.setContentEncoding("GBK");
			params.put("uid", uid);
			params.put("hash", "ac62265abc8a56fa12705ceca76c46da");
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

	public static void getHtml(String url, final OnRequestListener onRequestListener) {
		httpClient.get(url, new RequestParams(), new TextHttpResponseHandler("GBK") {

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

	public static Document getDoc(String html) {
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
				for (OnMessageListener onMessageListener : onMessageListeners) {
					onMessageListener.onMsg(msgCount);
				}
			}

			String uidHref = doc.select("#umenu > cite > a").attr("href");
			uid = Integer.valueOf(uidHref.substring(uidHref.indexOf("uid=") + 4));
		} catch (Error e) {

		}

		Elements hashInput = doc.select("input[name=formhash]");

		if (hashInput.size() > 0) {
			formhash = hashInput.val();
		}

		return doc;
	}

	public static void login(String username, String password, final OnRequestListener onRequestListener) {
		RequestParams params = new RequestParams();
		params.put("sid", "fa6m4o");
		params.put("formhash", "ad793a3f");
		params.put("loginfield", "username");
		params.put("username", username);
		params.put("password", password);
		params.put("questionid", "0");
		params.put("answer", "");
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
								String html;
								try {
									html = new String(responseBody, "GBK");
									if (html.contains("欢迎您回来")) {
										onRequestListener.onSuccess("");
									} else if (html.contains("密码错误次数过多，请 15 分钟后重新登录")) {
										onRequestListener.onError("密码错误次数过多，请 15 分钟后重新登录");
									} else {
										onRequestListener.onError("error");
									}
								} catch (UnsupportedEncodingException e) {
									onRequestListener.onError("error");
								}
							}
						});

	}

	public static void logout(OnRequestListener onRequestListener) {
		getHtml("http://www.hi-pda.com/forum/logging.php?action=logout&formhash=" + formhash, onRequestListener);
	}

	public static ArrayList<Post> parsePosts(String html, OnPostsListener onPostsListener) {
		ArrayList<Post> posts = new ArrayList<Post>();
		Document doc = getDoc(html);

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

		if (onPostsListener != null) {
			onPostsListener.onPosts(posts, currPage, hasNextPage);
		}

		return posts;
	}

	public static ArrayList<Post> parsePosts(String html) {
		return parsePosts(html, null);
	}

	private static Post toPostObj(Element ePost) {
		int idPrefixLength = "pid".length();

		String id = ePost.attr("id").substring(idPrefixLength);
		Elements eReply = ePost
				.select("strong a[href^=http://www.hi-pda.com/forum/redirect.php?goto=findpost]");
		if (eReply.size() == 0) {
			eReply = ePost
					.select("div.quote blockquote a[href^=http://www.hi-pda.com/forum/redirect.php?goto=findpost]");
		}

		String replyTo = null;
		if (eReply.size() != 0) {
			replyTo = eReply.attr("href");

			if (replyTo
					.matches("http://www.hi-pda.com/forum/redirect.php\\?goto=findpost&pid=\\d+&ptid=\\d+")) {
				replyTo = replyTo.substring(replyTo.indexOf("pid=") + 4,
						replyTo.indexOf("&ptid="));
			} else {
				replyTo = null;
			}
		}

		Elements eBody = ePost.select("td.t_msgfont");
		String[] niceBody;

		if (eBody.size() == 0) {
			niceBody = new String[]{"txt:blocked!"};
		} else {
			niceBody = postprocessPostBody(preprocessPostBody(eBody.get(0)), ePost.select("div.postattachlist"));
		}

		Element eUser = ePost.select("td.postauthor").get(0);
		Elements eUinfo = eUser.select("div.postinfo a");
		String userId = eUinfo.attr("href").substring("space.php?uid=".length());
		String userName = eUinfo.text();
		String userImg = eUser.select("div.avatar img").attr("src");

		User user = new User().setId(Integer.valueOf(userId)).setName(userName)
				.setImage(userImg);

		Post post = new Post().setId(Integer.valueOf(id)).setNiceBody(niceBody)
				.setAuthor(user);
		if (replyTo != null) {
			post.setReplyTo(Integer.valueOf(replyTo));
		}

		return post;
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

				if (tag.equals("br")) {
					sb.append("\r\n");
				} else if (tag.equals("img")) {
					String src = e.attr("src");

					if (iconKeys.contains(src)) {
						sb.append(icons.get(src));
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

		return temps.toArray(new String[0]);
	}

	public static void sendReply(int tid, String content, ArrayList<Integer> attachIds, final OnRequestListener onRequestListener) {
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

		httpClient
				.post("http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes",
						params, new AsyncHttpResponseHandler() {

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
								onRequestListener.onError(new String(responseBody));
							}

							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								onRequestListener.onSuccess(new String(responseBody));
							}

						});
	}

	public static void search(String word, final OnThreadsListener onThreadsListenerListener) {
		try {
			getHtml("http://www.hi-pda.com/forum/search.php?srchtxt=" + URLEncoder.encode(word, "GBK")
					+ "&srchtype=title&"
					+ "searchsubmit=true&"
					+ "st=on&"
					+ "srchuname=&"
					+ "srchfilter=all&"
					+ "srchfrom=0&"
					+ "before=&"
					+ "orderby=lastpost&"
					+ "ascdesc=desc&"
					+ "srchfid%5B0%5D=all", new OnRequestListener() {

				@Override
				public void onError(String error) {

				}

				@Override
				public void onSuccess(String html) {
					parseThreads(html, onThreadsListenerListener);
				}

			});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}



	public static ArrayList<Thread> parseThreads(String html, OnThreadsListener onThreadsListener) {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		Document doc = getDoc(html);

		Elements eThreads = doc.select("body#search").size() == 0 ? doc.select("tbody[id^=normalthread_]") : doc.select("div.searchlist tbody");

		for (Element eThread : eThreads) {
			threads.add(toThreadObj(eThread));
		}


		int currPage = 1;

		Elements page = doc.select("div.pages > strong");

		if (page.size() > 0) {
			currPage = Integer.valueOf(page.first().text());
		}

		Elements nextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]");

		if (onThreadsListener != null) {
			onThreadsListener.onThreads(threads, currPage, nextPage.size() > 0);
		}

		return threads;
	}

	public static ArrayList<Thread> parseThreads(String html) {
		return parseThreads(html, null);
	}

	private static Thread toThreadObj(Element eThread) {
		Elements eLastPost = eThread.select("td.lastpost em a");
		String lastHref = eLastPost.attr("href");
		String id = lastHref.substring(lastHref.indexOf("tid=") + 4, lastHref.indexOf("&goto"));

		String title = eThread.select("th.subject span a, th.subject a").first().text();
		boolean isNew = eThread.select("th.subject").hasClass("new");
		Elements eUser = eThread.select("td.author a");
		String userName = eUser.text();
		String userId = eUser.attr("href").substring("space.php?uid=".length());
		String commentNum = eThread.select("td.nums > strong").text().trim();

		User user = new User().setId(Integer.valueOf(userId)).setName(userName);
		Thread ret = new Thread();
		ret.setId(Integer.valueOf(id)).setTitle(title).setNew(isNew)
				.setCommentCount(Integer.valueOf(commentNum)).setAuthor(user);
		return ret;
	}

	public interface OnUserListener{
		void onUser(User u);
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

	public static void getFavorites(final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=favorites&type=thread", new OnRequestListener() {
			@Override
			public void onError(String error) {

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

	public static void getAlerts(final OnPostsListener onPostsListener) {
		getHtml("http://www.hi-pda.com/forum/notice.php", new OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements eNotices = doc.select("ul.feed > li.s_clear > div");
				ArrayList<Post> posts = new ArrayList<Post>();

				for (Element eNotice : eNotices) {
					String title = eNotice.select(">a").last().text();
					Elements eSummary = eNotice.select(">dl.summary");
					String body = eSummary.select("dt").last().text() + eSummary.select("dd").last().text();
					String findPostLink = eNotice.select(">p>a").last().attr("href");
					int ptidIndex = findPostLink.indexOf("&ptid=");
					String tid = findPostLink.substring(ptidIndex + 6);
					String pid = findPostLink.substring(findPostLink.indexOf("pid=") + 4, ptidIndex);
					Post post = new Post().setId(Integer.valueOf(pid))
							.setTid(Integer.valueOf(tid))
							.setTitle(title).setBody(body);
					posts.add(post);
				}

				int currPage = 1;
				Elements page = doc.select("div.pages > strong");

				if (page.size() > 0) {
					currPage = Integer.valueOf(page.first().text());
				}

				boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

				onPostsListener.onPosts(posts, currPage, hasNextPage);
			}
		});
	}

	public static void getMessages(final OnThreadsListener onThreadsListenerListener) {
		getHtml("http://www.hi-pda.com/forum/pm.php?filter=privatepm", new OnRequestListener() {
			@Override
			public void onError(String error) {

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

				onThreadsListenerListener.onThreads(threads, currPage, hasNextPage);
			}
		});
	}

	public static void getMyThreads(final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=threads", new OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Document doc = getDoc(html);

				Elements eThreads = doc.select("div.threadlist tbody tr");
				ArrayList<Thread> threads = new ArrayList<Thread>();

				for (Element eThread : eThreads) {
					Elements eTitle = eThread.select("th a");

					if (eTitle.size() > 0) {
						String href = eTitle.attr("href");
						String id = href.substring(href.indexOf("tid=") + 4);
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

	public static void getUserThreadsAtPage(int uid, int page, OnThreadsListener onThreadsListener) {
		String url = "http://www.hi-pda.com/forum/search.php?srchuid=" + uid + "&srchfid=all&srchfrom=0&searchsubmit=yes";

		getThreadsByUrl(url, onThreadsListener);
	}

	public static void getMyPosts(final OnThreadsListener onThreadsListener) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=posts", new OnRequestListener() {
			@Override
			public void onError(String error) {

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

	public interface OnPostsListener {
		void onPosts(ArrayList<Post> posts, int currPage, boolean hasNextPage);
	}

	public interface OnThreadsListener {
		void onThreads(ArrayList<Thread> threads, int currPage, boolean hasNextPage);
	}

	public static void getThreadsByUrl(String url, final OnThreadsListener onThreadsListener) {
		getHtml(url, new OnRequestListener() {

			@Override
			public void onError(String error) {
				onThreadsListener.onThreads(null, 1, false);
			}

			@Override
			public void onSuccess(String html) {
				parseThreads(html, onThreadsListener);
			}
		});
	}

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

	public interface OnImageCompressed {
		void onImage(File imageFile);
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
}
