package com.ladjzero.uzlee.api;

import android.content.Context;

import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.entities.User;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhuo on 16-2-12.
 */
abstract class HttpClientApi extends HttpClient implements ParserProvider {
	public HttpClientApi(Context context) {
		super(context);
	}

	public void getPosts(String url, OnRespondCallback callback) {
		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getMentions(OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/notice.php";

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getMessages(OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/pm.php?filter=privatepm";

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getOwnPosts(int page, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/my.php?item=posts&page=" + page;

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getOwnThreads(int page, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/my.php?item=threads&page" + page;

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getMarkedThreads(int page, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/my.php?item=favorites&type=thread&page=" + page;

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getThreads(int page, int fid, int typeid, String order, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/forumdisplay.php?fid=" + fid
				+ "&page=" + page
				+ "&filter=type&typeid=" + typeid
				+ "&orderby=" + order;

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getExistedAttach(OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/post.php?action=newthread&fid=57";

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void searchThreads(String query, int page, int[] fids, OnRespondCallback callback) {
		String url = null;

		try {
			String queryFid = "";

			if (fids != null && fids.length > 0) {
				for (int i = 0; i < fids.length; ++i) {
					queryFid = queryFid + "srchfid[" + i + "]=" + fids[i] + "&";
				}
			}

			url = "http://www.hi-pda.com/forum/search.php?srchtxt=" + URLEncoder.encode(query, getStore().getCode())
					+ "&srchtype=title&"
					+ "searchsubmit=true&"
					+ "st=on&"
					+ "srchuname=&"
					+ "srchfilter=all&"
					+ "srchfrom=0&"
					+ "before=&"
					+ "orderby=lastpost&"
					+ "ascdesc=desc&"
					+ "srchfid%5B0%5D=all&"
					+ queryFid
					+ "page=" + page;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Response res = new Response();
			res.setSuccess(false);
			res.setData(e.toString());
			callback.onRespond(res);
		}

		if (url != null) get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void searchUserThreads(String username, int page, OnRespondCallback callback) {
		String url = null;

		try {
			url = "http://www.hi-pda.com/forum/search.php?srchtype=title&srchtxt=&searchsubmit=true&st=on&srchuname="
					+ URLEncoder.encode(username, getStore().getCode())
					+ "&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&page="
					+ page;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Response res = new Response();
			res.setSuccess(false);
			res.setData(e.toString());
			return;
		}

		if (url != null) get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void addToFavorite(int tid, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/my.php?item=favorites&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg";

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void removeFromFavoriate(int tid, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/my.php?item=favorites&action=remove&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg";

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void login(String username, String password, int questionId, String answer, final OnRespondCallback callback) {
		Map<String, String> params = new HashMap();
		final Response res = new Response();
		params.put("sid", "fa6m4o");
		params.put("formhash", "ad793a3f");
		params.put("loginfield", "username");
		params.put("username", username);
		params.put("password", password);
		params.put("questionid", String.valueOf(questionId));
		params.put("answer", answer);
		params.put("loginsubmit", "true");

		post("http://www.hi-pda.com/forum/logging.php?action=login&loginsubmit=yes", params, null, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				if (response.contains("欢迎您回来")) {
					callback.onRespond(res);
				} else if (response.contains("密码错误次数过多，请 15 分钟后重新登录")) {
					res.setSuccess(false);
					res.setData("密码错误次数过多，请 15 分钟后重新登录");
				} else {
					res.setSuccess(false);
					res.setData("登录错误");
				}

				callback.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				res.setData(reason);
				callback.onRespond(res);
			}
		});
	}

	public void logout(final OnRespondCallback callback) {
		final Response res = new Response();
		
		get("http://www.hi-pda.com/forum/logging.php?action=logout&formhash=" + getStore().getFormhash(), new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				getStore().setUser(new User());
				getStore().setUnread(0);
				callback.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				res.setData(reason);
				callback.onRespond(res);
			}
		});
	}

	public void sendMessage(String name, String message, final OnRespondCallback callback) {
		String url = "http://www.hi-pda.com/forum/pm.php?action=send&pmsubmit=yes&infloat=yes&sendnew=yes";
		final Response res = new Response();

		Map<String, String> params = new HashMap();
		params.put("formhash", getStore().getFormhash());
		params.put("msgto", name);
		params.put("message", message);
		params.put("pmsubmit", "true");

		post(url, params, null, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				callback.onRespond(res);
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				res.setData(reason);
				callback.onRespond(res);
			}
		});
	}

	public void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/post.php?action=newthread&fid=" + fid + "&extra=&topicsubmit=yes";

		Map<String, String> params = new HashMap();
		params.put("formhash", getStore().getFormhash());
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

		post(url, params, null, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void sendReply(int tid, String content, ArrayList<Integer> attachIds, ArrayList<Integer> existedAttchIds, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", getStore().getFormhash());
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

		post(url, params, null, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void deletePost(int fid, int tid, int pid, final OnRespondCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", getStore().getFormhash());
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

		post(url, params, null, new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				if (response.contains("未定义操作，请返回。")) {
					onFailure("未定义操作");
				} else {
					Response res = new Response();
					res.setSuccess(false);
					res.setData(response);
					callback.onRespond(res);
				}
			}

			@Override
			public void onFailure(String reason) {
				Response res = new Response();
				res.setSuccess(false);
				res.setData(reason);
				callback.onRespond(res);
			}
		});
	}

	public void editPost(int fid, int tid, int pid, String subject, String message, ArrayList<Integer> attachIds, OnRespondCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", getStore().getFormhash());
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

		post(url, params, null, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void uploadImage(final File imageFile, final OnRespondCallback callback) {
		final Response res = new Response();
		
		get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				User user = getStore().getUser();
				String hash = getStore().getHash();

				if (hash == null || hash.length() == 0) {
					res.setSuccess(false);
					res.setData("error: fail to get hash string.");
					callback.onRespond(res);
					return;
				}

				if (user == null) {
					res.setSuccess(false);
					res.setData("error: fail to get user.");
					callback.onRespond(res);
					return;
				}

				if (!(imageFile.isFile() && imageFile.exists())) {
					res.setSuccess(false);
					res.setData("error: fail to open image file.");
					callback.onRespond(res);
					return;
				}

				Map<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(user.getId()));
				params.put("hash", getStore().getHash());
				params.put("filename", imageFile.getName());

				Map<String, File> files = new HashMap<String, File>();
				files.put("Filedata", imageFile);

				String url = "http://www.hi-pda.com/forum/misc.php?action=swfupload&operation=upload&simple=1&type=image";

				post(url, params, files, new HttpClientCallback() {
					@Override
					public void onSuccess(String response) {
						callback.onRespond(res);
					}

					@Override
					public void onFailure(String reason) {
						res.setSuccess(false);
						res.setData(reason);
						callback.onRespond(res);
					}
				});
			}

			@Override
			public void onFailure(String reason) {
				res.setSuccess(false);
				res.setData(reason);
				callback.onRespond(res);
			}
		});
	}

	public void getEditBody(int fid, int tid, int pid, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/post.php?action=edit&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";

		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getUser(int uid, OnRespondCallback callback) {
		final String url = "http://www.hi-pda.com/forum/space.php?uid=" + uid;
		
		get(url, new ApiCallback(this, getParserByUrl(url), callback));
	}

	public void getRawMessages(OnRespondCallback callback) {
		User user = getStore().getUser();
		final String url = "http://www.hi-pda.com/forum/pm.php?uid=" + user.getId() + "&filter=privatepm&daterange=5";
		
		get(url, new ApiCallback(this, getParserByUrl(url), callback));
		
	}
}
