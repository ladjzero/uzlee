package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by ladjzero on 2015/3/31.
 */
public class AdapterMenuItem extends ArrayAdapter {
	private Context context;
	private String[] types;
	private String[] icons;

	public AdapterMenuItem(Context context, String[] types, String[] icons) {
		super(context, 0, types);
		this.context = context;
		this.types = types;
		this.icons = icons;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View row = inflater.inflate(R.layout.bs_type_row, parent, false);
		TextView icon = (TextView) row.findViewById(R.id.icon);
		TextView text = (TextView) row.findViewById(R.id.text);

		icon.setText(icons[position]);
		text.setText(types[position]);

		return row;
	}
}
