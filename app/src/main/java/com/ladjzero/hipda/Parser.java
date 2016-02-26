package com.ladjzero.hipda;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by chenzhuo on 16-2-11.
 */
public class Parser {
	public static final String CODE_GBK = "GBK";
	public static final String CODE_UTF8 = "UTF-8";

	private static final String STATS = "论坛统计";
	private String mCode = CODE_GBK;
	private ApiStore mStore;

	public Parser() {
		mStore = ApiStore.getStore();
	}

	public Document getDoc(String html) {
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

			mStore.setUnread(msgCount);

			Elements eUser = doc.select("#umenu > cite > a");
			String uidHref = eUser.attr("href");
			String uid = Utils.getUriQueryParameter(uidHref).get("uid");

			if (uid != null && uid.length() > 0) {
				int id = Integer.valueOf(uid);
				String name = eUser.text().trim();

				saveUser(id, name);

			} else {
				saveUser(0, "");
			}
		} catch (Error e) {
//			Logger.e(TAG, e.toString());
		}

		Elements formHashInput = doc.select("input[name=formhash]");

		if (formHashInput.size() > 0) {
			mStore.setFormhash(formHashInput.val());
		}

		Elements hashInput = doc.select("input[name=hash]");

		if (hashInput.size() > 0) {
			mStore.setHash(hashInput.val());
		}

		String stats = doc.select("#footlink a[href=stats.php]").text();

		if (!stats.equals(STATS)) mCode = mCode.equals(CODE_GBK) ? CODE_UTF8 : CODE_GBK;

//		Logger.i("%d ms", System.currentTimeMillis() - time);
		return doc;
	}

	public String[] parseExistedAttach(String html) {
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

		return attachIds.toArray(new String[0]);
	}

	private void saveUser(int id, String name) {
		mStore.setUser(new User().setId(id).setName(name));
	}
}
