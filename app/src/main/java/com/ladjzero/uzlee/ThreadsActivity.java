package com.ladjzero.uzlee;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListView;

import com.ladjzero.hipda.*;
import com.ladjzero.hipda.Thread;

import java.util.ArrayList;


public class ThreadsActivity extends BaseActivity implements Core.OnThreadsListener {

	ListView listView;
	ArrayList<Thread> threads;
	ThreadsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threads);

		listView = (ListView) findViewById(R.id.threads);
		threads = new ArrayList<Thread>();
		adapter = new ThreadsAdapter(this, threads);
		listView.setAdapter(adapter);

		String url = getIntent().getStringExtra("url");
		Core.getThreadsByUrl(url, this);
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
	public void onThreads(ArrayList<Thread> threads) {
		this.threads.addAll(threads);
		adapter.notifyDataSetChanged();
	}
}
