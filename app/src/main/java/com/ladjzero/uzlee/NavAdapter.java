package com.ladjzero.uzlee;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by ladjzero on 2014/12/28.
 */
public class NavAdapter extends ArrayAdapter<String> {
    private final int AlertIndex = 3;
    private int alertCount = 0;

    public NavAdapter(Context context, int resource, String[] navs) {
        super(context, resource, navs);
    }

    public void setAlert(int count) {
        alertCount = count;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.nav_item, parent, false);
        TextView textView1 = (TextView) row.findViewById(R.id.nav_item_text);
        TextView textView2 = (TextView) row.findViewById(R.id.nav_item_alert_no);
        textView1.setText(getItem(position));

        if (alertCount != 0 && position == AlertIndex) {
            textView2.setText(alertCount + "");
            textView2.setVisibility(View.VISIBLE);
        } else {
            textView2.setVisibility(View.INVISIBLE);
        }

        return row;
    }
}
