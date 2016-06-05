package com.ladjzero.uzlee;

import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.PostsParser;
import com.ladjzero.uzlee.model.ObservablePosts;
import com.ladjzero.uzlee.utils.ReportableAsyncTask;
import com.ladjzero.uzlee.utils.Timeline;
import com.ladjzero.uzlee.utils.Utils;
import com.orhanobut.logger.Logger;

/**
 * Created by chenzhuo on 16-3-19.
 */
public class TaskPostsParse extends ReportableAsyncTask {
	private ObservablePosts mPosts;
	private ActivityPosts.Model mModel;
	private Timeline mTimeline;
	private PostsParser mParser;

	public TaskPostsParse(ObservablePosts posts, ActivityPosts.Model model) {
		mPosts = posts;
		mModel = model;
		mTimeline = new Timeline();
		mParser = App.getInstance().getCore().getPostsParser();
		mParser.setProgressReport(this);
	}

	@Override
	protected void onPreExecute() {
		mModel.setParsing(true);
	}

	@Override
	protected void onCancelled() {
		mPosts.clear();
	}

	@Override
	protected void onCancelled(Object o) {
		onCancelled();
	}

	@Override
	protected Object doInBackground(Object[] objects) {
		return mParser.parsePosts((String) objects[0]);
	}

	@Override
	protected void onProgressUpdate(Object... objects) {
		Post post = (Post) objects[2];
		int lzId = -1;
		if (post.getId() == 1) lzId = post.getAuthor().getId();
		post.setIsLz(post.getAuthor().getId() == lzId);
		post.setTimeStr(Utils.prettyTime(post.getTimeStr()));
		mPosts.add(post);

		mModel.setParsingPercentage((Integer) objects[0] * 1.0f / (Integer) objects[1]);
		Logger.t(Timeline.TAG).i("Parsed one %dms", mTimeline.timeLine());
	}

	@Override
	public void onProgress(int i, int size, Object o) {
		publishProgress(i, size, o);
	}

	@Override
	protected void onPostExecute(Object o) {
		Logger.t(Timeline.TAG).i("%dms", mTimeline.timeLine());
		mPosts.setMeta(((Posts) o).getMeta());

		mModel.setTitle(mPosts.getMeta().getTitle());
		mModel.setParsing(false);
	}
}
