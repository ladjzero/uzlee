package com.ladjzero.uzlee.api;

import android.os.AsyncTask;

import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.parsers.Parsable;

/**
 * Created by chenzhuo on 8/29/17.
 */
class ApiCallback implements HttpClientCallback {
    private HttpClientApi api;
    private Parsable p;
    private OnRespondCallback onRespondCallback;

    public ApiCallback(HttpClientApi api, Parsable p, OnRespondCallback onRespondCallback) {
        this.api = api;
        this.p = p;
        this.onRespondCallback = onRespondCallback;
    }

    @Override
    public void onSuccess(String response) {
        new AsyncTask<String, Void, Object>() {
            @Override
            protected Object doInBackground(String... strings) {
                return p.parse(strings[0]);
            }

            @Override
            protected void onPostExecute(Object o) {
                Response res = (Response) o;

                for (Interceptor i : api.getInterceptors()) {
                    res = i.intercept(res);
                }

                onRespondCallback.onRespond(res);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
    }

    @Override
    public void onFailure(String reason) {
        Response res = new Response();
        res.setSuccess(false);
        res.setData(reason);
        onRespondCallback.onRespond(res);
    }
}
