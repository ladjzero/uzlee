package com.ladjzero.hipda;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.Map;

/**
 * Created by chenzhuo on 16-2-11.
 */
public class PostsParser extends Parser {

	private static Post toPostObj(Element ePost) {
		int idPrefixLength = "pid".length();

		Post post = new Post();

		String id = ePost.attr("id").substring(idPrefixLength);
		Elements eBody = ePost.select("td.t_msgfont").tagName("div");

		if (eBody.size() != 0) {
			replaceQuoteLink(eBody.get(0));
			findSig(eBody.get(0));

			Elements imgPlaceHolders = eBody.select("span[id^=attach_]");
			if (imgPlaceHolders.select("> img").size() > 0) imgPlaceHolders.remove();

			Elements imgDownloadLinks = eBody.select("div.t_attach");
			imgDownloadLinks.remove();

			Elements newBody = new Elements();
			newBody.addAll(eBody);

			Elements attaches = ePost.select(".postattachlist");

			if (attaches.size() > 0) {
				Elements attachImgs = attaches.select(".attachimg p>img");
				newBody.addAll(attachImgs);

				Elements otherAttaches = attaches.select(".attachname");
				newBody.addAll(otherAttaches);
			}


			for (Element img : newBody.select("img")) {
				String src = img.attr("file");

				if (src.length() == 0) src = img.attr("src");

				if (!src.startsWith("images/smilies/") &&
						!src.endsWith("common/back.gif") &&
						!src.endsWith("default/attachimg.gif") &&
						!img.attr("width").equals("16")) {
					img.addClass("content-image");
				}

				img.removeAttr("file")
						.removeAttr("width")
						.removeAttr("height")
						.removeAttr("onclick")
						.removeAttr("onload")
						.removeAttr("onmouseover");

				if (!src.startsWith("http")) {
					img.attr("src", "http://www.hi-pda.com/forum/" + src);
				}
			}

			for (Element a : newBody.select("a")) {
				String href = a.attr("href");

				if (!href.startsWith("http")) {
					a.attr("href", "http://www.hi-pda.com/forum/" + href);
				}
			}

			Elements styles = newBody.select("[style]");

			for (Element style : styles) {
				style.removeAttr("style");
			}

			post.setBody(newBody.outerHtml());
		} else {
			post.setBody("<div class=\"error\">作者被禁止或删除</div>");
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
		String userId = Utils.getUriQueryParameter(url).get("uid");
		String userName = eUinfo.text();

		User user = new User().setId(Integer.valueOf(userId)).setName(userName);

		post.setId(Integer.valueOf(id))/*.setNiceBody(niceBody)*/
				.setAuthor(user).setTimeStr(timeStr).setPostIndex(Integer.valueOf(postIndex));

		return post;
	}

	private static void replaceQuoteLink(Element eBody) {
		Elements quoteArrowIcon = eBody.select("blockquote > font[size=2] > a[href^=http://www.hi-pda.com/forum/redirect.php?goto=findpost]");

		if (quoteArrowIcon.size() > 0) {
			quoteArrowIcon.get(0).html("查看");
		}
	}

	private static void findSig(Element eBody) {
		Elements children = eBody.children();

		if (children.size() > 0) {
			Element lastChild = children.last();

			if (lastChild.tagName().equalsIgnoreCase("FONT") && lastChild.attr("size").equals("1")) {
				lastChild.addClass("sig");

				if (lastChild.select("font[color=Gray]").size() > 0) {
					lastChild.addClass("sig-uzlee");
				} else {
					lastChild.addClass("sig-ios");
				}
			} else if (lastChild.tagName().equalsIgnoreCase("A") && lastChild.select("font[size=1]").size() > 0) {
				lastChild.addClass("sig").addClass("sig-android");
			} else if (lastChild.tagName().equalsIgnoreCase("IMG") && lastChild.attr("width").equals("16") && lastChild.attr("height").equals("16")) {
				Element newChild = new Element(Tag.valueOf("span"), lastChild.baseUri());
				lastChild.remove();
				newChild.appendChild(lastChild);
				newChild.addClass("sig").addClass("sig-wp");
				eBody.appendChild(newChild);
			}
		}
	}

	public Post parseEditablePost(String html) {
		Document doc = getDoc(html);
		String title = doc.select("#subject").val();
		String editBody = doc.select("#e_textarea").text();

		return new Post().setTitle(title).setBody(editBody);
	}

	public Posts parsePosts(String html) {
		if (mProgressReporter != null && mProgressReporter.isCancelled()) return null;

		Posts posts = new Posts();
		Document doc = getDoc(html);

		if (mProgressReporter != null && mProgressReporter.isCancelled()) return null;


		Element eFid = doc.select("#nav a").last();
		String fidStr = eFid.attr("href");

		int fid = -1;

		try {
			fid = Integer.valueOf(Utils.getUriQueryParameter(fidStr).get("fid"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String title = eFid.nextSibling().toString().replaceAll(" » ", "");

		Elements pages = doc.select("div.pages");
		int totalPage = 1;

		if (pages.size() == 2) {
			Elements lastPage = pages.select("a.last");
			if (lastPage.size() > 0) {
				try {
					totalPage = Integer.valueOf(Utils.getUriQueryParameter(lastPage.attr("href")).get("page"));
				} catch (Exception e) {

				}
			} else {
				lastPage = pages.select("a:not(.next)");

				if (lastPage.size() > 0) {
					try {
						totalPage = Integer.valueOf(Utils.getUriQueryParameter(lastPage.last().attr("href")).get("page"));
					} catch (Exception e) {

					}
				}
			}
		}

		Elements ePosts = doc.select("table[id^=pid]");

		int i = 0;

		for (Element ePost : ePosts) {
			Post post;
			posts.add(post = toPostObj(ePost));

			if (mProgressReporter != null) mProgressReporter.onProgress(++i, ePosts.size(), post);
			if (mProgressReporter != null && mProgressReporter.isCancelled()) return null;
		}

		int currPage = 1;
		Elements page = doc.select("div.pages > strong");

		if (page.size() > 0) {
			currPage = Integer.valueOf(page.first().text());
		}

		boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

		Posts.Meta meta = posts.getMeta();
		meta.setHasNextPage(hasNextPage);
		meta.setPage(currPage);
		meta.setTotalPage(Math.max(totalPage, currPage));
		meta.setFid(fid);
		meta.setTitle(title);

		return posts;
	}

	public Posts parseMentions(String html) {
		Posts mentions = new Posts();

		try {
			Document doc = getDoc(html);

			Elements eNotices = doc.select("ul.feed > li.s_clear > div");

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

					fid = Utils.getUriQueryParameter(viewPostLink).get("tid");
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

				Map<String, String> params = Utils.getUriQueryParameter(findPostLink);

				tid = params.get("ptid");
				pid = params.get("pid");
				if (tid == null) tid = "0";
				if (pid == null) pid = "0";

				Post post = new Post().setId(Integer.valueOf(pid))
						.setTid(Integer.valueOf(tid))
						.setFid(Integer.valueOf(fid))
						.setTitle(title).setBody(body);
				mentions.add(post);
			}

			int currPage = 1;
			Elements page = doc.select("div.pages > strong");

			if (page.size() > 0) {
				currPage = Integer.valueOf(page.first().text());
			}

			boolean hasNextPage = doc.select("div.pages > a[href$=&page=" + (currPage + 1) + "]").size() > 0;

			// TO-DO
			Posts.Meta meta = mentions.getMeta();
			meta.setHasNextPage(hasNextPage);
			meta.setPage(currPage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mentions;
	}

	public String parseMessagesToHtml(String html) {
		Posts posts = new Posts();
		Document doc = getDoc(html);
		Element ePosts = doc.select("#pmlist > .pm_list").first();
		Elements avatars = ePosts.select("a.avatar > img");

		for (Element avatar : avatars) {
			String src = avatar.attr("src");
			avatar.attr("src", src.replaceAll("_avatar_small", "_avatar_middle"));
		}

		return ePosts == null ? "" : ePosts.outerHtml();
	}
}
