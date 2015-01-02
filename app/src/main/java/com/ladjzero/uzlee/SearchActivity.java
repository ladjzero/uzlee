package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity implements Core.OnThreadsListener, View.OnKeyListener, AdapterView.OnItemClickListener{

	Menu menu;
	ThreadsAdapter adapter;
	ArrayList<Thread> threads;
	ListView listView;
	EditText searchInput;

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

		searchInput = (EditText) findViewById(R.id.search_input);

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
			searchInput.setHeight(actionBarHeight);
		}

		searchInput.setOnKeyListener(this);
	}


	@Override
	public boolean onKey(View view, int i, KeyEvent keyEvent) {
		if (i == KeyEvent.KEYCODE_SEARCH || i == KeyEvent.KEYCODE_ENTER) {
			String query = searchInput.getText().toString();
			Core.search(query, this);
		}

		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Thread t = (Thread) adapterView.getAdapter().getItem(i);
		Intent intent = new Intent(this, PostsActivity.class);
		intent.putExtra("thread_id", t.getId());
		intent.putExtra("title", t.getTitle());
		startActivity(intent);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		this.threads.clear();
		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}
}
