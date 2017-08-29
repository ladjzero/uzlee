package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.entities.Thread;
import com.ladjzero.hipda.entities.Threads;
import com.ladjzero.hipda.entities.User;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class MessagesParser extends Parser {
    public Response parse(String html) {
        Response res = new Response();
        Tuple<Document, Response.Meta> tuple = getDoc(html);
        Document doc = tuple.x;
        Response.Meta meta = tuple.y;
        res.setMeta(meta);

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

        res.setData(threads);

        return res;
    }
}
