package com.ladjzero.uzlee;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.ladjzero.hipda.Core;
import com.ladjzero.hipda.Core.LoginCB;
import com.ladjzero.hipda.DBHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class BaseActivity extends OrmLiteBaseActivity<DBHelper> implements Core.MsgCB {

	Core core;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        FlatUI.initDefaultValues(this);
        FlatUI.setDefaultTheme(FlatUI.DARK);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(FlatUI.getActionBarDrawable(this, FlatUI.DARK, false));
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

		core = Core.getInstance(this);
        core.addOnMsgListener(this);
				
		DisplayImageOptions ilOptions = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.none)
				.showImageOnLoading(R.drawable.none)
				.showImageOnFail(R.drawable.none)
				.cacheInMemory(true)
				.cacheOnDisk(true).build();
		ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(ilOptions).build();
		ImageLoader.getInstance().init(ilConfig);
	}

	public void showLoginDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("login  hi-pda");
		final View v = getLayoutInflater().inflate(R.layout.login_dialog, null);
		alert.setView(v);
		alert.setPositiveButton("login", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				String username = ((EditText) v.findViewById(R.id.user_name))
						.getText().toString();
				String password = ((EditText) v
						.findViewById(R.id.user_password)).getText().toString();
				final ProgressDialog progress = ProgressDialog.show(
						BaseActivity.this, "", "login...", true);
				Core.login(username, password, new LoginCB() {

					@Override
					public void onSuccess() {
						progress.dismiss();
						Toast.makeText(BaseActivity.this, "login succeed",
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void onFailure(String message) {
						progress.dismiss();
						Toast.makeText(BaseActivity.this, message,
								Toast.LENGTH_LONG).show();
					}

				});
			}

		});
		alert.setNegativeButton("cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}

		});
		alert.show();
	}

    @Override
    public void onMsg(int count) {

    }
}
