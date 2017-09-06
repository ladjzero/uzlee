package com.ladjzero.uzlee.api;

import com.ladjzero.hipda.api.Response;

import java.lang.reflect.Field;

/**
 * Created by chenzhuo on 9/6/17.
 */
public class Utils {
    public static Response.Meta smartMergeMeta(Response.Meta meta1, Response.Meta meta2) {
        if (null == meta2) {
            return meta1;
        }

        Field[] fields = Response.Meta.class.getDeclaredFields();
        Response.Meta ret = new Response.Meta();

        for (Field f : fields) {
            try {
                Object meta2FieldValue = f.get(meta2);
                f.set(ret, null == meta2FieldValue ? meta2FieldValue : f.get(meta1));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
}
