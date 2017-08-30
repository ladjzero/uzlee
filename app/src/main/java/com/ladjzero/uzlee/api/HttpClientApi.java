package com.ladjzero.uzlee.api;

import android.content.Context;

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
class HttpClientApi extends HttpClient{
	public HttpClientApi(Context context) {
		super(context);
	}

	protected void getMentions(HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/notice.php", callback);
	}

	protected void getMessages(HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/pm.php?filter=privatepm", callback);
	}

	protected void getOwnPosts(int page, HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/my.php?item=posts&page=" + page, callback);
	}

	protected void getOwnThreads(int page, HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/my.php?item=threads&page" + page, callback);
	}

	protected void getMarkedThreads(int page, HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/my.php?item=favorites&type=thread&page=" + page, callback);
	}

	protected void getThreads(int page, int fid, int typeid, String order, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/forumdisplay.php?fid=" + fid
				+ "&page=" + page
				+ "&filter=type&typeid=" + typeid
				+ "&orderby=" + order;

		get(url, callback);
	}

	protected void getExistedAttach(HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", callback);
	}

	protected void searchThreads(String query, int page, int[] fids, HttpClientCallback callback) {
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
			callback.onFailure(e.toString());
		}

		if (url != null) get(url, callback);
	}

	protected void searchUserThreads(String username, int page, HttpClientCallback callback) {
		String url = null;

		try {
			url = "http://www.hi-pda.com/forum/search.php?srchtype=title&srchtxt=&searchsubmit=true&st=on&srchuname="
					+ URLEncoder.encode(username, getStore().getCode())
					+ "&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&page="
					+ page;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.onFailure(e.toString());
		}

		if (url != null) get(url, callback);
	}

	protected void addToFavorite(int tid, HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/my.php?item=favorites&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg", callback);
	}

	protected void removeFromFavoriate(int tid, HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/my.php?item=favorites&action=remove&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg", callback);
	}

	protected void login(String username, String password, int questionId, String answer, final HttpClientCallback callback) {
		Map<String, String> params = new HashMap();
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
					callback.onSuccess(response);
				} else if (response.contains("密码错误次数过多，请 15 分钟后重新登录")) {
					callback.onFailure("密码错误次数过多，请 15 分钟后重新登录");
				} else {
					callback.onFailure("登录错误");
				}
			}

			@Override
			public void onFailure(String reason) {
				callback.onFailure(reason);
			}
		});
	}

	protected void logout(final HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/logging.php?action=logout&formhash=" + getStore().getFormhash(), new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				getStore().setUser(new User());
				getStore().setUnread(0);
				callback.onSuccess(response);
			}

			@Override
			public void onFailure(String reason) {
				callback.onFailure(reason);
			}
		});
	}

	protected void sendMessage(String name, String message, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/pm.php?action=send&pmsubmit=yes&infloat=yes&sendnew=yes";

		Map<String, String> params = new HashMap();
		params.put("formhash", getStore().getFormhash());
		params.put("msgto", name);
		params.put("message", message);
		params.put("pmsubmit", "true");

		post(url, params, null, callback);
	}

	protected void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=newthread&fid=" + fid + "&extra=&topicsubmit=yes";

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

		post(url, params, null, callback);
	}

	protected void sendReply(int tid, String content, ArrayList<Integer> attachIds, ArrayList<Integer> existedAttchIds, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes";

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

		post(url, params, null, callback);
	}

	protected void deletePost(int fid, int tid, int pid, final HttpClientCallback callback) {
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
					callback.onSuccess(response);
				}
			}

			@Override
			public void onFailure(String reason) {
				callback.onFailure(reason);
			}
		});
	}

	protected void editPost(int fid, int tid, int pid, String subject, String message, ArrayList<Integer> attachIds, HttpClientCallback callback) {
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

		post(url, params, null, callback);
	}

	protected void uploadImage(final File imageFile, final HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				User user = getStore().getUser();
				String hash = getStore().getHash();

				if (hash == null || hash.length() == 0) {
					callback.onFailure("error: fail to get hash string.");
					return;
				}

				if (user == null) {
					callback.onFailure("error: fail to get user.");
					return;
				}

				if (!(imageFile.isFile() && imageFile.exists())) {
					callback.onFailure("error: fail to open image file.");
					return;
				}

				Map<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(user.getId()));
				params.put("hash", getStore().getHash());
				params.put("filename", imageFile.getName());

				Map<String, File> files = new HashMap<String, File>();
				files.put("Filedata", imageFile);

				String url = "http://www.hi-pda.com/forum/misc.php?action=swfupload&operation=upload&simple=1&type=image";

				post(url, params, files, callback);
			}

			@Override
			public void onFailure(String reason) {
				callback.onFailure(reason);
			}
		});
	}

	protected void getEditBody(int fid, int tid, int pid, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";

		get(url, callback);
	}

	protected void getUser(int uid, HttpClientCallback callback) {
		get("http://www.hi-pda.com/forum/space.php?uid=" + uid, callback);
	}

	protected void getRawMessages(HttpClientCallback callback) {
		User user = getStore().getUser();
		get("http://www.hi-pda.com/forum/pm.php?uid=" + user.getId() + "&filter=privatepm&daterange=5", callback);
	}
}
