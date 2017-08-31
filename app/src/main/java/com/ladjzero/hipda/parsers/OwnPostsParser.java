package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.entities.Thread;
import com.ladjzero.hipda.entities.Threads;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class OwnPostsParser extends Parser {
    @Override
    public Response parse(String html) {
        Response res = new Response();
        Tuple<Document, Response.Meta> tuple = getDoc(html);
        Document doc = tuple.x;
        Response.Meta meta = tuple.y;
        res.setMeta(meta);


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

        res.setData(threads);
        return res;
    }

    @Override
    boolean test(List<String> paths, Map<String, String> query) {
        String item = query.get("item");

        return paths.contains("pm.php") && "posts".equals(item);
    }
}
