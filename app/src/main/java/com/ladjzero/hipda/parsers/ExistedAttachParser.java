package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.Tuple;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

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
}
