package com.ladjzero.uzlee.api;

import android.content.Context;

import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.parsers.EditablePostParser;
import com.ladjzero.hipda.parsers.ExistedAttachParser;
import com.ladjzero.hipda.parsers.FavoritesParser;
import com.ladjzero.hipda.parsers.JsonParser;
import com.ladjzero.hipda.parsers.LoggingParser;
import com.ladjzero.hipda.parsers.MarkedThreadsParser;
import com.ladjzero.hipda.parsers.MentionsParser;
import com.ladjzero.hipda.parsers.NullParser;
import com.ladjzero.hipda.parsers.OwnPostsParser;
import com.ladjzero.hipda.parsers.OwnThreadsParser;
import com.ladjzero.hipda.parsers.Parsable;
import com.ladjzero.hipda.parsers.Parser;
import com.ladjzero.hipda.parsers.ParserMatcher;
import com.ladjzero.hipda.parsers.PostEditingParser;
import com.ladjzero.hipda.parsers.PostsParser;
import com.ladjzero.hipda.parsers.RawMessagesParser;
import com.ladjzero.hipda.parsers.ThreadsParser;
import com.ladjzero.hipda.parsers.UserParser;
import com.ladjzero.uzlee.model.VersionsParser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public class Api extends HttpClientApi {
	private static Api singleton;
	private List<Parsable> mParsers;
	private Response.Meta mMeta;
	private List<Interceptor> mInterceptors = new ArrayList<>();

	public void setMode(Mode mMode) {
		this.mMode = mMode;
	}

	private Mode mMode = Mode.REMOTE;

	@Override
	public Parsable getParserByUrl(final String urlPattern) {
		if (mMode == Mode.REMOTE) {
			return new JsonParser();
		}

		Object parser = CollectionUtils.find(mParsers, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return ((ParserMatcher) o).test(urlPattern);
			}
		});

		if (parser == null) {
			return new NullParser();
		}

		return (Parsable) parser;
	}

	public enum Mode{ REMOTE, LOCAL }

	@Override
	Response.Meta getMeta() {
		return mMeta;
	}

	@Override
	String getCode() {
		if (mMeta == null) {
			return Parser.CODE_GBK;
		}

		String code = mMeta.getCode();

		if (code == null) {
			return Parser.CODE_GBK;
		}

		return code;
	}

	private Api(Context context) {
		super(context);

		mParsers = Arrays.asList(new Parsable[]{
						new EditablePostParser(),
						new ExistedAttachParser(),
						new FavoritesParser(),
						new LoggingParser(),
						new MarkedThreadsParser(),
						new MentionsParser(),
						new OwnPostsParser(),
						new OwnThreadsParser(),
						new PostEditingParser(),
						new PostsParser(),
						new RawMessagesParser(),
						new ThreadsParser(),
						new UserParser(),
				}
		);

		intercept(new Interceptor() {
			@Override
			public Response intercept(Response res) {
				Response.Meta meta = res.getMeta();

				if (meta != null) {
					mMeta = meta;
				}

				return res;
			}
		});
	}

	@Override
	public List<Interceptor> getInterceptors() {
		return mInterceptors;
	}

	@Override
	public void intercept(Interceptor i) {
		mInterceptors.add(i);
	}

	public static Api getApi(Context context) {
		if (singleton == null) {
			singleton = new Api(context);
		}

		return singleton;
	}

	public void getVersions(OnRespondCallback onRespondCallback) {
		get(
				"http://ladjzero.github.io/uzlee/js/version.json",
				"utf-8",
				new ApiCallback(this, new VersionsParser(), onRespondCallback));
	}
}
