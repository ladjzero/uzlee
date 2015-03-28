package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by ladjzero on 2015/3/28.
 */
public class BsAdapter extends ArrayAdapter {
	private Context context;
	public final static String[] TYPES = new String[]{
			"全部",
			"手机",
			"掌上电脑",
			"笔记本电脑",
			"无线设备",
			"数码相机",
			"MP3随身听"
	};
	public final static String[] ICONS = new String[] {
			"{fa-tags}",
			"{fa-mobile}",
			"{fa-tablet}",
			"{fa-laptop}",
			"{fa-wifi}",
			"{fa-camera-retro}",
			"{fa-music}"
	};

	public BsAdapter(Context context) {
		super(context, 0, TYPES);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View row = inflater.inflate(R.layout.bs_type_row, parent, false);
		TextView icon = (TextView) row.findViewById(R.id.icon);
		TextView text = (TextView) row.findViewById(R.id.text);
		icon.setText(ICONS[position]);
		text.setText(TYPES[position]);
		return row;
	}
}
