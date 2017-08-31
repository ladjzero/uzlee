package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.entities.Post;
import com.ladjzero.hipda.api.Response;

import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Map;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class EditablePostParser extends Parser{
    @Override
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

    @Override
    boolean test(List<String> paths, Map<String, String> query) {
        String editsubmit = query.get("editsubmit");
        String action = query.get("action");

        return paths.contains("post.php") && "edit".equals(action) && editsubmit == null;
    }
}
