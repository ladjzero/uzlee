package com.ladjzero.uzlee;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;


public class ThreadsActivity extends BaseActivity implements Core.OnThreadsListener, AdapterView.OnItemClickListener {

	ListView listView;
	ArrayList<Thread> threads;
	ThreadsAdapter adapter;
	boolean hasNextPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableBackAction();

		setContentView(R.layout.threads);

		Intent intent = getIntent();

		final String userName = intent.getStringExtra("name");

		listView = (ListView) findViewById(R.id.threads);
		threads = new ArrayList<Thread>();
		adapter = new ThreadsAdapter(this, threads);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				if (hasNextPage) {
					Core.getUserThreadsAtPage(userName, page, ThreadsActivity.this);
				}
			}
		});

		setTitle(getIntent().getStringExtra("name") + "的主题");
		Core.getUserThreadsAtPage(userName, 1, ThreadsActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_threads, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onThreads(ArrayList<Thread> threads, int page, boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Thread t = (Thread) adapterView.getAdapter().getItem(i);
		t.setNew(false);

		Intent intent = new Intent(this, PostsActivity.class);
		intent.putExtra("thread_id", t.getId());
		intent.putExtra("title", t.getTitle());

		startActivity(intent);
	}
}
