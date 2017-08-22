package com.ladjzero.hipda;

import com.alibaba.fastjson.JSON;
import com.ladjzero.uzlee.model.Version;
import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhuo on 16-2-11.
 */
public abstract class Parser implements Parse {
	public static final String CODE_GBK = "GBK";
	public static final String CODE_UTF8 = "UTF-8";

	private static final String STATS = "论坛统计";
	private String mCode = CODE_GBK;

//	public Document getDoc(String html) {
//		return getDoc(html, new Response.Meta());
//	}

	public Tuple<Document, Response.Meta> getDoc(String html) {
		long time = System.currentTimeMillis();
		Response.Meta meta = new Response.Meta();

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

			meta.setUnread(msgCount);

			Elements eUser = doc.select("#umenu > cite > a");
			String uidHref = eUser.attr("href");
			String uid = Utils.getUriQueryParameter(uidHref).get("uid");

			if (uid != null && uid.length() > 0) {
				int id = Integer.valueOf(uid);
				String name = eUser.text().trim();

				meta.setUser(new User().setId(id).setName(name));
			} else {
				meta.setUser(new User());
			}
		} catch (Error e) {
			Logger.e("Parser", e.toString());
		}

		Elements formHashInput = doc.select("input[name=formhash]");

		if (formHashInput.size() > 0) {
			meta.setFormhash(formHashInput.val());
		}

		Elements hashInput = doc.select("input[name=hash]");

		if (hashInput.size() > 0) {
			meta.setHash(hashInput.val());
		}

		String stats = doc.select("#footlink a[href=stats.php]").text();

		if (!stats.equals(STATS)) mCode = mCode.equals(CODE_GBK) ? CODE_UTF8 : CODE_GBK;

		return new Tuple<>(doc, meta);
	}

	public Response parseExistedAttach(String html) {
		Response res = new Response();
		Tuple<Document, Response.Meta> tuple = getDoc(html);
		Document doc = tuple.x;
		Response.Meta meta = tuple.y;
		res.setMeta(meta);

		Elements tds = doc.select("td[id^=image_td_]");
		ArrayList<String> attachIds = new ArrayList<>();

		for (Element td : tds) {
			String id = td.id();
			id = id.substring("image_td_".length());

			try {
				attachIds.add(id);
			} catch (Exception e) {

			}
		}

		res.setMeta(meta);
		res.setData(attachIds.toArray(new String[0]));

		return res;
	}

	public Response parseVersions(String json) {
		Response res = new Response();
		List<Version> info = null;

		try {
			info = JSON.parseArray(json, Version.class);
			res.setData(info);
		} catch (Exception e) {
			e.printStackTrace();
			res.setSuccess(false);
			res.setData(e);
		}

		return res;
	}
}
