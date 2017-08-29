package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.entities.Post;
import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.Tuple;

import org.jsoup.nodes.Document;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class EditablePostParser extends Parser{
    public Response parse(String html) {
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
}
