package com.ladjzero.hipda.parsers;

import com.alibaba.fastjson.JSON;
import com.ladjzero.hipda.Response;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class JsonParser extends Parser {
    @Override
    public Response parse(String json) {
        return JSON.parseObject(json, Response.class);
    }
}
