package com.ladjzero.hipda;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenzhuo on 16-2-11.
 */
public class ThreadsParser extends Parser {

	private static final Pattern COLOR_REG = Pattern.compile("#(\\d|[A-F])+");

	public Threads parseThreads(String html, boolean showFixedThreads) {
		Threads threads = new Threads();
		Document doc = getDoc(html);

		String selectStr = showFixedThreads ? "tbody[id^=normalthread_],tbody[id^=stickthread_" : "tbody[id^=normalthread_]";

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

		threads.getMeta().setHasNextPage(nextPage.size() > 0);
		threads.getMeta().setPage(currPage);

		return threads;
	}

	public Threads parseMessages(String html) {
		Document doc = getDoc(html);

		Elements pms = doc.select("ul.pm_list li.s_clear");
		Threads threads = new Threads();

		for (Element pm : pms) {
			try {
				Elements eUser = pm.select("p.cite a");
				String userName = eUser.text();
				String userLink = eUser.attr("href");
				String uid = Utils.getUriQueryParameter(userLink).get("uid");

				User u = new User().setId(Integer.valueOf(uid)).setName(userName);

				String title = pm.select("div.summary").text();
				boolean isNew = pm.select("img[alt=NEW]").size() != 0;
				String dateStr = ((TextNode) pm.select("p.cite").get(0).childNode(2)).text().replaceAll("\u00a0", "");

				Thread thread = new Thread().setTitle(title).setAuthor(u).setNew(isNew).setDateStr(dateStr);
				threads.add(thread);
			} catch (Exception e) {
				e.printStackTrace();
//				Logger.e("Can not parse user in PMs, pm: %s", pm.html());
			}
		}

		int currPage = 1;
		Elements page = doc.select("div.pages > strong");

		if (page.size() > 0) {
			currPage = Integer.valueOf(page.first().text());
		}

		boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;
		threads.getMeta().setHasNextPage(hasNextPage);
		threads.getMeta().setPage(currPage);

		return threads;
	}

	public Threads parseOwnPosts(String html) {
		Document doc = getDoc(html);

		Elements eThreads = doc.select("div.threadlist tbody tr");
		Threads threads = new Threads();

		for (int i = 0; i < eThreads.size(); i += 2) {
			Elements eTitle = eThreads.get(i).select("th a");

			if (eTitle.size() > 0) {
				String href = eTitle.attr("href");
				Map<String, String> params = Utils.getUriQueryParameter(href);
				String id = params.get("ptid");
				String pid = params.get("pid");
				String title = eTitle.text();
				String body = eThreads.get(i + 1).select("th.lighttxt").text().trim();
				String forumStr = eThreads.get(i).select("td.forum > a").attr("href");
				String fid = Utils.getUriQueryParameter(forumStr).get("fid");

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

		threads.getMeta().setPage(currPage);
		threads.getMeta().setHasNextPage(hasNextPage);

		return threads;
	}

	public Threads parseOwnThreads(String html) {
		Document doc = getDoc(html);

		Elements eThreads = doc.select("div.threadlist tbody tr");
		Threads threads = new Threads();

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
		threads.getMeta().setHasNextPage(hasNextPage);
		threads.getMeta().setPage(currPage);

		return threads;
	}

	public Threads parseMarkedThreads(String html) {
		Document doc = getDoc(html);

		Elements eThreads = doc.select("form[method=post] tbody tr");
		Threads threads = new Threads();

		for (Element eThread : eThreads) {
			Elements eTitle = eThread.select("th a");

			if (eTitle.size() > 0) {
				String href = eTitle.attr("href");
				String id = href.substring(href.indexOf("tid=") + 4, href.indexOf("&from"));
				String title = eTitle.text();
				String forumStr = eThread.select("td.forum > a").attr("href");
				String fid = Utils.getUriQueryParameter(forumStr).get("fid");
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
		threads.getMeta().setHasNextPage(hasNextPage);
		threads.getMeta().setPage(currPage);

		return threads;
	}

	private Thread toThreadObj(Element eThread) {
		Elements eSubject = eThread.select("th.subject");
		Elements eLastPost = eThread.select("td.lastpost em a");
		String lastHref = eLastPost.attr("href");
		String style;
		String type = eSubject.select("em > a[href^=forumdisplay.php]").text();


		if (lastHref != null && lastHref.length() > 0) {
			String id = Utils.getUriQueryParameter(lastHref).get("tid");

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

			String userId = Utils.getUriQueryParameter(userHref).get("uid");
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

			Matcher matcher = COLOR_REG.matcher(style);
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
}
