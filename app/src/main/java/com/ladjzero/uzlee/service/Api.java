package com.ladjzero.uzlee.service;

import android.os.AsyncTask;

import com.ladjzero.hipda.parsers.Parsable;
import com.ladjzero.hipda.Response;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.hipda.parsers.VersionsParser;
import com.ladjzero.uzlee.App;
import com.ladjzero.uzlee.HttpClientCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public class Api {
	private static final String[] sParserNames = new String[]{
			"EditablePost",
			"ExistedAttach",
			"Json",
			"MarkedThreads",
			"Mentions",
			"Messages",
			"OwnPosts",
			"OwnThreads",
			"Posts",
			"Threads",
			"User",
			"Versions",
	};

	private Map<String, Parsable> mParsers;

	private static Api singleton;

	public void setMode(Mode mMode) {
		this.mMode = mMode;
	}

	private Mode mMode = Mode.REMOTE;

	public enum Mode{ REMOTE, LOCAL }

	private Api() {
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

	public static Api getApi() {
		if (singleton == null) {
			singleton = new Api();
		}

		return singleton;
	}

	public void getPosts(String url, OnRespond onRespond) {
		App.getInstance().getHttpClient().get(url, new ApiHttpClientCallback(getParser("Posts"), onRespond));
	}

	public void getThreads(int page, int fid, int typeid, String order, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getThreads(page, fid, typeid, order, new ApiHttpClientCallback(getParser("Threads"), onRespond));
	}

	public void searchThreads(String query, int page, int[] fids, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().searchThreads(query, page, fids, new ApiHttpClientCallback(getParser("Threads"), onRespond));
	}

	public void searchUserThreads(String name, int page, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().searchUserThreads(name, page, new ApiHttpClientCallback(getParser("Threads"), onRespond));
	}

	public void getUser(int uid, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getUser(uid, new ApiHttpClientCallback(getParser("User"), onRespond));
	}

	public void getExistedAttach(OnRespond onRespond) {
		App.getInstance().getHttpClient().get(
				"http://www.hi-pda.com/forum/post.php?action=newthread&fid=57",
				new ApiHttpClientCallback(getParser("ExistedAttach"), onRespond)
		);
	}

	public void getEditBody(int fid, int tid, int pid, OnRespond onRespond) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";

		App.getInstance().getHttpClient().get(url, new ApiHttpClientCallback(getParser("EditablePost"), onRespond));
	}

	public void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, OnRespond onRespond) {
		String url = "http://www.hi-pda.com/forum/post.php?action=newthread&fid=" + fid + "&extra=&topicsubmit=yes";

		Map<String, String> params = new HashMap();
		params.put("formhash", App.getInstance().getCore().getApiStore().getFormhash());
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("wysiwyg", "1");
		params.put("iconid", "");
		params.put("tags", "");
		params.put("attention_add", "1");
		params.put("subject", subject);
		params.put("message", message);

		if (attachIds != null) {
			for (Integer attachId : attachIds) {
				params.put("attachnew[" + attachId + "][description]", "");
			}
		}

		App.getInstance().getHttpClient().post(url, params, null, new ApiHttpClientCallback(getParser("Posts"), onRespond));
	}

	public void editPost(int fid, int tid, int pid, String subject, String message, ArrayList<Integer> attachIds, OnRespond onRespond) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", App.getInstance().getCore().getApiStore().getFormhash());
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("wysiwyg", "1");
		params.put("iconid", "0");
		params.put("fid", String.valueOf(fid));
		params.put("tid", String.valueOf(tid));
		params.put("pid", String.valueOf(pid));
		params.put("page", "1");
		params.put("tags", "");
		params.put("editsubmit", "true");
		params.put("subject", subject);
		params.put("message", message);

		if (attachIds != null) {
			for (Integer attachId : attachIds) {
				params.put("attachnew[" + attachId + "][description]", "");
			}
		}

		App.getInstance().getHttpClient().post(url, params, null, new ApiHttpClientCallback(getParser("Posts"), onRespond));
	}

	public void sendReply(int tid, String content, ArrayList<Integer> attachIds, ArrayList<Integer> existedAttchIds, OnRespond onRespond) {
		String url = "http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", App.getInstance().getCore().getApiStore().getFormhash());
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("subject", "");
		params.put("wysiwyg", "1");
		params.put("noticeauthor", "");
		params.put("noticetrimstr", "");
		params.put("noticeauthormsg", "");
		params.put("subject", "");
		params.put("message", content);

		if (attachIds != null) {
			for (Integer attachId : attachIds) {
				params.put("attachnew[" + attachId + "][description]", "");
			}
		}

		if (existedAttchIds != null) {
			for (Integer id : existedAttchIds) {
				params.put("attachdel[]", String.valueOf(id));
			}
		}

		App.getInstance().getHttpClient().post(url, params, null, new ApiHttpClientCallback(getParser("Posts"), onRespond));
	}


	public void deletePost(int fid, int tid, int pid, OnRespond onRespond) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", App.getInstance().getCore().getApiStore().getFormhash());
		params.put("posttime", Long.valueOf(System.currentTimeMillis() / 1000).toString());
		params.put("wysiwyg", "1");
		params.put("page", "1");
		params.put("delete", "1");
		params.put("editsubmit", "true");
		params.put("subject", "");
		params.put("message", "");
		params.put("fid", String.valueOf(fid));
		params.put("tid", String.valueOf(tid));
		params.put("pid", String.valueOf(pid));

		App.getInstance().getHttpClient().post(url, params, null, new ApiHttpClientCallback(new Parsable() {
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
		}, onRespond));
	}

	public void uploadImage(final File imageFile, final OnRespond onRespond) {
		final ApiStore store = App.getInstance().getCore().getApiStore();
		final Response res = new Response();

		App.getInstance().getHttpClient().get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				User user = store.getUser();
				String hash = store.getHash();

				if (hash == null || hash.length() == 0) {
					res.setSuccess(false);
					res.setData("error: fail to get hash string.");
					return;
				}

				if (user == null) {
					res.setSuccess(false);
					res.setData("error: fail to get user.");
					return;
				}

				if (!(imageFile.isFile() && imageFile.exists())) {
					res.setSuccess(false);
					res.setData("error: fail to open image file.");
					return;
				}

				Map<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(user.getId()));
				params.put("hash", store.getHash());
				params.put("filename", imageFile.getName());

				Map<String, File> files = new HashMap<String, File>();
				files.put("Filedata", imageFile);

				String url = "http://www.hi-pda.com/forum/misc.php?action=swfupload&operation=upload&simple=1&type=image";

				App.getInstance().getHttpClient().post(url, params, files, new ApiHttpClientCallback(new Parsable() {
					@Override
					public Response parse(String html) {
						if (html.startsWith("DISCUZUPLOAD")) {
							int attachId = -1;

							try {
								attachId = Integer.valueOf(html.split("\\|")[2]);
							} catch (Exception e) {

							}

							if (attachId != -1) {
								res.setData(attachId);
							}
						} else {
							res.setSuccess(false);
							res.setData("图片长传失败: " + html);
						}

						return res;
					}
				}, onRespond));
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				res.setData(reason);
				onRespond.onRespond(res);
			}
		});
	}

	public void login(String username, String password, int questionId, String answer, final OnRespond onRespond) {
		final Response res = new Response();

		App.getInstance().getCore().getHttpApi().login(
				username,
				password,
				questionId,
				answer,
				new ApiHttpClientCallback(new Parsable() {
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
			}, onRespond));
	}

	public void logout(final OnRespond onRespond) {
		final Response res = new Response();

		App.getInstance().getCore().getHttpApi().logout(new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				onRespond.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				onRespond.onRespond(res);
			}
		});
	}

	public void sendMessage(String name, String message, final OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().sendMessage(name, message, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				onRespond.onRespond(new Response());
			}

			@Override
			public void onFailure(String reason) {
				Response res = new Response();
				res.setSuccess(false);
				res.setData(reason);
				onRespond.onRespond(res);
			}
		});
	}

	public void addToFavorite(int tid, final OnRespond onRespond) {
		final Response res = new Response();

		App.getInstance().getCore().getHttpApi().addToFavorite(tid, new HttpClientCallback() {
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

				onRespond.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				onRespond.onRespond(res);
			}
		});
	}

	public void removeFromFavoriate(int tid, final OnRespond onRespond) {
		final Response res = new Response();

		App.getInstance().getCore().getHttpApi().removeFromFavoriate(tid, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				if (response.contains("此主题已成功从您的收藏夹中移除")) {
					res.setData("移除成功");
					onRespond.onRespond(res);
				} else {
					res.setData("移除失败");
					onRespond.onRespond(res);
				}
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				onRespond.onRespond(res);
			}
		});
	}

	public void getMessages(OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getMessages(new ApiHttpClientCallback(getParser("Messages"), onRespond));
	}

	public void getMentions(OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getMentions(new ApiHttpClientCallback(getParser("Mentions"), onRespond));
	}
	public void getOwnThreads(int page, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getOwnThreads(page, new ApiHttpClientCallback(getParser("OwnThreads"), onRespond));
	}

	public void getOwnPosts(int page, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getOwnPosts(page, new ApiHttpClientCallback(getParser("OwnPosts"), onRespond));
	}

	public void getMarkedThreads(int page, OnRespond onRespond) {
		App.getInstance().getCore().getHttpApi().getMarkedThreads(page, new ApiHttpClientCallback(getParser("MarkedThreads"), onRespond));
	}

	public void getVersions(OnRespond onRespond) {
		App.getInstance().getHttpClient().get(
				"http://ladjzero.github.io/uzlee/js/version.json",
				"utf-8",
				new ApiHttpClientCallback(new VersionsParser(), onRespond));
	}

	public interface OnRespond {
		void onRespond(Response res);
	}

	protected class ApiHttpClientCallback implements HttpClientCallback {
		private Parsable p;
		private OnRespond onRespond;

		public ApiHttpClientCallback(Parsable p, OnRespond onRespond) {
			this.p = p;
			this.onRespond = onRespond;
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

					ApiStore store = App.getInstance().getCore().getApiStore();
					Response.Meta meta = res.getMeta();

					if (meta != null) {
						store.setUser(meta.getUser());
						store.setCode(meta.getCode());
						store.setFormhash(meta.getFormhash());
						store.setHash(meta.getHash());
						store.setUnread(meta.getUnread());
					}

					onRespond.onRespond(res);
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);
		}

		@Override
		public void onFailure(String reason) {
			Response res = new Response();
			res.setSuccess(false);
			res.setData(reason);
			onRespond.onRespond(res);
		}
	}
}
