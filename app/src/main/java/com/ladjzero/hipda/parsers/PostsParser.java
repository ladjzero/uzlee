package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.entities.Post;
import com.ladjzero.hipda.entities.Posts;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.Tuple;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.hipda.Utils;
import com.orhanobut.logger.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

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

			// hipda wraps an a element around image.
			for (Element a : newBody.select("a[href=javascript:;]")) {
				a.tagName("span");
			}

			for (Element img : newBody.select("img")) {
				String src = img.attr("file");

				if (src.length() == 0) src = img.attr("src");

				if (!src.contains("images/smilies/") &&
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
				} else {
					img.attr("src", src);
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

		User user = new User().setId(0).setName("");

		try {
			Element eUinfo = ePost.select("a[href^=space.php?uid=]").get(0);
			String url = eUinfo.attr("href");
			String userId = Utils.getUriQueryParameter(url).get("uid");
			String userName = eUinfo.text();

			user.setId(Integer.valueOf(userId)).setName(userName);
		} catch (Exception e) {

		}

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

	public Response parseEditablePost(String html) {
		Response res = new Response();
		Tuple<Document, Response.Meta> tuple = getDoc(html);
		Document doc = tuple.x;
		res.setMeta(tuple.y);

		String title = doc.select("#subject").val();
		String editBody = doc.select("#e_textarea").text();
		Post post =  new Post().setTitle(title).setBody(editBody);
		res.setData(post);
		return res;
	}

	private void replaceThumbImage(Document doc) {
		for (Element img : doc.select("img")) {
			String src = img.attr("src");

			try {
				Logger.i("111 - " + src + " - " + src.split(".thumb.jpg")[0]);
				img.attr("src", src.split(".thumb.jpg")[0]);
			} catch (Exception e) {
				Logger.e(e.toString());
			}
		}
	}

	@Override
	public Response parse(String html) {
		Response res = new Response();
		Posts posts = new Posts();
		Tuple<Document, Response.Meta> tuple = getDoc(html);
		Document doc = tuple.x;
		Response.Meta resMeta = tuple.y;
		res.setMeta(resMeta);

		replaceThumbImage(doc);


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
			posts.add(toPostObj(ePost));
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

		res.setData(posts);
		res.setSuccess(true);

		return res;
	}
}
