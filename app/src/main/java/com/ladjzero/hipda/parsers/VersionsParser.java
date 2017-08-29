package com.ladjzero.hipda.parsers;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.Response;
import com.ladjzero.uzlee.model.Version;

import java.util.List;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class VersionsParser extends Parser {
    public Response parse(String json) {
        Response res = new Response();
        List<Version> info = null;

        try {
            info = JSON.parseArray(json, Version.class);
            res.setData(info);
        } catch (Exception e) {
            e.printStackTrace();
            res.setSuccess(false);
            res.setData(e);
        }

        return res;
    }

}
