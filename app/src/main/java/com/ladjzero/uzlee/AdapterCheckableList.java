package com.ladjzero.uzlee;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import java.util.List;

/**
 * Created by chenzhuo on 16/6/4.
 */
public class AdapterCheckableList extends ArrayAdapter {

	public AdapterCheckableList(Context context, int resource, List<DataWrapper> objects) {
		super(context, resource, objects);
	}

	public AdapterCheckableList(Context context, int resource, DataWrapper[] objects) {
		super(context, resource, objects);
	}


	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		View rootView = super.getView(position, convertView, parent);

		if (!(rootView instanceof CompoundButton)) {
			throw new Error("View must be a CompoundButton");
		}

		CompoundButton _v = (CompoundButton) rootView;
		DataWrapper _d = (DataWrapper) getItem(position);

		rootView.setTag(getItem(position));
		_v.setChecked(_d.checked);

		_v.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				((DataWrapper) compoundButton.getTag()).checked = b;
			}
		});


		return rootView;
	}

	public static class DataWrapper {
		boolean checked;
		Object data;

		public DataWrapper(boolean checked, Object data) {
			this.checked = checked;
			this.data = data;
		}

		@Override
		public String toString() {
			return data.toString();
		}
	}
}
