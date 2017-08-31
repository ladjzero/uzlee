package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;

import java.util.List;
import java.util.Map;

/**
 * Created by chenzhuo on 8/31/17.
 */
public class FavoritesParser extends Parser {
    @Override
    public Response parse(String html) {
        Response res = new Response();

        if (html.contains("此主题已成功添加到收藏夹中")) {
            res.setData("收藏成功");
        } else if (html.contains("您曾经收藏过这个主题")) {
            res.setData("已经收藏过该主题");
        } else if (html.contains("此主题已成功从您的收藏夹中移除")) {
            res.setData("移除成功");
        } else {
            res.setData("操作失败");
        }

        return res;
    }

    @Override
    boolean test(List<String> paths, Map<String, String> query) {
        String inajax = query.get("inajax");
        String item = query.get("item");

        return paths.contains("my.php") && "1".equals(inajax) && "favorites".equals(item);
    }
}
