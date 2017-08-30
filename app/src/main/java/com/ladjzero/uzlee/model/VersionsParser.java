package com.ladjzero.uzlee.model;

import com.google.gson.reflect.TypeToken;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.parsers.Parser;
import com.ladjzero.uzlee.utils.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhuo on 8/29/17.
 */
public class VersionsParser extends Parser {
    @Override
    public Response parse(String json) {
        Response res = new Response();
        List<Version> info;

        try {
            info = Json.fromJson(json, new TypeToken<ArrayList<Version>>(){}.getType());
            res.setData(info);
        } catch (Exception e) {
            e.printStackTrace();
            res.setSuccess(false);
            res.setData(e);
        }

        return res;
    }

}
