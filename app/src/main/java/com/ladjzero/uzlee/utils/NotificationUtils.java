package com.ladjzero.uzlee.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.ladjzero.uzlee.R;

/**
 * Created by R9NKCC3 on 2016/2/23.
 */
public class NotificationUtils {
	public static void nofity(Context context, Notification noti) {
		NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.icon)
				.setContentTitle(noti.title)
				.setContentText(noti.text)
				.setContentIntent(PendingIntent.getActivity(context, 0, noti.intent, PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true);
		mgr.notify(noti.id, builder.build());
	}

	public static class Notification {
		public int id;
		public String title;
		public String text;
		public Intent intent;
	}
}
