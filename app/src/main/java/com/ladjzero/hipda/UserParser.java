package com.ladjzero.hipda;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by chenzhuo on 16-2-12.
 */
public class UserParser extends Parser {

	public Response parse(String html) {
		Response.Meta resMeta = new Response.Meta();
		Document doc = getDoc(html, resMeta);

		String img = doc.select("div.avatar>img").attr("src");
		String uidLink = doc.select("li.searchpost a").attr("href");
		String uid = Utils.getUriQueryParameter(uidLink).get("srchuid");
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

		User user = new User()
				.setId(Integer.valueOf(uid))
				.setImage(img)
				.setName(name)
				.setRegisterDateStr(registerDate)
				.setQq(qq)
				.setSex(sex)
				.setPoints(point)
				.setLevel(level)
				.setTotalThreads(totalThreads);

		Response res = new Response();
		res.setMeta(resMeta);
		res.setData(res);
		res.setSuccess(true);
		return res;
	}
}
