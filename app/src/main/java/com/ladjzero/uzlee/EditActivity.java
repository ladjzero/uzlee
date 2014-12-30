package com.ladjzero.uzlee;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.ladjzero.hipda.Core;


public class EditActivity extends BaseActivity {

	int tid;
	String content;
	TextView contentInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tid = getIntent().getIntExtra("thread_id", 0);

		getActionBar().setTitle(getIntent().getStringExtra("title"));
		setContentView(R.layout.edit);
	}

	@Override
	public void onStart() {
		super.onStart();

		View titleInput = findViewById(R.id.edit_title);
		contentInput = (TextView) findViewById(R.id.edit_body);
		if (getIntent().getBooleanExtra("hideTitleInput", false)) {
			titleInput.setVisibility(View.GONE);
		} else {
			titleInput.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_reply, menu);

		menu.findItem(R.id.reply_send).setIcon(new IconDrawable(this, Iconify.IconValue.fa_send).colorRes(android.R.color.white).actionBarSize());
		menu.findItem(R.id.reply_add_image).setIcon(new IconDrawable(this, Iconify.IconValue.fa_image).colorRes(android.R.color.white).actionBarSize());

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.reply_send) {
			Core.sendReply(tid, contentInput.getText().toString());
		}

		return super.onOptionsItemSelected(item);
	}
}
