package com.ladjzero.uzlee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;

public class SearchActivity extends SwipeActivity implements Core.OnThreadsListener, View.OnKeyListener, AdapterView.OnItemClickListener {

	Menu menu;
	ThreadsAdapter adapter;
	ArrayList<Thread> threads;
	ListView listView;
	EditText searchInput;
	TextView close;
	boolean hasNextPage;
	TextView hint;
	String query;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();

		threads = new ArrayList<Thread>();
		adapter = new ThreadsAdapter(this, threads);

		setContentView(R.layout.search);
		listView = (ListView) findViewById(R.id.threads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage && query != null) {
					hint.setVisibility(View.VISIBLE);
					Core.search(query, page, SearchActivity.this);
				}
			}
		});

		close = (TextView) findViewById(R.id.search_close);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String q = searchInput.getText().toString();
				if (q.length() == 0) {
					finish();
				} else {
					searchInput.setText(query = "");
				}
			}
		});

		searchInput = (EditText) findViewById(R.id.search_input);

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
			searchInput.setHeight(actionBarHeight);
		}

		searchInput.setOnKeyListener(this);

		hint = (TextView) findViewById(R.id.hint);
		hint.setVisibility(View.GONE);

		registerForContextMenu(listView);
	}

	@Override
	public void onResume() {
		adapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	public boolean onKey(View view, int i, KeyEvent keyEvent) {
		if (keyEvent.getAction() == KeyEvent.ACTION_UP && (i == KeyEvent.KEYCODE_SEARCH || i == KeyEvent.KEYCODE_ENTER)) {
			query = searchInput.getText().toString();
			query = query.trim();

			if (query.length() != 0) {
				threads.clear();
				Core.search(query, 1, this);
			}
		}

		return false;
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
		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		StringBuilder builder = new StringBuilder();

		ClipData clipData = ClipData.newPlainText("post content", thread.getTitle());
		clipboardManager.setPrimaryClip(clipData);
		showToast("复制到剪切版");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Thread t = (Thread) adapterView.getAdapter().getItem(i);
		Intent intent = new Intent(this, PostsActivity.class);
		intent.putExtra("tid", t.getId());
		intent.putExtra("title", t.getTitle());
		startActivity(intent);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		hint.setVisibility(View.GONE);
		this.hasNextPage = hasNextPage;
		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onError() {
		hint.setVisibility(View.GONE);
		showToast("请求错误");
	}
}
