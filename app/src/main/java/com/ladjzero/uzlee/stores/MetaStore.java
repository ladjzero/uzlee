package com.ladjzero.uzlee.stores;

import android.content.Context;

import com.ladjzero.hipda.api.Response;
import com.ladjzero.uzlee.App;
import com.ladjzero.uzlee.api.Api;
import com.ladjzero.uzlee.api.Interceptor;
import com.orhanobut.logger.Logger;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.functions.Action;

/**
 * Created by chenzhuo on 8/31/17.
 */
public class MetaStore {
    static private Response.Meta mMeta;

    public static Response.Meta getMeta() {
        return mMeta == null ? new Response.Meta() : mMeta;
    }

    public static Observable<Response.Meta> getObservable() {
        return Observable.create(new ObservableOnSubscribe<Response.Meta>() {
            @Override
            public void subscribe(final ObservableEmitter<Response.Meta> e) throws Exception {
                App.getInstance().getApi().intercept(new Interceptor() {
                    @Override
                    public Response intercept(Response res) {
                        if (e.isDisposed()) {

                            App.getInstance().getApi().unIntercept(this);
                        } else if (res.getMeta() != null) {
                            e.onNext(res.getMeta());
                        }

                        return res;
                    }
                });
            }
        });
    }

    public static void init(Context context) {
        App.getInstance().getApi().intercept(new Interceptor() {
            @Override
            public Response intercept(Response res) {
                if (res.getMeta() != null) {
                    mMeta = res.getMeta();
                }

                Logger.d("MetaStore.meta: " + mMeta);
                return res;
            }
        });
    }
}
