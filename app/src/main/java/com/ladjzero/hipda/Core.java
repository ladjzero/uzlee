package com.ladjzero.hipda;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.ladjzero.hipda.cb.UserStatsCB;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;

public class Core {
	private static Core core;
	private PersistentCookieStore cookieStore;
	private static AsyncHttpClient httpClient = new AsyncHttpClient();
	private static ArrayList<MsgCB> msgCbs = new ArrayList<MsgCB>();

	static final int uidPrefixLength = "space.php?uid=".length();
	private static String formhash;

	public Core(Context context) {
		this.cookieStore = new PersistentCookieStore(context);
		httpClient.setCookieStore(cookieStore);
	}

	public static Core getInstance(Context context) {
		return core == null ? (core = new Core(context)) : core;
	}

	public static Date parseDate(String str) {
		try {
			return DateUtils.parseDate(str);
		} catch (DateParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void addOnMsgListener(MsgCB msgCb) {
		msgCbs.add(msgCb);
	}

	public static void removeOnMsgListener(MsgCB msgCb) {
		msgCbs.remove(msgCb);
	}

	public interface MsgCB {
		public void onMsg(int count);
	}

	public interface LoginCB {
		public void onSuccess();

		public void onFailure(String message);
	}

	public interface GetHtmlCB {
		public void onSuccess(String html);
	}

	public interface GetUserCB {
		public void onGet(User u);
	}

	public static void login(String username, String password, final LoginCB cb) {
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
							public void onFailure(int statusCode,
												  Header[] headers, byte[] responseBody,
												  Throwable error) {
								cb.onFailure("error");
							}

							@Override
							public void onSuccess(int statusCode,
												  Header[] headers, byte[] responseBody) {
								String html;
								try {
									html = new String(responseBody, "GBK");
									if (html.indexOf("欢迎您回来") > -1) {
										cb.onSuccess();
									} else if (html
											.indexOf("密码错误次数过多，请 15 分钟后重新登录") > -1) {
										cb.onFailure("密码错误次数过多，请 15 分钟后重新登录");
									} else {
										cb.onFailure("error");
									}
								} catch (UnsupportedEncodingException e) {
									cb.onFailure("error");
								}
							}

						});

	}

	public void logout() {

	}

	public static void getHtml(String url, final GetHtmlCB cb) {
		httpClient.get(url, new RequestParams(), new TextHttpResponseHandler(
				"GBK") {

			@Override
			public void onFailure(int arg0, Header[] arg1, String arg2,
								  Throwable arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, String arg2) {
				cb.onSuccess(arg2);
			}

		});
	}

	public static ArrayList<Post> parsePosts(String html) {
		ArrayList<Post> posts = new ArrayList<Post>();
		Document doc = Jsoup.parse(html);

		if (formhash == null) {
			formhash = doc.select("input[name=formhash]").val();
		}

		Elements ePosts = doc.select("table[id^=pid]");

		if (doc.select("a#myprompt.new").size() != 0) {
			for (MsgCB msgCb : msgCbs) {
				msgCb.onMsg(1);
			}
		}

		for (Element ePost : ePosts) {
			posts.add(toPostObj(ePost));
		}

		return posts;
	}

	private static Post toPostObj(Element ePost) {
		int idPrefixLength = "pid".length();

		String id = ePost.attr("id").substring(idPrefixLength);
		// �ظ�
		Elements eReply = ePost
				.select("strong a[href^=http://www.hi-pda.com/forum/redirect.php?goto=findpost]");
		// ����
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
		String[] niceBody = null;

		if (eBody.size() == 0) {
			niceBody = new String[]{"txt:blocked!"};
		} else {
			niceBody = postprocessPostBody(preprocessPostBody(eBody.get(0)));
		}

		Element eUser = ePost.select("td.postauthor").get(0);
		Elements eUinfo = eUser.select("div.postinfo a");
		String userId = eUinfo.attr("href").substring(uidPrefixLength);
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
		if (first != null && first.tagName().toLowerCase() == "strong") {
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
	 * @return
	 */
	private static String[] postprocessPostBody(Element eBody) {
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

				if (tag == "br") {
					sb.append("\r\n");
				} else if (tag == "img") {
					String src = e.attr("src");

					if (iconKeys.contains(src)) {
						sb.append(":{" + icons.get(src) + "}:");
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

		return temps.toArray(new String[0]);
	}

	public static void sendReply(int tid, String content) {
		RequestParams params = new RequestParams();
		params.setContentEncoding("GBK");
		params.put("formhash", formhash);
		params.put("subject", "");
		params.put("usesig", "0");
		params.put("message", content);

		httpClient
				.post("http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes&infloat=yes&handlekey=fastpost&inajax=1",
						params, new AsyncHttpResponseHandler() {

							@Override
							public void onFailure(int statusCode,
												  Header[] headers, byte[] responseBody,
												  Throwable error) {
							}

							@Override
							public void onSuccess(int statusCode,
												  Header[] headers, byte[] responseBody) {
							}

						});
	}

	public interface Search {
		void onSearch(ArrayList<Thread> threads);
	}

	public void search(String word, final Search s) {
		try {
			// 论坛接收GBK编码
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
					+ "srchfid%5B0%5D=all", new GetHtmlCB() {

				@Override
				public void onSuccess(String html) {
					s.onSearch(parseThreads(html));
				}

			});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			s.onSearch(null);
		}
	}

	public static ArrayList<Thread> parseThreads(String html) {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		Document doc = Jsoup.parse(html);

		if (doc.select("a#myprompt.new").size() != 0) {
			for (MsgCB msgCb : msgCbs) {
				msgCb.onMsg(1);
			}
		}

		Elements eThreads = doc.select("body#search").size() == 0 ? doc.select("tbody[id^=normalthread_]") : doc.select("div.searchlist tbody");

		for (Element eThread : eThreads) {
			threads.add(toThreadObj(eThread));
		}

		return threads;
	}

	private static Thread toThreadObj(Element eThread) {
		int idPrefixLength = "normalthread_".length();

		String id = eThread.id();
		if (id.length() != 0) {
			id = id.substring(idPrefixLength);
		} else {
			id = "-1";
		}

		String title = eThread.select("th.subject a").text();
		boolean isNew = eThread.select("th.subject").hasClass("new");
		Elements eUser = eThread.select("td.author a");
		String userName = eUser.text();
		String userId = eUser.attr("href").substring(uidPrefixLength);
		String commentNum = eThread.select("td.nums > strong").text().trim();

		User user = new User().setId(Integer.valueOf(userId)).setName(userName);
		Thread ret = new Thread();
		ret.setId(Integer.valueOf(id)).setTitle(title).setNew(isNew)
				.setCommentCount(Integer.valueOf(commentNum)).setAuthor(user);
		return ret;
	}

	public void getUserFromServer(int uid, final GetUserCB cb) {
		getHtml("http://www.hi-pda.com/forum/space.php?uid=" + uid,
				new GetHtmlCB() {

					@Override
					public void onSuccess(String html) {

					}

				});
	}

	public static void getFavorites(final OnThreads onThreads) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=favorites&type=thread", new GetHtmlCB() {
			@Override
			public void onSuccess(String html) {
				Document doc = Jsoup.parse(html);

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

				onThreads.onThreads(threads);
			}
		});
	}

	public static void getMyThreads(final OnThreads onThreads) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=threads", new GetHtmlCB() {
			@Override
			public void onSuccess(String html) {
				Document doc = Jsoup.parse(html);

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

				onThreads.onThreads(threads);
			}
		});
	}

	public static void getMyPosts(final OnThreads onThreads) {
		getHtml("http://www.hi-pda.com/forum/my.php?item=posts", new GetHtmlCB() {
			@Override
			public void onSuccess(String html) {
				Document doc = Jsoup.parse(html);

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

				onThreads.onThreads(threads);
			}
		});
	}

	public interface OnThreads {
		void onThreads(ArrayList<Thread> threads);
	}

	public void getThreadsByUrl(String url, final OnThreads onPostsCb,
								final UserStatsCB userCb) {
		final ArrayList<Thread> threads = new ArrayList<Thread>();

		getHtml(url, new GetHtmlCB() {

			@Override
			public void onSuccess(String html) {
				threads.addAll(parseThreads(html));

				onPostsCb.onThreads(threads);

				if (html.indexOf("您无权进行当前操作") > -1 || threads.size() == 0) {
					userCb.onOffline();
				}
			}
		});
	}

	public static final HashMap<String, String> icons = new HashMap<String, String>();
	public static final Set<String> iconKeys;
	public static final Collection<String> iconValues;

	static {
		icons.put("images/smilies/default/smile.gif", "smile");
		icons.put("images/smilies/default/sweat.gif", "sweat");
		icons.put("images/smilies/default/huffy.gif", "huffy");
		icons.put("images/smilies/default/cry.gif", "cry");
		icons.put("images/smilies/default/titter.gif", "titter");
		icons.put("images/smilies/default/handshake.gif", "handshake");
		icons.put("images/smilies/default/victory.gif", "victory");
		icons.put("images/smilies/default/curse.gif", "curse");
		icons.put("images/smilies/default/dizzy.gif", "dizzy");
		icons.put("images/smilies/default/shutup.gif", "shutup");
		icons.put("images/smilies/default/funk.gif", "funk");
		icons.put("images/smilies/default/loveliness.gif", "loveliness");
		icons.put("images/smilies/default/sad.gif", "sad");
		icons.put("images/smilies/default/biggrin.gif", "biggrin");
		icons.put("images/smilies/default/cool.gif", "cool");
		icons.put("images/smilies/default/mad.gif", "mad");
		icons.put("images/smilies/default/shocked.gif", "shocked");
		icons.put("images/smilies/default/tongue.gif", "tongue");
		icons.put("images/smilies/default/lol.gif", "lol");
		icons.put("images/smilies/default/shy.gif", "shy");
		icons.put("images/smilies/default/sleepy.gif", "sleepy");

		icons.put("images/smilies/coolmonkey/01.gif", "coolmonkey01");
		icons.put("images/smilies/coolmonkey/02.gif", "coolmonkey02");
		icons.put("images/smilies/coolmonkey/03.gif", "coolmonkey03");
		icons.put("images/smilies/coolmonkey/04.gif", "coolmonkey04");
		icons.put("images/smilies/coolmonkey/05.gif", "coolmonkey05");
		icons.put("images/smilies/coolmonkey/06.gif", "coolmonkey06");
		icons.put("images/smilies/coolmonkey/07.gif", "coolmonkey07");
		icons.put("images/smilies/coolmonkey/08.gif", "coolmonkey08");
		icons.put("images/smilies/coolmonkey/09.gif", "coolmonkey09");
		icons.put("images/smilies/coolmonkey/10.gif", "coolmonkey10");
		icons.put("images/smilies/coolmonkey/11.gif", "coolmonkey11");
		icons.put("images/smilies/coolmonkey/12.gif", "coolmonkey12");
		icons.put("images/smilies/coolmonkey/13.gif", "coolmonkey13");
		icons.put("images/smilies/coolmonkey/14.gif", "coolmonkey14");
		icons.put("images/smilies/coolmonkey/15.gif", "coolmonkey15");
		icons.put("images/smilies/coolmonkey/16.gif", "coolmonkey16");

		icons.put("images/smilies/grapeman/01.gif", "grapeman01");
		icons.put("images/smilies/grapeman/02.gif", "grapeman02");
		icons.put("images/smilies/grapeman/03.gif", "grapeman03");
		icons.put("images/smilies/grapeman/04.gif", "grapeman04");
		icons.put("images/smilies/grapeman/05.gif", "grapeman05");
		icons.put("images/smilies/grapeman/06.gif", "grapeman06");
		icons.put("images/smilies/grapeman/07.gif", "grapeman07");
		icons.put("images/smilies/grapeman/08.gif", "grapeman08");
		icons.put("images/smilies/grapeman/09.gif", "grapeman09");
		icons.put("images/smilies/grapeman/10.gif", "grapeman10");
		icons.put("images/smilies/grapeman/11.gif", "grapeman11");
		icons.put("images/smilies/grapeman/12.gif", "grapeman12");
		icons.put("images/smilies/grapeman/13.gif", "grapeman13");
		icons.put("images/smilies/grapeman/14.gif", "grapeman14");
		icons.put("images/smilies/grapeman/15.gif", "grapeman15");
		icons.put("images/smilies/grapeman/16.gif", "grapeman16");
		icons.put("images/smilies/grapeman/17.gif", "grapeman17");
		icons.put("images/smilies/grapeman/18.gif", "grapeman18");
		icons.put("images/smilies/grapeman/19.gif", "grapeman19");
		icons.put("images/smilies/grapeman/20.gif", "grapeman20");
		icons.put("images/smilies/grapeman/21.gif", "grapeman21");
		icons.put("images/smilies/grapeman/22.gif", "grapeman22");
		icons.put("images/smilies/grapeman/23.gif", "grapeman23");
		icons.put("images/smilies/grapeman/24.gif", "grapeman24");

		iconKeys = icons.keySet();
		iconValues = icons.values();
	}

}
