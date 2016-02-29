package com.ladjzero.hipda;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhuo on 16-2-12.
 */
public class HttpApi {
	private HttpClient mHttpClient;
	private ApiStore mStore;

	public HttpApi(HttpClient httpClient) {
		mHttpClient = httpClient;
		mStore = ApiStore.getStore();
	}

	public void getMentions(HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/notice.php", callback);
	}

	public void getMessages(HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/pm.php?filter=privatepm", callback);
	}

	public void getOwnPosts(int page, HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/my.php?item=posts&page=" + page, callback);
	}

	public void getOwnThreads(int page, HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/my.php?item=threads&page" + page, callback);
	}

	public void getMarkedThreads(int page, HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/my.php?item=favorites&type=thread&page=" + page, callback);
	}

	public void getThreads(int page, int fid, int typeid, String order, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/forumdisplay.php?fid=" + fid
				+ "&page=" + page
				+ "&filter=type&typeid=" + typeid
				+ "&orderby=" + order;

		mHttpClient.get(url, callback);
	}

	public void getExistedAttach(HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", callback);
	}

	public void searchThreads(String query, int page, HttpClientCallback callback) {
		String url = null;

		try {
			url = "http://www.hi-pda.com/forum/search.php?srchtxt=" + URLEncoder.encode(query, mStore.getCode())
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
					+ "page=" + page;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.onFailure(e.toString());
		}

		if (url != null) mHttpClient.get(url, callback);
	}

	public void searchUserThreads(String username, int page, HttpClientCallback callback) {
		String url = null;

		try {
			url = "http://www.hi-pda.com/forum/search.php?srchtype=title&srchtxt=&searchsubmit=true&st=on&srchuname="
					+ URLEncoder.encode(username, mStore.getCode())
					+ "&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&page="
					+ page;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.onFailure(e.toString());
		}

		if (url != null) mHttpClient.get(url, callback);
	}

	public void addToFavorite(int tid, HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/my.php?item=favorites&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg", callback);
	}

	public void removeFromFavoriate(int tid, HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/my.php?item=favorites&action=remove&tid=" + tid + "&inajax=1&ajaxtarget=favorite_msg", callback);
	}

	public void login(String username, String password, int questionId, String answer, final HttpClientCallback callback) {
		Map<String, String> params = new HashMap();
		params.put("sid", "fa6m4o");
		params.put("formhash", "ad793a3f");
		params.put("loginfield", "username");
		params.put("username", username);
		params.put("password", password);
		params.put("questionid", String.valueOf(questionId));
		params.put("answer", answer);
		params.put("loginsubmit", "true");

		mHttpClient.post("http://www.hi-pda.com/forum/logging.php?action=login&loginsubmit=yes", params, null, new HttpClientCallback() {
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

	public void logout(final HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/logging.php?action=logout&formhash=" + mStore.getFormhash(), new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				mStore.setUser(null);
				mStore.setUnread(0);
				callback.onSuccess(response);
			}

			@Override
			public void onFailure(String reason) {
				callback.onFailure(reason);
			}
		});
	}

	public void sendMessage(String name, String message, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/pm.php?action=send&pmsubmit=yes&infloat=yes&sendnew=yes";

		Map<String, String> params = new HashMap();
		params.put("formhash", mStore.getFormhash());
		params.put("msgto", name);
		params.put("message", message);
		params.put("pmsubmit", "true");

		mHttpClient.post(url, params, null, callback);
	}

	public void newThread(int fid, String subject, String message, ArrayList<Integer> attachIds, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=newthread&fid=" + fid + "&extra=&topicsubmit=yes";

		Map<String, String> params = new HashMap();
		params.put("formhash", mStore.getFormhash());
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

		mHttpClient.post(url, params, null, callback);
	}

	public void sendReply(int tid, String content, ArrayList<Integer> attachIds, ArrayList<Integer> existedAttchIds, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=reply&fid=57&tid=" + tid + "&extra=&replysubmit=yes";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", mStore.getFormhash());
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

		mHttpClient.post(url, params, null, callback);
	}

	public void deletePost(int fid, int tid, int pid, final HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", mStore.getFormhash());
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

		mHttpClient.post(url, params, null, new HttpClientCallback() {
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

	public void editPost(int fid, int tid, int pid, String subject, String message, ArrayList<Integer> attachIds, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&extra=&editsubmit=yes&mod=";

		Map<String, String> params = new HashMap<>();
		params.put("formhash", mStore.getFormhash());
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

		mHttpClient.post(url, params, null, callback);
	}

	public void uploadImage(final File imageFile, final HttpClientCallback callback) {
		mHttpClient.get("http://www.hi-pda.com/forum/post.php?action=newthread&fid=57", new HttpClientCallback() {
			@Override
			public void onSuccess(String response) {
				User user = mStore.getUser();

				if (user == null) return;

				Map<String, String> params = new HashMap<String, String>();
				params.put("uid", String.valueOf(user.getId()));
				params.put("hash", mStore.getHash());
				params.put("filename", imageFile.getName());

				Map<String, File> files = new HashMap<String, File>();
				files.put("Filedata", imageFile);

				String url = "http://www.hi-pda.com/forum/misc.php?action=swfupload&operation=upload&simple=1&type=image";

				mHttpClient.post(url, params, files, callback);
			}

			@Override
			public void onFailure(String reason) {
				callback.onFailure(reason);
			}
		});
	}

	public void getEditBody(int fid, int tid, int pid, HttpClientCallback callback) {
		String url = "http://www.hi-pda.com/forum/post.php?action=edit&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";

		mHttpClient.get(url, callback);
	}
}
