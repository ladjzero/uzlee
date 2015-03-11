package com.ladjzero.uzlee;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.dao.Dao;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Core.OnThreadsListener;
import com.ladjzero.hipda.DBHelper;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Thread;
import com.ladjzero.hipda.User;
import com.nineoldandroids.animation.Animator;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.jsoup.Connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class ThreadsFragment extends Fragment implements OnRefreshListener, AdapterView.OnItemClickListener, OnThreadsListener {

	private BaseActivity mActivity;
	private final ArrayList<Thread> threads = new ArrayList<Thread>();
	private SwipeRefreshLayout swipe;
	private DBHelper db;
	private Dao<Thread, Integer> threadDao;
	private Dao<User, Integer> userDao;
	private ListView listView;
	private ThreadsAdapter adapter;
	private boolean hasNextPage = false;
	private int fid;
	private View mGoTop;
	private boolean mIsAnimating = false;
	private boolean mGoTopVisible = false;
	private int mPage = 1;
	private boolean mIsFetching = false;

	public static ThreadsFragment newInstance(int fid) {
		ThreadsFragment fragment = new ThreadsFragment();
		Bundle args = new Bundle();
		args.putInt("fid", fid);
		fragment.setArguments(args);
		fragment.fid = fid;

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (BaseActivity) getActivity();

		if (db == null) db = ((BaseActivity) getActivity()).getHelper();

		try {
			threadDao = db.getThreadDao();
			userDao = db.getUserDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		View rootView = inflater.inflate(R.layout.threads_can_refresh, container, false);

		swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.thread_swipe);
		swipe.setOnRefreshListener(this);
		swipe.setColorSchemeResources(R.color.dark_primary, R.color.grape_primary, R.color.deep_primary, R.color.snow_dark);

		listView = (ListView) rootView.findViewById(R.id.threads);
		adapter = new ThreadsAdapter(getActivity(), threads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {
					mActivity.showToast("载入下一页");
					fetch(page, ThreadsFragment.this);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				super.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);

				if (firstVisibleItem > 10 && !mGoTopVisible) {
					YoYo.with(Techniques.FadeIn)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mGoTop.setVisibility(View.VISIBLE);
									mGoTopVisible = true;
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mIsAnimating = false;
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mGoTop);
				} else if (firstVisibleItem <= 10 && mGoTopVisible) {
					YoYo.with(Techniques.FadeOut)
							.duration(200)
							.withListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
									mIsAnimating = true;
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									mGoTop.setVisibility(View.GONE);
									mGoTopVisible = false;
									mIsAnimating = false;
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}
							})
							.playOn(mGoTop);
				}
			}
		});

		mGoTop = rootView.findViewById(R.id.go_top);
		mGoTop.setVisibility(View.GONE);
		mGoTop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mGoTopVisible) listView.setSelection(0);
			}
		});

		registerForContextMenu(listView);
		return rootView;
	}

	private void fetch(int page, final OnThreadsListener onThreadsListener) {
		mIsFetching = true;
		// Hack. http://stackoverflow.com/questions/26858692/swiperefreshlayout-setrefreshing-not-showing-indicator-initially
		swipe.post(new Runnable() {
			@Override
			public void run() {
				swipe.setRefreshing(true);
			}
		});

		Core.getHtml("http://www.hi-pda.com/forum/forumdisplay.php?fid=" + getArguments().getInt("fid") + "&page=" + page, new Core.OnRequestListener() {
			@Override
			public void onError(String error) {
				onThreadsListener.onError();
				swipe.setRefreshing(false);
				mIsFetching = false;
			}

			@Override
			public void onSuccess(String html) {
				new AsyncTask<String, Void, Core.ThreadsRet>() {
					@Override
					protected Core.ThreadsRet doInBackground(String... strings) {
						return Core.parseThreads(strings[0]);
					}

					@Override
					protected void onPostExecute(Core.ThreadsRet ret) {
						swipe.setRefreshing(false);

						onThreadsListener.onThreads(ret.threads, ret.page, ret.hasNextPage);
					}
				}.execute(html);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (threads.size() == 0) fetch(1, this);
		adapter.notifyDataSetChanged();

		swipe.setRefreshing(false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (threads != null) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (Thread t : threads) {
				ids.add(t.getId());
			}
			outState.putIntegerArrayList("ids", ids);
		}
		outState.putInt("index", listView.getFirstVisiblePosition());
		View v = listView.getChildAt(0);
		outState.putInt("top", v == null ? 0 : v.getTop());
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Thread t = (Thread) adapterView.getAdapter().getItem(i);
		t.setNew(false);

		Intent intent = new Intent(getActivity(), PostsActivity.class);
		intent.putExtra("fid", fid);
		intent.putExtra("tid", t.getId());
		intent.putExtra("title", t.getTitle());

		startActivity(intent);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		mPage = page;
		mIsFetching = false;

		final Collection<Integer> ids = CollectionUtils.collect(this.threads, new Transformer() {
			@Override
			public Object transform(Object o) {
				return ((Thread) o).getId();
			}
		});

		threads = (ArrayList<Thread>) CollectionUtils.selectRejected(threads, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return ids.contains(((Thread) o).getId());
			}
		});

		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onError() {
		((MainActivity) getActivity()).showToast("请求错误");
	}

	@Override
	public void onRefresh() {
		fetch(1, new OnThreadsListener() {
			@Override
			public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
				mIsFetching = false;
				ThreadsFragment.this.hasNextPage = hasNextPage;
				ThreadsFragment.this.threads.clear();
				ThreadsFragment.this.threads.addAll(threads);
				adapter.notifyDataSetChanged();
				swipe.setRefreshing(false);
			}

			@Override
			public void onError() {
				mIsFetching = false;
				swipe.setRefreshing(false);
				((MainActivity) getActivity()).showToast("请求错误");
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(0, 1, 0, "复制标题");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Thread thread = adapter.getItem(info.position);
		ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		StringBuilder builder = new StringBuilder();

		ClipData clipData = ClipData.newPlainText("post content", thread.getTitle());
		clipboardManager.setPrimaryClip(clipData);
		((BaseActivity) getActivity()).showToast("复制到剪切版");
		return super.onContextItemSelected(item);
	}

	class SaveData extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
