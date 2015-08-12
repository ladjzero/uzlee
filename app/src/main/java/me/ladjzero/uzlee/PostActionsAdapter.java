package me.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.joanzapata.android.iconify.Iconify;

/**
 * Created by ladjzero on 2015/3/31.
 */
public class PostActionsAdapter extends ArrayAdapter{
	private Context context;
	public final static String[] TYPES = new String[]{
			"逆序阅读",
			"刷新",
			"收藏",
			"复制链接",
			"从浏览器打开"
	};
	public final static String[] ICONS = new String[] {
			"{fa-sort-numeric-desc}",
			"{fa-refresh}",
			"{fa-bookmark}",
			"{fa-link}",
			"{fa-external-link}"
	};
	public final static Iconify.IconValue[] ICON_VALUES = new Iconify.IconValue[] {
			Iconify.IconValue.fa_sort_numeric_desc,
			Iconify.IconValue.fa_refresh,
			Iconify.IconValue.fa_bookmark,
			Iconify.IconValue.fa_link,
			Iconify.IconValue.fa_external_link
	};

	public PostActionsAdapter(Context context) {
		super(context, 0, TYPES);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View row = inflater.inflate(R.layout.bs_type_row, parent, false);
		TextView icon = (TextView) row.findViewById(R.id.icon);
		TextView text = (TextView) row.findViewById(R.id.text);
		if (position == 0) {
			if (((PostsActivity)context).orderType == 0) {
				icon.setText(ICONS[0]);
				text.setText(TYPES[0]);
			} else {
				icon.setText("{fa-sort-numeric-asc}");
				text.setText("顺序阅读");
			}
		} else {
			icon.setText(ICONS[position]);
			text.setText(TYPES[position]);
		}

		return row;
	}
}
