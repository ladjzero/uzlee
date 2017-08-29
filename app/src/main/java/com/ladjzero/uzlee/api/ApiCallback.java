package com.ladjzero.uzlee.api;

import android.os.AsyncTask;

import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.parsers.Parsable;

/**
 * Created by chenzhuo on 8/29/17.
 */
class ApiCallback implements HttpClientCallback {
    private Api api;
    private Parsable p;
    private OnRespondCallback onRespondCallback;

    public ApiCallback(Api api, Parsable p, OnRespondCallback onRespondCallback) {
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

                ApiStore store = api.getStore();
                Response.Meta meta = res.getMeta();

                if (meta != null) {
                    store.setUser(meta.getUser());
                    store.setCode(meta.getCode());
                    store.setFormhash(meta.getFormhash());
                    store.setHash(meta.getHash());
                    store.setUnread(meta.getUnread());
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
