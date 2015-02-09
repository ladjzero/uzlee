package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ladjzero.hipda.Core;

/**
 * Created by ladjzero on 2014/12/28.
 */
public class NavAdapter extends ArrayAdapter<String> {
	private int alertCount = 0;
	Context context;
	boolean hasUpdate = false;

	public NavAdapter(Context context, int resource, String[] navs) {
		super(context, resource, navs);
		this.context = context;
	}

	public void setAlert(int count) {
		alertCount = count;
		notifyDataSetChanged();
	}

	public void setUpdate(boolean hasUpdate) {
		this.hasUpdate = hasUpdate;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
		View row = inflater.inflate(R.layout.nav_item, parent, false);
		TextView icon = (TextView) row.findViewById(R.id.nav_item_icon);
		TextView text = (TextView) row.findViewById(R.id.nav_item_text);
		View alertWrap = row.findViewById(R.id.nav_item_wrap);
		TextView alert = (TextView) row.findViewById(R.id.nav_item_alert_no);

		String str = getItem(position);
		String[] iconAndText = str.split("\\|");
		icon.setText(iconAndText[0]);
		text.setText(iconAndText[1]);

		if ("{fa-shopping-cart}".equals(iconAndText[0]) || "{fa-book}".equals(iconAndText[0]) || "{fa-gear}".equals(iconAndText[0])) {
			icon.setTextColor(context.getResources().getColor(R.color.dark_primary));
			text.setTextColor(context.getResources().getColor(R.color.dark_primary));
		}

		if ("{fa-bell}".equals(iconAndText[0]) && alertCount != 0) {
			alert.setText(alertCount + "");
			alertWrap.setVisibility(View.VISIBLE);
		} else {
			alertWrap.setVisibility(View.INVISIBLE);
		}


		if ("{fa-sign-out}".equals(iconAndText[0]) || "{fa-sign-in}".equals(iconAndText[0])) {
			if (Core.isOnline()) {
				icon.setText("{fa-sign-out}");
				text.setText("登出");
			} else {
				icon.setText("{fa-sign-in}");
				text.setText("登入");

			}
		}

		if ("{fa-info}".equals(iconAndText[0])) {
			try {
				icon.setText("{fa-info}");
				text.setText("关于\t\tv" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}

			if (hasUpdate) {
				alert.setText("{fa-angle-up}");
				alertWrap.setVisibility(View.VISIBLE);
			}
		}

		return row;
	}
}
