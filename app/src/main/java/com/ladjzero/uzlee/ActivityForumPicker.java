package com.ladjzero.uzlee;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ladjzero.uzlee.model.Forum;
import com.ladjzero.uzlee.utils.Constants;
import com.ladjzero.uzlee.utils.Utils;
import com.mobeta.android.dslv.DragSortListView;
import com.rey.material.app.Dialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by chenzhuo on 15-12-12.
 */
public class ActivityForumPicker extends ActivityEasySlide {

	@Bind(R.id.list)
	DragSortListView listView;

	ArrayAdapter<Forum> selectedAdapter;
	Dialog dialog;
	List<Forum> selectedForums;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_forum_picker);
		ButterKnife.bind(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar mActionbar = getSupportActionBar();
		mActionbar.setTitle("选择和排序板块");
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);

		selectedForums = Utils.getUserSelectedForums(this);

		if (selectedForums.size() == 0) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					showPicker();
				}
			}, 100);
		}

		selectedAdapter = new ArrayAdapter<Forum>(this, R.layout.list_item_forum_sort, R.id.text, selectedForums);

		listView.setAdapter(selectedAdapter);

		listView.setDragSortListener(new DragSortListView.DragSortListener() {
			@Override
			public void drag(int from, int to) {

			}

			@Override
			public void drop(int from, int to) {
				if (from != to) {
					Forum move = selectedAdapter.getItem(from);

					selectedAdapter.remove(move);
					selectedAdapter.insert(move, to);
					selectedAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void remove(int which) {

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.forums, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		showPicker();
		return super.onOptionsItemSelected(item);
	}

	private void showPicker() {
		dialog = new Dialog(this);
		View contentView = this.getLayoutInflater().inflate(R.layout.list_forum, null);
		final ListView list = (ListView) contentView.findViewById(R.id.list);
		final List<Forum> forums = App.getInstance().getUserFlattenForums();
		List<Integer> selected = Utils.getAllSelectedForumIds(this);

		final List<AdapterCheckableList.DataWrapper> forums2 = new ArrayList<>();

		for (Forum f : forums) {
			forums2.add(new AdapterCheckableList.DataWrapper(selected.contains(f.getFid()), f));
		}

		list.setAdapter(new AdapterCheckableList(this, R.layout.checkbox, forums2));

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				selectedAdapter.clear();
				List<Forum> seleted = new ArrayList<Forum>();

				for (AdapterCheckableList.DataWrapper d : forums2) {
					if (d.checked) {
						seleted.add((Forum) d.data);
					}
				}

				selectedAdapter.addAll(seleted);
				selectedAdapter.notifyDataSetChanged();
			}
		});


		dialog.title("板块")
				.positiveAction("确定")
				.positiveActionClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				})
				.contentView(contentView)
				.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		List<String> toCommit = (List<String>) CollectionUtils.collect(selectedForums, new Transformer() {
			@Override
			public Object transform(Object o) {
				return String.valueOf(((Forum) o).getFid());
			}
		});

		getSettings().edit().putString(Constants.PREF_KEY_SELECTED_FORUMS, StringUtils.join(toCommit, ',')).commit();
	}
}
