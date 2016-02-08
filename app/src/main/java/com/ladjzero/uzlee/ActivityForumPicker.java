package com.ladjzero.uzlee;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Forum;
import com.mobeta.android.dslv.DragSortListView;
import com.r0adkll.slidr.Slidr;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Slider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
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

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_forum_picker);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar mActionbar = getSupportActionBar();
        mActionbar.setTitle("排序板块");
        mActionbar.setDisplayHomeAsUpEnabled(true);
        mActionbar.setDisplayShowCustomEnabled(true);

        final List<Forum> selectedForums = Core.getSelectedForums(this);

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

                    commitSelected(selectedForums);
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
        int id = item.getItemId();
        dialog = new Dialog(this);
        View contentView = this.getLayoutInflater().inflate(R.layout.list_forum, null);
        final ListView list = (ListView) contentView.findViewById(R.id.list);
        final List<Forum> forums = Core.getFlattenForums(this);

        list.setAdapter(new ArrayAdapter<Forum>(this, R.layout.list_item_forum, R.id.forum, forums) {
            @Override
            public View getView(int position, View convertView, final ViewGroup parent) {
                View rootView = getLayoutInflater().inflate(R.layout.list_item_forum, null);
                CheckBox textView = (CheckBox) rootView.findViewById(R.id.forum);

                final Forum f = getItem(position);
                List<Forum> selected = Core.getSelectedForums(ActivityForumPicker.this);

                textView.setText(f.toString());
                textView.setChecked(CollectionUtils.exists(selected, new Predicate() {
                    @Override
                    public boolean evaluate(Object o) {
                        return ((Forum) o).getFid() == f.getFid();
                    }
                }));

                textView.setTag(f);

                textView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.getTag() == f) {
                            List<Forum> selected = Core.getSelectedForums(ActivityForumPicker.this);

                            boolean isSelectedBefore = CollectionUtils.exists(selected, new Predicate() {
                                @Override
                                public boolean evaluate(Object o) {
                                    return ((Forum) o).getFid() == f.getFid();
                                }
                            });

                            boolean isSelectedNext = !isSelectedBefore;

                            if (isSelectedNext) {
                                selected.add(f);
                            } else {
                                selected.remove(f);
                            }

                            commitSelected(selected);
                        }
                    }
                });

                return rootView;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                selectedAdapter.clear();
                selectedAdapter.addAll(Core.getSelectedForums(ActivityForumPicker.this));
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

        return super.onOptionsItemSelected(item);
    }

    private void commitSelected(List<Forum> forums) {
        List<String> toCommit = (List<String>) CollectionUtils.collect(forums, new Transformer() {
            @Override
            public Object transform(Object o) {
                return String.valueOf(((Forum) o).getFid());
            }
        });

        getSettings().edit().putString("selected_forums", StringUtils.join(toCommit, ',')).commit();
    }

}
