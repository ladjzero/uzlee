package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;

import java.util.List;
import java.util.Map;

/**
 * Created by chenzhuo on 8/31/17.
 */
public class LoggingParser extends Parser {
    @Override
    public Response parse(String html) {
        Response res = new Response();

        if (html.contains("欢迎您回来")) {
            return res;
        } else if (html.contains("密码错误次数过多，请 15 分钟后重新登录")) {
            res.setSuccess(false);
            res.setData("密码错误次数过多，请 15 分钟后重新登录");
        } else {
            res.setSuccess(false);
            res.setData("登录错误");
        }

        return res;
    }

    @Override
    boolean test(List<String> paths, Map<String, String> query) {
        String action = query.get("action");

        return paths.contains("logging.php") && "login".equals(action);
    }
}
