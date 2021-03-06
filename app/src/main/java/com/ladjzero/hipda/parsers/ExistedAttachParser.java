package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class ExistedAttachParser extends Parser{
    @Override
    public Response parse(String html) {
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

    @Override
    boolean test(List<String> paths, Map<String, String> query) {
        String action = query.get("action");
        String topicsubmit = query.get("topicsubmit");

        return paths.contains("post.php") && "newthread".equals(action) && !"yes".equals(topicsubmit);
    }
}
