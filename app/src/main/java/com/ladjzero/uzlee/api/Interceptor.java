package com.ladjzero.uzlee.api;

import com.ladjzero.hipda.api.Response;

/**
 * Created by chenzhuo on 9/6/17.
 */
public interface Interceptor {
    Response intercept(Response res);
}
