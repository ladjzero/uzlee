package com.ladjzero.uzlee;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ladjzero.hipda.Guide;
import com.r0adkll.slidr.Slidr;

/**
 * Created by chenzhuo on 15-8-13.
 */
public class GuidePicker extends ActivityBase {

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.guide_picker);
		Slidr.attach(this);
//		mActionbar.setDisplayHomeAsUpEnabled(true);

		Guide.setStringify(new Guide.Stringify() {
			@Override
			public String stringify(Guide.Topic topic) {
				return topic.parent == null ? topic.title : " - " + topic.title;
			}
		});

		ListView listView = (ListView) findViewById(R.id.listView);

		listView.setAdapter(new ArrayAdapter<Guide.Topic>(this, R.layout.guide_picker_item, Guide.getAccessibleTopics()) {
		});
	}
}
