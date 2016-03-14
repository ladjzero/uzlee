package com.ladjzero.uzlee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by chenzhuo on 16-3-14.
 */
public class ConnectionStatusChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		((Application2) context.getApplicationContext()).setImageNetwork();
	}
}