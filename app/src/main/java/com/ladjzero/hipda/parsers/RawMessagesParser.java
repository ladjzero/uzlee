package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class RawMessagesParser extends Parser {
    @Override
    public Response parse(String html) {
        Response res = new Response();
        res.setData(parseMessagesToHtml(html));
        return res;
    }

    public String parseMessagesToHtml(String html) {
        Tuple<Document, Response.Meta> tuple = getDoc(html);
        Document doc = tuple.x;
        Element ePosts = doc.select("#pmlist > .pm_list").first();
        Elements avatars = ePosts.select("a.avatar > img");

        for (Element avatar : avatars) {
            String src = avatar.attr("src");
            avatar.attr("src", src.replaceAll("_avatar_small", "_avatar_middle"));
        }

        return ePosts == null ? "" : ePosts.outerHtml();
    }

    @Override
    boolean test(List<String> paths, Map<String, String> query) {
        String filter = query.get("filter");
        String uid = query.get("uid");

        return paths.contains("pm.php") && "privatepm".equals(filter) && uid != null;
    }
}
