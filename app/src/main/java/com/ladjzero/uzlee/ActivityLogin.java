package com.ladjzero.uzlee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.rey.material.widget.Spinner;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenzhuo on 15-11-8.
 */
public class ActivityLogin extends ActionBarActivity {
	@Bind(R.id.user_name) TextView name;
	@Bind(R.id.user_password) TextView passwd;
	@Bind(R.id.answer) TextView answer;
	@Bind(R.id.question) Spinner spn;
	@Bind(R.id.login_background) WebView webview;

	@OnClick(R.id.login) void onLogin() {
		Core.login(name.getText().toString(), passwd.getText().toString(), spn.getSelectedItemPosition(), answer.getText().toString(), new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Utils.showToast(ActivityLogin.this, "登录成功");
				startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
			}
		});
	}

	@OnClick(R.id.register) void onRegister() {
		String url = "http://www.hi-pda.com/forum/tobenew.php";

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);

		Core.setup(this, false);

		if (Core.getUser() != null) {
			startActivity(new Intent(this, ActivityMain.class));
		} else {
			webview.loadUrl("file:///android_asset/login_bg.html");

			String[] questions = getResources().getStringArray(R.array.questions);
			spn.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, questions));
			spn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
				@Override
				public void onItemSelected(Spinner spinner, View view, int i, long l) {
					answer.setVisibility(i == 0 ? View.GONE : View.VISIBLE);
				}
			});
		}

		StatConfig.setDebugEnable(true);
		StatService.trackCustomEvent(this, "onCreate", "");
	}
}
