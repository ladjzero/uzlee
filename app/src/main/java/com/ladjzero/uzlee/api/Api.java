package com.ladjzero.uzlee.api;

import android.content.Context;

import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.parsers.Parsable;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.uzlee.model.VersionsParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public class Api extends HttpClientApi {
	private static final String[] sParserNames = new String[]{
			"EditablePost",
			"ExistedAttach",
			"Json",
			"MarkedThreads",
			"Mentions",
			"Messages",
			"Null",
			"OwnPosts",
			"OwnThreads",
			"Posts",
			"RawMessages",
			"Threads",
			"User",
	};

	private Map<String, Parsable> mParsers;

	private static Api singleton;

	public void setMode(Mode mMode) {
		this.mMode = mMode;
	}

	private Mode mMode = Mode.REMOTE;

	public enum Mode{ REMOTE, LOCAL }

	private Api(Context context) {
		super(context);
		mParsers = new HashMap<>();

		try {
			for (String parserName : sParserNames) {
				Class parserClass = Class.forName("com.ladjzero.hipda.parsers." + parserName + "Parser");
				mParsers.put(parserName, (Parsable) parserClass.newInstance());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private Parsable getParser(String localParserName, String remoteParserName) {
		String parserName = mMode == Mode.REMOTE ? remoteParserName : localParserName;
		Parsable p = mParsers.get(parserName);

		if (p == null) {
			throw new Error("parser not found.");
		}

		return p;
	}

	// TODO. make it private.
	public Parsable getParser(String localParserName) {
		return getParser(localParserName, "Json");
	}

	public static Api getApi(Context context) {
		if (singleton == null) {
			singleton = new Api(context);
		}

		return singleton;
	}

	public void getPosts(String url, OnRespondCallback onRespondCallback) {
		get(url, new ApiCallback(this, getParser("Posts"), onRespondCallback));
	}

	public void getThreads(int page, int fid, int typeid, String order, OnRespondCallback onRespondCallback) {
		getThreads(page, fid, typeid, order, new ApiCallback(this, getParser("Threads"), onRespondCallback));
	}

	public void searchThreads(String query, int page, int[] fids, OnRespondCallback onRespondCallback) {
		searchThreads(query, page, fids, new ApiCallback(this, getParser("Threads"), onRespondCallback));
	}

	public void searchUserThreads(String name, int page, OnRespondCallback onRespondCallback) {
		searchUserThreads(name, page, new ApiCallback(this, getParser("Threads"), onRespondCallback));
	}

	public void getUser(int uid, OnRespondCallback onRespondCallback) {
		getUser(uid, new ApiCallback(this, getParser("User"), onRespondCallback));
	}

	public void getExistedAttach(OnRespondCallback onRespondCallback) {
		getExistedAttach(
				new ApiCallback(this, getParser("ExistedAttach"), onRespondCallback)
		);
	}

	public void getEditBody(int fid, int tid, int pid, OnRespondCallback onRespondCallback) {
		getEditBody(fid, tid, pid, new ApiCallback(this, getParser("EditablePost"), onRespondCallback));
	}

	public void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, OnRespondCallback onRespondCallback) {
		newThread(fid, subject, message, attachIds, new ApiCallback(this, getParser("Posts"), onRespondCallback));
	}

	public void editPost(int fid, int tid, int pid, String subject, String message, ArrayList<Integer> attachIds, OnRespondCallback onRespondCallback) {
		editPost(fid, tid, pid, subject, message, attachIds, new ApiCallback(this, getParser("Posts"), onRespondCallback));
	}

	public void sendReply(int tid, String content, ArrayList<Integer> attachIds, ArrayList<Integer> existedAttchIds, OnRespondCallback onRespondCallback) {
		sendReply(tid, content, attachIds, existedAttchIds, new ApiCallback(this, getParser("Posts"), onRespondCallback));
	}

	public void deletePost(int fid, int tid, int pid, OnRespondCallback onRespondCallback) {
		deletePost(fid, tid, pid, new ApiCallback(this, new Parsable() {
			@Override
			public Response parse(String html) {
				Response res = new Response();

				if (html.contains("未定义操作，请返回。")) {
					res.setSuccess(false);
					res.setData("未定义操作");
				} else {
					// TODO.
//					res =  mPostsParser.parse(html);
				}

				return res;
			}
		}, onRespondCallback));
	}

	public void uploadImage(final File imageFile, final OnRespondCallback onRespondCallback) {
		uploadImage(imageFile, new ApiCallback(this, new Parsable() {
			@Override
			public Response parse(String html) {
				return new Response();
			}
		}, onRespondCallback));
	}

	public void login(String username, String password, int questionId, String answer, final OnRespondCallback onRespondCallback) {
		final Response res = new Response();

		login(
				username,
				password,
				questionId,
				answer,
				new ApiCallback(this, new Parsable() {
					@Override
					public Response parse(String html) {
						Response.Meta meta = new Response.Meta();
					/*
						Hack. apiStore.user will be fetched with any page. BUT one mock user MUST
						be set to authorize fetching security forums.
					*/
						meta.setUser(new User().setId(1));
						res.setMeta(meta);
						return res;
					}
				}, onRespondCallback));
	}

	public void logout(final OnRespondCallback onRespondCallback) {
		final Response res = new Response();

		logout(new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				onRespondCallback.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				onRespondCallback.onRespond(res);
			}
		});
	}

	public void sendMessage(String name, String message, final OnRespondCallback onRespondCallback) {
		sendMessage(name, message, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				onRespondCallback.onRespond(new Response());
			}

			@Override
			public void onFailure(String reason) {
				Response res = new Response();
				res.setSuccess(false);
				res.setData(reason);
				onRespondCallback.onRespond(res);
			}
		});
	}

	public void addToFavorite(int tid, final OnRespondCallback onRespondCallback) {
		final Response res = new Response();

		addToFavorite(tid, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				if (response.contains("此主题已成功添加到收藏夹中")) {
					res.setData("收藏成功");
				} else {
					if (response.contains("您曾经收藏过这个主题")) {
						res.setData("已经收藏过该主题");
					} else {
						res.setSuccess(false);
						res.setData("收藏失败");
					}
				}

				onRespondCallback.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				onRespondCallback.onRespond(res);
			}
		});
	}

	public void removeFromFavoriate(int tid, final OnRespondCallback onRespondCallback) {
		final Response res = new Response();

		removeFromFavoriate(tid, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				if (response.contains("此主题已成功从您的收藏夹中移除")) {
					res.setData("移除成功");
					onRespondCallback.onRespond(res);
				} else {
					res.setData("移除失败");
					onRespondCallback.onRespond(res);
				}
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				onRespondCallback.onRespond(res);
			}
		});
	}

	public void getMessages(OnRespondCallback onRespondCallback) {
		getMessages(new ApiCallback(this, getParser("Messages"), onRespondCallback));
	}

	public void getMentions(OnRespondCallback onRespondCallback) {
		getMentions(new ApiCallback(this, getParser("Mentions"), onRespondCallback));
	}
	public void getOwnThreads(int page, OnRespondCallback onRespondCallback) {
		getOwnThreads(page, new ApiCallback(this, getParser("OwnThreads"), onRespondCallback));
	}

	public void getOwnPosts(int page, OnRespondCallback onRespondCallback) {
		getOwnPosts(page, new ApiCallback(this, getParser("OwnPosts"), onRespondCallback));
	}

	public void getMarkedThreads(int page, OnRespondCallback onRespondCallback) {
		getMarkedThreads(page, new ApiCallback(this, getParser("MarkedThreads"), onRespondCallback));
	}

	public void getVersions(OnRespondCallback onRespondCallback) {
		get(
				"http://ladjzero.github.io/uzlee/js/version.json",
				"utf-8",
				new ApiCallback(this, new VersionsParser(), onRespondCallback));
	}

	public void getRawMessages(OnRespondCallback onRespondCallback) {
		getRawMessages(new ApiCallback(this, getParser("RawMessages", "Null"), onRespondCallback));
	}

}
