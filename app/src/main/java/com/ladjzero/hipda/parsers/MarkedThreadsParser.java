package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.entities.Thread;
import com.ladjzero.hipda.entities.Threads;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class MarkedThreadsParser extends Parser {
    @Override
    public Response parse(String html) {
        Response res = new Response();
        Tuple<Document, Response.Meta> tuple = getDoc(html);
        Document doc = tuple.x;
        Response.Meta meta = tuple.y;
        res.setMeta(meta);

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

        res.setData(threads);
        return res;
    }

}
