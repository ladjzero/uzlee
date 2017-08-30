package com.ladjzero.uzlee.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by chenzhuo on 8/30/17.
 */
public class Json {
    private static Gson mGson = new Gson();

    public static String toJson(Object src) {
        return mGson.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return mGson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type T) {
        return mGson.fromJson(json, T);
    }
}
